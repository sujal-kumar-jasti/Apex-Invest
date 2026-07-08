package com.apexinvest.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.apexinvest.app.api.AdvancedStockApiService
import com.apexinvest.app.api.ApexAuthApiService
import com.apexinvest.app.api.CurrencyApiService
import com.apexinvest.app.api.GlobalStockApiService
import com.apexinvest.app.api.IdeasApi
import com.apexinvest.app.api.PredictionApiService
import com.apexinvest.app.api.StockApiService
import com.apexinvest.app.api.TradingViewStockApiService
import com.apexinvest.app.api.YahooFinanceApiService
import com.apexinvest.app.api.util.YahooAuthInterceptor
import com.apexinvest.app.api.util.YahooAuthManager
import com.apexinvest.app.data.remote.ApexInvestApiService
import com.apexinvest.app.data.remote.TradingViewWebSocketClient
import com.apexinvest.app.data.repository.AuthRepository
import com.apexinvest.app.data.repository.MarketRepository
import com.apexinvest.app.data.repository.NotificationRepository
import com.apexinvest.app.data.repository.StockDetailsRepository
import com.apexinvest.app.db.AppDatabase
import com.apexinvest.app.service.FinancialNotificationService
import com.apexinvest.app.utils.SessionManager
import com.apexinvest.app.viewmodel.ExploreViewModel
import com.apexinvest.app.viewmodel.PortfolioViewModel
import com.apexinvest.app.viewmodel.PredictionViewModel
import com.apexinvest.app.viewmodel.StockDetailViewModel
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {

    private val database by lazy { AppDatabase.getDatabase(context) }
    private val prefs: SharedPreferences by lazy { context.getSharedPreferences("apex_invest_prefs", Context.MODE_PRIVATE) }
    val sessionManager by lazy { SessionManager(context) }

    private val sharedOkHttpClient by lazy {
        val dispatcher = Dispatcher().apply {
            maxRequests = 64
            maxRequestsPerHost = 15
        }

        OkHttpClient.Builder()
            .dispatcher(dispatcher)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val heavyOkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .build()
    }

    private val authOkHttpClient by lazy {
        sharedOkHttpClient.newBuilder()
            .addInterceptor { chain ->
                val original = chain.request()
                val token = sessionManager.fetchAuthToken()

                val requestBuilder = original.newBuilder()
                if (token != null) {
                    requestBuilder.header("Authorization", "Bearer $token")
                }
                chain.proceed(requestBuilder.build())
            }
            .build()
    }

    // Yahoo client
    private val yahooOkHttpClient by lazy {
        sharedOkHttpClient.newBuilder()
            .cookieJar(YahooAuthManager.getCookieJar())
            .addInterceptor(YahooAuthInterceptor())
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    // API Initialization

    private val renderRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://apexinvest-api-5ql5.onrender.com/")
            .client(authOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val authApiService: ApexAuthApiService by lazy { renderRetrofit.create(ApexAuthApiService::class.java) }

    // Yahoo Retrofit
    private val yahooRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://query1.finance.yahoo.com/")
            .client(yahooOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val yahooFinanceApiService: YahooFinanceApiService by lazy { yahooRetrofit.create(YahooFinanceApiService::class.java) }

    private val pythonRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://jsujalkumar7899-stock-api.hf.space/")
            .client(sharedOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val stockApiService: StockApiService by lazy { pythonRetrofit.create(StockApiService::class.java) }

    private val globalStockRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://sujal7899-stocks-api.hf.space/")
            .client(sharedOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val globalStockApiService: GlobalStockApiService by lazy { globalStockRetrofit.create(GlobalStockApiService::class.java) }

    private val predictionRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://swapna7899-prognosai-fastapi-backend-1.hf.space/")
            .client(heavyOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val predictionApiService: PredictionApiService by lazy { predictionRetrofit.create(PredictionApiService::class.java) }

    private val advancedRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://sujal7337-stock-details.hf.space/")
            .client(sharedOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val advancedStockApiService: AdvancedStockApiService by lazy { advancedRetrofit.create(AdvancedStockApiService::class.java) }

    private val tradingViewRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://swapna7899-stockdetails.hf.space/")
            .client(sharedOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val tradingViewStockApiService: TradingViewStockApiService by lazy { tradingViewRetrofit.create(TradingViewStockApiService::class.java) }

    private val apexInvestRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://sujal7899-prognos-data-engine.hf.space/")
            .client(sharedOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val apexInvestApiService: ApexInvestApiService by lazy { apexInvestRetrofit.create(ApexInvestApiService::class.java) }

    private val ideasRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://sujal8310-apex-invest-ideas-generator.hf.space/")
            .client(sharedOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val ideasApi: IdeasApi by lazy { ideasRetrofit.create(IdeasApi::class.java) }

    private val currencyRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.exchangerate-api.com/v4/")
            .client(sharedOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val currencyService: CurrencyApiService by lazy { currencyRetrofit.create(CurrencyApiService::class.java) }

    val tradingViewWebSocketClient by lazy { TradingViewWebSocketClient(sharedOkHttpClient) }

    // Repositories

    val authRepository by lazy {
        AuthRepository(
            apiService = authApiService,
            sessionManager = sessionManager
        )
    }

    val marketRepository by lazy { MarketRepository(apexInvestApiService) }

    val notificationRepository by lazy { NotificationRepository(database.notificationDao()) }
    val financialNotificationService by lazy { FinancialNotificationService(context, notificationRepository) }
    val appDatabase by lazy { database }

    val stockDetailsRepository by lazy {
        StockDetailsRepository(
            staticDao = database.stockStaticDao(),
            analysisCacheDao = database.analysisCacheDao(),
            stockCacheDao = database.stockCacheDao(),
            yahooFinanceApiService = yahooFinanceApiService,
            advancedApiService = advancedStockApiService,
            globalApiService = globalStockApiService,
            apexInvestApiService = apexInvestApiService,
            stockApiService = stockApiService
        )
    }

    val portfolioRepository by lazy {
        PortfolioRepository(
            portfolioDao = database.portfolioDao(),
            watchlistDao = database.watchlistDao(),
            transactionDao = database.transactionDao(),
            stockCacheDao = database.stockCacheDao(),
            analysisCacheDao = database.analysisCacheDao(),
            notificationDao = database.notificationDao(),
            sessionManager = sessionManager,
            authApiService = authApiService,
            yahooFinanceApiService = yahooFinanceApiService, // Replaced liveStockApiService
            currencyApiService = currencyService,
            predictionApiService = predictionApiService,
            ideasApi = ideasApi,
            tradingViewApiService = tradingViewStockApiService,
            apexInvestApiService = apexInvestApiService
        )
    }

    // ViewModels

    val exploreViewModelFactory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return ExploreViewModel(
                marketRepository = marketRepository,
                portfolioRepository = portfolioRepository,
                exploreDao = database.exploreDao()
            ) as T
        }
    }

    val portfolioViewModelFactory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PortfolioViewModel(
                repository = portfolioRepository,
                notificationRepository = notificationRepository,
                sessionManager = sessionManager,
                prefs = prefs
            ) as T
        }
    }

    val predictionViewModelFactory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PredictionViewModel::class.java)) {
                return PredictionViewModel(portfolioRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    val stockDetailViewModelFactory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StockDetailViewModel::class.java)) {
                return StockDetailViewModel(stockDetailsRepository, portfolioRepository, sessionManager, tradingViewWebSocketClient) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}