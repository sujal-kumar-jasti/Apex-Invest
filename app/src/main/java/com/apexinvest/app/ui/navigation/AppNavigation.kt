package com.apexinvest.app.ui.navigation

import android.content.SharedPreferences
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.apexinvest.app.ui.screens.AIInvestmentIdeasScreen
import com.apexinvest.app.ui.screens.AnalyticsScreen
import com.apexinvest.app.ui.screens.AuthScreen
import com.apexinvest.app.ui.screens.DashboardScreen
import com.apexinvest.app.ui.screens.ExploreScreen
import com.apexinvest.app.ui.screens.PhoneAuthScreen
import com.apexinvest.app.ui.screens.PortfolioScreen
import com.apexinvest.app.ui.screens.PredictionScreen
import com.apexinvest.app.ui.screens.ProfileScreen
import com.apexinvest.app.ui.screens.SplashScreen
import com.apexinvest.app.ui.screens.StockDetailScreen
import com.apexinvest.app.ui.screens.WatchlistScreen
import com.apexinvest.app.viewmodel.AuthViewModel
import com.apexinvest.app.viewmodel.ExploreViewModel
import com.apexinvest.app.viewmodel.PortfolioViewModel
import com.apexinvest.app.viewmodel.PredictionViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object Login : Screen("login_screen")
    object SignUp : Screen("signup_screen")
    object PhoneAuth : Screen("phone_auth_screen")
    object Main : Screen("main_pager")
    object Watchlist : Screen("watchlist_screen")
    object Profile : Screen("profile_screen")
    object Analytics : Screen("analytics_screen")
    object InvestmentIdeas : Screen("investment_ideas_screen")
    object Predictions : Screen("predictions_screen")
    object Portfolio : Screen("portfolio_screen") // Full screen version
    object StockDetail : Screen("stock_detail/{symbol}") {
        fun createRoute(symbol: String) = "stock_detail/$symbol"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    portfolioViewModel: PortfolioViewModel, // <--- PRELOADED INSTANCE
    exploreViewModel: ExploreViewModel,     // <--- PRELOADED INSTANCE
    viewModelProviderFactory: ViewModelProvider.Factory,
    webClientId: String,
    prefs: SharedPreferences,
    safeNavigate: (String) -> Unit,
    safePopBack: () -> Unit,
    isConnected: Boolean
) {
    val animSpec = tween<IntOffset>(400, easing = FastOutSlowInEasing)

    // --- INSTANTIATE AI VIEWMODEL ---
    // We create it here using the factory so it can be passed to the screen
    val predictionViewModel: PredictionViewModel = viewModel(factory = viewModelProviderFactory)

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = animSpec) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = animSpec) },
        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = animSpec) },
        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = animSpec) }
    ) {
        composable(Screen.Splash.route) { SplashScreen(isConnected=isConnected) }
        composable(Screen.Login.route) { AuthScreen(true, { safeNavigate(Screen.SignUp.route) }, { safeNavigate(Screen.PhoneAuth.route) }, authViewModel, webClientId) }
        composable(Screen.SignUp.route) { AuthScreen(false, { safeNavigate(Screen.Login.route) }, { safeNavigate(Screen.PhoneAuth.route) }, authViewModel, webClientId) }
        composable(Screen.PhoneAuth.route) { PhoneAuthScreen(authViewModel, safePopBack, prefs) }

        // --- MAIN PAGER (Holds Tabs) ---
        composable("main_pager") {
            // CRITICAL: We pass the Activity-Scoped ViewModels here
            MainPagerScreen(
                portfolioViewModel = portfolioViewModel,
                exploreViewModel = exploreViewModel,
                onNavigate = safeNavigate,
                isConnected = isConnected
            )
        }

        composable(Screen.Watchlist.route) { Box(Modifier.fillMaxSize()) { WatchlistScreen(portfolioViewModel, safePopBack, safeNavigate,isConnected = isConnected) } }
        composable(Screen.Profile.route) { Box(Modifier.fillMaxSize()) { ProfileScreen(authViewModel, portfolioViewModel, safePopBack,isConnected = isConnected) } }

        composable(
            route = Screen.StockDetail.route,
            arguments = listOf(navArgument("symbol") { type = NavType.StringType })
        ) {
            val symbol = it.arguments?.getString("symbol") ?: "UNKNOWN"
            Box(Modifier.fillMaxSize().statusBarsPadding()) { StockDetailScreen(symbol, portfolioViewModel, safePopBack,isConnected = isConnected) }
        }

        composable(Screen.InvestmentIdeas.route) {
            Box(Modifier.fillMaxSize().statusBarsPadding()) {
                // Standalone Screen version (with Back button enabled)
                AIInvestmentIdeasScreen(portfolioViewModel = portfolioViewModel, onNavigate = { route -> navController.navigate(route) }, onBack = { navController.popBackStack() }, isBackEnabled = true,isConnected = isConnected)
            }
        }

        composable(Screen.Predictions.route) {
            Box(Modifier.fillMaxSize()) {
                PredictionScreen(
                    portfolioViewModel = portfolioViewModel,
                    predictionViewModel = predictionViewModel,
                    onNavigate = safeNavigate,
                    onBack = { safePopBack() },
                    isConnected = isConnected
                )
            }
        }

        composable(Screen.Portfolio.route) {
            // Standalone Screen version (Full list)
            Box(Modifier.fillMaxSize().statusBarsPadding()) { PortfolioScreen(
                portfolioViewModel,
                onBack = { safePopBack() },
                onNavigate = safeNavigate,
                isConnected = isConnected
            ) }
        }

        composable(Screen.Analytics.route) {
            AnalyticsScreen(viewModel = portfolioViewModel, onNavigate = { route -> navController.navigate(route) }, onBack = { navController.popBackStack() },isConnected = isConnected)
        }
    }
}

@Composable
fun MainPagerScreen(
    portfolioViewModel: PortfolioViewModel,
    exploreViewModel: ExploreViewModel,
    onNavigate: (String) -> Unit,
    isConnected: Boolean
) {
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }

    // If back is pressed on a non-home tab, go back to Home first
    BackHandler(enabled = selectedIndex != 0) {
        selectedIndex = 0
    }

    Scaffold(
        contentWindowInsets = if (!isConnected) {
            WindowInsets(0.dp) // Offline: Banner pushes us down, so ignore system bars
        } else {
            ScaffoldDefaults.contentWindowInsets // Online: Respect system bars normally
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp
            ) {
                val items = listOf(
                    Triple(0, "Home", Icons.Filled.Home),
                    Triple(1, "Explore", Icons.Filled.Search),
                    Triple(2, "Holdings", Icons.Filled.PieChart),
                    Triple(3, "Ideas", Icons.Filled.Lightbulb)
                )

                items.forEach { (index, label, icon) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = label) },
                        label = {
                            Text(
                                label,
                                style = MaterialTheme.typography.labelSmall // 👈 more compact
                            )
                        },
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            AnimatedContent(
                targetState = selectedIndex,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                            scaleIn(initialScale = 0.98f, animationSpec = tween(220)) togetherWith
                            fadeOut(animationSpec = tween(180))
                },
                label = "TabTransition"
            ) { targetPage ->
                when (targetPage) {
                    0 -> DashboardScreen(
                        portfolioViewModel = portfolioViewModel, // Using Preloaded
                        onNavigate = { route ->
                            // Handle Internal Tab Switching Shortcuts
                            when (route) {
                                Screen.InvestmentIdeas.route -> selectedIndex = 3
                                Screen.Portfolio.route -> selectedIndex = 2
                                else -> onNavigate(route)
                            }
                        },isConnected=isConnected

                    )
                    1 -> ExploreScreen(
                        viewModel = exploreViewModel, // Using Preloaded
                        onNavigate = { route ->
                            if (route == "search_screen") onNavigate("search_screen") // If you implement search later
                            else onNavigate(route)
                        },
                        isConnected = isConnected
                    )
                    2 -> PortfolioScreen(
                        portfolioViewModel = portfolioViewModel, // Using Preloaded
                        onBack = { selectedIndex = 0 }, // Back inside tab goes to Home
                        onNavigate = onNavigate,
                        isConnected = isConnected
                    )
                    3 -> AIInvestmentIdeasScreen(
                        portfolioViewModel = portfolioViewModel, // Using Preloaded
                        onBack = { selectedIndex = 0 }, // Back inside tab goes to Home
                        onNavigate = onNavigate,
                        isConnected = isConnected
                    )
                }
            }
        }
    }
}