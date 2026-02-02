package com.apexinvest.app

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.apexinvest.app.data.AppContainer
import com.apexinvest.app.ui.components.OfflineBanner
import com.apexinvest.app.ui.navigation.AppNavigation
import com.apexinvest.app.ui.navigation.Screen
import com.apexinvest.app.ui.theme.ApexInvestTheme
import com.apexinvest.app.util.NetworkObserver
import com.apexinvest.app.viewmodel.AuthState
import com.apexinvest.app.viewmodel.AuthViewModel
import com.apexinvest.app.viewmodel.ExploreViewModel
import com.apexinvest.app.viewmodel.PortfolioViewModel
import com.apexinvest.app.viewmodel.PredictionViewModel
import com.apexinvest.app.worker.PriceAlertWorker
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val appContainer: AppContainer by lazy { (application as ApexApplication).container }
    private val webclientid = "680800719517-l7sgs82gi01uinvlvpf02n9tqfi57mcf.apps.googleusercontent.com"
    private lateinit var prefs: SharedPreferences

    // --- ACTIVITY SCOPED VIEWMODELS ---
    private lateinit var authViewModel: AuthViewModel
    private lateinit var exploreViewModel: ExploreViewModel
    private lateinit var portfolioViewModel: PortfolioViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Install System Splash (Android 12+)
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)

        prefs = getSharedPreferences("apex_auth", MODE_PRIVATE)
        val shouldOpenWatchlist = intent.getBooleanExtra("OPEN_WATCHLIST", false)

        handleDeepLink(intent)
        setupWorkers()

        // --- 2. INITIALIZE VIEWMODELS ---
        val factory = viewModelFactory
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]
        exploreViewModel = ViewModelProvider(this, factory)[ExploreViewModel::class.java]
        portfolioViewModel = ViewModelProvider(this, factory)[PortfolioViewModel::class.java]

        // --- 3. START DATA LOADING (Background) ---
        // A. Load Market Data (Public) - Now hits Cache first!
        exploreViewModel.loadMarketData()

        // B. Load User Data (If Logged In)
        val auth = FirebaseAuth.getInstance()
        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                portfolioViewModel.loadPortfolioAndPrices()
            }
        }
        auth.addAuthStateListener(authListener)

        setContent {
            ApexInvestTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    PrognosAIApp(
                        viewModelFactory = factory,
                        webClientId = webclientid,
                        prefs = prefs,
                        openWatchlistOnStart = shouldOpenWatchlist,
                        authViewModel = authViewModel,
                        exploreViewModel = exploreViewModel,
                        portfolioViewModel = portfolioViewModel,
                        isConnected = true
                    )
                }
            }
        }
        val networkObserver = NetworkObserver(applicationContext)

        setContent {
            ApexInvestTheme {
                // 1. Observe Network
                val isConnected by networkObserver.observe().collectAsState(initial = true)

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {

                        // --- OFFLINE BANNER (Fixed) ---
                        // 1. Animated Container
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize()
                        ) {
                            if (!isConnected) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .statusBarsPadding()
                                ) {
                                    // 3. The Actual Red Banner
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFD32F2F))
                                    ) {
                                        OfflineBanner(isConnected = false)
                                    }
                                }
                            }
                        }

                        // --- MAIN APP CONTENT ---
                        Box(modifier = Modifier.fillMaxSize()) {
                            PrognosAIApp(
                                viewModelFactory = factory,
                                webClientId = webclientid,
                                prefs = prefs,
                                openWatchlistOnStart = shouldOpenWatchlist,
                                authViewModel = authViewModel,
                                exploreViewModel = exploreViewModel,
                                portfolioViewModel = portfolioViewModel,
                                isConnected=isConnected
                            )
                        }
                    }
                }
            }
        }
    }

    private fun setupWorkers() {
        val immediateRequest = OneTimeWorkRequestBuilder<PriceAlertWorker>().build()
        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            "StockPriceCheck_Immediate", ExistingWorkPolicy.REPLACE, immediateRequest
        )

        val bgRequest = PeriodicWorkRequestBuilder<PriceAlertWorker>(4, TimeUnit.HOURS)
            .setInitialDelay(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "StockPriceMonitor", ExistingPeriodicWorkPolicy.KEEP, bgRequest
        )
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val data = intent?.data
        if (data != null && data.scheme == "apexinvest" && data.host == "demat_connect") {
            Toast.makeText(this, "Demat Account Linked Successfully!", Toast.LENGTH_LONG).show()
        }
    }

    // --- VIEWMODEL FACTORY ---
    private val viewModelFactory by lazy {
        // Shared instance for Auth (scoped to Activity via this factory)
        val authVm = AuthViewModel()

        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(AuthViewModel::class.java) -> authVm as T

                    modelClass.isAssignableFrom(PortfolioViewModel::class.java) ->
                        PortfolioViewModel.Factory(
                            appContainer.portfolioRepository,
                            appContainer.marketRepository,
                            appContainer.ideaGenerator,
                            authVm.userIdFlow,
                            prefs
                        ).create(modelClass)

                    // FIX: Use AppContainer's factory to inject Database/DAO automatically
                    modelClass.isAssignableFrom(ExploreViewModel::class.java) ->
                        appContainer.exploreViewModelFactory.create(modelClass, CreationExtras.Empty)

                    modelClass.isAssignableFrom(PredictionViewModel::class.java) ->
                        PredictionViewModel() as T

                    else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }
    }
}

@Composable
fun PrognosAIApp(
    viewModelFactory: ViewModelProvider.Factory,
    webClientId: String,
    prefs: SharedPreferences,
    openWatchlistOnStart: Boolean,
    authViewModel: AuthViewModel,
    exploreViewModel: ExploreViewModel,
    portfolioViewModel: PortfolioViewModel,
    isConnected: Boolean
) {
    val navController = rememberNavController()
    val activity = LocalContext.current as ComponentActivity
    val context = LocalContext.current
    val currentThemeColors = MaterialTheme.colorScheme

    val authState by authViewModel.authState.collectAsState()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { }
    )

    LaunchedEffect(currentThemeColors.surface) {
        activity.window.decorView.setBackgroundColor(currentThemeColors.surface.toArgb())
    }

    // --- NAVIGATION LOGIC ---
    LaunchedEffect(authState) {
        // 1. WAIT for Custom Splash Animation
        delay(2000)

        // 2. CHECK AUTH & NAVIGATE
        when (authState) {
            is AuthState.LoggedIn -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                prefs.edit { putBoolean("phone_auth_in_progress", false) }

                navController.navigate("main_pager") {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }

                if (openWatchlistOnStart) portfolioViewModel.showAddWatchlistDialog.value = true
            }
            is AuthState.LoggedOut -> {
                prefs.edit { putBoolean("phone_auth_in_progress", false) }
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
            else -> {}
        }
    }

    AppNavigation(
        navController = navController,
        startDestination = Screen.Splash.route,
        authViewModel = authViewModel,
        portfolioViewModel = portfolioViewModel,
        exploreViewModel = exploreViewModel,
        viewModelProviderFactory = viewModelFactory,
        webClientId = webClientId,
        prefs = prefs,
        safeNavigate = { route -> navController.navigate(route) { launchSingleTop = true } },
        safePopBack = { navController.popBackStack() },
        isConnected = isConnected
    )
}