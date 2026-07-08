package com.apexinvest.app

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
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

                        // Show banner if offline or if there is a message
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
                                authViewModel = authViewModel,
                                exploreViewModel = exploreViewModel,
                                portfolioViewModel = portfolioViewModel,
                                predictionViewModel = predictionViewModel,
                                isConnected = isConnected,
                                isAppThemeDark = darkTheme, // Passed exactly from the calculation above
                            ) {
                                scope.launch { triggerGoogleSignIn() }
                            }
                        }
                    }
                }
            }
        }
    }

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
        catch (_: GetCredentialException) { Toast.makeText(this, "Google Sign-In Cancelled", Toast.LENGTH_SHORT).show() }
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
    }
}

@Composable
fun ApexInvestApp(
    viewModelFactory: ViewModelProvider.Factory,
    authViewModel: AuthViewModel,
    exploreViewModel: ExploreViewModel,
    portfolioViewModel: PortfolioViewModel,
    predictionViewModel: PredictionViewModel,
    isConnected: Boolean,
    isAppThemeDark: Boolean,
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

            // Navigate to main screen on login
            if (currentRoute == Screen.Login.route || currentRoute == Screen.SignUp.route) {
                portfolioViewModel.onLoginSuccess()
                navController.navigate(Screen.Main.route) { popUpTo(0) { inclusive = true } }
            }

            // Handle deep links
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
                        navController.navigate(Screen.StockDetail.createRoute(symbol, "USD"))
                    }
                }
                MainActivity.pendingDeepLink.value = null
            }
        } else if (authState is AuthState.LoggedOut && currentRoute != Screen.Splash.route) {
            navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
        }
    }

    AppNavigation(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = Modifier,
        authViewModel = authViewModel,
        portfolioViewModel = portfolioViewModel,
        exploreViewModel = exploreViewModel,
        predictionViewModel = predictionViewModel,
        viewModelProviderFactory = viewModelFactory,
        safeNavigate = { navController.navigate(it) { launchSingleTop = true } },
        safePopBack = { navController.popBackStack() },
        onGoogleSignInClick = onGoogleSignInClick,
        isConnected = isConnected,
        isAppThemeDark = isAppThemeDark
    )
}