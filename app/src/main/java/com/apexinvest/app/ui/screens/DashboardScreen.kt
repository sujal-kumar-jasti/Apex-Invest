package com.apexinvest.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apexinvest.app.ui.components.ActionPill
import com.apexinvest.app.ui.components.AppTickerText
import com.apexinvest.app.ui.components.CommonScreenHeader
import com.apexinvest.app.ui.components.CommonStockRow
import com.apexinvest.app.ui.components.PremiumLineChart
import com.apexinvest.app.ui.components.UserProfileHeader
import com.apexinvest.app.ui.components.glassCard
import com.apexinvest.app.ui.navigation.Screen
import com.apexinvest.app.ui.theme.BrandPurple
import com.apexinvest.app.ui.theme.LocalAppColors
import com.apexinvest.app.util.getConvertedValue
import com.apexinvest.app.util.getCurrencySymbol
import com.apexinvest.app.util.guessCurrencyFromSymbol
import com.apexinvest.app.util.toCleanString
import com.apexinvest.app.viewmodel.AuthViewModel
import com.apexinvest.app.viewmodel.PortfolioStats
import com.apexinvest.app.viewmodel.PortfolioViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

// Static constants hoisted out of composition scopes to preserve memory allocation
private val DashboardCardShape = RoundedCornerShape(32.dp)
private val DummyRowShape = RoundedCornerShape(4.dp)
private val EmptyContainerShape = RoundedCornerShape(24.dp)

@Composable
fun rememberShimmerAlpha(): Float {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "alpha"
    )
    return alpha
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    authViewModel: AuthViewModel,
    portfolioViewModel: PortfolioViewModel,
    onNavigate: (String) -> Unit,
    isConnected: Boolean
) {
    val userEmail = remember(authViewModel) { authViewModel.getUserEmail() ?: "Trader" }
    val uiState by portfolioViewModel.uiState.collectAsStateWithLifecycle()
    val notificationsEnabled by portfolioViewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val portfolioList by portfolioViewModel.portfolioStocks.collectAsStateWithLifecycle()
    val portfolioStats by portfolioViewModel.portfolioStats.collectAsStateWithLifecycle()
    val topStocks by portfolioViewModel.topSortedStocks.collectAsStateWithLifecycle()
    val sparklineCache by portfolioViewModel.sparklineCache.collectAsStateWithLifecycle()

    var isManualRefreshing by rememberSaveable{ mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // 🌟 DEFERRED RENDERING: post-navigation interface layout generation
    var canRenderHeavyList by rememberSaveable{ mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!canRenderHeavyList) {
            delay(350)
            canRenderHeavyList = true
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, isConnected) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && isConnected) {
                scope.launch {
                    delay(400) // 🌟 LIFECYCLE DEBOUNCE: Prevents loading spikes from choking graphics engines
                    portfolioViewModel.forceRefresh()
                    portfolioViewModel.startPeriodicUpdates()
                }
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                portfolioViewModel.stopPeriodicUpdates()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            portfolioViewModel.stopPeriodicUpdates()
        }
    }

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val meshBrush = remember(isDark) {
        Brush.verticalGradient(listOf(BrandPurple.copy(alpha = if (isDark) 0.12f else 0.05f), Color.Transparent))
    }
    val currencySymbol = remember(uiState.isUsd) { getCurrencySymbol(if (uiState.isUsd) "USD" else "INR") }
    val pullRefreshState = rememberPullToRefreshState()
    val showShimmer = !uiState.isHydrated || (portfolioList.isEmpty() && uiState.isLoading)

    Column(modifier = Modifier.fillMaxSize().background(meshBrush)) {
        // Render upper header structure instantly
        // 🚀 FIX: Skip statusBarsPadding if banner is already handling it
        CommonScreenHeader(
            applyStatusBarsPadding = isConnected,
            leadingContent = { UserProfileHeader(email = userEmail) { onNavigate(Screen.Profile.route) } },
            trailingContent = {
                IconButton(
                    onClick = { portfolioViewModel.toggleCurrency() },
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), CircleShape)
                ) { Text(text = currencySymbol, fontWeight = FontWeight.Black, color = BrandPurple) }
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { onNavigate(Screen.Notifications.route) }) {
                    Icon(
                        imageVector = if (notificationsEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                        contentDescription = null,
                        tint = if (notificationsEnabled) BrandPurple else Color.Gray
                    )
                }
            }
        )

        if (canRenderHeavyList) {
            PullToRefreshBox(
                isRefreshing = isManualRefreshing,
                onRefresh = {
                    scope.launch {
                        isManualRefreshing = true
                        try { portfolioViewModel.loadPortfolioAndPrices(); delay(500.milliseconds) }
                        finally { isManualRefreshing = false }
                    }
                },
                state = pullRefreshState,
                modifier = Modifier.fillMaxSize(),
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        modifier = Modifier.align(Alignment.TopCenter),
                        isRefreshing = isManualRefreshing,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        color = BrandPurple,
                        state = pullRefreshState
                    )
                }
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(top = 4.dp, bottom = 140.dp)) {
                    // 1. Portfolio Net Worth Element
                    item {
                        if (showShimmer) {
                            Box(modifier = Modifier.fillMaxWidth().height(220.dp).padding(horizontal = 20.dp).clip(DashboardCardShape).shimmerEffect())
                        } else {
                            portfolioStats?.let { stats ->
                                UltraGlassPortfolioCard(stats = stats, currency = currencySymbol, isDark = isDark, onClick = { onNavigate(Screen.Analytics.route) })
                            } ?: Box(modifier = Modifier.fillMaxWidth().height(190.dp).padding(horizontal = 20.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), DashboardCardShape))
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    // 2. Control Layout Grid
                    item {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            ActionPill(Icons.Default.Visibility, "Watchlist", Color(0xFF1976D2), onClick = { onNavigate(Screen.Watchlist.route) }, isDark = isDark)
                            ActionPill(Icons.Default.PieChart, "Analytics", BrandPurple, onClick = { onNavigate(Screen.Analytics.route) }, isDark = isDark)
                            ActionPill(Icons.Default.Analytics, "Predictions", Color(0xFF388E3C), onClick = { onNavigate(Screen.Predictions.route) }, isDark = isDark)
                            ActionPill(Icons.Default.Lightbulb, "AI Insights", Color(0xFFF57C00), onClick = { onNavigate(Screen.InvestmentIdeas.route) }, isDark = isDark)
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    // 3. Section Divider Heading
                    item {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Holdings map", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp)
                            TextButton(onClick = { onNavigate(Screen.Portfolio.route) }) {
                                Text("Manage", fontWeight = FontWeight.Bold, color = BrandPurple)
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = BrandPurple)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }

                    // 4. Financial Record Matrix
                    if (showShimmer) {
                        items(5) {
                            val alpha = rememberShimmerAlpha()
                            val baseColor = MaterialTheme.colorScheme.onSurface
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(baseColor.copy(alpha = alpha)))
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Box(modifier = Modifier.width(80.dp).height(16.dp).clip(DummyRowShape).background(baseColor.copy(alpha = alpha)))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(modifier = Modifier.width(120.dp).height(12.dp).clip(DummyRowShape).background(baseColor.copy(alpha = alpha)))
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(horizontalAlignment = Alignment.End) {
                                    Box(modifier = Modifier.width(60.dp).height(16.dp).clip(DummyRowShape).background(baseColor.copy(alpha = alpha)))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(modifier = Modifier.width(40.dp).height(12.dp).clip(DummyRowShape).background(baseColor.copy(alpha = alpha)))
                                }
                            }
                        }
                    } else if (portfolioList.isEmpty()) {
                        item { EmptyDashboardState() }
                    } else {
                        items(topStocks, key = { it.symbol }) { stock ->
                            // 🌟 O(1) MEMOIZATION: Locks metadata calculations per unique record key
                            val companyName = remember(stock.symbol) { portfolioViewModel.getCompanyNameForSymbol(stock.symbol) }
                            val passCurrency = remember(stock.symbol) { guessCurrencyFromSymbol(stock.symbol) }
                            val historyData = sparklineCache[stock.symbol] ?: emptyList()
                            val convertedPrice = remember(stock.currentPrice, uiState.isUsd, uiState.rates) {
                                getConvertedValue(stock.currentPrice, stock.symbol, uiState.isUsd, uiState.rates)
                            }

                            Box(Modifier.animateItem().padding(horizontal = 20.dp, vertical = 6.dp)) {
                                CommonStockRow(
                                    symbol = stock.symbol,
                                    companyName = companyName,
                                    price = convertedPrice,
                                    percentChange = stock.changePercent,
                                    isUsd = uiState.isUsd,
                                    historyData = historyData,
                                    isDark = isDark,
                                    quantity = stock.quantity
                                ) {
                                    onNavigate(Screen.StockDetail.createRoute(stock.symbol, passCurrency))
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Placeholder while animating to shield UI thread resources
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
fun UltraGlassPortfolioCard(stats: PortfolioStats, currency: String, isDark: Boolean, onClick: () -> Unit) {
    val isProfit = stats.isPositive
    val appColors = LocalAppColors.current
    val accentColor = if (isProfit) appColors.trendGreen else appColors.trendRed

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(220.dp)
            .shadow(24.dp, DashboardCardShape, spotColor = BrandPurple.copy(alpha = 0.15f))
            .glassCard(isDark, DashboardCardShape)
            .clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(top = 65.dp, bottom = 12.dp, start = 2.dp, end = 8.dp)) {
            AnimatedVisibility(visible = stats.chartData.isNotEmpty(), enter = fadeIn(animationSpec = tween(400))) {
                PremiumLineChart(dataPoints = stats.chartData, isPositive = isProfit, modifier = Modifier.fillMaxSize(), strokeColor = accentColor, fillColor = accentColor)
            }
        }

        Column(Modifier.fillMaxWidth().wrapContentHeight().padding(start = 20.dp, end = 20.dp, top = 16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "TOTAL NET WORTH", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Row(verticalAlignment = Alignment.Bottom) {
                        AppTickerText(value = stats.totalValue, currencySymbol = currency, textStyle = androidx.compose.ui.text.TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface))
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = accentColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(50),
                            border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.2f)),
                            modifier = Modifier.padding(bottom = 5.dp)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = if (isProfit) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown, contentDescription = null, tint = accentColor, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(4.dp))
                                AppTickerText(
                                    value = stats.totalGain, currencySymbol = currency, showExplicitSign = true, suffix = " (${stats.totalPercent.toCleanString()}%)",
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accentColor)
                                )
                            }
                        }
                    }
                }
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = BrandPurple.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun EmptyDashboardState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .height(140.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.3f), EmptyContainerShape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f), EmptyContainerShape),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.PieChart, null, tint = BrandPurple.copy(alpha = 0.5f), modifier = Modifier.size(36.dp))
            Spacer(Modifier.height(12.dp))
            Text("No active holdings found.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
        }
    }
}