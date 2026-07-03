package com.apexinvest.app.viewmodel

import android.content.SharedPreferences
<<<<<<< HEAD
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.apexinvest.app.api.models.StockSearchResult
import com.apexinvest.app.data.PortfolioRepository
import com.apexinvest.app.data.StockEntity
import com.apexinvest.app.data.TransactionType
import com.apexinvest.app.data.WatchlistEntity
import com.apexinvest.app.data.repository.MarketRepository
import com.apexinvest.app.data.repository.NotificationRepository
import com.apexinvest.app.ui.components.MessageType
import com.apexinvest.app.util.MathUtils
import com.apexinvest.app.util.toCleanString
import com.apexinvest.app.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
=======
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.apexinvest.app.api.models.PredictionResponse
import com.apexinvest.app.data.GeminiIdeaGenerator
import com.apexinvest.app.data.PortfolioRepository
import com.apexinvest.app.data.StockEntity
import com.apexinvest.app.data.StockFullDetail
import com.apexinvest.app.data.StockSearchResult
import com.apexinvest.app.data.TransactionEntity
import com.apexinvest.app.data.TransactionType
import com.apexinvest.app.data.WatchlistEntity
import com.apexinvest.app.data.model.StockNews
import com.apexinvest.app.data.repository.MarketRepository
import kotlinx.coroutines.Job
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
<<<<<<< HEAD
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

data class TransactionUiModel(
    val id: Int,
    val symbol: String,
    val quantityStr: String,
    val formattedDate: String,
    val totalValue: Double,
    val convertedPrice: Double,
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
=======
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

// ===========================================================================
// 1. STATE DEFINITIONS
// ===========================================================================
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2

data class AppUiState(
    val portfolio: List<StockEntity> = emptyList(),
    val watchlist: List<WatchlistEntity> = emptyList(),
<<<<<<< HEAD
    val liveRate: Double = 84.0,
    val rates: Map<String, Double> = mapOf("INR" to 84.0, "USD" to 1.0),
    val isUsd: Boolean = false,
    val isLoading: Boolean = false,
    val isHydrated: Boolean = false
)

=======
    val liveRate: Double = 91.0, // Updated default to be closer to reality
    val isUsd: Boolean = false,
    val isLoading: Boolean = false
)

sealed class NewsUiState {
    object Loading : NewsUiState()
    data class Success(val articles: List<StockNews>) : NewsUiState()
    data class Error(val message: String) : NewsUiState()
}

sealed class IdeasState {
    object Idle : IdeasState()
    object Loading : IdeasState()
    data class Success(val ideas: String) : IdeasState()
    data class Error(val message: String) : IdeasState()
}

sealed class PredictionState {
    object Idle : PredictionState()
    object Loading : PredictionState()
    data class Success(val response: PredictionResponse) : PredictionState()
    data class Error(val message: String) : PredictionState()
}

sealed class StockDetailState {
    object Loading : StockDetailState()
    data class Success(val data: StockFullDetail) : StockDetailState()
    data class Error(val message: String) : StockDetailState()
}

// --- ANALYTICS STATES ---
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
sealed class AnalyticsUiState {
    object Loading : AnalyticsUiState()
    object Empty : AnalyticsUiState()
    data class Success(
        val totalValue: Double,
        val sectors: Map<String, Double>,
        val allocations: List<StockAllocation>,
<<<<<<< HEAD
=======
        val historyCurve: List<Pair<String, Double>>,
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
        val topGainer: StockEntity?,
        val topLoser: StockEntity?,
        val winRate: Double,
        val currencySymbol: String
    ) : AnalyticsUiState()
}

<<<<<<< HEAD
data class StockAllocation(val symbol: String, val value: Double, val percent: Double, val isProfit: Boolean, val changePercent: Double)

=======
data class StockAllocation(
    val symbol: String,
    val value: Double,
    val percent: Double,
    val isProfit: Boolean,
    val changePercent: Double
)

// --- AI INTELLIGENCE STATES (Phase 4) ---
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
sealed class AiUiState {
    object Idle : AiUiState()
    object Thinking : AiUiState()
    data class Success(val insights: List<AiInsight>, val suggestions: List<StockSuggestion>) : AiUiState()
    data class Error(val message: String) : AiUiState()
}

<<<<<<< HEAD
data class AiInsight(val title: String, val description: String, val type: InsightType)
enum class InsightType { WARNING, SUCCESS, OPPORTUNITY }
data class StockSuggestion(val symbol: String, val sector: String, val reason: String)
data class UiMessage(val text: String, val type: MessageType = MessageType.INFO)

class PortfolioViewModel(
    val repository: PortfolioRepository,
    private val marketRepository: MarketRepository,
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

    private val _sparklineCache = MutableStateFlow<Map<String, List<Double>>>(emptyMap())
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

    val isPriceUpdating = _uiState.map { it.isLoading }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue = false)
    val showAddWatchlistDialog = MutableStateFlow(false)

    private val _isStartupComplete = MutableStateFlow(false)
    val isStartupComplete = _isStartupComplete.asStateFlow()

    val transactionHistory: StateFlow<List<com.apexinvest.app.data.TransactionEntity>> = repository.getRecentTransactions(500).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var lastPortfolioSignature = ""
    private var lastChartShiftTime = System.currentTimeMillis()
    private var lastRefreshTime = 0L

    private val sparklineMutex = Mutex()

    val transactionAnalyticsState: StateFlow<TransactionAnalyticsState> = combine(
        transactionHistory,
        _uiState.map { it.isUsd }.distinctUntilChanged(),
        _uiState.map { it.rates }.distinctUntilChanged()
    ) { history: List<com.apexinvest.app.data.TransactionEntity>, isUsd: Boolean, rates: Map<String, Double> ->
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
                    convertedPrice = convertedPrice,
                    isBuy = isBuy
                )
            }
            TransactionAnalyticsState(mappedHistory = sortedAndMapped, totalBuy = buyAcc, totalSell = sellAcc, isInitial = false)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TransactionAnalyticsState())

    val analyticsState: StateFlow<AnalyticsUiState> = combine(
        portfolioStocks,
        _uiState.map { it.isUsd }.distinctUntilChanged(),
        _uiState.map { it.rates }.distinctUntilChanged()
    ) { portfolio, isUsd, rates ->
        if (portfolio.isEmpty()) return@combine AnalyticsUiState.Empty
        withContext(Dispatchers.Default) {
            val totalValue = portfolio.sumOf { com.apexinvest.app.util.getConvertedValue(it.currentPrice * it.quantity, it.symbol, isUsd, rates) }
            val (allocations, sectorMap) = MathUtils.calculateAllocations(portfolio, totalValue, isUsd, rates) { symbol -> getSectorForSymbol(symbol) }
            AnalyticsUiState.Success(
                totalValue,
                sectorMap,
                allocations,
                portfolio.maxByOrNull { it.changePercent },
                portfolio.minByOrNull { it.changePercent },
                (portfolio.count { it.currentPrice >= it.buyPrice }.toDouble() / portfolio.size) * 100,
                com.apexinvest.app.util.getCurrencySymbol(if (isUsd) "USD" else "INR")
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AnalyticsUiState.Loading)

    init {
        _uiState.update { it.copy(isUsd = prefs.getBoolean("is_usd_selected", false)) }

        // 🚀 REAL-TIME SYNC: Listen for price updates from anywhere (WS or Yahoo)
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

                // Update Sparkline Cache as well
                _sparklineCache.update { current ->
                    val updated = current.toMutableMap()
                    val upperSym = update.symbol.uppercase()
                    val chart = updated[upperSym]?.toMutableList()
                    if (!chart.isNullOrEmpty()) {
                        chart[chart.size - 1] = update.price
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
                    val pricesMatch = old.first.zip(new.first).all { (o, n) -> o.currentPrice == n.currentPrice }
                    structureMatch && pricesMatch
                }
                .collectLatest { (portfolio, watchlist) ->
                    _uiState.update { it.copy(portfolio = portfolio, watchlist = watchlist) }

                    _sparklineCache.update { current ->
                        val updated = current.toMutableMap()
                        portfolio.forEach { stock ->
                            val chart = updated[stock.symbol]?.toMutableList()
                            if (chart != null && chart.isNotEmpty()) {
                                // 🚀 FIX: Only update sparkline last point if market is open
                                if (com.apexinvest.app.util.StockMetadataUtils.isMarketOpen(stock.symbol).first) {
                                    chart[chart.size - 1] = stock.currentPrice
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

            repository.hydrateSparklineCache()
            loadCachedAiInsights()
            calculateDashboardData()
            _isStartupComplete.value = true

            if (isLoggedIn) {
                try {
                    fetchRate()
                    repository.refreshMissingStockMetadata()
                    repository.fullCloudSync(forceLoginSync = false)
                    repository.syncAllDataAndPrices(forceFullRefresh = true)
                } finally {
                    _uiState.update { it.copy(isLoading = false, isHydrated = true) }
                    calculateDashboardData()
                }
            } else {
                _uiState.update { it.copy(isHydrated = true) }
=======
data class AiInsight(
    val title: String,
    val description: String,
    val type: InsightType
)

enum class InsightType { WARNING, SUCCESS, OPPORTUNITY }

data class StockSuggestion(
    val symbol: String,
    val sector: String,
    val reason: String
)

class PortfolioViewModel(
    private val repository: PortfolioRepository,
    private val marketRepository: MarketRepository,
    private val ideaGenerator: GeminiIdeaGenerator,
    private val userIdFlow: StateFlow<String?>,
    private val prefs: SharedPreferences
) : ViewModel() {

    // --- SECTION A: CORE STATE ---
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState = _uiState.asStateFlow()

    // --- SECTION B: LEGACY COMPATIBILITY ---
    val portfolioStocks: StateFlow<List<StockEntity>> = _uiState
        .map { it.portfolio }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchlistStocks: StateFlow<List<WatchlistEntity>> = _uiState
        .map { it.watchlist }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isPriceUpdating: StateFlow<Boolean> = _uiState
        .map { it.isLoading }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val showAddWatchlistDialog = MutableStateFlow(false)
    val isUsd: StateFlow<Boolean> = _uiState.map { it.isUsd }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _loadingIndicator = MutableStateFlow(false)
    val isLoadingIdeas: StateFlow<Boolean> = _loadingIndicator.asStateFlow()

    // --- SECTION C: FEATURES & SEARCH ---
    private val _searchResults = MutableStateFlow<List<StockSearchResult>>(emptyList())
    val searchResults: StateFlow<List<StockSearchResult>> = _searchResults.asStateFlow()

    // Legacy Text Ideas (Thematic)
    private val _portfolioIdeas = MutableStateFlow<IdeasState>(IdeasState.Idle)
    val portfolioIdeas: StateFlow<IdeasState> = _portfolioIdeas.asStateFlow()

    private val _thematicIdeas = MutableStateFlow<IdeasState>(IdeasState.Idle)
    val thematicIdeas: StateFlow<IdeasState> = _thematicIdeas.asStateFlow()

    // NEW AI Rich UI State
    private val _aiState = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val aiState: StateFlow<AiUiState> = _aiState.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    private val _predictionState = MutableStateFlow<PredictionState>(PredictionState.Idle)
    val predictionState: StateFlow<PredictionState> = _predictionState.asStateFlow()

    private val _stockDetailState = MutableStateFlow<StockDetailState>(StockDetailState.Loading)
    val stockDetailState = _stockDetailState.asStateFlow()

    private val _selectedRange = MutableStateFlow("1M")
    val selectedRange = _selectedRange.asStateFlow()

    private val _transactionHistory = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val transactionHistory = _transactionHistory.asStateFlow()

    private var refreshJob: Job? = null
    private val _dataCache = mutableMapOf<String, StockFullDetail>()

    private val _newsState = MutableStateFlow<NewsUiState>(NewsUiState.Loading)
    val newsState = _newsState.asStateFlow()

    init {
        val savedIsUsd = prefs.getBoolean("is_usd_selected", false)
        _uiState.update { it.copy(isUsd = savedIsUsd) }
        fetchRate()

        viewModelScope.launch {
            combine(
                repository.getLocalPortfolio(),
                repository.getLocalWatchlist()
            ) { portfolio, watchlist ->
                Pair(portfolio, watchlist)
            }.collectLatest { (portfolio, watchlist) ->
                _uiState.update { it.copy(portfolio = portfolio, watchlist = watchlist) }
            }
        }

        viewModelScope.launch {
            userIdFlow.collectLatest { userId ->
                if (userId != null) loadPortfolioAndPrices()
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
            }
        }
    }

<<<<<<< HEAD
    private suspend fun calculateDashboardData() = withContext(Dispatchers.Default) {
        val list = portfolioStocks.value
        val isUsd = _uiState.value.isUsd
        val rates = _uiState.value.rates

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
        val shouldShiftWindow = (currentTime - lastChartShiftTime) > 120_000

        sparklineMutex.withLock {
            var currentSparklines = _sparklineCache.value.toMutableMap()
            val isMissingSparklines = list.any { !currentSparklines.containsKey(it.symbol) || currentSparklines[it.symbol].isNullOrEmpty() }

            if (currentSignature != lastPortfolioSignature || currentSparklines.isEmpty() || isMissingSparklines) {
                repository.prefetchSparklines(list.map { it.symbol })

                currentSparklines = list.associateBy({ it.symbol }, { repository.getCachedSparklineSync(it.symbol) }).toMutableMap()
                lastPortfolioSignature = currentSignature
                lastChartShiftTime = currentTime
            } else {
                var modified = false
                list.forEach { stock ->
                    val chart = currentSparklines[stock.symbol]?.toMutableList()
                    if (chart != null && chart.isNotEmpty()) {
                        // 🚀 FIX: Only update sparkline if market is open
                        if (com.apexinvest.app.util.StockMetadataUtils.isMarketOpen(stock.symbol).first) {
                            if (shouldShiftWindow) {
                                chart.removeAt(0)
                                chart.add(stock.currentPrice)
                            } else {
                                chart[chart.size - 1] = stock.currentPrice
                            }
                            currentSparklines[stock.symbol] = chart
                            modified = true
                        }
                    }
                }
                if (shouldShiftWindow) lastChartShiftTime = currentTime
                if (!modified) return@withContext
            }

            val newStats = MathUtils.calculatePortfolioStats(list, isUsd, rates, currentSparklines)
            _sparklineCache.value = currentSparklines
            _portfolioStats.value = newStats
        }
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
                    // 🚀 Snappier Dashboard: Poll every 5s instead of 8s (matches the 4s cache TTL in repository)
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
                delay(800)
                calculateDashboardData()
            } finally {
                _uiState.update { it.copy(isLoading = false, isHydrated = true) }
            }
        }
    }

    fun loadCurrentPrice(s: String) { viewModelScope.launch(Dispatchers.IO) { repository.fetchLivePriceOnly(s) } }
    fun loadStockChart(symbol: String, range: String = "1d") = flow { emit(repository.fetchChartOnly(symbol, range)) }

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

    fun deleteStock(stock: StockEntity) { viewModelScope.launch(Dispatchers.IO) { repository.deletePortfolioStock(stock.symbol) } }
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
            delay(400) // 🚀 Debounce: Wait for 400ms gap in typing
            if (isActive) {
                _searchResults.value = repository.searchStocks(query)
=======
    private fun fetchRate() {
        viewModelScope.launch {
            try {
                // Reusing your Repo logic which we know works (gets ~91)
                val rate = repository.getConversionRate()
                if (rate > 0) _uiState.update { it.copy(liveRate = rate) }
            } catch (_: Exception) {}
        }
    }

    // --- SECTION D: ACTIONS ---

    fun toggleCurrency() {
        val newIsUsd = !_uiState.value.isUsd
        prefs.edit().putBoolean("is_usd_selected", newIsUsd).apply()
        _uiState.update { it.copy(isUsd = newIsUsd) }
    }

    fun loadPortfolioAndPrices() {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // 1. Sync Stock Prices
                repository.syncAllDataAndPrices()
                // 2. Sync Currency Rate (Added this so rate is always fresh on swipe-refresh)
                fetchRate()
                _uiMessage.value = "Synced."
            } catch (e: Exception) {
                _uiMessage.value = "Sync Error: ${e.message}"
            } finally {
                _uiState.update { it.copy(isLoading = false) }
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
            }
        }
    }

<<<<<<< HEAD
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

            // 1. EMPTY PORTFOLIO GUARD
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

                // ✅ 2. POST-SUCCESS SAVE: Only commit cache if server responded with real data
                if (parsedState is AiUiState.Success && !isThematic) {
                    repository.saveAiInsights("AI_PORTFOLIO_IDEAS", result)
                    prefs.edit(commit = true) { putString("last_ai_portfolio_sig", currentSig) }
                } else if (parsedState is AiUiState.Error && !isThematic) {
                    prefs.edit(commit = true) { remove("last_ai_portfolio_sig") }
                }
            } catch (e: Exception) {
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

    // 🛡️ 3. FALLBACK INTERCEPTOR: Do not let offline text pretend to be a Success
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
=======
    fun refreshPrices() { loadPortfolioAndPrices() }

    suspend fun refreshPricesAndGenerateCsv(): Result<String> {
        if (_uiState.value.isLoading) return Result.failure(IllegalStateException("Busy"))
        _uiState.update { it.copy(isLoading = true) }
        return try {
            repository.syncAllDataAndPrices()
            val csv = repository.generatePortfolioCsv()
            Result.success(csv)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    // --- TRANSACTION ENGINE ---

    fun executeTrade(symbol: String, isBuy: Boolean, quantityStr: String, priceStr: String, dateStr: String) {
        viewModelScope.launch {
            val quantity = quantityStr.toIntOrNull()
            val price = priceStr.toDoubleOrNull()

            if (quantity == null || price == null || quantity <= 0 || price <= 0.0) {
                _uiMessage.value = "Invalid input details."
                return@launch
            }

            try {
                val type = if (isBuy) TransactionType.BUY else TransactionType.SELL
                repository.recordTrade(symbol, type, quantity, price, dateStr)
                loadPortfolioAndPrices()
                loadTransactionHistory(symbol)
                _uiMessage.value = "Trade Executed: $type $quantity $symbol"
            } catch (e: Exception) {
                _uiMessage.value = "Trade Failed: ${e.message}"
            }
        }
    }

    private var historyJob: Job? = null

    fun loadTransactionHistory(symbol: String) {
        historyJob?.cancel()
        historyJob = viewModelScope.launch {
            repository.getTransactionHistory(symbol)
                .catch { e -> _uiMessage.value = "Failed to load history: ${e.message}" }
                .collectLatest { history ->
                    _transactionHistory.value = history
                }
        }
    }

    // Legacy Wrappers
    fun addOrUpdateStock(symbol: String, quantityStr: String, buyPriceStr: String, buyDateStr: String) {
        executeTrade(symbol, true, quantityStr, buyPriceStr, buyDateStr)
    }

    fun deleteStock(stock: StockEntity) {
        executeTrade(stock.symbol, false, stock.quantity.toString(), stock.buyPrice.toString(), stock.buyDate)
    }

    // Watchlist Actions
    fun addWatchlistStock(symbol: String) {
        viewModelScope.launch {
            try {
                repository.addWatchlistStock(symbol.uppercase())
                _uiMessage.value = "Added $symbol"
            } catch (e: Exception) {
                _uiMessage.value = "Add failed: ${e.message}"
            }
        }
    }

    fun deleteWatchlistStock(symbol: String) {
        viewModelScope.launch {
            val currentList = _uiState.value.watchlist.toMutableList()
            currentList.removeAll { it.symbol == symbol }
            _uiState.update { it.copy(watchlist = currentList) }
            try {
                repository.deleteWatchlistStock(symbol.uppercase())
                _uiMessage.value = "Removed $symbol"
            } catch (e: Exception) {
                loadPortfolioAndPrices()
            }
        }
    }

    // --- SEARCH & DETAILS ---

    fun searchStocks(query: String) {
        if (query.length < 2) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            try {
                _searchResults.value = repository.searchStocks(query)
            } catch (e: Exception) {
                _searchResults.value = emptyList()
            }
        }
    }

    fun clearSearchResults() { _searchResults.value = emptyList() }

    fun loadStockDetails(symbol: String, range: String = "1M") {
        _selectedRange.value = range
        val cacheKey = "${symbol}_${range}"

        if (_dataCache.containsKey(cacheKey)) {
            _stockDetailState.value = StockDetailState.Success(_dataCache[cacheKey]!!)
            return
        }

        _stockDetailState.value = StockDetailState.Loading
        viewModelScope.launch {
            repository.getFullStockDetails(symbol, range).fold(
                onSuccess = { data ->
                    _dataCache[cacheKey] = data
                    _stockDetailState.value = StockDetailState.Success(data)
                },
                onFailure = { e ->
                    _stockDetailState.value = StockDetailState.Error(e.message ?: "Failed")
                }
            )
        }
    }

    // 2. Thematic Search State
    private val _thematicState = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val thematicState: StateFlow<AiUiState> = _thematicState.asStateFlow()

    fun generateIdeas(theme: String? = null) {
        viewModelScope.launch {
            // CASE 1: Thematic Search
            if (!theme.isNullOrBlank()) {
                _thematicState.value = AiUiState.Thinking
                val result = ideaGenerator.generateThematicIdeas(theme)
                _thematicState.value = parseAiResponse(result)
                return@launch
            }

            // CASE 2: Portfolio Audit
            if (_loadingIndicator.value) return@launch
            _aiState.value = AiUiState.Thinking

            val portfolioSummary = if (_uiState.value.portfolio.isEmpty()) "Client has no holdings."
            else _uiState.value.portfolio.joinToString("; ") { "${it.symbol} (Qty: ${it.quantity})" }

            val result = ideaGenerator.generatePortfolioIdeas(portfolioSummary)
            _aiState.value = parseAiResponse(result)
        }
    }

    // --- ROBUST PARSER ---
    private fun parseAiResponse(response: String): AiUiState {
        val insights = mutableListOf<AiInsight>()
        val suggestions = mutableListOf<StockSuggestion>()

        response.lines().forEach { line ->
            val cleanLine = line.trim()
            if (cleanLine.isBlank()) return@forEach

            when {
                // 1. RISK -> Red/Orange Warning
                cleanLine.startsWith("[RISK]") -> {
                    val rawContent = cleanLine.replace("[RISK]", "").trim()
                    val parts = rawContent.split(":", limit = 2)
                    if (parts.size == 2) {
                        insights.add(AiInsight(parts[0].trim(), parts[1].trim(), InsightType.WARNING))
                    } else {
                        insights.add(AiInsight("Risk Alert", rawContent, InsightType.WARNING))
                    }
                }

                // 2. OPPORTUNITY -> Blue Trend
                cleanLine.startsWith("[OPPORTUNITY]") -> {
                    val rawContent = cleanLine.replace("[OPPORTUNITY]", "").trim()
                    val parts = rawContent.split(":", limit = 2)
                    if (parts.size == 2) {
                        insights.add(AiInsight(parts[0].trim(), parts[1].trim(), InsightType.OPPORTUNITY))
                    } else {
                        insights.add(AiInsight("Opportunity", rawContent, InsightType.OPPORTUNITY))
                    }
                }

                // 3. STOCK SUGGESTIONS
                cleanLine.startsWith("[SUGGESTION]") -> {
                    val content = cleanLine.replace("[SUGGESTION]", "").trim()
                    val parts = content.split("|")
                    if (parts.size >= 3) {
                        suggestions.add(StockSuggestion(parts[0].trim(), parts[1].trim(), parts[2].trim()))
                    }
                }
            }
        }

        return if (insights.isEmpty() && suggestions.isEmpty()) {
            AiUiState.Error("AI returned invalid format. Try again.")
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
        } else {
            AiUiState.Success(insights, suggestions)
        }
    }

<<<<<<< HEAD
    // 🚀 4. NEW: Dedicated Auto-Heal hook for your UI to call on reconnection
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

    fun loadTransactionHistory() {}
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
            lastPortfolioSignature = ""
            prefs.edit(commit = true) { remove("last_ai_portfolio_sig") }
        }
    }

    fun deleteUserAccount() { clearAllData() }
    fun clearMessage() { _uiMessage.value = null }
    fun showMessage(msg: String, type: MessageType = MessageType.INFO) { _uiMessage.value = UiMessage(msg, type) }
    fun refreshPricesAndGenerateCsv() = flow { repository.syncAllDataAndPrices(); emit(repository.generatePortfolioCsv()) }

    companion object {
        fun Factory(repository: PortfolioRepository, marketRepository: MarketRepository, notificationRepository: NotificationRepository, sessionManager: SessionManager, prefs: SharedPreferences) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = PortfolioViewModel(repository, marketRepository, notificationRepository, sessionManager, prefs) as T
        }
    }
    fun onAppResumed() { forceRefresh() }
=======
    fun clearMessage() { _uiMessage.value = null }

    // --- SECTION E: PORTFOLIO ANALYTICS ---

    private val _analyticsState = MutableStateFlow<AnalyticsUiState>(AnalyticsUiState.Loading)
    val analyticsState = _analyticsState.asStateFlow()

    fun loadPortfolioAnalytics() {
        viewModelScope.launch {
            _analyticsState.value = AnalyticsUiState.Loading

            val portfolio = _uiState.value.portfolio
            val isUsd = _uiState.value.isUsd
            val liveRate = _uiState.value.liveRate

            if (portfolio.isEmpty()) {
                _analyticsState.value = AnalyticsUiState.Empty
                return@launch
            }

            // Calculation logic (Depends on util helper)
            val totalValue = portfolio.sumOf {
                com.apexinvest.app.util.getConvertedValue(it.currentPrice * it.quantity, it.symbol, isUsd, liveRate)
            }

            val totalOpen = portfolio.sumOf {
                val openPrice = it.currentPrice / (1 + it.dailyChange / 100)
                com.apexinvest.app.util.getConvertedValue(openPrice * it.quantity, it.symbol, isUsd, liveRate)
            }

            val sectorMap = mutableMapOf<String, Double>()

            val allocationList = portfolio.map { stock ->
                val currentVal = com.apexinvest.app.util.getConvertedValue(stock.currentPrice * stock.quantity, stock.symbol, isUsd, liveRate)
                val sector = identifySector(stock.symbol)
                sectorMap[sector] = (sectorMap[sector] ?: 0.0) + currentVal

                StockAllocation(
                    symbol = stock.symbol,
                    value = currentVal,
                    percent = if (totalValue > 0) currentVal / totalValue else 0.0,
                    isProfit = stock.dailyChange >= 0,
                    changePercent = stock.dailyChange
                )
            }.sortedByDescending { it.value }

            val topGainer = portfolio.maxByOrNull { it.dailyChange }
            val topLoser = portfolio.minByOrNull { it.dailyChange }
            val profitableCount = portfolio.count { (it.currentPrice - it.buyPrice) >= 0 }
            val winRate = if (portfolio.isNotEmpty()) (profitableCount.toDouble() / portfolio.size) * 100 else 0.0

            val historyCurve = generateSimulatedIntradayCurve(totalOpen, totalValue)

            _analyticsState.value = AnalyticsUiState.Success(
                totalValue = totalValue,
                sectors = sectorMap,
                allocations = allocationList,
                historyCurve = historyCurve,
                topGainer = topGainer,
                topLoser = topLoser,
                winRate = winRate,
                currencySymbol = if (isUsd) "$" else "₹"
            )
        }
    }

    fun loadStockNews(symbol: String) {
        viewModelScope.launch {
            _newsState.value = NewsUiState.Loading
            try {
                // Fetch from your Hugging Face Brain
                val news = marketRepository.getNews(symbol)
                _newsState.value = NewsUiState.Success(news)
            } catch (e: Exception) {
                _newsState.value = NewsUiState.Error("Failed to load news")
            }
        }
    }

    // --- NOTIFICATIONS LOGIC ---
    // Initialize state from Prefs
    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean("notifications_enabled", true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    fun toggleNotifications() {
        val newState = !_notificationsEnabled.value
        _notificationsEnabled.value = newState

        // Save to Prefs so the Background Worker can see it
        prefs.edit().putBoolean("notifications_enabled", newState).apply()

        // Optional: Show a UI feedback message
        if (newState) {
            _uiMessage.value = "Price Alerts Enabled"
        } else {
            _uiMessage.value = "Price Alerts Paused"
        }
    }

    // --- UPDATED SECTOR LOGIC (Includes US Stocks) ---
    private fun identifySector(symbol: String): String {
        val s = symbol.uppercase()
        return when {
            // Tech (US + India)
            s.contains("TCS") || s.contains("INFY") || s.contains("GOOG") ||
                    s.contains("MSFT") || s.contains("NVDA") || s.contains("AAPL") ||
                    s.contains("AMD") || s.contains("TECHM") || s.contains("HCLTECH") -> "Technology"

            // Finance (US + India)
            s.contains("HDFC") || s.contains("SBIN") || s.contains("JPM") ||
                    s.contains("BAC") || s.contains("ICICI") || s.contains("AXIS") ||
                    s.contains("BAJFINANCE") -> "Finance"

            // Auto (US + India)
            s.contains("TATAMOTORS") || s.contains("TSLA") || s.contains("MARUTI") ||
                    s.contains("M&M") || s.contains("HEROMOTO") -> "Auto"

            // Energy
            s.contains("RELIANCE") || s.contains("ONGC") || s.contains("XOM") ||
                    s.contains("NTPC") || s.contains("POWERGRID") || s.contains("BPCL") -> "Energy"

            // FMCG / Consumer
            s.contains("ITC") || s.contains("HUL") || s.contains("KO") ||
                    s.contains("TITAN") || s.contains("ASIANPAINT") -> "FMCG/Consumer"

            // Pharma
            s.contains("SUNPHARMA") || s.contains("CIPLA") || s.contains("DRREDDY") ||
                    s.contains("DIVISLAB") -> "Healthcare"

            else -> "Other"
        }
    }

    private fun generateSimulatedIntradayCurve(start: Double, end: Double): List<Pair<String, Double>> {
        val points = mutableListOf<Pair<String, Double>>()
        val steps = 50
        val range = end - start
        val random = java.util.Random()

        var currentHour = 9
        var currentMinute = 15

        for (i in 0 until steps) {
            val noise = (random.nextDouble() - 0.5) * (range * 0.1)
            val progress = i.toDouble() / steps
            val trend = start + (range * progress)
            val price = trend + noise

            val timeLabel = String.format(Locale.US, "%02d:%02d", currentHour, currentMinute)
            points.add(Pair(timeLabel, price))

            currentMinute += 8
            if (currentMinute >= 60) {
                currentHour += 1
                currentMinute -= 60
            }
        }
        points.add(Pair("Now", end))
        return points
    }
    fun loadCurrentPrice(symbol: String) {
        // 1. Safety Check: Only update if we are already displaying data (Success State)
        val currentState = _stockDetailState.value
        if (currentState !is StockDetailState.Success) return

        viewModelScope.launch {
            try {
                // 2. Determine Market Type (As requested)
                val isIndian = symbol.uppercase().endsWith(".NS") ||
                        symbol.uppercase().endsWith(".BO") ||
                        symbol.uppercase() == "^NSEI" ||
                        symbol.uppercase() == "^BSESN"

                // 3. Fetch Data based on Market Type
                // Note: In a real architecture, the Repository handles the endpoint logic.
                // Here we call the repository to get the freshest data.
                val result = if (isIndian) {
                    // Logic for Indian Stocks (e.g., specific specific API or Yahoo Finance)
                    repository.getFullStockDetails(symbol, "1D")
                } else {
                    // Logic for US/Global Stocks
                    repository.getFullStockDetails(symbol, "1D")
                }

                // 4. Seamless UI Update
                result.onSuccess { freshData ->
                    // We copy the OLD data (Description, Chart, etc.)
                    // and ONLY replace the Price/Change fields.
                    // This prevents the Chart from "re-drawing" or flickering.
                    val updatedData = currentState.data.copy(
                        price = freshData.price,
                        change = freshData.change,
                        changePercent = freshData.changePercent,
                        dayHigh = freshData.dayHigh,
                        dayLow = freshData.dayLow,
                        prevClose = freshData.prevClose,
                        // Keep the old chart points so the user interaction isn't reset
                        historyPoints = currentState.data.historyPoints
                    )

                    _stockDetailState.value = StockDetailState.Success(updatedData)

                    // Optional: If this stock is in the portfolio, update the portfolio list too
                    updatePortfolioPrice(symbol, freshData.price, freshData.change)
                }
            } catch (e: Exception) {
                // Silent failure is okay for background auto-refresh
            }
        }
    }
    private fun updatePortfolioPrice(symbol: String, newPrice: Double, newChange: Double) {
        val currentPortfolio = _uiState.value.portfolio
        if (currentPortfolio.any { it.symbol == symbol }) {
            val updatedList = currentPortfolio.map { stock ->
                if (stock.symbol == symbol) {
                    stock.copy(currentPrice = newPrice, dailyChange = newChange)
                } else {
                    stock
                }
            }
            _uiState.update { it.copy(portfolio = updatedList) }
        }
    }

    companion object {
        fun Factory(
            repository: PortfolioRepository,
            marketRepository: MarketRepository,
            ideaGenerator: GeminiIdeaGenerator,
            userIdFlow: StateFlow<String?>,
            prefs: SharedPreferences
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PortfolioViewModel(repository, marketRepository, ideaGenerator, userIdFlow, prefs) as T
            }
        }
    }
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
}