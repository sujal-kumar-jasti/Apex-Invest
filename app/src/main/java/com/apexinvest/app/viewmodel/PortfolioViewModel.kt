package com.apexinvest.app.viewmodel

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexinvest.app.api.models.CandlePointDto
import com.apexinvest.app.api.models.StockSearchResult
import com.apexinvest.app.data.PortfolioRepository
import com.apexinvest.app.data.StockEntity
import com.apexinvest.app.data.TransactionType
import com.apexinvest.app.data.WatchlistEntity
import com.apexinvest.app.data.repository.NotificationRepository
import com.apexinvest.app.ui.components.MessageType
import com.apexinvest.app.util.MathUtils
import com.apexinvest.app.util.toCleanString
import com.apexinvest.app.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds

data class TransactionUiModel(
    val id: Int,
    val symbol: String,
    val quantityStr: String,
    val formattedDate: String,
    val totalValue: Double,
    val totalValueStr: String,
    val convertedPrice: Double,
    val convertedPriceStr: String,
    val isBuy: Boolean
)

data class TransactionAnalyticsState(
    val mappedHistory: List<TransactionUiModel> = emptyList(),
    val totalBuy: Double = 0.0,
    val totalSell: Double = 0.0,
    val isInitial: Boolean = true
)

data class PortfolioStats(
    val totalValue: Double,
    val totalInvested: Double,
    val totalGain: Double,
    val totalPercent: Double,
    val isPositive: Boolean,
    val chartData: List<Double>,
    val dailyGain: Double = 0.0,
    val dailyPercent: Double = 0.0
)

data class AppUiState(
    val portfolio: List<StockEntity> = emptyList(),
    val watchlist: List<WatchlistEntity> = emptyList(),
    val liveRate: Double = 84.0,
    val rates: Map<String, Double> = mapOf("INR" to 84.0, "USD" to 1.0),
    val isUsd: Boolean = false,
    val isLoading: Boolean = false,
    val isHydrated: Boolean = false
)

// 🚀 NEW: Data class specifically formatted for the Top Impact Drivers UI
data class ImpactDriver(
    val symbol: String,
    val allocationPercent: Double,
    val changePercent: Double,
    val impactScore: Double // Pre-calculated allocation * return
)

sealed class AnalyticsUiState {
    object Loading : AnalyticsUiState()
    object Empty : AnalyticsUiState()
    data class Success(
        val totalValue: Double,
        val sectors: Map<String, Double>,
        val topDrivers: List<ImpactDriver>, // 🚀 UI receives pre-sorted, pre-sliced data
        val hiddenImpactCount: Int,         // 🚀 UI knows how many were skipped without receiving the list
        val topGainer: StockEntity?,
        val topLoser: StockEntity?,
        val winRate: Double,
        val currencySymbol: String
    ) : AnalyticsUiState()
}

data class StockAllocation(val symbol: String, val value: Double, val percent: Double, val isProfit: Boolean, val changePercent: Double)

sealed class AiUiState {
    object Idle : AiUiState()
    object Thinking : AiUiState()
    data class Success(val insights: List<AiInsight>, val suggestions: List<StockSuggestion>) : AiUiState()
    data class Error(val message: String) : AiUiState()
}

data class AiInsight(val title: String, val description: String, val type: InsightType)
enum class InsightType { WARNING, SUCCESS, OPPORTUNITY }
data class StockSuggestion(val symbol: String, val sector: String, val reason: String)
data class UiMessage(val text: String, val type: MessageType = MessageType.INFO)

class PortfolioViewModel(
    val repository: PortfolioRepository,
    private val notificationRepository: NotificationRepository,
    private val sessionManager: SessionManager,
    private val prefs: SharedPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState = _uiState.asStateFlow()

    val portfolioStocks = repository.allPortfolioStocks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val watchlistStocks = repository.allWatchlistStocks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _portfolioStats = MutableStateFlow<PortfolioStats?>(null)
    val portfolioStats = _portfolioStats.asStateFlow()

    private val _topSortedStocks = MutableStateFlow<List<StockEntity>>(emptyList())
    val topSortedStocks = _topSortedStocks.asStateFlow()

    private val _sparklineCache = MutableStateFlow<Map<String, List<CandlePointDto>>>(emptyMap())
    val sparklineCache = _sparklineCache.asStateFlow()

    private val _aiState = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val aiState = _aiState.asStateFlow()

    private val _thematicState = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val thematicState = _thematicState.asStateFlow()

    private val _uiMessage = MutableStateFlow<UiMessage?>(null)
    val uiMessage = _uiMessage.asStateFlow()

    private val _themeMode = MutableStateFlow(prefs.getInt("theme_mode", 0))
    val themeMode = _themeMode.asStateFlow()

    val notifications = notificationRepository.allNotifications.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    fun markNotificationAsRead(id: Int) {
        viewModelScope.launch { notificationRepository.markAsRead(id) }
    }

    fun deleteNotification(id: Int) {
        viewModelScope.launch { notificationRepository.deleteNotification(id) }
    }

    fun clearAllNotifications() {
        viewModelScope.launch { notificationRepository.clearAll() }
    }

    private val _isStartupComplete = MutableStateFlow(false)
    val isStartupComplete = _isStartupComplete.asStateFlow()

    val transactionHistory: StateFlow<List<com.apexinvest.app.data.TransactionEntity>> = repository.getRecentTransactions(500).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private var lastPortfolioSignature = ""
    private var lastChartShiftTime = System.currentTimeMillis()
    private var lastRefreshTime = 0L

    private var lastIsUsd = false
    private var lastRates: Map<String, Double> = emptyMap()
    private var lastPricesSignature = ""

    private val sparklineMutex = Mutex()

    val transactionAnalyticsState: StateFlow<TransactionAnalyticsState> = combine(
        transactionHistory,
        _uiState.map { it.isUsd }.distinctUntilChanged(),
        _uiState.map { it.rates }.distinctUntilChanged()
    ) { history, isUsd, rates ->
        withContext(Dispatchers.Default) {
            var buyAcc = 0.0
            var sellAcc = 0.0
            val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.US)

            val sortedAndMapped = history.sortedByDescending { it.timestamp }.map { tx ->
                val convertedPrice = com.apexinvest.app.util.getConvertedValue(tx.price, tx.symbol, isUsd, rates)
                val totalTxValue = convertedPrice * tx.quantity
                val isBuy = tx.type == TransactionType.BUY

                if (isBuy) buyAcc += totalTxValue else sellAcc += totalTxValue

                TransactionUiModel(
                    id = tx.id,
                    symbol = tx.symbol,
                    quantityStr = tx.quantity.toCleanString(),
                    formattedDate = dateFormatter.format(Date(tx.timestamp)),
                    totalValue = totalTxValue,
                    totalValueStr = totalTxValue.toCleanString(),
                    convertedPrice = convertedPrice,
                    convertedPriceStr = convertedPrice.toCleanString(),
                    isBuy = isBuy
                )
            }
            TransactionAnalyticsState(mappedHistory = sortedAndMapped, totalBuy = buyAcc, totalSell = sellAcc, isInitial = false)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, TransactionAnalyticsState())

    // 🚀 ARCHITECTURE UPGRADE: Pre-chewed State Strategy applied here
    val analyticsState: StateFlow<AnalyticsUiState> = combine(
        portfolioStocks,
        _uiState.map { it.isUsd }.distinctUntilChanged(),
        _uiState.map { it.rates }.distinctUntilChanged()
    ) { portfolio, isUsd, rates ->
        if (portfolio.isEmpty()) return@combine AnalyticsUiState.Empty
        withContext(Dispatchers.Default) { // Heavy lifting stays strictly on background thread
            val totalValue = portfolio.sumOf { com.apexinvest.app.util.getConvertedValue(it.currentPrice * it.quantity, it.symbol, isUsd, rates) }
            val (allocations, sectorMap) = MathUtils.calculateAllocations(portfolio, totalValue, isUsd, rates) { symbol -> getSectorForSymbol(symbol) }

            // 1. Crunch the Impact math in O(N) once, instead of inside Compose
            val mappedDrivers = allocations.map {
                ImpactDriver(
                    symbol = it.symbol,
                    allocationPercent = it.percent,
                    changePercent = it.changePercent,
                    impactScore = it.percent * it.changePercent
                )
            }.sortedByDescending { abs(it.impactScore) }

            // 2. Data Decimation: Slice list so UI only iterates over max 6 items
            val maxVisible = 6
            val topDrivers = mappedDrivers.take(maxVisible)
            val hiddenCount = (mappedDrivers.size - maxVisible).coerceAtLeast(0)

            AnalyticsUiState.Success(
                totalValue = totalValue,
                sectors = sectorMap,
                topDrivers = topDrivers,         // Passed pre-calculated
                hiddenImpactCount = hiddenCount, // Passed metadata
                topGainer = portfolio.maxByOrNull { it.changePercent },
                topLoser = portfolio.minByOrNull { it.changePercent },
                winRate = (portfolio.count { it.currentPrice >= it.buyPrice }.toDouble() / portfolio.size) * 100,
                currencySymbol = com.apexinvest.app.util.getCurrencySymbol(if (isUsd) "USD" else "INR")
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AnalyticsUiState.Loading)

    init {
        _uiState.update { it.copy(isUsd = prefs.getBoolean("is_usd_selected", false)) }

        viewModelScope.launch {
            repository.globalPriceUpdates.collect { update ->
                _uiState.update { state ->
                    val updatedPortfolio = state.portfolio.map {
                        if (it.symbol.equals(update.symbol, ignoreCase = true)) {
                            it.copy(currentPrice = update.price, dailyChange = update.change, changePercent = update.changePercent, previousClose = update.previousClose)
                        } else it
                    }
                    val updatedWatchlist = state.watchlist.map {
                        if (it.symbol.equals(update.symbol, ignoreCase = true)) {
                            it.copy(lastPrice = update.price, dailyChange = update.change, changePercent = update.changePercent, previousClose = update.previousClose)
                        } else it
                    }
                    state.copy(portfolio = updatedPortfolio, watchlist = updatedWatchlist)
                }

                _sparklineCache.update { current ->
                    val updated = current.toMutableMap()
                    val upperSym = update.symbol.uppercase()
                    val chart = updated[upperSym]?.toMutableList()
                    if (!chart.isNullOrEmpty()) {
                        val last = chart.last()
                        chart[chart.size - 1] = last.copy(
                            close = update.price,
                            high = last.high.coerceAtLeast(update.price),
                            low = last.low.coerceAtMost(update.price)
                        )
                        updated[upperSym] = chart
                    }
                    updated
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            combine(portfolioStocks, watchlistStocks) { p, w -> p to w }
                .distinctUntilChanged { old, new ->
                    val structureMatch = old.first.size == new.first.size && old.second.size == new.second.size
                    val pricesMatch = old.first.zip(new.first).all { (o, n) -> o.currentPrice == n.currentPrice && o.quantity == n.quantity }
                    structureMatch && pricesMatch
                }
                .collectLatest { (portfolio, watchlist) ->
                    val oldPortfolio = _uiState.value.portfolio
                    _uiState.update { it.copy(portfolio = portfolio, watchlist = watchlist) }

                    // 🚀 DETECT NEW SYMBOLS: If a new symbol is added, trigger a standard rolling fetch
                    val currentSymbols = portfolio.map { it.symbol.uppercase() }.toSet()
                    val oldSymbols = oldPortfolio.map { it.symbol.uppercase() }.toSet()
                    val newSymbols = currentSymbols - oldSymbols
                    
                    if (newSymbols.isNotEmpty()) {
                        viewModelScope.launch(Dispatchers.IO) {
                            newSymbols.forEach { sym ->
                                // This uses the standard YahooParser rolling window logic used during login
                                repository.fetchAndUpdatePrice(sym)
                            }
                            calculateDashboardData()
                        }
                    }

                    _sparklineCache.update { current ->
                        val updated = current.toMutableMap()
                        portfolio.forEach { stock ->
                            val chart = updated[stock.symbol]?.toMutableList()
                            if (!chart.isNullOrEmpty()) {
                                if (com.apexinvest.app.util.StockMetadataUtils.isMarketOpen(stock.symbol).first) {
                                    val last = chart.last()
                                    chart[chart.size - 1] = last.copy(
                                        close = stock.currentPrice,
                                        high = last.high.coerceAtLeast(stock.currentPrice),
                                        low = last.low.coerceAtMost(stock.currentPrice)
                                    )
                                    updated[stock.symbol] = chart
                                }
                            }
                        }
                        updated
                    }
                    calculateDashboardData()
                }
        }

        viewModelScope.launch(Dispatchers.IO) {
            val isLoggedIn = sessionManager.isLoggedIn()
            if (isLoggedIn) { _uiState.update { it.copy(isLoading = true, isHydrated = false) } }

            _portfolioStats.value = null
            _sparklineCache.value = emptyMap()

            if (isLoggedIn) fetchRate()

            repository.hydrateSparklineCache()
            loadCachedAiInsights()

            if (_uiState.value.rates.size > 1 || !isLoggedIn) {
                calculateDashboardData()
            }

            _isStartupComplete.value = true

            if (isLoggedIn) {
                try {
                    repository.refreshMissingStockMetadata()
                    repository.fullCloudSync(forceLoginSync = false)
                    repository.syncAllDataAndPrices(forceFullRefresh = true)
                } finally {
                    _uiState.update { it.copy(isLoading = false, isHydrated = true) }
                    calculateDashboardData()
                }
            } else {
                _uiState.update { it.copy(isHydrated = true) }
            }
        }
    }

    private suspend fun calculateDashboardData() = withContext(Dispatchers.Default) {
        val list = portfolioStocks.value
        val isUsd = _uiState.value.isUsd
        val rates = _uiState.value.rates

        if (isUsd && (rates.size <= 1)) return@withContext

        val currencyChanged = (isUsd != lastIsUsd)
        val ratesChanged = (rates != lastRates)
        val currentPricesSignature = list.joinToString(",") { "${it.currentPrice}:${it.quantity}" }
        val pricesChanged = (currentPricesSignature != lastPricesSignature)

        lastIsUsd = isUsd
        lastRates = rates
        lastPricesSignature = currentPricesSignature

        if (list.isEmpty()) {
            _portfolioStats.value = null
            _topSortedStocks.value = emptyList()
            _sparklineCache.value = emptyMap()
            lastPortfolioSignature = ""
            return@withContext
        }

        val sorted = list.asSequence().sortedByDescending { it.currentPrice * it.quantity }.take(5).toList()
        _topSortedStocks.value = sorted

        val currentSignature = list.joinToString("|") { "${it.symbol}:${it.quantity}" }
        val currentTime = System.currentTimeMillis()

        sparklineMutex.withLock {
            var currentSparklines = _sparklineCache.value.toMutableMap()
            val isMissingSparklines = list.any { !currentSparklines.containsKey(it.symbol) || currentSparklines[it.symbol].isNullOrEmpty() }

            if (currentSignature != lastPortfolioSignature || currentSparklines.isEmpty() || isMissingSparklines) {
                repository.prefetchSparklines(list.map { it.symbol })
                currentSparklines = list.associateBy({ it.symbol }, { repository.getCachedSparklineDtoSync(it.symbol) }).toMutableMap()
                lastPortfolioSignature = currentSignature
                lastChartShiftTime = currentTime
            } else {
                var modified = false
                list.forEach { stock ->
                    val chart = currentSparklines[stock.symbol]?.toMutableList()
                    if (!chart.isNullOrEmpty()) {
                        if (com.apexinvest.app.util.StockMetadataUtils.isMarketOpen(stock.symbol).first) {
                            // 🚀 LIVE UPDATE ONLY: Stop manual window shifting (handled by Repo/YahooParser)
                            val last = chart.last()
                            chart[chart.size - 1] = last.copy(
                                close = stock.currentPrice,
                                high = last.high.coerceAtLeast(stock.currentPrice),
                                low = last.low.coerceAtMost(stock.currentPrice)
                            )
                            currentSparklines[stock.symbol] = chart
                            modified = true
                        }
                    }
                }
                if (!modified && !currencyChanged && !ratesChanged && !pricesChanged) return@withContext
            }

            val newStats = MathUtils.calculatePortfolioStats(list, isUsd, rates, currentSparklines)

            // 🚀 ARCHITECTURE UPGRADE: Downsample Chart Data (Strategy 3)
            // If the data set exceeds 150 points, mathematically buckle it to avoid overwhelming the Canvas UI.
            val maxCanvasPoints = 150
            val optimizedChartData = if (newStats.chartData.size > maxCanvasPoints) {
                downsampleChartData(newStats.chartData, maxCanvasPoints)
            } else {
                newStats.chartData
            }

            _sparklineCache.value = currentSparklines
            _portfolioStats.value = newStats.copy(chartData = optimizedChartData)
        }
    }

    // 🚀 NEW: Fast decimation algorithm ensuring O(N) smooth downsampling for UI rendering
    private fun downsampleChartData(data: List<Double>, targetSize: Int): List<Double> {
        if (data.isEmpty() || targetSize <= 0) return emptyList()
        val result = ArrayList<Double>(targetSize)
        val bucketSize = data.size.toDouble() / targetSize

        for (i in 0 until targetSize) {
            val startIndex = (i * bucketSize).toInt()
            val endIndex = ((i + 1) * bucketSize).toInt().coerceAtMost(data.size)

            if (startIndex >= endIndex) continue

            var sum = 0.0
            for (j in startIndex until endIndex) {
                sum += data[j]
            }
            result.add(sum / (endIndex - startIndex)) // Average of bucket
        }
        return result
    }

    private suspend fun fetchRate() {
        val rates = repository.getConversionRates()
        if (rates.isNotEmpty()) { _uiState.update { it.copy(rates = rates, liveRate = rates["INR"] ?: 84.0) } }
    }

    private suspend fun fastRefreshLivePrices() = withContext(Dispatchers.IO) { repository.syncAllDataAndPrices() }

    private var updateJob: Job? = null
    private val periodicJobLock = Any()

    fun startPeriodicUpdates() {
        synchronized(periodicJobLock) {
            if (updateJob?.isActive == true) return
            updateJob = viewModelScope.launch(Dispatchers.IO) {
                while (isActive) {
                    fastRefreshLivePrices()
                    delay(5000.milliseconds)
                }
            }
        }
    }

    fun stopPeriodicUpdates() {
        synchronized(periodicJobLock) {
            updateJob?.cancel()
            updateJob = null
        }
    }

    fun forceRefresh() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastRefreshTime < 10_000) return
        lastRefreshTime = currentTime
        viewModelScope.launch(Dispatchers.IO) { repository.syncAllDataAndPrices() }
    }

    fun loadPortfolioAndPrices() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch(Dispatchers.IO) {
            val showLoading = portfolioStocks.value.isEmpty()
            if (showLoading) _uiState.update { it.copy(isLoading = true) }
            try { repository.syncAllDataAndPrices(); lastRefreshTime = System.currentTimeMillis() }
            finally { if (showLoading) _uiState.update { it.copy(isLoading = false) } }
        }
    }

    fun onLoginSuccess() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, isHydrated = false) }
            try {
                lastPortfolioSignature = ""
                _sparklineCache.value = emptyMap()
                fetchRate()
                repository.fullCloudSync(forceLoginSync = true)
                repository.refreshMissingStockMetadata()
                repository.syncAllDataAndPrices(forceFullRefresh = true)
                delay(800.milliseconds)
                calculateDashboardData()
            } finally {
                _uiState.update { it.copy(isLoading = false, isHydrated = true) }
            }
        }
    }

    fun toggleCurrency() {
        val newIsUsd = !_uiState.value.isUsd
        prefs.edit(commit = true) { putBoolean("is_usd_selected", newIsUsd) }
        _uiState.update { it.copy(isUsd = newIsUsd) }
        viewModelScope.launch { calculateDashboardData() }
    }

    fun executeTrade(symbol: String, isBuy: Boolean, quantityStr: String, priceStr: String, dateStr: String) {
        val q = quantityStr.toDoubleOrNull(); val p = priceStr.toDoubleOrNull()
        if (q == null || p == null || (q <= 0) || (p <= 0)) { _uiMessage.value = UiMessage("Invalid input", MessageType.ERROR); return }
        viewModelScope.launch(Dispatchers.IO) {
            repository.recordTrade(symbol, if (isBuy) TransactionType.BUY else TransactionType.SELL, q, p, dateStr)
            _uiMessage.value = UiMessage("${if (isBuy) "Bought" else "Sold"} $q shares of $symbol", MessageType.SUCCESS)
        }
    }

    fun addWatchlistStock(symbol: String) { viewModelScope.launch(Dispatchers.IO) { repository.addStockToWatchlist(symbol) } }
    fun deleteWatchlistStock(symbol: String) { viewModelScope.launch(Dispatchers.IO) { repository.deleteWatchlistStock(symbol) } }

    private val _searchResults = MutableStateFlow<List<StockSearchResult>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private var searchJob: Job? = null

    fun searchStocks(query: String) {
        if (query.length < 2) {
            searchJob?.cancel()
            _searchResults.value = emptyList()
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch(Dispatchers.IO) {
            delay(400.milliseconds)
            if (isActive) {
                _searchResults.value = repository.searchStocks(query)
            }
        }
    }

    fun clearSearchResults() {
        searchJob?.cancel()
        _searchResults.value = emptyList()
    }

    private fun getPortfolioSignature(portfolio: List<StockEntity>): String {
        if (portfolio.isEmpty()) return "EMPTY"
        return portfolio.sortedBy { it.symbol }.joinToString("|") { "${it.symbol}:${it.quantity}" }
    }

    fun generateIdeas(explicitStocks: List<StockEntity> = portfolioStocks.value, theme: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val isThematic = !theme.isNullOrBlank()
            val stateToUpdate = if (isThematic) _thematicState else _aiState

            if (!isThematic && explicitStocks.isEmpty()) {
                stateToUpdate.value = AiUiState.Error("Add stocks to your portfolio to generate personalized AI strategies!")
                return@launch
            }

            var currentSig = ""
            if (!isThematic) {
                currentSig = getPortfolioSignature(explicitStocks)
                val cachedSig = prefs.getString("last_ai_portfolio_sig", "")

                if (currentSig != "EMPTY" && currentSig == cachedSig && _aiState.value is AiUiState.Success) return@launch

                if (currentSig != "EMPTY" && currentSig == cachedSig && _aiState.value !is AiUiState.Success) {
                    val cachedResult = repository.getCachedAiInsights("AI_PORTFOLIO_IDEAS")
                    if (cachedResult != null) {
                        _aiState.value = parseAiResponse(cachedResult)
                        if (_aiState.value is AiUiState.Success) return@launch
                    }
                }
            }

            stateToUpdate.value = AiUiState.Thinking
            val summary = explicitStocks.joinToString("; ") { "${it.symbol} (${it.quantity})" }

            try {
                val result = if (theme.isNullOrBlank()) {
                    repository.getAiPortfolioInsights(summary)
                } else {
                    repository.getAiThematicInsights(theme)
                }

                val parsedState = parseAiResponse(result)
                stateToUpdate.value = parsedState

                if (parsedState is AiUiState.Success && !isThematic) {
                    repository.saveAiInsights("AI_PORTFOLIO_IDEAS", result)
                    prefs.edit(commit = true) { putString("last_ai_portfolio_sig", currentSig) }
                } else if (parsedState is AiUiState.Error && !isThematic) {
                    prefs.edit(commit = true) { remove("last_ai_portfolio_sig") }
                }
            } catch (_: Exception) {
                stateToUpdate.value = AiUiState.Error("Network error. Tap to retry.")
                if (!isThematic) prefs.edit(commit = true) { remove("last_ai_portfolio_sig") }
            }
        }
    }

    private suspend fun loadCachedAiInsights() {
        val currentSig = getPortfolioSignature(portfolioStocks.value)
        val cachedSig = prefs.getString("last_ai_portfolio_sig", "")
        if (currentSig != "EMPTY" && currentSig == cachedSig) {
            repository.getCachedAiInsights("AI_PORTFOLIO_IDEAS")?.let {
                _aiState.value = parseAiResponse(it)
            }
        }
    }

    private fun parseAiResponse(response: String): AiUiState {
        if (response.contains("Offline Mode: Unable to reach server", ignoreCase = true)) {
            return AiUiState.Error("Currently offline. Waiting for connection to generate AI insights...")
        }

        val insights = mutableListOf<AiInsight>()
        val suggestions = mutableListOf<StockSuggestion>()
        response.lines().forEach { line ->
            val l = line.trim()
            if (l.isBlank()) return@forEach
            when {
                l.startsWith("[RISK]") -> insights.add(AiInsight("Risk", l.replace("[RISK]", "").trim(), InsightType.WARNING))
                l.startsWith("[OPPORTUNITY]") -> insights.add(AiInsight("Opportunity", l.replace("[OPPORTUNITY]", "").trim(), InsightType.OPPORTUNITY))
                l.startsWith("[SUGGESTION]") -> {
                    val p = l.replace("[SUGGESTION]", "").split("|")
                    if (p.size >= 2) suggestions.add(StockSuggestion(p[0].trim(), if (p.size > 2) p[1].trim() else "Market", p.last().trim()))
                }
            }
        }
        return if (insights.isEmpty() && suggestions.isEmpty()) {
            AiUiState.Error("Failed to parse AI response")
        } else {
            AiUiState.Success(insights, suggestions)
        }
    }

    fun autoHealAI(lastThemeSearched: String? = null) {
        val currentAIState = _aiState.value
        if (currentAIState is AiUiState.Error && !currentAIState.message.contains("Add stocks", ignoreCase = true)) {
            generateIdeas()
        }
        val currentThematicState = _thematicState.value
        if (currentThematicState is AiUiState.Error && !lastThemeSearched.isNullOrBlank()) {
            generateIdeas(theme = lastThemeSearched)
        }
    }

    fun setThemeMode(mode: Int) { _themeMode.value = mode; prefs.edit { putInt("theme_mode", mode) } }
    fun toggleNotifications() { val n = !notificationsEnabled.value; _notificationsEnabled.value = n; prefs.edit { putBoolean("notifications_enabled", n) } }
    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean("notifications_enabled", true)); val notificationsEnabled = _notificationsEnabled.asStateFlow()

    private val _defaultBuyQty = MutableStateFlow(sessionManager.getDefaultBuyQty()); val defaultBuyQty = _defaultBuyQty.asStateFlow()
    private val _defaultSellQty = MutableStateFlow(sessionManager.getDefaultSellQty()); val defaultSellQty = _defaultSellQty.asStateFlow()

    fun setTradingPresets(buy: Double, sell: Double) { sessionManager.saveTradingPresets(buy, sell); _defaultBuyQty.value = buy; _defaultSellQty.value = sell }
    fun getCompanyNameForSymbol(s: String) = portfolioStocks.value.find { it.symbol == s }?.companyName ?: watchlistStocks.value.find { it.symbol == s }?.companyName ?: s
    fun getSectorForSymbol(s: String) = portfolioStocks.value.find { it.symbol == s }?.sector ?: watchlistStocks.value.find { it.symbol == s }?.sector ?: "Other"
    fun getCachedSparkline(s: String): List<Double> = repository.getCachedSparklineSync(s)

    fun deleteSelectedTransactions(ids: Set<Int>) { viewModelScope.launch(Dispatchers.IO) { val history = repository.getAllTransactionHistory().first(); history.filter { it.id in ids }.forEach { repository.deleteTransaction(it) } } }

    fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllLocalData()
            _uiState.value = AppUiState()
            _portfolioStats.value = null
            _topSortedStocks.value = emptyList()
            _sparklineCache.value = emptyMap()
            _aiState.value = AiUiState.Idle
            _thematicState.value = AiUiState.Idle
            _uiMessage.value = null // 🚀 ADDED
            lastPortfolioSignature = ""
            prefs.edit(commit = true) { 
                remove("last_ai_portfolio_sig")
                remove("theme_mode") // 🚀 Optional: Reset theme on full wipe
                remove("is_usd_selected")
            }
        }
    }

    fun clearMessage() { _uiMessage.value = null }
    fun showMessage(msg: String, type: MessageType = MessageType.INFO) { _uiMessage.value = UiMessage(msg, type) }
    fun refreshPricesAndGenerateCsv() = flow { repository.syncAllDataAndPrices(); emit(repository.generatePortfolioCsv()) }

    companion object

    fun onAppResumed() { forceRefresh() }
}