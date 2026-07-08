package com.apexinvest.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexinvest.app.api.models.DeepAnalysisResponse
import com.apexinvest.app.api.models.PortfolioSummary
import com.apexinvest.app.data.PortfolioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// --- UI STATES (STALE-WHILE-REVALIDATE) ---

sealed class AnalysisState {
    object Idle : AnalysisState()
    data class Loading(val message: String, val data: DeepAnalysisResponse? = null) : AnalysisState()
    data class Success(val data: DeepAnalysisResponse) : AnalysisState()
    data class Error(val message: String, val data: DeepAnalysisResponse? = null) : AnalysisState()
}

sealed class PortfolioHealthState {
    object Idle : PortfolioHealthState()
    data class Loading(val message: String, val summary: PortfolioSummary? = null) : PortfolioHealthState()
    data class Success(val summary: PortfolioSummary) : PortfolioHealthState()
    data class Error(val message: String, val summary: PortfolioSummary? = null) : PortfolioHealthState()
}

// --- VIEW MODEL ---

class PredictionViewModel(
    private val repository: PortfolioRepository
) : ViewModel() {

    private val _analysisState = MutableStateFlow<AnalysisState>(AnalysisState.Idle)
    val analysisState: StateFlow<AnalysisState> = _analysisState.asStateFlow()

    private val _portfolioHealthState = MutableStateFlow<PortfolioHealthState>(PortfolioHealthState.Idle)
    val portfolioHealthState: StateFlow<PortfolioHealthState> = _portfolioHealthState.asStateFlow()

    // Remembers last queried symbol for auto-retry
    private var lastAnalyzedSymbol: String? = null

    // --- FUNCTION 1: Analyze Single Stock ---
    fun analyzeStock(symbol: String, forceRefresh: Boolean = false) {
        lastAnalyzedSymbol = symbol
        val currentState = _analysisState.value

        // Prevent duplicate requests while loading
        if (currentState is AnalysisState.Loading) return

        // Skip network call if data is already present and fresh
        if (!forceRefresh && currentState is AnalysisState.Success && currentState.data.symbol == symbol.uppercase().trim()) {
            return
        }

        val cleanSymbol = symbol.uppercase().trim()
        val existingData = when (currentState) {
            is AnalysisState.Success -> currentState.data
            is AnalysisState.Error -> currentState.data
            else -> null
        }

        _analysisState.value = AnalysisState.Loading("Starting analysis...", existingData)

        viewModelScope.launch(Dispatchers.IO) {
            repository.getDeepAnalysisFlow(cleanSymbol, forceRefresh).collect { (status, data) ->
                when (status) {
                    "COMPLETED" -> {
                        if (data != null) {
                            _analysisState.value = AnalysisState.Success(data)
                        } else {
                            _analysisState.value = AnalysisState.Error("Failed to parse data.", existingData)
                        }
                    }
                    else -> {
                        if (status.startsWith("Error", ignoreCase = true) || status.startsWith("Connection Error", ignoreCase = true)) {
                            // Make exceptions human-readable
                            val cleanMsg = sanitizeErrorMessage(status)
                            _analysisState.value = AnalysisState.Error(cleanMsg, data ?: existingData)
                        } else {
                            _analysisState.value = AnalysisState.Loading(status, data ?: existingData)
                        }
                    }
                }
            }
        }
    }

    // --- FUNCTION 2: Scan Full Portfolio ---
    fun scanPortfolio(forceRefresh: Boolean = false) {
        val currentState = _portfolioHealthState.value
        if (currentState is PortfolioHealthState.Loading) return
        if (!forceRefresh && currentState is PortfolioHealthState.Success) return

        val existingSummary = when (currentState) {
            is PortfolioHealthState.Success -> currentState.summary
            is PortfolioHealthState.Error -> currentState.summary
            else -> null
        }

        _portfolioHealthState.value = PortfolioHealthState.Loading("Gathering Portfolio Data...", existingSummary)

        viewModelScope.launch(Dispatchers.IO) {
            repository.getPortfolioAnalysisFlow(forceRefresh).collect { (status, summary) ->
                when (status) {
                    "COMPLETED" -> {
                        if (summary != null) {
                            _portfolioHealthState.value = PortfolioHealthState.Success(summary)
                        } else {
                            _portfolioHealthState.value = PortfolioHealthState.Error("Failed to parse portfolio data.", existingSummary)
                        }
                    }
                    "EMPTY_PORTFOLIO" -> {
                        // Treat an empty tracking list as an explicit clean Error/Idle state
                        _portfolioHealthState.value = PortfolioHealthState.Error("Your portfolio is empty. Add stocks to view predictions.", null)
                    }
                    else -> {
                        if (status.startsWith("Error", ignoreCase = true)) {
                            // Make exceptions human-readable
                            val cleanMsg = sanitizeErrorMessage(status)
                            _portfolioHealthState.value = PortfolioHealthState.Error(cleanMsg, summary ?: existingSummary)
                        } else {
                            _portfolioHealthState.value = PortfolioHealthState.Loading(status, summary ?: existingSummary)
                        }
                    }
                }
            }
        }
    }

    // Sanitize technical errors into clean UI states
    private fun sanitizeErrorMessage(rawError: String): String {
        val lower = rawError.lowercase()
        return when {
            lower.contains("unable to resolve host") || lower.contains("unknownhost") || lower.contains("timeout") || lower.contains("network") ->
                "Currently offline. Waiting for connection..."
            lower.contains("backend failure") || lower.contains("500") || lower.contains("502") ->
                "Server is temporarily unavailable. Retrying..."
            else -> "Analysis interrupted. Tap to retry."
        }
    }

    fun setAnalysisData(data: DeepAnalysisResponse) {
        _analysisState.value = AnalysisState.Success(data)
    }

    fun clearAnalysisState() {
        _analysisState.value = AnalysisState.Idle
        lastAnalyzedSymbol = null
    }

    fun clearAllData() {
        _analysisState.value = AnalysisState.Idle
        _portfolioHealthState.value = PortfolioHealthState.Idle
        lastAnalyzedSymbol = null
    }
}