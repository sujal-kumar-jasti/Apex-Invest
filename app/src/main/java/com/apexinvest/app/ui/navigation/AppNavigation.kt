package com.apexinvest.app.ui.navigation

import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.toColorInt
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
import com.apexinvest.app.R
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

// Navigation routes
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
    object Portfolio : Screen("portfolio_screen?action={action}") {
        fun createRoute(action: String? = null) = if (action != null) "portfolio_screen?action=$action" else "portfolio_screen"
    }
    object TransactionHistory : Screen("transaction_history_screen")
    object Notifications : Screen("notifications_screen")
    object Explore : Screen("explore_screen")

    object StockDetail : Screen("stock_detail/{symbol}/{currency}") {
        fun createRoute(symbol: String, currency: String) = "stock_detail/$symbol/$currency"
    }
}

private const val TAB_HOME = "tab_home"
private const val TAB_EXPLORE = "tab_explore"
private const val TAB_IDEAS = "tab_ideas"
private const val TAB_PROFILE = "tab_profile"

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
    safeNavigate: (String) -> Unit,
    safePopBack: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    isConnected: Boolean,
    isAppThemeDark: Boolean
) {
    val authState by authViewModel.authState.collectAsState()

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
                globalNavigate = safeNavigate,
                isConnected = isConnected,
                isAppThemeDark = isAppThemeDark
            )
        }
        composable(
            Screen.Portfolio.route,
            arguments = listOf(navArgument("action") { type = NavType.StringType; nullable = true })
        ) { backStackEntry ->
            val action = backStackEntry.arguments?.getString("action")
            PortfolioScreen(
                portfolioViewModel = portfolioViewModel,
                onBack = safePopBack,
                onNavigate = safeNavigate,
                action = action,
                isConnected = isConnected
            )
        }

        composable(Screen.Watchlist.route)
        { WatchlistScreen(portfolioViewModel,
            safePopBack,
            safeNavigate,
            isConnected) }

        composable(Screen.Profile.route) {
            ProfileScreen(
                authViewModel = authViewModel,
                portfolioViewModel = portfolioViewModel,
                predictionViewModel = predictionViewModel,
                exploreViewModel = exploreViewModel,
                onNavigateToLogin = { navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } } },
                isConnected = isConnected,
            )
        }
        composable(Screen.StockDetail.route, arguments = listOf(navArgument("symbol") { type = NavType.StringType }, navArgument("currency") { type = NavType.StringType })) { backStackEntry ->
            val symbol = backStackEntry.arguments?.getString("symbol") ?: "UNKNOWN"
            val currency = backStackEntry.arguments?.getString("currency") ?: ""
            val stockDetailViewModel: StockDetailViewModel = viewModel(factory = viewModelProviderFactory)
            StockDetailScreen(symbol, currency, portfolioViewModel, stockDetailViewModel, safePopBack, { safeNavigate(Screen.StockDetail.createRoute(it, currency)) }, isConnected)
        }
        composable(Screen.InvestmentIdeas.route) { AIInvestmentIdeasScreen(portfolioViewModel, safePopBack, { safeNavigate(it) }, true, isConnected) }
        composable(Screen.Predictions.route) { 
            PredictionScreen(
                portfolioViewModel, 
                predictionViewModel, 
                onBack = safePopBack,
                onNavigateToPortfolio = { safeNavigate(Screen.Portfolio.createRoute("OPEN_TRADE")) },
                isConnected = isConnected
            ) 
        }
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
    globalNavigate: (String) -> Unit,
    isConnected: Boolean,
    isAppThemeDark: Boolean
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: TAB_HOME

    var isExploreSearchActive by rememberSaveable { mutableStateOf(false) }

    val navigateToBottomTab = { route: String ->
        if (currentRoute != route) {
            bottomNavController.navigate(route) {
                popUpTo(bottomNavController.graph.findStartDestination().id) {
                    saveState = true
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

        NavHost(
            navController = bottomNavController,
            startDestination = TAB_HOME,
            modifier = Modifier.fillMaxSize(),
            enterTransition = {
                val targetIndex = getTabIndex(targetState.destination.route)
                val currentIndex = getTabIndex(initialState.destination.route)
                val direction = if (targetIndex > currentIndex) AnimatedContentTransitionScope.SlideDirection.Left
                else AnimatedContentTransitionScope.SlideDirection.Right
                slideIntoContainer(direction, tween(300, easing = FastOutSlowInEasing))
            },
            exitTransition = {
                val targetIndex = getTabIndex(targetState.destination.route)
                val currentIndex = getTabIndex(initialState.destination.route)
                val direction = if (targetIndex > currentIndex) AnimatedContentTransitionScope.SlideDirection.Left
                else AnimatedContentTransitionScope.SlideDirection.Right
                slideOutOfContainer(direction, tween(300, easing = FastOutSlowInEasing))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            }
        ) {
            composable(TAB_HOME) {
                DashboardScreen(
                    portfolioViewModel = portfolioViewModel,
                    exploreViewModel = exploreViewModel,
                    onNavigate = { route ->
                        when (route) {
                            Screen.InvestmentIdeas.route -> navigateToBottomTab(TAB_IDEAS)
                            Screen.Profile.route -> navigateToBottomTab(TAB_PROFILE)
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
            composable(TAB_IDEAS) {
                AIInvestmentIdeasScreen(
                    portfolioViewModel = portfolioViewModel,
                    onBack = { navigateToBottomTab(TAB_HOME) },
                    onNavigate = globalNavigate,
                    isConnected = isConnected
                )
            }
            composable(TAB_PROFILE) {
                ProfileScreen(
                    authViewModel = authViewModel,
                    portfolioViewModel = portfolioViewModel,
                    predictionViewModel = predictionViewModel,
                    exploreViewModel = exploreViewModel,
                    onNavigateToLogin = { globalNavigate(Screen.Login.route) },
                    isConnected = isConnected
                )
            }
        }

        XmlBottomNavigationBar(
            currentRoute = currentRoute,
            isBarThemeDark = isAppThemeDark,
            modifier = Modifier.align(Alignment.BottomCenter),
            onItemSelected = navigateToBottomTab
        )
    }
}

private fun getTabIndex(route: String?): Int {
    return when (route) {
        TAB_HOME -> 0
        TAB_EXPLORE -> 1
        TAB_IDEAS -> 2
        TAB_PROFILE -> 3
        else -> 0
    }
}

@SuppressLint("InflateParams")
@Composable
private fun XmlBottomNavigationBar(
    currentRoute: String,
    isBarThemeDark: Boolean,
    modifier: Modifier = Modifier,
    onItemSelected: (String) -> Unit
) {
    val darkState = rememberUpdatedState(isBarThemeDark)

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
            .padding(horizontal =24.dp)
            .height(89.dp),
        factory = { context ->
            LayoutInflater.from(context).inflate(R.layout.bottom_nav_telegram, null, false)
        },
        update = { view ->
            view.findViewById<FrameLayout>(R.id.tab_home).setOnClickListener { onItemSelected(TAB_HOME) }
            view.findViewById<FrameLayout>(R.id.tab_explore).setOnClickListener { onItemSelected(TAB_EXPLORE) }
            view.findViewById<FrameLayout>(R.id.tab_ideas).setOnClickListener { onItemSelected(TAB_IDEAS) }
            view.findViewById<FrameLayout>(R.id.tab_profile).setOnClickListener { onItemSelected(TAB_PROFILE) }

            val isDark = darkState.value

            val purpleColor = if (isDark) "#BFABFF".toColorInt() else "#512DA8".toColorInt()
            val unselectedColor = if (isDark) "#C8CCD5".toColorInt() else "#545454".toColorInt()

            val purpleGradientTop = if (isDark)
                android.graphics.Color.rgb(38, 37, 42) // Dark grey-purple
                else
                android.graphics.Color.rgb(240, 238, 245) // Light purple-grey

            val barBgColor = if (isDark)
                android.graphics.Color.rgb(35, 35, 37) // Darkened fill color
                else
                android.graphics.Color.rgb(245, 245, 247) // Lightened fill color

            val barBorderColor = if (isDark)
                android.graphics.Color.argb(80, 255, 255, 255) // Darkened border
                else
                android.graphics.Color.argb(30, 0, 0, 0) // Lightened border

            val activePillColor = if (isDark) "#339E86FF".toColorInt() else "#20673AB7".toColorInt()

            val density = view.context.resources.displayMetrics.density
            val pillRadius = 100f * density

            view.background = null

            val mainPill = view.findViewById<LinearLayout>(R.id.main_nav_pill)
            val sideBadge = view.findViewById<FrameLayout>(R.id.tab_profile)

            val mainBarDrawable = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(purpleGradientTop, barBgColor)
            ).apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = pillRadius
                setStroke((1.5f * density).toInt(), barBorderColor)
            }
            mainPill.background = mainBarDrawable

            sideBadge.background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(purpleGradientTop, barBgColor)
            ).apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = pillRadius
                setStroke((1.5f * density).toInt(), barBorderColor)
            }

            val selectionPillDrawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = pillRadius
                setColor(activePillColor)
            }

            // textId is nullable
            fun updateTab(tabId: String, bgId: Int, iconId: Int, textId: Int?) {
                val isSelected = currentRoute == tabId

                val bgView = view.findViewById<View>(bgId)
                bgView.background = selectionPillDrawable
                bgView.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE

                view.findViewById<ImageView>(iconId).setColorFilter(if (isSelected) purpleColor else unselectedColor)

                // Only update text if textId is provided
                if (textId != null) {
                    val textView = view.findViewById<TextView>(textId)
                    textView.setTextColor(if (isSelected) purpleColor else unselectedColor)
                    textView.setTypeface(null, if (isSelected) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
                }
            }

            updateTab(TAB_HOME, R.id.bg_home, R.id.icon_home, R.id.text_home)
            updateTab(TAB_EXPLORE, R.id.bg_explore, R.id.icon_explore, R.id.text_explore)
            updateTab(TAB_IDEAS, R.id.bg_ideas, R.id.icon_ideas, R.id.text_ideas)

            // Profile tab logic
            updateTab(TAB_PROFILE, R.id.bg_profile, R.id.icon_profile, null)
        }
    )
}