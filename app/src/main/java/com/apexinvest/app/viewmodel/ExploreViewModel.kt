package com.apexinvest.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexinvest.app.data.PortfolioRepository
import com.apexinvest.app.data.model.CommodityDto
import com.apexinvest.app.data.model.SearchResultDto
import com.apexinvest.app.data.model.TrendingStockDto
import com.apexinvest.app.data.repository.MarketRepository
import com.apexinvest.app.db.ExploreCacheEntity
import com.apexinvest.app.db.ExploreDao
import com.apexinvest.app.util.getCurrencySymbol
import com.apexinvest.app.util.guessCurrencyFromSymbol
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

sealed class ExploreUiState {
    object Loading : ExploreUiState()
    data class Success(
        val trendingStocks: List<TrendingStockDto>,
        val indices: List<CommodityUiModel>,
        val globalIndices: List<CommodityUiModel>,
        val commodities: List<CommodityUiModel>
    ) : ExploreUiState()
    data class Error(val message: String) : ExploreUiState()
}

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val results: List<SearchResultDto>) : SearchUiState()
    object Empty : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

data class CommodityUiModel(
    val symbol: String,
    val name: String,
    val value: String,
    val isPositive: Boolean,
    val changePercent: String,
    val currency: String = "USD" // 🆕 Store native currency for navigation
)

class ExploreViewModel(
    private val marketRepository: MarketRepository,
    private val portfolioRepository: PortfolioRepository,
    private val exploreDao: ExploreDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExploreUiState>(ExploreUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _searchUiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val searchUiState = _searchUiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    private var searchJob: Job? = null

    private var lastFetchTime = 0L
    private var refreshJob: Job? = null

    init {
        viewModelScope.launch {
            exploreDao.getExploreData().collect { cache ->
                if (cache != null) {
                    val rate = cache.conversionRate
                    _uiState.value = ExploreUiState.Success(
                        trendingStocks = sanitizeTrending(cache.trendingStocks),
                        indices = processCommodities(cache.indices, rate),
                        globalIndices = processCommodities(cache.globalIndices, rate),
                        commodities = processCommodities(cache.commodities, rate)
                    )
                }
            }
        }
    }

    fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (isActive) {
                fetchAndSaveMarketData()
                delay(8000.milliseconds)
            }
        }
    }

    fun stopAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = null
    }

    fun loadMarketData() {
        val currentTime = System.currentTimeMillis()
        // 🚀 PREVENTS TAB-SWITCH LAG: 10-second cooldown so it doesn't freeze the UI thread
        if (currentTime - lastFetchTime < 10_000) return

        viewModelScope.launch { fetchAndSaveMarketData() }
    }

    private suspend fun fetchAndSaveMarketData() = withContext(Dispatchers.IO) {
        lastFetchTime = System.currentTimeMillis() // Update the cooldown timer

        try {
            val rate = portfolioRepository.getConversionRate()
            val trending = marketRepository.getTrending()
            val commodities = marketRepository.getCommodities()
            val global = marketRepository.getGlobalIndices()

            val indicesList = mutableListOf<CommodityDto>()
            val commoditiesList = mutableListOf<CommodityDto>()
            commodities.forEach { if (it.type == "COMMODITY") commoditiesList.add(it) else indicesList.add(it) }

            exploreDao.insertData(ExploreCacheEntity(
                indices = indicesList,
                trendingStocks = trending,
                commodities = commoditiesList,
                globalIndices = global,
                conversionRate = rate
            ))
        } catch (_: Exception) {}
    }

    private fun processCommodities(list: List<CommodityDto>, rate: Double): List<CommodityUiModel> {
        return list.map { item ->
            val symbol = item.symbol
            // 🛠️ FIX: Use the API's currency if available, fallback to robust guessing
            val nativeCurrency = item.currency ?: guessCurrencyFromSymbol(symbol)
            
            val price = item.price ?: 0.0
            val prefix = getCurrencySymbol(nativeCurrency)

            val isPos = (item.changePercent ?: 0.0) >= 0
            CommodityUiModel(
                symbol, item.name ?: symbol,
                "$prefix${String.format(Locale.US, "%.2f", price)}",
                isPos, "${if (isPos) "+" else ""}${String.format(Locale.US, "%.2f", item.changePercent ?: 0.0)}%",
                currency = nativeCurrency
            )
        }
    }

    private fun sanitizeTrending(list: List<TrendingStockDto>) = list.map {
        // 🛠️ FIX: Use API currency if available
        val nativeCurrency = it.currency ?: guessCurrencyFromSymbol(it.symbol)
        it.copy(currency = nativeCurrency)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query; searchJob?.cancel()
        if (query.isBlank()) { _searchUiState.value = SearchUiState.Idle; return }

        // UI Feedback: Set to loading immediately but debounce actual work
        _searchUiState.value = SearchUiState.Loading

        searchJob = viewModelScope.launch {
            delay(300.milliseconds) // Reduced from 500ms for snappier feel
            try {
                val res = marketRepository.search(query)
                _searchUiState.value = if (res.isEmpty()) SearchUiState.Empty else SearchUiState.Success(res)
            } catch (_: Exception) { _searchUiState.value = SearchUiState.Error("Search failed") }
        }
    }

    fun clearSearch() { _searchQuery.value = ""; _searchUiState.value = SearchUiState.Idle }

    fun clearAllData() {
        _searchQuery.value = ""
        _searchUiState.value = SearchUiState.Idle
    }
}