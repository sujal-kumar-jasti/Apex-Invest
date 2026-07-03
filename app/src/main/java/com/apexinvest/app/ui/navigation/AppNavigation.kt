package com.apexinvest.app.ui.navigation

import android.content.SharedPreferences
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.apexinvest.app.ui.screens.AIInvestmentIdeasScreen
import com.apexinvest.app.ui.screens.AnalyticsScreen
import com.apexinvest.app.ui.screens.AuthScreen
import com.apexinvest.app.ui.screens.DashboardScreen
import com.apexinvest.app.ui.screens.ExploreScreen
import com.apexinvest.app.ui.screens.PortfolioScreen
import com.apexinvest.app.ui.screens.PredictionScreen
import com.apexinvest.app.ui.screens.ProfileScreen
import com.apexinvest.app.ui.screens.SplashScreen
import com.apexinvest.app.ui.screens.StockDetailScreen
import com.apexinvest.app.ui.screens.TransactionHistory
import com.apexinvest.app.ui.screens.WatchlistScreen
import com.apexinvest.app.viewmodel.AuthState
import com.apexinvest.app.viewmodel.AuthViewModel
import com.apexinvest.app.viewmodel.ExploreViewModel
import com.apexinvest.app.viewmodel.PortfolioViewModel
import com.apexinvest.app.viewmodel.PredictionViewModel
import com.apexinvest.app.viewmodel.StockDetailViewModel

// --- Core Screen Definitions ---
sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object Login : Screen("login_screen")
    object SignUp : Screen("signup_screen")
    object Main : Screen("main_screen")
    object Watchlist : Screen("watchlist_screen")
    object Profile : Screen("profile_screen")
    object Analytics : Screen("analytics_screen")
    object InvestmentIdeas : Screen("investment_ideas_screen")
    object Predictions : Screen("predictions_screen")
    object Portfolio : Screen("portfolio_screen")
    object TransactionHistory : Screen("transaction_history_screen")
    object Notifications : Screen("notifications_screen")
    object Explore : Screen("explore_screen") // 🚀 FIXED: Added missing Explore route

    object StockDetail : Screen("stock_detail/{symbol}/{currency}") {
        fun createRoute(symbol: String, currency: String) = "stock_detail/$symbol/$currency"
    }
}

// --- Bottom Tab Definitions ---
data class BottomTabItem(val label: String, val icon: ImageVector, val route: String, val badgeCount: Int = 0)

private const val TAB_HOME = "tab_home"
private const val TAB_EXPLORE = "tab_explore"
private const val TAB_HOLDINGS = "tab_holdings"
private const val TAB_IDEAS = "tab_ideas"

private val bottomNavItems = listOf(
    BottomTabItem("Home", Icons.Filled.Home, TAB_HOME),
    BottomTabItem("Explore", Icons.Filled.Search, TAB_EXPLORE),
    BottomTabItem("Holdings", Icons.Filled.PieChart, TAB_HOLDINGS),
    BottomTabItem("Ideas", Icons.Filled.Lightbulb, TAB_IDEAS)
)

private val BottomBarShape = RoundedCornerShape(42.dp)

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    portfolioViewModel: PortfolioViewModel,
    exploreViewModel: ExploreViewModel,
    predictionViewModel: PredictionViewModel,
    viewModelProviderFactory: ViewModelProvider.Factory,
    webClientId: String,
    prefs: SharedPreferences,
    safeNavigate: (String) -> Unit,
    safePopBack: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    isConnected: Boolean
) {
    val authState by authViewModel.authState.collectAsState()

    // ROOT NAV HOST
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition ={ fadeIn(animationSpec = tween(150, easing = FastOutSlowInEasing)) },
        exitTransition = { fadeOut(animationSpec = tween(150, easing = FastOutSlowInEasing)) },
        popEnterTransition = { fadeIn(animationSpec = tween(150, easing = FastOutSlowInEasing)) },
        popExitTransition = { fadeOut(animationSpec = tween(150, easing = FastOutSlowInEasing)) }
    ){
        composable(Screen.Splash.route) {
            val isStartupComplete by portfolioViewModel.isStartupComplete.collectAsState()
            SplashScreen(
                isConnected = isConnected,
                isStartupComplete = isStartupComplete,
                onTasksFinished = {
                    if (authState is AuthState.LoggedIn) {
                        navController.navigate(Screen.Main.route) { popUpTo(0) { inclusive = true } }
                        if (com.apexinvest.app.MainActivity.pendingDeepLink.value == "watchlist") {
                            navController.navigate(Screen.Watchlist.route) { launchSingleTop = true }
                            com.apexinvest.app.MainActivity.pendingDeepLink.value = null
                        }
                    } else {
                        navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                    }
                }
            )
        }

        composable(Screen.Login.route) { AuthScreen(true, { authViewModel.clearError(); safeNavigate(Screen.SignUp.route) }, {}, onGoogleSignInClick, authViewModel, isConnected) }
        composable(Screen.SignUp.route) { AuthScreen(false, {}, { authViewModel.clearError(); safeNavigate(Screen.Login.route) }, onGoogleSignInClick, authViewModel, isConnected) }

        composable(Screen.Main.route) {
            MainScreen(
                authViewModel = authViewModel,
                portfolioViewModel = portfolioViewModel,
                exploreViewModel = exploreViewModel,
                predictionViewModel = predictionViewModel,
                viewModelProviderFactory = viewModelProviderFactory,
                globalNavigate = safeNavigate,
                isConnected = isConnected
            )
        }

        // 🚀 These are on the ROOT NavHost, meaning the bottom bar will NOT display when routed here
        composable(Screen.Watchlist.route) { WatchlistScreen(portfolioViewModel, safePopBack, safeNavigate, isConnected) }
        composable(Screen.Profile.route) {
            ProfileScreen(
                authViewModel = authViewModel,
                portfolioViewModel = portfolioViewModel,
                predictionViewModel = predictionViewModel,
                exploreViewModel = exploreViewModel,
                onBack = safePopBack,
                onNavigateToLogin = { navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } } },
                isConnected = isConnected
            )
        }
        composable(Screen.StockDetail.route, arguments = listOf(navArgument("symbol") { type = NavType.StringType }, navArgument("currency") { type = NavType.StringType })) { backStackEntry ->
            val symbol = backStackEntry.arguments?.getString("symbol") ?: "UNKNOWN"
            val currency = backStackEntry.arguments?.getString("currency") ?: ""
            val stockDetailViewModel: StockDetailViewModel = viewModel(factory = viewModelProviderFactory)
            StockDetailScreen(symbol, currency, portfolioViewModel, stockDetailViewModel, safePopBack, { safeNavigate(Screen.StockDetail.createRoute(it, currency)) }, isConnected)
        }
        composable(Screen.InvestmentIdeas.route) { AIInvestmentIdeasScreen(portfolioViewModel, safePopBack, { safeNavigate(it) }, true, isConnected) }
        composable(Screen.Predictions.route) { PredictionScreen(portfolioViewModel, predictionViewModel, safePopBack, isConnected) }
        composable(Screen.Portfolio.route) { PortfolioScreen(portfolioViewModel, safePopBack, safeNavigate, isConnected = isConnected) }
        composable(Screen.Analytics.route) { AnalyticsScreen(portfolioViewModel, { safeNavigate(it) }, safePopBack, isConnected) }
        composable(Screen.TransactionHistory.route) { TransactionHistory(portfolioViewModel, safePopBack, isConnected) }
        composable(Screen.Notifications.route) {
            com.apexinvest.app.ui.screens.NotificationScreen(portfolioViewModel, safePopBack, isConnected)
        }
    }
}

@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    portfolioViewModel: PortfolioViewModel,
    exploreViewModel: ExploreViewModel,
    predictionViewModel: PredictionViewModel,
    viewModelProviderFactory: ViewModelProvider.Factory,
    globalNavigate: (String) -> Unit,
    isConnected: Boolean
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: TAB_HOME

    var isExploreSearchActive by rememberSaveable { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()

    val navigateToBottomTab = { route: String ->
        if (currentRoute != route) {
            bottomNavController.navigate(route) {
                bottomNavController.graph.findStartDestination().route?.let { startRoute ->
                    popUpTo(startRoute) { saveState = true }
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    BackHandler(enabled = currentRoute != TAB_HOME) {
        if (currentRoute == TAB_EXPLORE && isExploreSearchActive) {
            isExploreSearchActive = false
        } else {
            navigateToBottomTab(TAB_HOME)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // 1. CONTENT LAYER
        NavHost(
            navController = bottomNavController,
            startDestination = TAB_HOME,
            modifier = Modifier.fillMaxSize(),
            enterTransition = {
                val targetIndex = bottomNavItems.indexOfFirst { it.route == targetState.destination.route }
                val currentIndex = bottomNavItems.indexOfFirst { it.route == initialState.destination.route }
                val direction = if (targetIndex > currentIndex) AnimatedContentTransitionScope.SlideDirection.Left
                else AnimatedContentTransitionScope.SlideDirection.Right
                slideIntoContainer(direction, tween(300, easing = FastOutSlowInEasing))
            },
            exitTransition = {
                val targetIndex = bottomNavItems.indexOfFirst { it.route == targetState.destination.route }
                val currentIndex = bottomNavItems.indexOfFirst { it.route == initialState.destination.route }
                val direction = if (targetIndex > currentIndex) AnimatedContentTransitionScope.SlideDirection.Left
                else AnimatedContentTransitionScope.SlideDirection.Right
                slideOutOfContainer(direction, tween(300, easing = FastOutSlowInEasing))
            },
            popEnterTransition = {
                // Keep symmetrical for pops
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            }
        ) {
            composable(TAB_HOME) {
                DashboardScreen(
                    authViewModel = authViewModel,
                    portfolioViewModel = portfolioViewModel,
                    onNavigate = { route ->
                        when (route) {
                            Screen.InvestmentIdeas.route -> navigateToBottomTab(TAB_IDEAS)
                            Screen.Portfolio.route -> navigateToBottomTab(TAB_HOLDINGS)
                            Screen.Explore.route -> navigateToBottomTab(TAB_EXPLORE)
                            else -> globalNavigate(route)
                        }
                    },
                    isConnected = isConnected
                )
            }
            composable(TAB_EXPLORE) {
                ExploreScreen(
                    viewModel = exploreViewModel,
                    onNavigate = globalNavigate,
                    isConnected = isConnected,
                    isSearchActive = isExploreSearchActive,
                    onSearchActiveChange = { isExploreSearchActive = it }
                )
            }
            composable(TAB_HOLDINGS) {
                PortfolioScreen(
                    portfolioViewModel = portfolioViewModel,
                    onBack = { navigateToBottomTab(TAB_HOME) },
                    onNavigate = globalNavigate,
                    isConnected = isConnected
                )
            }
            composable(TAB_IDEAS) {
                AIInvestmentIdeasScreen(
                    portfolioViewModel = portfolioViewModel,
                    onBack = { navigateToBottomTab(TAB_HOME) },
                    onNavigate = globalNavigate,
                    isConnected = isConnected
                )
            }
        }

        // 2. FLOATING BAR LAYER
        CustomBottomNavigationBar(
            currentRoute = currentRoute,
            isDark = isDark,
            modifier = Modifier.align(Alignment.BottomCenter),
            onItemSelected = navigateToBottomTab
        )
    }
}

// --- OPTIMIZED FLOATING BOTTOM BAR ---
@Composable
private fun CustomBottomNavigationBar(
    currentRoute: String,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    onItemSelected: (String) -> Unit
) {
    val purpleAccent = remember(isDark) { if (isDark) Color(0xFF9E86FF) else Color(0xFF673AB7) }
    val shadowSize = remember(isDark) { if (isDark) 24.dp else 16.dp }
    val bgColor = remember(isDark) { if (isDark) 0.85f else 0.95f }
    val borderColor = remember(isDark) { if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .height(80.dp)
            .shadow(shadowSize, BottomBarShape, spotColor = purpleAccent.copy(alpha = 0.3f))
            .clip(BottomBarShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = bgColor))
            .border(1.5.dp, borderColor, BottomBarShape)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { item ->
                val isSelected = currentRoute == item.route

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = { onItemSelected(item.route) }
                        )
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) purpleAccent.copy(alpha = 0.15f) else Color.Transparent)
                    ) {
                        BadgedBox(
                            badge = {
                                if (item.badgeCount > 0) {
                                    Badge(containerColor = purpleAccent, contentColor = Color.White) { Text(item.badgeCount.toString()) }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                modifier = Modifier.size(if (isSelected) 32.dp else 28.dp),
                                tint = if (isSelected) purpleAccent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}