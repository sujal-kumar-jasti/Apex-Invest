package com.apexinvest.app.ui.screens

<<<<<<< HEAD
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
=======
import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
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
<<<<<<< HEAD
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
=======
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apexinvest.app.data.StockEntity
import com.apexinvest.app.ui.components.TradeEntrySheet
import com.apexinvest.app.ui.navigation.Screen
import com.apexinvest.app.util.getConvertedValue
import com.apexinvest.app.util.toCleanString
import com.apexinvest.app.viewmodel.PortfolioViewModel
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
@Composable
fun PortfolioScreen(
    portfolioViewModel: PortfolioViewModel,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
<<<<<<< HEAD
    initialStockToBuy: String? = null,
    isConnected: Boolean
) {
    BackHandler { onBack() }

    val state by portfolioViewModel.uiState.collectAsStateWithLifecycle()
    val searchResults by portfolioViewModel.searchResults.collectAsStateWithLifecycle()
    val portfolioList by portfolioViewModel.portfolioStocks.collectAsStateWithLifecycle()
    val sparklineCache by portfolioViewModel.sparklineCache.collectAsStateWithLifecycle()

    var isManualRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf(initialStockToBuy ?: "") }
    var showTradeSheet by remember { mutableStateOf(initialStockToBuy != null) }
    var canRenderHeavyList by rememberSaveable{ mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!canRenderHeavyList) {
            delay(350)
            canRenderHeavyList = true
        }
    }

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val meshBrush = remember(isDark) { Brush.verticalGradient(listOf(BrandPurple.copy(alpha = if (isDark) 0.12f else 0.05f), Color.Transparent)) }
    val pullRefreshState = rememberPullToRefreshState()

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, isConnected) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && isConnected) {
                scope.launch {
                    delay(400)
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

    var headerStats by remember { mutableStateOf<HeaderStats?>(null) }
    LaunchedEffect(portfolioList, state.isUsd, state.liveRate) {
        headerStats = withContext(Dispatchers.Default) {
            val totalVal = portfolioList.sumOf { getConvertedValue(it.currentPrice * it.quantity, it.symbol, state.isUsd, state.rates) }
            val investedVal = portfolioList.sumOf { getConvertedValue(it.buyPrice * it.quantity, it.symbol, state.isUsd, state.rates) }
            val totalPrevVal = portfolioList.sumOf {
                val p = if (it.previousClose > 0.0) it.previousClose else it.currentPrice
                getConvertedValue(p * it.quantity, it.symbol, state.isUsd, state.rates)
            }
            HeaderStats(totalVal, investedVal, totalVal - totalPrevVal)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(meshBrush)) {
        headerStats?.let { stats ->
            GlassWalletHeaderCard(
                totalVal = stats.totalVal,
                investedVal = stats.investedVal,
                dailyGain = stats.dailyGain,
                isUsd = state.isUsd,
                isDark = isDark,
                isConnected = isConnected,
                onCurrencyToggle = { portfolioViewModel.toggleCurrency() },
                onAddClick = { portfolioViewModel.clearSearchResults(); searchQuery = ""; showTradeSheet = true },
                onHistoryClick = { onNavigate(Screen.TransactionHistory.route) }
            )
        } ?: Box(modifier = Modifier.fillMaxWidth().height(220.dp).padding(16.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(32.dp)))

        Text("HOLDINGS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp))

        // Only draw the heavy list if the animation is finished
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
                        item { EmptyPortfolioView() }
                    } else {
                        items(portfolioList, key = { it.symbol }) { stock ->
                            // 🌟 OPTIMIZATION: Memoize string operations inside the list so they don't block scroll passes
                            val companyName = remember(stock.symbol) { portfolioViewModel.getCompanyNameForSymbol(stock.symbol) }
                            val passCurrency = remember(stock.symbol) { guessCurrencyFromSymbol(stock.symbol) }
                            val historyData = sparklineCache[stock.symbol] ?: emptyList()
                            val convertedPrice = remember(stock.currentPrice, state.isUsd, state.rates) { getConvertedValue(stock.currentPrice, stock.symbol, state.isUsd, state.rates) }

                            Box(Modifier.animateItem().padding(horizontal = 16.dp, vertical = 6.dp)) {
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
            // Placeholder while animating
            Spacer(Modifier.weight(1f))
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
=======
    initialStockToBuy: String? = null, // New Parameter for AI redirection
    isConnected: Boolean
) {
    val state by portfolioViewModel.uiState.collectAsState()
    val searchResults by portfolioViewModel.searchResults.collectAsState()

    // 1. Logic to handle Auto-Opening the Trade Sheet
    var searchQuery by remember { mutableStateOf(initialStockToBuy ?: "") }
    var showTradeSheet by remember { mutableStateOf(initialStockToBuy != null) }

    val user = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current

    val onLinkDematClick: () -> Unit = {
        user?.uid?.let { uid ->
            val backendUrl = "https://jsujalkumar7899-prognosai-fastapi-backend-1.hf.space/api/v1/aa/login?uid=$uid"
            try {
                CustomTabsIntent.Builder().setShowTitle(true).build().launchUrl(context, Uri.parse(backendUrl))
            } catch (e: Exception) {
                Toast.makeText(context, "Browser not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(

        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    portfolioViewModel.clearSearchResults()
                    searchQuery = "" // Reset query for manual add
                    showTradeSheet = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, "Add Trade") },
                text = { Text("Add Trade", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .background(MaterialTheme.colorScheme.background)
        ) {
            // --- HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "My Portfolio",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Currency Toggle
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clickable { portfolioViewModel.toggleCurrency() }
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text = if (state.isUsd) "USD" else "INR",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Sync
                    IconButton(onClick = { portfolioViewModel.loadPortfolioAndPrices() }) {
                        if (state.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Sync, "Sync")
                        }
                    }

                    // Broker Link
                    IconButton(onClick = onLinkDematClick) {
                        Icon(Icons.Default.AccountBalance, "Link Demat", tint = MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            // --- SUMMARY CARD ---
            if (state.portfolio.isNotEmpty()) {
                PortfolioSummaryHeader(state.portfolio, state.isUsd, state.liveRate)
            }

            // --- HOLDINGS LIST ---
            if (state.portfolio.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Your portfolio is empty.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = state.portfolio, key = { it.symbol }) { stock ->
                        PortfolioItemCardClean(
                            stock = stock,
                            isUsd = state.isUsd,
                            liveRate = state.liveRate,
                            onDelete = {
                                portfolioViewModel.executeTrade(stock.symbol, false, stock.quantity.toString(), stock.currentPrice.toString(), LocalDate.now().toString())
                            },
                            onClick = { onNavigate(Screen.StockDetail.createRoute(stock.symbol)) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }

        // --- TRADE SHEET ---
        if (showTradeSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showTradeSheet = false
                    searchQuery = "" // Reset on close
                },
                containerColor = MaterialTheme.colorScheme.surface,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                TradeEntrySheet(
                    onDismiss = { showTradeSheet = false },
                    portfolioStocks = state.portfolio.map { it.symbol },
                    searchResults = searchResults,
                    // Pass the query (empty for manual, pre-filled for AI redirect)
                    initialQuery = searchQuery,
                    onSearch = { query ->
                        searchQuery = query
                        portfolioViewModel.searchStocks(query)
                    },
                    onConfirm = { symbol, isBuy, qty, price, date ->
                        portfolioViewModel.executeTrade(symbol, isBuy, qty, price, date)
                        showTradeSheet = false
                    }
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
                )
            }
        }
    }
}
<<<<<<< HEAD
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
    val totalGainPct = if (investedVal > 0) (totalGain / investedVal) * 100 else 0.0
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
            .padding(horizontal = 16.dp, vertical = 10.dp)
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
=======

@Composable
fun PortfolioSummaryHeader(portfolio: List<StockEntity>, isUsd: Boolean, liveRate: Double) {
    val currentVal = portfolio.sumOf { getConvertedValue(it.currentPrice * it.quantity, it.symbol, isUsd, liveRate) }
    val investedVal = portfolio.sumOf { getConvertedValue(it.buyPrice * it.quantity, it.symbol, isUsd, liveRate) }
    val totalGain = currentVal - investedVal
    val totalGainPct = if (investedVal > 0) (totalGain / investedVal) * 100 else 0.0
    val currency = if (isUsd) "$" else "₹"
    val isProfit = totalGain >= 0
    val color = if (isProfit) Color(0xFF00C853) else Color(0xFFD32F2F)

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Current Value", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Text("$currency${currentVal.toCleanString()}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Total Returns", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Text(
                    "${if (isProfit) "+" else ""}${currency}${totalGain.toCleanString()} (${totalGainPct.toCleanString()}%)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

// --- NEW "CLEAN & PRO" CARD DESIGN ---
@Composable
fun PortfolioItemCardClean(
    stock: StockEntity,
    isUsd: Boolean,
    liveRate: Double,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val currency = if (isUsd) "$" else "₹"

    // Values
    val totalValue = getConvertedValue(stock.currentPrice * stock.quantity, stock.symbol, isUsd, liveRate)
    val avgPrice = getConvertedValue(stock.buyPrice, stock.symbol, isUsd, liveRate)
    val ltp = getConvertedValue(stock.currentPrice, stock.symbol, isUsd, liveRate)

    // Profit Logic
    val invested = stock.buyPrice * stock.quantity
    val currentRaw = stock.currentPrice * stock.quantity
    val gainRaw = currentRaw - invested
    val gainPct = if (invested > 0) (gainRaw / invested) * 100 else 0.0
    val isProfit = gainPct >= 0
    val color = if (isProfit) Color(0xFF00C853) else Color(0xFFD32F2F)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- ICON ---
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stock.symbol.take(1),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.width(16.dp))

            // --- DATA GRID ---
            Column(modifier = Modifier.weight(1f)) {
                // ROW 1: Symbol & Total Value
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stock.symbol,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$currency${totalValue.toCleanString()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(4.dp))

                // ROW 2: Qty/Avg & LTP/Percent
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${stock.quantity} @ $currency${avgPrice.toCleanString()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$currency${ltp.toCleanString()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "(${if (isProfit) "+" else ""}${gainPct.toCleanString()}%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = color,
                            fontWeight = FontWeight.Bold
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
                        )
                    }
                }
            }
<<<<<<< HEAD
        }
    }
}

@Composable
fun EmptyPortfolioView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .height(140.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.AccountBalanceWallet, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(36.dp))
            Spacer(Modifier.height(12.dp))
            Text("No active holdings found.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
        }
    }
}

data class HeaderStats(
    val totalVal: Double,
    val investedVal: Double,
    val dailyGain: Double
)
=======

            Spacer(Modifier.width(12.dp))

            // --- DELETE BUTTON ---
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Sell All",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
