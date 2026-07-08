package com.apexinvest.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexinvest.app.api.models.CandlePoint
import com.apexinvest.app.api.models.FinancialsDto
import com.apexinvest.app.api.models.MarketPricing
import com.apexinvest.app.api.models.StockDetailsResponse
import com.apexinvest.app.api.models.StockSearchResult
import com.apexinvest.app.data.PortfolioRepository
import com.apexinvest.app.data.StockEntity
import com.apexinvest.app.data.WatchlistEntity
import com.apexinvest.app.data.remote.TradingViewWebSocketClient
import com.apexinvest.app.data.repository.StockDetailsRepository
import com.apexinvest.app.data.util.SessionPriceCache
import com.apexinvest.app.util.StockMetadataUtils
import com.apexinvest.app.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds

sealed class StockDetailState {
    object Loading : StockDetailState()
    data class Success(val data: StockDetailsResponse, val isRefreshing: Boolean = false) : StockDetailState()
    data class Error(val message: String) : StockDetailState()
}

sealed class FinancialsUiState {
    object Loading : FinancialsUiState()
    data class Success(val data: FinancialsDto) : FinancialsUiState()
    data class Error(val message: String) : FinancialsUiState()
}

class StockDetailViewModel(
    private val stockDetailsRepository: StockDetailsRepository,
    private val portfolioRepository: PortfolioRepository,
    private val sessionManager: SessionManager,
    private val tradingViewWebSocketClient: TradingViewWebSocketClient
) : ViewModel() {

    private val _stockDetailState = MutableStateFlow<StockDetailState>(StockDetailState.Loading)
    val stockDetailState: StateFlow<StockDetailState> = _stockDetailState

    private val _selectedRange = MutableStateFlow("1D")

    private val _searchResults = MutableStateFlow<List<StockSearchResult>>(emptyList())

    private val _tradeStatusMessage = MutableStateFlow<String?>(null)
    val tradeStatusMessage: StateFlow<String?> = _tradeStatusMessage

    private val _financialsState = MutableStateFlow<FinancialsUiState>(FinancialsUiState.Loading)
    val financialsState: StateFlow<FinancialsUiState> = _financialsState

    private val _livePricing = MutableStateFlow<MarketPricing?>(null)
    val livePricing: StateFlow<MarketPricing?> = _livePricing

    // StateFlow for candles
    private val _activeCandles = MutableStateFlow<List<CandlePoint>>(emptyList())
    val activeCandles: StateFlow<List<CandlePoint>> = _activeCandles

    private var wsJob: Job? = null
    private var pollingJob: Job? = null
    private var detailJob: Job? = null
    private var financialsJob: Job? = null

    val portfolioStocks: StateFlow<List<StockEntity>> = portfolioRepository.allPortfolioStocks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val watchlistStocks: StateFlow<List<WatchlistEntity>> = portfolioRepository.allWatchlistStocks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var lastDbWriteTime: Long = 0
    private var lastSeenPrice: Double = 0.0
    private var lastPriceTickTime: Long = 0

    val currentStockHolding: StateFlow<StockEntity?> = combine(portfolioStocks, _stockDetailState) { stocks, state ->
        if (state is StockDetailState.Success) stocks.find { it.symbol.equals(state.data.ticker, ignoreCase = true) }
        else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val marketStatus: StateFlow<Pair<Boolean, String>> = _stockDetailState.combine(_livePricing) { state, _ ->
        val symbol = (state as? StockDetailState.Success)?.data?.ticker ?: ""
        if (symbol.isBlank()) false to "Loading..."
        else StockMetadataUtils.isMarketOpen(symbol)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false to "Checking...")

    val canSell: StateFlow<Boolean> = currentStockHolding.combine(marketStatus) { holding, status ->
        holding != null && holding.quantity > 0 && status.first
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        // Sync price updates from other screens
        viewModelScope.launch {
            portfolioRepository.globalPriceUpdates.collect { update ->
                val state = _stockDetailState.value
                val ticker = if (state is StockDetailState.Success) state.data.ticker else ""
                
                // Only process if not a recent local tick
                val isRecentLocalTick = (System.currentTimeMillis() - lastPriceTickTime) < 1000
                
                if (ticker.equals(update.symbol, ignoreCase = true) && !isRecentLocalTick) {
                    applyLivePriceUpdate(update.symbol, update.price, marketState = "REGULAR")
                }
            }
        }
    }

    fun formatAnalystRating(rating: String?): String {
        return when (rating?.uppercase(Locale.ROOT)) {
            "STRONG_BUY", "STRONG BUY" -> "Strong Buy"
            "BUY" -> "Buy"
            "HOLD" -> "Hold"
            "SELL" -> "Sell"
            "STRONG_SELL", "STRONG SELL" -> "Strong Sell"
            else -> rating ?: "N/A"
        }
    }

    fun loadStockDetails(symbol: String, range: String = "1D", forceRefresh: Boolean = false) {
        val normalizedSymbol = symbol.uppercase(Locale.ROOT).trim()
        val uiRange = range.uppercase(Locale.ROOT).trim()
        _selectedRange.value = uiRange

        val yahooRange = when (uiRange) {
            "1D"  -> "1d"
            "5D"  -> "5d"
            "1M"  -> "1mo"
            "1Y"  -> "1y"
            "5Y"  -> "5y"
            "MAX" -> "max"
            else  -> "1d"
        }

        val currentState = _stockDetailState.value as? StockDetailState.Success
        val currentData = currentState?.data

        // Full reload required on symbol change
        if (currentData?.ticker?.uppercase(Locale.ROOT) != normalizedSymbol) {
            _stockDetailState.value = StockDetailState.Loading
            _financialsState.value = FinancialsUiState.Loading

            viewModelScope.launch(Dispatchers.Default) {
                // Check shared session cache
                val sessionData = SessionPriceCache.get(normalizedSymbol)
                
                val ramSparkline = portfolioRepository.getCachedSparklineSync(normalizedSymbol)
                
                val localStock = portfolioStocks.value.find { it.symbol.uppercase() == normalizedSymbol }
                val localWatch = watchlistStocks.value.find { it.symbol.uppercase() == normalizedSymbol }

                val initialPrice = sessionData?.price ?: ramSparkline.lastOrNull() ?: localStock?.currentPrice ?: localWatch?.lastPrice ?: 0.0
                val initialChange = sessionData?.change ?: localStock?.dailyChange ?: localWatch?.dailyChange ?: 0.0
                val initialPercent = sessionData?.changePercent ?: localStock?.changePercent ?: localWatch?.changePercent ?: 0.0

                if (initialPrice > 0.0) {
                    lastSeenPrice = initialPrice
                    val updatedPricing = MarketPricing(
                        priceLast = initialPrice, priceOpen = null, priceHigh = initialPrice, priceLow = initialPrice,
                        changePct1D = initialPercent, changeAbsolute1D = initialChange, high52Week = null, low52Week = null,
                        volumeCurrent = null, tradedValueDaily = null, avgVolume10D = null, avgVolume30D = null,
                        avgVolume90D = null, relativeVolume10D = null, beta1Y = null, beta5Y = null
                    )
                    _livePricing.value = updatedPricing
                }
            }
        } else {
            // Keep live pricing and candles for smooth transition
            _stockDetailState.value = StockDetailState.Success(currentData, isRefreshing = true)
        }

        detailJob?.cancel()
        detailJob = viewModelScope.launch(Dispatchers.IO) {
            stockDetailsRepository.getMergedStockDetailsFlow(normalizedSymbol, yahooRange, forceRefresh)
                .collect { (result, isComplete) ->
                    result.onSuccess { data ->
                        val activeState = _stockDetailState.value as? StockDetailState.Success
                        
                        // Prioritize SessionPriceCache
                        val sessionData = SessionPriceCache.get(normalizedSymbol)
                        val mirroredPrice = sessionData?.price ?: lastSeenPrice.let { if (it > 0.0) it else null } ?: data.marketPricing?.priceLast ?: 0.0
                        
                        val updatedPricing = data.marketPricing?.copy(
                            priceLast = mirroredPrice,
                            changeAbsolute1D = sessionData?.change ?: data.marketPricing.changeAbsolute1D,
                            changePct1D = sessionData?.changePercent ?: data.marketPricing.changePct1D,
                            previousClose = sessionData?.previousClose ?: data.marketPricing.previousClose,
                            // Ensure pre/post values are also mirrored if missing in the new partial update
                            preMarketPrice = data.marketPricing.preMarketPrice ?: activeState?.data?.marketPricing?.preMarketPrice,
                            preMarketChange = data.marketPricing.preMarketChange ?: activeState?.data?.marketPricing?.preMarketChange,
                            postMarketPrice = data.marketPricing.postMarketPrice ?: activeState?.data?.marketPricing?.postMarketPrice,
                            postMarketChange = data.marketPricing.postMarketChange ?: activeState?.data?.marketPricing?.postMarketChange
                        )

                        // Preserve range changes
                        val finalRangeAbs = if (data.rangeChangeAbsolute != null && data.rangeChangeAbsolute != 0.0) data.rangeChangeAbsolute else activeState?.data?.rangeChangeAbsolute
                        val finalRangePct = if (data.rangeChangePercent != null && data.rangeChangePercent != 0.0) data.rangeChangePercent else activeState?.data?.rangeChangePercent

                        val mirroredData = data.copy(
                            marketPricing = updatedPricing,
                            rangeChangeAbsolute = finalRangeAbs,
                            rangeChangePercent = finalRangePct
                        )
                        if (updatedPricing != null) {
                            _livePricing.value = updatedPricing
                        }

                        // Merge candles
                        if (data.candles.isNotEmpty()) {
                            val newCandles = data.candles.map { dto ->
                                CandlePoint(
                                    time = dto.time, open = dto.open, high = dto.high,
                                    low = dto.low, close = dto.close, volume = dto.volume
                                )
                            }
                            
                            val currentList = _activeCandles.value
                            if (currentList.isEmpty() || currentList.size != newCandles.size || uiRange != "1D") {
                                _activeCandles.value = newCandles
                            } else {
                                // If same size, only replace if the new candles have a newer last point
                                val currentLast = currentList.lastOrNull()?.time?.toLongOrNull() ?: 0L
                                val incomingLast = newCandles.lastOrNull()?.time?.toLongOrNull() ?: 0L
                                if (incomingLast >= currentLast) {
                                    _activeCandles.value = newCandles
                                }
                            }
                        }

                        _stockDetailState.value = StockDetailState.Success(mirroredData, isRefreshing = !isComplete)
                    }.onFailure { e ->
                        if (_stockDetailState.value !is StockDetailState.Error) {
                            _stockDetailState.value = StockDetailState.Error(e.message ?: "Failed to load stock details.")
                        }
                    }
                }
        }
    }

    fun loadCurrentPrice(symbol: String, forceNetwork: Boolean = false) {
        val normalizedSymbol = symbol.uppercase(Locale.ROOT).trim()
        Log.d("StockDetailVM", "loadCurrentPrice: $normalizedSymbol, force: $forceNetwork")
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val freshLiveDto = portfolioRepository.fetchLivePriceOnly(normalizedSymbol, forceNetwork = forceNetwork)
                if (freshLiveDto != null && (freshLiveDto.price > 0.0)) {
                    Log.d("StockDetailVM", "loadCurrentPrice result for $normalizedSymbol: ${freshLiveDto.price}, state=${freshLiveDto.marketState}")
                    updateUiWithLivePrice(normalizedSymbol, freshLiveDto.price, freshLiveDto.open, freshLiveDto.dayHigh, freshLiveDto.dayLow,
                        prePrice = freshLiveDto.prePrice, preChange = freshLiveDto.preChange,
                        postPrice = freshLiveDto.postPrice, postChange = freshLiveDto.postChange,
                        marketState = freshLiveDto.marketState)
                } else {
                    Log.w("StockDetailVM", "loadCurrentPrice: Got null or 0 price for $normalizedSymbol")
                }
            } catch (e: Exception) {
                Log.e("StockDetailVM", "loadCurrentPrice error for $normalizedSymbol", e)
            }
        }
    }

    fun startLiveUpdates(symbol: String) {
        val normalizedSymbol = symbol.uppercase(Locale.ROOT).trim()
        wsJob?.cancel()
        pollingJob?.cancel()

        val (isOpen, status) = StockMetadataUtils.isMarketOpen(normalizedSymbol)
        val isExtended = StockMetadataUtils.isExtendedMarketActive(normalizedSymbol)

        if (isOpen) {
            Log.d("StockDetailVM", "Starting Live Updates for $normalizedSymbol ($status)")
            tradingViewWebSocketClient.connect(normalizedSymbol)
            wsJob = viewModelScope.launch {
                tradingViewWebSocketClient.livePrice.collect { price ->
                    updateUiWithLivePrice(normalizedSymbol, price)
                }
            }
        } else if (isExtended) {
            Log.d("StockDetailVM", "Market Closed but Extended Hours Active for $normalizedSymbol. Starting Polling.")
            startPollingUpdates(normalizedSymbol)
        } else {
            Log.i("StockDetailVM", "Market & Extended Hours Closed for $normalizedSymbol: $status")
        }
    }

    private fun startPollingUpdates(symbol: String) {
        pollingJob = viewModelScope.launch {
            while (isActive) {
                // Fetch latest price
                loadCurrentPrice(symbol, forceNetwork = true)
                
                // Polling interval
                delay(8.seconds)
            }
        }
    }

    fun stopLiveUpdates() {
        wsJob?.cancel()
        pollingJob?.cancel()
        tradingViewWebSocketClient.disconnect()
        Log.d("StockDetailVM", "Stopped Live Updates")
    }

    private fun updateUiWithLivePrice(
        symbol: String,
        price: Double,
        open: Double? = null,
        high: Double? = null,
        low: Double? = null,
        prePrice: Double? = null,
        preChange: Double? = null,
        postPrice: Double? = null,
        postChange: Double? = null,
        marketState: String? = null
    ) {
        if (price <= 0.0) return
        
        // Update local state and tick time
        lastSeenPrice = price
        lastPriceTickTime = System.currentTimeMillis()

        // Inform the rest of the app about this WebSocket tick
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = _stockDetailState.value as? StockDetailState.Success
            val pricing = currentState?.data?.marketPricing
            val localStock = portfolioStocks.value.find { it.symbol.equals(symbol, ignoreCase = true) }
            val localWatch = watchlistStocks.value.find { it.symbol.equals(symbol, ignoreCase = true) }
            val prevClose = localStock?.previousClose ?: localWatch?.previousClose ?: pricing?.previousClose ?: 0.0
            
            val change = if (prevClose > 0.0) price - prevClose else 0.0
            val pct = if (prevClose > 0.0) (change / prevClose) * 100.0 else 0.0
            
            portfolioRepository.updatePriceRAM(symbol, price, change, pct, prevClose)
        }

        applyLivePriceUpdate(symbol, price, open, high, low, prePrice, preChange, postPrice, postChange, marketState)
    }

    /**
     * Process price update on background thread
     */
    private fun applyLivePriceUpdate(
        symbol: String,
        price: Double,
        open: Double? = null,
        high: Double? = null,
        low: Double? = null,
        prePrice: Double? = null,
        preChange: Double? = null,
        postPrice: Double? = null,
        postChange: Double? = null,
        marketState: String? = null
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            val currentState = _stockDetailState.value as? StockDetailState.Success
            val ticker = currentState?.data?.ticker ?: ""
            
            // Current source of truth for the details screen
            val existing = _livePricing.value
            
            if (ticker.isBlank() || !ticker.equals(symbol, ignoreCase = true)) {
                // If not success yet, still update _livePricing
                val updated = existing?.copy(
                    priceLast = price,
                    preMarketPrice = prePrice ?: existing.preMarketPrice,
                    preMarketChange = preChange ?: existing.preMarketChange,
                    postMarketPrice = postPrice ?: existing.postMarketPrice,
                    postMarketChange = postChange ?: existing.postMarketChange,
                    marketState = marketState ?: existing.marketState
                ) ?: MarketPricing(
                    priceLast = price, priceOpen = open ?: 0.0, priceHigh = high ?: price, priceLow = low ?: price,
                    changePct1D = 0.0, changeAbsolute1D = 0.0, high52Week = null, low52Week = null,
                    volumeCurrent = null, tradedValueDaily = null, avgVolume10D = null, avgVolume30D = null,
                    avgVolume90D = null, relativeVolume10D = null, beta1Y = null, beta5Y = null,
                    preMarketPrice = prePrice, preMarketChange = preChange,
                    postMarketPrice = postPrice, postMarketChange = postChange,
                    marketState = marketState
                )
                _livePricing.value = updated
                return@launch
            }

            val currentData = currentState?.data ?: return@launch
            val pricing = currentData.marketPricing

            // Memory check
            val localStock = portfolioStocks.value.find { it.symbol.equals(symbol, ignoreCase = true) }
            val localWatch = watchlistStocks.value.find { it.symbol.equals(symbol, ignoreCase = true) }
            val prevClose = localStock?.previousClose ?: localWatch?.previousClose ?: pricing?.previousClose ?: 0.0

            val finalChange = if (prevClose > 0.0) price - prevClose else 0.0
            val finalChangePercent = if (prevClose > 0.0) (finalChange / prevClose) * 100.0 else 0.0

            // Throttle DB writes
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastDbWriteTime > 5000) {
                launch(Dispatchers.IO) {
                    portfolioRepository.updateStockPricesInDb(symbol, price, finalChange, finalChangePercent, prevClose)
                }
                lastDbWriteTime = currentTime
            }

            val finalOpen = if (open != null && open > 0.0) open else pricing?.priceOpen ?: 0.0
            val finalHigh = if (high != null && high > 0.0) high else max(pricing?.priceHigh ?: 0.0, price)
            val finalFocusLow = pricing?.priceLow ?: 0.0
            val finalLow = if (low != null && low > 0.0) low else if (finalFocusLow > 0.0) min(finalFocusLow, price) else price

            val updatedPricing = pricing?.copy(
                priceLast = price,
                changeAbsolute1D = finalChange,
                changePct1D = finalChangePercent,
                priceOpen = finalOpen, priceHigh = finalHigh, priceLow = finalLow,
                preMarketPrice = prePrice ?: existing?.preMarketPrice ?: pricing.preMarketPrice,
                preMarketChange = preChange ?: existing?.preMarketChange ?: pricing.preMarketChange,
                postMarketPrice = postPrice ?: existing?.postMarketPrice ?: pricing.postMarketPrice,
                postMarketChange = postChange ?: existing?.postMarketChange ?: pricing.postMarketChange,
                marketState = marketState ?: existing?.marketState ?: pricing.marketState
            ) ?: MarketPricing(
                priceLast = price, priceOpen = finalOpen, priceHigh = finalHigh, priceLow = finalLow,
                changeAbsolute1D = finalChange, changePct1D = finalChangePercent, high52Week = null, low52Week = null,
                volumeCurrent = null, tradedValueDaily = null, avgVolume10D = null, avgVolume30D = null,
                avgVolume90D = null, relativeVolume10D = null, beta1Y = null, beta5Y = null,
                preMarketPrice = prePrice, preMarketChange = preChange, 
                postMarketPrice = postPrice, postMarketChange = postChange,
                marketState = marketState
            )

            // Update live pricing
            if (_livePricing.value != updatedPricing) {
                _livePricing.value = updatedPricing
            }

            // Smoothly update the last candle point
            if (_selectedRange.value == "1D" && _activeCandles.value.isNotEmpty()) {
                val (isRegularOpen, _) = StockMetadataUtils.isMarketOpen(symbol)
                if (isRegularOpen) {
                    val currentList = _activeCandles.value.toMutableList()
                    val lastCandle = currentList.last()
                    if (price != lastCandle.close) {
                        val updatedLast = lastCandle.copy(
                            close = price,
                            high = max(lastCandle.high, price),
                            low = if (lastCandle.low <= 0.0) price else min(lastCandle.low, price)
                        )
                        currentList[currentList.size - 1] = updatedLast
                        _activeCandles.value = currentList
                    }
                }
            }
        }
    }

    fun searchStocks(query: String) {
        viewModelScope.launch(Dispatchers.IO) { _searchResults.value = portfolioRepository.searchStocks(query) }
    }

    fun clearTradeMessage() { _tradeStatusMessage.value = null }

    fun executePresetTrade(isBuy: Boolean) {
        val sym = (stockDetailState.value as? StockDetailState.Success)?.data?.ticker ?: return
        val currentPrice = lastSeenPrice
        if (currentPrice <= 0.0) {
            _tradeStatusMessage.value = "Error: Invalid Price"
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val quantity = if (isBuy) sessionManager.getDefaultBuyQty() else sessionManager.getDefaultSellQty()
            val type = if (isBuy) com.apexinvest.app.data.TransactionType.BUY else com.apexinvest.app.data.TransactionType.SELL

            try {
                portfolioRepository.recordTrade(sym, type, quantity, currentPrice, "USD")
                _tradeStatusMessage.value = if (isBuy) "" else "Sold ${String.format(Locale.US, "%.2f", quantity)} shares"
            } catch (e: Exception) {
                _tradeStatusMessage.value = "Trade Failed: ${e.message}"
            }
        }
    }


    fun loadFinancialsCharts(symbol: String, forceRefresh: Boolean = false) {
        financialsJob?.cancel()
        financialsJob = viewModelScope.launch(Dispatchers.IO) {
            stockDetailsRepository.getFinancialsChartSlice(symbol, forceRefresh)
                .onSuccess { data -> _financialsState.value = FinancialsUiState.Success(data) }
                .onFailure { e -> _financialsState.value = FinancialsUiState.Error(e.message ?: "Failed") }
        }
    }

    companion object
}
