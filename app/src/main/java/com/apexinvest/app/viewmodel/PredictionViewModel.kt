package com.apexinvest.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
<<<<<<< HEAD
import com.apexinvest.app.api.models.DeepAnalysisResponse
import com.apexinvest.app.api.models.PortfolioSummary
import com.apexinvest.app.data.PortfolioRepository
import kotlinx.coroutines.Dispatchers
=======
import com.apexinvest.app.api.PredictionRetrofitClient
import com.apexinvest.app.data.PortfolioAnalysisRequest
import com.apexinvest.app.data.PortfolioSummary
import com.apexinvest.app.data.StockAnalysisRequest
import com.apexinvest.app.data.StockAnalysisResponse
import com.google.firebase.auth.FirebaseAuth
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
<<<<<<< HEAD

// --- UI STATES (STALE-WHILE-REVALIDATE) ---

sealed class AnalysisState {
    object Idle : AnalysisState()
    data class Loading(val message: String, val data: DeepAnalysisResponse? = null) : AnalysisState()
    data class Success(val data: DeepAnalysisResponse) : AnalysisState()
    data class Error(val message: String, val data: DeepAnalysisResponse? = null) : AnalysisState()
=======
import kotlinx.coroutines.tasks.await

// --- UI STATES ---

sealed class AnalysisState {
    object Idle : AnalysisState()
    object Loading : AnalysisState()
    data class Success(val data: StockAnalysisResponse) : AnalysisState()
    data class Error(val message: String) : AnalysisState()
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
}

sealed class PortfolioHealthState {
    object Idle : PortfolioHealthState()
<<<<<<< HEAD
    data class Loading(val message: String, val summary: PortfolioSummary? = null) : PortfolioHealthState()
    data class Success(val summary: PortfolioSummary) : PortfolioHealthState()
    data class Error(val message: String, val summary: PortfolioSummary? = null) : PortfolioHealthState()
=======
    object Loading : PortfolioHealthState()
    data class Success(val summary: PortfolioSummary) : PortfolioHealthState()
    data class Error(val message: String) : PortfolioHealthState()
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
}

// --- VIEW MODEL ---

<<<<<<< HEAD
class PredictionViewModel(
    private val repository: PortfolioRepository
) : ViewModel() {

    private val _analysisState = MutableStateFlow<AnalysisState>(AnalysisState.Idle)
    val analysisState: StateFlow<AnalysisState> = _analysisState.asStateFlow()

    private val _portfolioHealthState = MutableStateFlow<PortfolioHealthState>(PortfolioHealthState.Idle)
    val portfolioHealthState: StateFlow<PortfolioHealthState> = _portfolioHealthState.asStateFlow()

    // 🚀 MEMORY: Remembers the last queried symbol so we can auto-retry when internet returns
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
            is AnalysisState.Loading -> currentState.data
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
                            // 🚀 THE FIX: Intercept raw Java exceptions and make them human-readable
                            val cleanMsg = sanitizeErrorMessage(status)
                            _analysisState.value = AnalysisState.Error(cleanMsg, data ?: existingData)
                        } else {
                            _analysisState.value = AnalysisState.Loading(status, data ?: existingData)
                        }
                    }
                }
=======
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
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
            }
        }
    }

<<<<<<< HEAD
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
                            // 🚀 THE FIX: Intercept raw Java exceptions and make them human-readable
                            val cleanMsg = sanitizeErrorMessage(status)
                            _portfolioHealthState.value = PortfolioHealthState.Error(cleanMsg, summary ?: existingSummary)
                        } else {
                            _portfolioHealthState.value = PortfolioHealthState.Loading(status, summary ?: existingSummary)
                        }
                    }
                }
=======
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
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
            }
        }
    }

<<<<<<< HEAD
    // 🚀 THE FIX: Sanitizes ugly technical errors into clean UI states
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

    // 🚀 AUTO-HEAL HOOKS: Call these from your UI when 'isConnected' turns true
    fun autoHealPortfolioScan() {
        val state = _portfolioHealthState.value
        if (state is PortfolioHealthState.Error) {
            // Do not retry if the error is just an empty portfolio warning
            if (!state.message.contains("empty", ignoreCase = true)) {
                scanPortfolio(forceRefresh = true)
            }
        }
    }

    fun autoHealStockAnalysis() {
        if (_analysisState.value is AnalysisState.Error) {
            lastAnalyzedSymbol?.let { analyzeStock(it, forceRefresh = true) }
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
=======
    // Call this ONLY if you explicitly want to reset (e.g., Pull-to-Refresh)
    fun clearCache() {
        lastAnalyzedSearchSymbol = null
        lastAnalyzedSymbols = null
        _analysisState.value = AnalysisState.Idle
        _portfolioHealthState.value = PortfolioHealthState.Idle
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
    }
}