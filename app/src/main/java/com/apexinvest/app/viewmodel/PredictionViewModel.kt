package com.apexinvest.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexinvest.app.api.PredictionRetrofitClient
import com.apexinvest.app.data.PortfolioAnalysisRequest
import com.apexinvest.app.data.PortfolioSummary
import com.apexinvest.app.data.StockAnalysisRequest
import com.apexinvest.app.data.StockAnalysisResponse
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// --- UI STATES ---

sealed class AnalysisState {
    object Idle : AnalysisState()
    object Loading : AnalysisState()
    data class Success(val data: StockAnalysisResponse) : AnalysisState()
    data class Error(val message: String) : AnalysisState()
}

sealed class PortfolioHealthState {
    object Idle : PortfolioHealthState()
    object Loading : PortfolioHealthState()
    data class Success(val summary: PortfolioSummary) : PortfolioHealthState()
    data class Error(val message: String) : PortfolioHealthState()
}

// --- VIEW MODEL ---

class PredictionViewModel : ViewModel() {

    // Access the API Service via your specific Client Object
    private val apiService = PredictionRetrofitClient.predictionApiService
    private val auth = FirebaseAuth.getInstance()

    // 1. Deep Dive State (Single Stock Analysis)
    private val _analysisState = MutableStateFlow<AnalysisState>(AnalysisState.Idle)
    val analysisState: StateFlow<AnalysisState> = _analysisState.asStateFlow()

    // 2. Portfolio Health State (Batch Analysis)
    private val _portfolioHealthState = MutableStateFlow<PortfolioHealthState>(PortfolioHealthState.Idle)
    val portfolioHealthState: StateFlow<PortfolioHealthState> = _portfolioHealthState.asStateFlow()

    // --- CACHE VARIABLES (Session Persistence) ---
    private var lastAnalyzedSearchSymbol: String? = null
    private var lastAnalyzedSymbols: List<String>? = null

    // --- FUNCTION 1: Analyze Single Stock (Deep Dive) ---
    fun analyzeStock(symbol: String, days: Int = 7) {
        val cleanSymbol = symbol.uppercase()

        // CACHE CHECK: If already showing success for this symbol, do NOT reload.
        if (_analysisState.value is AnalysisState.Success && lastAnalyzedSearchSymbol == cleanSymbol) {
            return
        }

        viewModelScope.launch {
            _analysisState.value = AnalysisState.Loading
            try {
                // 1. Get Security Token
                val user = auth.currentUser
                if (user == null) {
                    _analysisState.value = AnalysisState.Error("User not logged in.")
                    return@launch
                }

                // Force refresh token to ensure it's valid
                val tokenResult = user.getIdToken(true).await()
                val token = tokenResult.token ?: ""

                // 2. Call API
                val request = StockAnalysisRequest(cleanSymbol, days)
                val response = apiService.analyzeStock(token, request)

                // 3. Update Cache & State
                lastAnalyzedSearchSymbol = cleanSymbol
                _analysisState.value = AnalysisState.Success(response)

            } catch (e: Exception) {
                e.printStackTrace()
                val errorMsg = e.localizedMessage ?: "Unknown connection error"
                _analysisState.value = AnalysisState.Error("AI Analysis Failed: $errorMsg")
            }
        }
    }

    // --- FUNCTION 2: Scan Entire Portfolio (Health Check) ---
    fun scanPortfolio(symbols: List<String>) {
        if (symbols.isEmpty()) {
            _portfolioHealthState.value = PortfolioHealthState.Error("Portfolio is empty. Add stocks to analyze.")
            return
        }

        // CACHE CHECK: If already showing success for this EXACT list of stocks, do NOT reload.
        if (_portfolioHealthState.value is PortfolioHealthState.Success && lastAnalyzedSymbols == symbols) {
            return
        }

        viewModelScope.launch {
            _portfolioHealthState.value = PortfolioHealthState.Loading
            try {
                // 1. Get Security Token
                val user = auth.currentUser
                if (user == null) {
                    _portfolioHealthState.value = PortfolioHealthState.Error("User not logged in.")
                    return@launch
                }
                val tokenResult = user.getIdToken(true).await()
                val token = tokenResult.token ?: ""

                // 2. Call API
                val request = PortfolioAnalysisRequest(symbols)
                val response = apiService.analyzePortfolio(token, request)

                // 3. Update Cache & State
                lastAnalyzedSymbols = symbols
                _portfolioHealthState.value = PortfolioHealthState.Success(response)

            } catch (e: Exception) {
                e.printStackTrace()
                val errorMsg = e.localizedMessage ?: "Unknown connection error"
                _portfolioHealthState.value = PortfolioHealthState.Error("Health Check Failed: $errorMsg")
            }
        }
    }

    // Call this ONLY if you explicitly want to reset (e.g., Pull-to-Refresh)
    fun clearCache() {
        lastAnalyzedSearchSymbol = null
        lastAnalyzedSymbols = null
        _analysisState.value = AnalysisState.Idle
        _portfolioHealthState.value = PortfolioHealthState.Idle
    }
}