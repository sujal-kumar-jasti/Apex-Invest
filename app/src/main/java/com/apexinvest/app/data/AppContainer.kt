package com.apexinvest.app.data

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.apexinvest.app.BuildConfig
import com.apexinvest.app.api.CurrencyApiService
import com.apexinvest.app.api.GlobalStockApiService
import com.apexinvest.app.api.StockApiService
import com.apexinvest.app.data.remote.PrognosApiService
import com.apexinvest.app.data.repository.MarketRepository
import com.apexinvest.app.db.AppDatabase
import com.apexinvest.app.viewmodel.ExploreViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(context: Context) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // 1. DATABASE INSTANCE
    private val database = AppDatabase.getDatabase(context)

    // Gemini AI
    private val geminiApiKey = BuildConfig.GEMINI_API_KEY
    val ideaGenerator = GeminiIdeaGenerator(geminiApiKey)

    // --- RETROFIT INSTANCES ---

    // Indian Stock API (Python Backend)
    private val pythonRetrofit = Retrofit.Builder()
        .baseUrl("https://jsujalkumar7899-stock-api.hf.space/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    // This is needed for ExploreViewModel
    private val stockApiService = pythonRetrofit.create(StockApiService::class.java)

    // Currency Converter
    private val currencyRetrofit = Retrofit.Builder()
        .baseUrl("https://api.exchangerate-api.com/v4/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val currencyService = currencyRetrofit.create(CurrencyApiService::class.java)

    // Prognos Data Engine
    private val prognosRetrofit = Retrofit.Builder()
        .baseUrl("https://sujal7899-prognos-data-engine.hf.space/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val prognosApiService = prognosRetrofit.create(PrognosApiService::class.java)

    // Global Stock API
    private val globalStockRetrofit = Retrofit.Builder()
        .baseUrl("https://sujal7899-stocks-api.hf.space/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val globalStockApiService = globalStockRetrofit.create(GlobalStockApiService::class.java)


    // --- REPOSITORIES ---

    val marketRepository = MarketRepository(prognosApiService)

    val portfolioRepository = PortfolioRepository(
        portfolioDao = database.portfolioDao(),
        watchlistDao = database.watchlistDao(),
        transactionDao = database.transactionDao(),
        firestore = firestore,
        auth = auth,
        stockApiService = stockApiService,
        globalApiService = globalStockApiService,
        currencyApiService = currencyService
    )

    // In AppContainer.kt
    val exploreViewModelFactory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return ExploreViewModel(
                marketRepository = marketRepository,
                portfolioRepository = portfolioRepository,
                exploreDao = database.exploreDao() // <--- PASS THE DAO
            ) as T
        }
    }


}