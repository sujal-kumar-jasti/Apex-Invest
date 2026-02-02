package com.apexinvest.app.viewmodel

import android.content.SharedPreferences
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

data class AppUiState(
    val portfolio: List<StockEntity> = emptyList(),
    val watchlist: List<WatchlistEntity> = emptyList(),
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
sealed class AnalyticsUiState {
    object Loading : AnalyticsUiState()
    object Empty : AnalyticsUiState()
    data class Success(
        val totalValue: Double,
        val sectors: Map<String, Double>,
        val allocations: List<StockAllocation>,
        val historyCurve: List<Pair<String, Double>>,
        val topGainer: StockEntity?,
        val topLoser: StockEntity?,
        val winRate: Double,
        val currencySymbol: String
    ) : AnalyticsUiState()
}

data class StockAllocation(
    val symbol: String,
    val value: Double,
    val percent: Double,
    val isProfit: Boolean,
    val changePercent: Double
)

// --- AI INTELLIGENCE STATES (Phase 4) ---
sealed class AiUiState {
    object Idle : AiUiState()
    object Thinking : AiUiState()
    data class Success(val insights: List<AiInsight>, val suggestions: List<StockSuggestion>) : AiUiState()
    data class Error(val message: String) : AiUiState()
}

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
            }
        }
    }

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
            }
        }
    }

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
        } else {
            AiUiState.Success(insights, suggestions)
        }
    }

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
}