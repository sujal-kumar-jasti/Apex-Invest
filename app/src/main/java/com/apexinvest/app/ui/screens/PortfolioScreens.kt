package com.apexinvest.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.apexinvest.app.ui.components.AppTickerText
import com.apexinvest.app.ui.components.CommonStockRow
import com.apexinvest.app.ui.components.TradeEntrySheet
import com.apexinvest.app.ui.components.glassCard
import com.apexinvest.app.ui.navigation.Screen
import com.apexinvest.app.ui.theme.BrandPurple
import com.apexinvest.app.ui.theme.LocalAppColors
import com.apexinvest.app.util.getConvertedValue
import com.apexinvest.app.util.getCurrencySymbol
import com.apexinvest.app.util.guessCurrencyFromSymbol
import com.apexinvest.app.util.toCleanString
import com.apexinvest.app.viewmodel.PortfolioViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PortfolioScreen(
    portfolioViewModel: PortfolioViewModel,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    initialStockToBuy: String? = null,
    action: String? = null, // 🚀 ADDED
    isConnected: Boolean
) {
    BackHandler { onBack() }

    val state by portfolioViewModel.uiState.collectAsStateWithLifecycle()
    val portfolioStats by portfolioViewModel.portfolioStats.collectAsStateWithLifecycle()
    val searchResults by portfolioViewModel.searchResults.collectAsStateWithLifecycle()
    val portfolioList by portfolioViewModel.portfolioStocks.collectAsStateWithLifecycle()
    val sparklineCache by portfolioViewModel.sparklineCache.collectAsStateWithLifecycle()

    var isManualRefreshing by remember { mutableStateOf(false) }
    var canRenderHeavyList by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf(initialStockToBuy ?: "") }
    var showTradeSheet by remember { mutableStateOf(false) }

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val meshBrush = remember(isDark) { Brush.verticalGradient(listOf(BrandPurple.copy(alpha = if (isDark) 0.12f else 0.05f), Color.Transparent)) }
    val pullRefreshState = rememberPullToRefreshState()

    // 🚀 FIX: Deferred rendering to prevent navigation jank (matching Ideas screen logic)
    LaunchedEffect(Unit) {
        if (!canRenderHeavyList) {
            delay(350.milliseconds)
            canRenderHeavyList = true
        }
        if (initialStockToBuy != null || action == "OPEN_TRADE") {
            showTradeSheet = true
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, isConnected) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && isConnected) {
                scope.launch {
                    delay(400.milliseconds)
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

    Box(modifier = Modifier.fillMaxSize().background(meshBrush)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // 🚀 USE VIEWMODEL STATS DIRECTLY TO PREVENT POP-IN JANK
            portfolioStats?.let { stats ->
                GlassWalletHeaderCard(
                    totalVal = stats.totalValue,
                    investedVal = stats.totalInvested,
                    dailyGain = stats.dailyGain,
                    isUsd = state.isUsd,
                    isDark = isDark,
                    isConnected = isConnected,
                    onCurrencyToggle = { portfolioViewModel.toggleCurrency() },
                    onAddClick = { portfolioViewModel.clearSearchResults(); searchQuery = ""; showTradeSheet = true },
                    onHistoryClick = { onNavigate(Screen.TransactionHistory.route) }
                )
            } ?: Box(modifier = Modifier.fillMaxWidth().statusBarsPadding().height(220.dp).padding(horizontal = 20.dp, vertical = 10.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(32.dp)))

            // 🚀 RENDER HOLDINGS TITLE IMMEDIATELY
            Text("HOLDINGS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp))

            if (canRenderHeavyList) {
                PullToRefreshBox(
                    isRefreshing = isManualRefreshing,
                    onRefresh = { scope.launch { isManualRefreshing = true; try { portfolioViewModel.loadPortfolioAndPrices(); delay(500.milliseconds) } finally { isManualRefreshing = false } } },
                    state = pullRefreshState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    indicator = { PullToRefreshDefaults.Indicator(modifier = Modifier.align(Alignment.TopCenter), isRefreshing = isManualRefreshing, containerColor = MaterialTheme.colorScheme.surfaceContainerHigh, color = BrandPurple, state = pullRefreshState) }
                ) {
                    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(top = 0.dp, bottom = 140.dp)) {
                        if (portfolioList.isEmpty() && !state.isLoading) {
                            item { 
                                EmptyPortfolioView(
                                    onAddClick = { 
                                        portfolioViewModel.clearSearchResults()
                                        searchQuery = ""
                                        showTradeSheet = true 
                                    }
                                ) 
                            }
                        } else {
                            items(portfolioList, key = { it.symbol }) { stock ->
                                val companyName = remember(stock.symbol) { portfolioViewModel.getCompanyNameForSymbol(stock.symbol) }
                                val passCurrency = remember(stock.symbol) { guessCurrencyFromSymbol(stock.symbol) }
                                val historyData = sparklineCache[stock.symbol]?.map { it.close } ?: emptyList()
                                val convertedPrice = remember(stock.currentPrice, state.isUsd, state.rates) { getConvertedValue(stock.currentPrice, stock.symbol, state.isUsd, state.rates) }

                                Box(Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                                    CommonStockRow(
                                        symbol = stock.symbol, companyName = companyName, price = convertedPrice, percentChange = stock.changePercent,
                                        isUsd = state.isUsd, historyData = historyData, isDark = isDark, quantity = stock.quantity,
                                        onDelete = { portfolioViewModel.executeTrade(stock.symbol, false, stock.quantity.toString(), stock.currentPrice.toString(), LocalDate.now().toString()) }
                                    ) { onNavigate(Screen.StockDetail.createRoute(stock.symbol, passCurrency)) }
                                }
                            }
                        }
                    }
                }
            } else {
                // Smooth shimmer placeholder during transition
                Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
                    repeat(5) {
                        val alpha = com.apexinvest.app.ui.components.rememberShimmerAlpha()
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(85.dp)
                                .padding(vertical = 6.dp)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha), RoundedCornerShape(24.dp))
                        )
                    }
                }
            }
        }

        if (showTradeSheet) {
            ModalBottomSheet(
                onDismissRequest = { showTradeSheet = false; searchQuery = "" },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                TradeEntrySheet(
                    onDismiss = { showTradeSheet = false },
                    portfolioStocks = portfolioList.map { it.symbol }, searchResults = searchResults, initialQuery = searchQuery,
                    onSearch = { query -> searchQuery = query; portfolioViewModel.searchStocks(query) },
                    onConfirm = { symbol, isBuy, qty, price, date -> portfolioViewModel.executeTrade(symbol, isBuy, qty, price, date); showTradeSheet = false }
                )
            }
        }
    }
}

@Composable
fun GlassWalletHeaderCard(
    totalVal: Double,
    investedVal: Double,
    dailyGain: Double,
    isUsd: Boolean,
    isDark: Boolean,
    isConnected: Boolean,
    onCurrencyToggle: () -> Unit,
    onAddClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    val totalGain = totalVal - investedVal
    val isPos = totalGain >= 0

    val isDailyPos = dailyGain >= 0
    val dailyPct = if (totalVal - dailyGain > 0) (dailyGain / (totalVal - dailyGain)) * 100 else 0.0

    val sym = getCurrencySymbol(if (isUsd) "USD" else "INR")
    val appColors = LocalAppColors.current
    val accentColor = if (isPos) appColors.trendGreen else appColors.trendRed
    val dailyAccentColor = if (isDailyPos) appColors.trendGreen else appColors.trendRed

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isConnected) Modifier.statusBarsPadding() else Modifier)
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .shadow(if (isDark) 12.dp else 4.dp, RoundedCornerShape(32.dp), spotColor = BrandPurple.copy(alpha = 0.15f))
            .glassCard(isDark, RoundedCornerShape(32.dp))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("NET WORTH", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.2.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(onClick = onHistoryClick, shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), modifier = Modifier.size(36.dp)) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.History, "History", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)) }
                    }
                    Spacer(Modifier.width(8.dp))
                    Surface(onClick = onCurrencyToggle, shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                        Text(if (isUsd) "USD" else "INR", Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontSize = 11.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(Modifier.width(8.dp))
                    FilledTonalButton(onClick = onAddClick, modifier = Modifier.height(36.dp), contentPadding = PaddingValues(horizontal = 14.dp)) {
                        Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Trade", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            AppTickerText(value = totalVal, currencySymbol = sym, textStyle = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface))
            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Unrealized P&L", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    AppTickerText(
                        value = totalGain,
                        currencySymbol = sym,
                        showExplicitSign = true,
                        textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = accentColor)
                    )
                }

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("Today's P&L", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (isDailyPos) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown, null, tint = dailyAccentColor, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        AppTickerText(
                            value = dailyGain,
                            currencySymbol = sym,
                            showExplicitSign = true,
                            suffix = " (${dailyPct.toCleanString()}%)",
                            textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = dailyAccentColor)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyPortfolioView(onAddClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .height(180.dp) // 🚀 Slightly taller for button
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
            Icon(Icons.Default.AccountBalanceWallet, null, tint = BrandPurple.copy(alpha = 0.5f), modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(12.dp))
            Text("No active holdings found.", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("Start building your wealth today.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(16.dp))
            androidx.compose.material3.Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = BrandPurple)
            ) {
                Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Add Your First Trade", fontWeight = FontWeight.Bold)
            }
        }
    }
}
