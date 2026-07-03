package com.apexinvest.app

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
<<<<<<< HEAD
=======
import android.content.pm.PackageManager
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
<<<<<<< HEAD
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
=======
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
<<<<<<< HEAD
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.apexinvest.app.data.AppContainer
import com.apexinvest.app.ui.components.AppMessageBanner
import com.apexinvest.app.ui.components.MessageType
=======
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
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
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
<<<<<<< HEAD
import com.apexinvest.app.worker.MarketUpdateWorker
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    companion object {
        val pendingDeepLink = MutableStateFlow<String?>(null)
    }

    private val appContainer: AppContainer by lazy { (application as ApexApplication).container }
    private val webclientid = "253879412042-r92pr90lm5r852140ra66aphf30qseo0.apps.googleusercontent.com"
    private lateinit var prefs: SharedPreferences

    private val authViewModel: AuthViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                AuthViewModel(appContainer.authRepository) as T
        }
    }
    private val exploreViewModel: ExploreViewModel by viewModels { appContainer.exploreViewModelFactory }
    private val portfolioViewModel: PortfolioViewModel by viewModels { appContainer.portfolioViewModelFactory }
    private val predictionViewModel: PredictionViewModel by viewModels { appContainer.predictionViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Must be called before super.onCreate for Android 12+ API compliance
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        prefs = getSharedPreferences("apex_auth", MODE_PRIVATE)

        handleIntent(intent)
        lifecycleScope.launch(Dispatchers.Default) { setupWorkers() }

        // Pre-warm the database and session manager in the background
        lifecycleScope.launch(Dispatchers.IO) {
            appContainer.sessionManager
            appContainer.portfolioRepository
        }

        setContent {
            val themeMode by portfolioViewModel.themeMode.collectAsState()
            val darkTheme = when (themeMode) { 1 -> false; 2 -> true; else -> isSystemInDarkTheme() }

            ApexInvestTheme(darkTheme = darkTheme) {
                val networkObserver = remember { NetworkObserver(applicationContext) }
                val isConnected by networkObserver.observe().collectAsState(initial = true)
                val scope = rememberCoroutineScope()
                val authState by authViewModel.authState.collectAsState()

                val currentPortfolioViewModel by rememberUpdatedState(portfolioViewModel)
                val lifecycleOwner = LocalLifecycleOwner.current

                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            if (authState is AuthState.LoggedIn) {
                                currentPortfolioViewModel.onAppResumed()
                            }
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        val uiMessage by portfolioViewModel.uiMessage.collectAsState()

                        // 1. SYSTEM INSETS LAYER (Banners go here)
                        // 🚀 FIX: Only apply padding and show Column if there's actually a banner
                        if (!isConnected || uiMessage != null) {
                            Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().consumeWindowInsets(WindowInsets.statusBars)) {
                                if (!isConnected) {
                                    OfflineBanner(isConnected = false)
                                }

                                AppMessageBanner(
                                    message = uiMessage?.text,
                                    type = uiMessage?.type ?: MessageType.INFO,
                                    onDismiss = { portfolioViewModel.clearMessage() }
                                )
                            }
                        }

                        // 2. MAIN CONTENT LAYER
                        // We use weight(1f) to take remaining space. 
                        Box(modifier = Modifier.weight(1f)) {
                            ApexInvestApp(
                                viewModelFactory = appContainer.stockDetailViewModelFactory,
                                webClientId = webclientid,
                                prefs = prefs,
                                authViewModel = authViewModel,
                                exploreViewModel = exploreViewModel,
                                portfolioViewModel = portfolioViewModel,
                                predictionViewModel = predictionViewModel,
                                isConnected = isConnected
                            ) {
                                scope.launch { triggerGoogleSignIn() }
                            }
=======
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
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
                        }
                    }
                }
            }
        }
    }

<<<<<<< HEAD
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.data?.let {
            when {
                (it.scheme == "apexinvest") && (it.host == "watchlist") -> {
                    pendingDeepLink.value = "watchlist"
                }
                (it.scheme == "apexinvest") && (it.host == "notifications") -> {
                    pendingDeepLink.value = "notifications"
                }
                (it.scheme == "apexinvest") && (it.host == "portfolio") -> {
                    pendingDeepLink.value = "portfolio"
                }
                (it.scheme == "apexinvest") && (it.host == "stock") -> {
                    val symbol = it.lastPathSegment
                    if (symbol != null) {
                        pendingDeepLink.value = "stock:$symbol"
                    }
                }
                (it.scheme == "apexinvest") && (it.host == "analysis") -> {
                    val symbol = it.lastPathSegment
                    if (symbol != null) {
                        pendingDeepLink.value = "analysis:$symbol"
                    }
                }
            }
        }
    }

    private suspend fun triggerGoogleSignIn() {
        val cm = CredentialManager.create(this)
        val opt = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts = false)
            .setServerClientId(webclientid)
            .setAutoSelectEnabled(autoSelectEnabled = false)
            .build()
        val req = GetCredentialRequest.Builder().addCredentialOption(opt).build()
        try { handleSignInResult(cm.getCredential(this, req)) }
        catch (e: GetCredentialException) { Toast.makeText(this, "Google Sign-In Cancelled", Toast.LENGTH_SHORT).show() }
    }

    private fun handleSignInResult(result: GetCredentialResponse) {
        val cred = result.credential
        if ((cred is CustomCredential) && (cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
            try { authViewModel.signInWithGoogle(GoogleIdTokenCredential.createFrom(cred.data).idToken) }
            catch (_: Exception) {}
        }
    }

    private fun setupWorkers() {
        val req = PeriodicWorkRequestBuilder<MarketUpdateWorker>(30, TimeUnit.MINUTES, 5, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("PeriodicMarketCheck", ExistingPeriodicWorkPolicy.UPDATE, req)
=======
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
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
    }
}

@Composable
<<<<<<< HEAD
fun ApexInvestApp(
    viewModelFactory: ViewModelProvider.Factory,
    webClientId: String,
    prefs: SharedPreferences,
    authViewModel: AuthViewModel,
    exploreViewModel: ExploreViewModel,
    portfolioViewModel: PortfolioViewModel,
    predictionViewModel: PredictionViewModel,
    isConnected: Boolean,
    onGoogleSignInClick: () -> Unit
) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val pendingLink by MainActivity.pendingDeepLink.collectAsState()
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    LaunchedEffect(authState, currentRoute, pendingLink) {
        if (authState is AuthState.LoggedIn) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }

            // 🚀 FIX: Updated to route to Screen.Main.route instead of deprecated "main_pager"
            if (currentRoute == Screen.Login.route || currentRoute == Screen.SignUp.route) {
                portfolioViewModel.onLoginSuccess()
                navController.navigate(Screen.Main.route) { popUpTo(0) { inclusive = true } }
            }

            // 🚀 FIX: Updated routing condition
            if (currentRoute == Screen.Main.route && pendingLink != null) {
                val link = pendingLink!!
                when {
                    link == "watchlist" -> navController.navigate(Screen.Watchlist.route)
                    link == "notifications" -> navController.navigate(Screen.Notifications.route)
                    link == "portfolio" -> navController.navigate(Screen.Portfolio.route)
                    link.startsWith("stock:") -> {
                        val symbol = link.substringAfter(":")
                        navController.navigate(Screen.StockDetail.createRoute(symbol, "USD"))
                    }
                    link.startsWith("analysis:") -> {
                        val symbol = link.substringAfter(":")
                        // Note: Screen.Predictions is currently where DeepAnalysis is hosted or linked from in the UI
                        // But for direct access, let's go to StockDetail then user can jump to DeepAnalysis
                        // Or if DeepAnalysis had its own Screen route, we'd use that.
                        // For now, StockDetail is the best entry point for specific stock alerts.
                        navController.navigate(Screen.StockDetail.createRoute(symbol, "USD"))
                    }
                }
                MainActivity.pendingDeepLink.value = null
            }
        } else if (authState is AuthState.LoggedOut && currentRoute != Screen.Splash.route) {
            navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
=======
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
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
        }
    }

    AppNavigation(
        navController = navController,
        startDestination = Screen.Splash.route,
<<<<<<< HEAD
        modifier = Modifier,
        authViewModel = authViewModel,
        portfolioViewModel = portfolioViewModel,
        exploreViewModel = exploreViewModel,
        predictionViewModel = predictionViewModel,
        viewModelProviderFactory = viewModelFactory,
        webClientId = webClientId,
        prefs = prefs,
        safeNavigate = { navController.navigate(it) { launchSingleTop = true } },
        safePopBack = { navController.popBackStack() },
        onGoogleSignInClick = onGoogleSignInClick,
        isConnected = isConnected
    )
}
=======
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
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
