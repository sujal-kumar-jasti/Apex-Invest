package com.apexinvest.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.apexinvest.app.data.PortfolioRepository
import com.apexinvest.app.data.model.SearchResultDto
import com.apexinvest.app.data.model.TrendingStockDto
import com.apexinvest.app.data.repository.MarketRepository
import com.apexinvest.app.db.ExploreCacheEntity
import com.apexinvest.app.db.ExploreDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

// --- STATES ---
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
    val changePercent: String
)

class ExploreViewModel(
    private val marketRepository: MarketRepository,
    private val portfolioRepository: PortfolioRepository,
    private val exploreDao: ExploreDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExploreUiState>(ExploreUiState.Loading)
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private val _searchUiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val searchUiState: StateFlow<SearchUiState> = _searchUiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    private var searchJob: Job? = null

    init {
        // JOB 1: OBSERVE DATABASE (Source of Truth)
        viewModelScope.launch {
            exploreDao.getExploreData().collect { cache ->
                if (cache != null) {
                    _uiState.value = ExploreUiState.Success(
                        trendingStocks = cache.trendingStocks,
                        indices = cache.indices,
                        globalIndices = cache.globalIndices,
                        commodities = cache.commodities
                    )
                } else {
                    // If Cache is null/empty, ensure we aren't stuck in Success state with empty lists
                    if (_uiState.value is ExploreUiState.Success) {
                        _uiState.value = ExploreUiState.Loading
                    }
                }
            }
        }

        // JOB 2: START DYNAMIC AUTO-REFRESH
        startAutoRefresh()
    }

    private fun startAutoRefresh() {
        viewModelScope.launch {
            // Initial delay to let DB load
            delay(1000)

            while (isActive) {
                // We pass 'true' for isBackground, but the logic inside handles the empty state check
                fetchAndSaveMarketData()
                delay(1000) // 1 Second refresh
            }
        }
    }

    fun loadMarketData() {
        viewModelScope.launch {
            fetchAndSaveMarketData()
        }
    }

    private suspend fun fetchAndSaveMarketData() {
        // Move heavy work to IO thread to prevent UI stutter
        withContext(Dispatchers.IO) {
            try {
                val liveRate = try { portfolioRepository.getConversionRate() } catch (e: Exception) { 91.0 }
                val trending = marketRepository.getTrending()
                val rawData = marketRepository.getCommodities()

                val indicesList = mutableListOf<CommodityUiModel>()
                val globalList = mutableListOf<CommodityUiModel>()
                val commList = mutableListOf<CommodityUiModel>()

                rawData.forEach { item ->
                    var displayPrice = item.price
                    var displayPrefix = if (item.currency == "INR") "₹" else "$"

                    if (item.symbol == "GOLDBEES.NS") {
                        displayPrice = item.price * 1225; displayPrefix = "₹"
                    } else if (item.symbol == "SILVERBEES.NS") {
                        displayPrice = item.price * 1058; displayPrefix = "₹"
                    } else if (item.symbol == "USO") {
                        displayPrice = item.price * liveRate; displayPrefix = "₹"
                    } else if (item.currency == "USD" && item.type != "GLOBAL_INDEX") {
                        displayPrice = item.price * liveRate; displayPrefix = "₹"
                    }

                    val isPositive = item.changePercent >= 0
                    val sign = if (isPositive) "+" else ""

                    val uiModel = CommodityUiModel(
                        symbol = item.symbol,
                        name = item.name,
                        value = "$displayPrefix${String.format(Locale.US, "%.2f", displayPrice)}",
                        isPositive = isPositive,
                        changePercent = "$sign${String.format(Locale.US, "%.2f", item.changePercent)}%"
                    )

                    when (item.type) {
                        "COMMODITY" -> commList.add(uiModel)
                        "GLOBAL_INDEX" -> globalList.add(uiModel)
                        else -> indicesList.add(uiModel)
                    }
                }

                val entity = ExploreCacheEntity(
                    trendingStocks = trending,
                    indices = indicesList,
                    globalIndices = globalList,
                    commodities = commList
                )
                exploreDao.insertData(entity)

            } catch (e: Exception) {
                // --- CRITICAL FIX FOR BLANK SCREEN ---
                // If the update fails (Offline), check if we have data on screen.
                // 1. If we HAVE data (Success), we do NOTHING. Keep showing old data.
                // 2. If we have NO data (Loading/Error), we MUST show Error so user sees "Offline Retry".
                if (_uiState.value !is ExploreUiState.Success) {
                    _uiState.value = ExploreUiState.Error("Offline: Failed to update")
                }
            }
        }
    }

    // --- Search Logic ---
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        if (query.isBlank()) { _searchUiState.value = SearchUiState.Idle; return }

        searchJob = viewModelScope.launch {
            _searchUiState.value = SearchUiState.Loading
            delay(500)
            performSearch(query)
        }
    }

    private suspend fun performSearch(query: String) {
        try {
            val results = marketRepository.search(query)
            if (results.isEmpty()) _searchUiState.value = SearchUiState.Empty
            else _searchUiState.value = SearchUiState.Success(results)
        } catch (e: Exception) {
            if (_searchQuery.value.isNotEmpty()) {
                _searchUiState.value = SearchUiState.Error("Search failed")
            }
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchUiState.value = SearchUiState.Idle
    }

    class Factory(
        private val marketRepository: MarketRepository,
        private val portfolioRepository: PortfolioRepository,
        private val exploreDao: ExploreDao
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ExploreViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ExploreViewModel(marketRepository, portfolioRepository, exploreDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}