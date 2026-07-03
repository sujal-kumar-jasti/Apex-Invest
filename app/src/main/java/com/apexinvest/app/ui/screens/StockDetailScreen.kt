package com.apexinvest.app.ui.screens

<<<<<<< HEAD
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
=======
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
<<<<<<< HEAD
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
=======
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
<<<<<<< HEAD
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apexinvest.app.api.models.CandlePoint
import com.apexinvest.app.api.models.MarketPricing
import com.apexinvest.app.api.models.StockDetailsResponse
import com.apexinvest.app.data.StockEntity
import com.apexinvest.app.ui.components.TabLoadingPlaceholder
import com.apexinvest.app.ui.components.glassCard
import com.apexinvest.app.ui.screens.stockdetail.components.AiApexInvestCard
import com.apexinvest.app.ui.screens.stockdetail.components.ChartType
import com.apexinvest.app.ui.screens.stockdetail.components.ChartTypeToggle
import com.apexinvest.app.ui.screens.stockdetail.components.DomainTabs
import com.apexinvest.app.ui.screens.stockdetail.components.EfficiencyRiskPane
import com.apexinvest.app.ui.screens.stockdetail.components.GlassShimmer
import com.apexinvest.app.ui.screens.stockdetail.components.GlassTopBar
import com.apexinvest.app.ui.screens.stockdetail.components.MomentumPane
import com.apexinvest.app.ui.screens.stockdetail.components.NewsAndPeersPane
import com.apexinvest.app.ui.screens.stockdetail.components.OptimizedGlassChart
import com.apexinvest.app.ui.screens.stockdetail.components.OverviewPane
import com.apexinvest.app.ui.screens.stockdetail.components.PriceHeroSection
import com.apexinvest.app.ui.screens.stockdetail.components.QuickMetricsStrip
import com.apexinvest.app.ui.screens.stockdetail.components.ScorecardPane
import com.apexinvest.app.ui.screens.stockdetail.components.TechnicalsPane
import com.apexinvest.app.ui.screens.stockdetail.components.TimeframePills
import com.apexinvest.app.ui.screens.stockdetail.components.TradeActionBar
import com.apexinvest.app.ui.theme.BrandPurple
import com.apexinvest.app.util.StockMetadataUtils
import com.apexinvest.app.util.getCurrencySymbol
import com.apexinvest.app.viewmodel.FinancialsUiState
import com.apexinvest.app.viewmodel.PortfolioViewModel
import com.apexinvest.app.viewmodel.StockDetailState
import com.apexinvest.app.viewmodel.StockDetailViewModel
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailScreen(
    symbol: String,
    passedCurrency: String,
    portfolioViewModel: PortfolioViewModel,
    stockDetailViewModel: StockDetailViewModel,
    onBack: () -> Unit,
    onNavigateToStock: (String) -> Unit,
    isConnected: Boolean
) {
    BackHandler { onBack() }

    val detailState by stockDetailViewModel.stockDetailState.collectAsStateWithLifecycle()
    val financialsState by stockDetailViewModel.financialsState.collectAsStateWithLifecycle()
    val livePricing by stockDetailViewModel.livePricing.collectAsStateWithLifecycle()

    val watchlist by portfolioViewModel.watchlistStocks.collectAsStateWithLifecycle()
    val isFollowing = remember(watchlist) { watchlist.any { it.symbol == symbol } }

    val currentHolding by stockDetailViewModel.currentStockHolding.collectAsStateWithLifecycle()
    val marketStatus by stockDetailViewModel.marketStatus.collectAsStateWithLifecycle()
    val canSell by stockDetailViewModel.canSell.collectAsStateWithLifecycle()
    val tradeMessage by stockDetailViewModel.tradeStatusMessage.collectAsStateWithLifecycle()

    // 🚀 NEW: Watch for the global UI message banner
    val uiMessage by portfolioViewModel.uiMessage.collectAsStateWithLifecycle()

    LaunchedEffect(tradeMessage) {
        tradeMessage?.let {
            if (!it.contains("Error") && !it.contains("Offline") && !it.contains("stale")) {
                portfolioViewModel.showMessage(it, com.apexinvest.app.ui.components.MessageType.SUCCESS)
            } else {
                portfolioViewModel.showMessage(it, com.apexinvest.app.ui.components.MessageType.ERROR)
            }
            stockDetailViewModel.clearTradeMessage()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val exchangeInfo = remember(symbol) { StockMetadataUtils.getExchangeInfo(symbol) }
    val nativeCurrencyCode = remember(passedCurrency, exchangeInfo) {
        if (passedCurrency.isNotBlank() && passedCurrency != "USD" && passedCurrency != "null") passedCurrency
        else exchangeInfo.currency
    }

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !isDark
        }
    }

    val meshBrush = remember(isDark) {
        Brush.verticalGradient(
            listOf(BrandPurple.copy(alpha = if (isDark) 0.12f else 0.05f), Color.Transparent)
        )
    }

    var isUiReady by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(400.milliseconds)
        isUiReady = true
    }

    var latchedData by remember {
        mutableStateOf(
            StockDetailsResponse(
                ticker = symbol, companyName = "Loading...", sector = null, industry = null, assetType = null, logoId = null, employeeCount = null, country = null,
                marketPricing = MarketPricing(
                    priceLast = 0.0, priceOpen = 0.0, priceHigh = 0.0, priceLow = 0.0,
                    changePct1D = 0.0, changeAbsolute1D = 0.0, high52Week = 0.0, low52Week = 0.0,
                    volumeCurrent = 0.0, tradedValueDaily = 0.0, avgVolume10D = 0.0, avgVolume30D = 0.0,
                    avgVolume90D = 0.0, relativeVolume10D = 0.0, beta1Y = 0.0, beta5Y = 0.0
                )
            )
        )
    }

    var currentLiveCandles by remember { mutableStateOf<List<CandlePoint>>(emptyList()) }
    var isChartLoading by remember { mutableStateOf(true) }
    var currentRange by remember { mutableStateOf("1D") }
    var loadingRange by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(detailState) {
        if (detailState is StockDetailState.Success) {
            val state = detailState as StockDetailState.Success
            val newData = state.data
            val isRef = state.isRefreshing

            isChartLoading = isRef && currentLiveCandles.isEmpty()
            
            // If we are no longer refreshing, clear the loading lock for the range
            if (!isRef) {
                loadingRange = null
            }

            if (newData.candles.isNotEmpty()) {
                currentLiveCandles = newData.candles
            }

            if (newData.ticker == symbol) {
                val oldPricing = latchedData.marketPricing
                val newPricing = newData.marketPricing

                val mergedPricing = if (newPricing != null) {
                    oldPricing?.copy(
                        priceLast = newPricing.priceLast?.let { if (it > 0.0) newPricing.priceLast else oldPricing.priceLast },
                        changeAbsolute1D = newPricing.priceLast?.let { if (it > 0.0) newPricing.changeAbsolute1D else oldPricing.changeAbsolute1D } 
                            ?: oldPricing.changeAbsolute1D,
                        changePct1D = newPricing.priceLast?.let { if (it > 0.0) newPricing.changePct1D else oldPricing.changePct1D }
                            ?: oldPricing.changePct1D,
                        priceOpen = newPricing.priceOpen?.let { if (it > 0.0) newPricing.priceOpen else oldPricing.priceOpen },
                        priceHigh = newPricing.priceHigh?.let { if (it > 0.0) newPricing.priceHigh else oldPricing.priceHigh },
                        priceLow = newPricing.priceLow?.let { if (it > 0.0) newPricing.priceLow else oldPricing.priceLow },
                        high52Week = newPricing.high52Week ?: oldPricing.high52Week,
                        low52Week = newPricing.low52Week ?: oldPricing.low52Week,
                        volumeCurrent = newPricing.volumeCurrent ?: oldPricing.volumeCurrent,
                        tradedValueDaily = newPricing.tradedValueDaily ?: oldPricing.tradedValueDaily,
                        avgVolume10D = newPricing.avgVolume10D ?: oldPricing.avgVolume10D,
                        avgVolume30D = newPricing.avgVolume30D ?: oldPricing.avgVolume30D,
                        avgVolume90D = newPricing.avgVolume90D ?: oldPricing.avgVolume90D,
                        relativeVolume10D = newPricing.relativeVolume10D ?: oldPricing.relativeVolume10D,
                        beta1Y = newPricing.beta1Y ?: oldPricing.beta1Y,
                        beta5Y = newPricing.beta5Y ?: oldPricing.beta5Y,
                        preMarketPrice = newPricing.preMarketPrice ?: oldPricing.preMarketPrice,
                        preMarketChange = newPricing.preMarketChange ?: oldPricing.preMarketChange,
                        postMarketPrice = newPricing.postMarketPrice ?: oldPricing.postMarketPrice,
                        postMarketChange = newPricing.postMarketChange ?: oldPricing.postMarketChange
                    ) ?: newPricing
                } else oldPricing

                latchedData = newData.copy(
                    companyName = if (newData.companyName != "Loading...") newData.companyName else latchedData.companyName,
                    marketPricing = mergedPricing,
                    rangeChangeAbsolute = if (newData.rangeChangeAbsolute != null && newData.rangeChangeAbsolute != 0.0) newData.rangeChangeAbsolute else latchedData.rangeChangeAbsolute,
                    rangeChangePercent = if (newData.rangeChangePercent != null && newData.rangeChangePercent != 0.0) newData.rangeChangePercent else latchedData.rangeChangePercent,
                    valuationMultipliers = newData.valuationMultipliers ?: latchedData.valuationMultipliers,
                    incomeStatement = newData.incomeStatement ?: latchedData.incomeStatement,
                    balanceSheet = newData.balanceSheet ?: latchedData.balanceSheet,
                    cashFlows = newData.cashFlows ?: latchedData.cashFlows,
                    efficiencyMargins = newData.efficiencyMargins ?: latchedData.efficiencyMargins,
                    historicalReturns = newData.historicalReturns ?: latchedData.historicalReturns,
                    advancedTechnicals = newData.advancedTechnicals ?: latchedData.advancedTechnicals,
                    analystCoverage = newData.analystCoverage ?: latchedData.analystCoverage,
                    advancedRiskMetrics = newData.advancedRiskMetrics ?: latchedData.advancedRiskMetrics,
                    news = newData.news.ifEmpty { latchedData.news },
                    similarStocks = newData.similarStocks.ifEmpty { latchedData.similarStocks }
                )
=======
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apexinvest.app.data.StockFullDetail
import com.apexinvest.app.data.TransactionEntity
import com.apexinvest.app.data.model.StockNews
import com.apexinvest.app.ui.components.TransactionHistorySection
import com.apexinvest.app.viewmodel.NewsUiState
import com.apexinvest.app.viewmodel.PortfolioViewModel
import com.apexinvest.app.viewmodel.StockDetailState
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun StockDetailScreen(
    symbol: String,
    viewModel: PortfolioViewModel,
    onBack: () -> Unit,
    isConnected: Boolean
) {
    // 1. Load Data
    LaunchedEffect(symbol) {
        viewModel.loadStockDetails(symbol, "1D")
        viewModel.loadTransactionHistory(symbol)
        viewModel.loadStockNews(symbol)
    }

    LaunchedEffect(symbol) {
        while (true) {
            kotlinx.coroutines.delay(30_000) // Wait 30 seconds
            if (isConnected) {
                // calls the LIGHTWEIGHT endpoint (Just price/change, no chart)
                viewModel.loadCurrentPrice(symbol)
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
            }
        }
    }

<<<<<<< HEAD
    val lastPrice = remember(livePricing, latchedData) { 
        livePricing?.priceLast ?: latchedData.marketPricing?.priceLast ?: 0.0 
    }

    LaunchedEffect(lastPrice) {
        // 🚀 FIX: Only append/update live price to the chart during REGULAR market hours.
        // This prevents after-hours prices from skewing the regular-hours chart.
        val (isRegularOpen, _) = StockMetadataUtils.isMarketOpen(symbol)
        
        if (currentRange == "1D" && currentLiveCandles.isNotEmpty() && lastPrice > 0.0 && isRegularOpen) {
            val lastCandle = currentLiveCandles.last()
            if (lastPrice != lastCandle.close) {
                val updatedList = currentLiveCandles.toMutableList()
                val updatedLast = lastCandle.copy(
                    close = lastPrice,
                    high = max(lastCandle.high, lastPrice),
                    low = if (lastCandle.low <= 0.0) lastPrice else min(lastCandle.low, lastPrice)
                )
                updatedList[updatedList.size - 1] = updatedLast
                currentLiveCandles = updatedList
            }
        }
    }

    LaunchedEffect(symbol, isConnected) {
        stockDetailViewModel.loadStockDetails(symbol, "1D")
        stockDetailViewModel.loadFinancialsCharts(symbol)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, symbol, isConnected) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                if (isConnected) stockDetailViewModel.startLiveUpdates(symbol)
            } else if (event == Lifecycle.Event.ON_STOP) {
                stockDetailViewModel.stopLiveUpdates()
            }
        }

        if (isConnected && lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            stockDetailViewModel.startLiveUpdates(symbol)
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            stockDetailViewModel.stopLiveUpdates()
        }
    }

    // 🚀 THE FIX: Check if EITHER the offline banner OR a message banner is visible
    val isTopBannerVisible = !isConnected || uiMessage != null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(meshBrush)
            .then(
                if (isTopBannerVisible) Modifier.consumeWindowInsets(WindowInsets.statusBars)
                else Modifier
            )
    ) {
        Column(Modifier.fillMaxSize()) {
            GlassTopBar(symbol, isFollowing, isDark, onBack) {
                if (isFollowing) portfolioViewModel.deleteWatchlistStock(symbol) else portfolioViewModel.addWatchlistStock(symbol)
            }

            if (isUiReady) {
                StockDetailContent(
                    data = latchedData,
                    livePricing = livePricing,
                    liveCandles = currentLiveCandles,
                    holding = currentHolding,
                    isDark = isDark,
                    isChartLoading = isChartLoading,
                    currencyCode = nativeCurrencyCode,
                    currentRange = currentRange,
                    symbol = symbol,
                    onRangeSelected = {
                        currentRange = it
                        loadingRange = it // 🚀 UX: Lock values for new range
                        stockDetailViewModel.loadStockDetails(symbol, it)
                    },
                    onPeerClick = onNavigateToStock,
                    formatAnalystRating = stockDetailViewModel::formatAnalystRating,
                    financialsState = financialsState,
                    latchedData = latchedData,
                    isLoadingRange = loadingRange == currentRange
                )
            } else {
                val nativeCurrencySymbol = getCurrencySymbol(nativeCurrencyCode)

                Column(Modifier.fillMaxSize()) {
                    PriceHeroSection(
                        title = latchedData.companyName ?: latchedData.ticker,
                        symbol = symbol,
                        exchangeName = exchangeInfo.name,
                        price = lastPrice,
                        change = latchedData.marketPricing?.changeAbsolute1D ?: 0.0,
                        percent = latchedData.marketPricing?.changePct1D ?: 0.0,
                        isPositive = (latchedData.marketPricing?.changeAbsolute1D ?: 0.0) >= 0,
                        isDark = isDark,
                        isLoading = lastPrice == 0.0,
                        currencySymbol = nativeCurrencySymbol,
                        preMarketPrice = livePricing?.preMarketPrice ?: latchedData.marketPricing?.preMarketPrice,
                        preMarketChange = livePricing?.preMarketChange ?: latchedData.marketPricing?.preMarketChange,
                        postMarketPrice = livePricing?.postMarketPrice ?: latchedData.marketPricing?.postMarketPrice,
                        postMarketChange = livePricing?.postMarketChange ?: latchedData.marketPricing?.postMarketChange,
                        marketState = livePricing?.marketState ?: latchedData.marketPricing?.marketState,
                        hasPrePost = livePricing?.hasPrePost ?: latchedData.marketPricing?.hasPrePost ?: true
                    )
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        TabLoadingPlaceholder()
                    }
=======
    // 2. Observe States
    val globalState by viewModel.uiState.collectAsState()
    val detailState by viewModel.stockDetailState.collectAsState()
    val currentRange by viewModel.selectedRange.collectAsState()
    val history by viewModel.transactionHistory.collectAsState()
    val newsState by viewModel.newsState.collectAsState()

    val isFollowing = remember(globalState.watchlist, symbol) {
        globalState.watchlist.any { it.symbol == symbol }
    }

    // 3. Helper: Determine Market/Context Strings
    val marketLabel = remember(symbol) {
        when {
            symbol.contains(".NS") || symbol == "^NSEI" || symbol == "^NSEBANK" -> "NSE India"
            symbol.contains(".BO") || symbol == "^BSESN" -> "BSE India"
            else -> "US Market / Global"
        }
    }

    // 4. Tab State
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "News")
    val context = LocalContext.current
    Scaffold(
        // Dynamic Insets: 0dp when Offline (Banner pushes us down), Default when Online
        contentWindowInsets = if (!isConnected) {
            WindowInsets(0.dp)
        } else {
            ScaffoldDefaults.contentWindowInsets
        }
    ) { innerPadding ->

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(innerPadding)
    ) {
        // --- CUSTOM HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = symbol,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = marketLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    if (isFollowing) {
                        viewModel.deleteWatchlistStock(symbol)
                        Toast.makeText(context, "Unfollowed $symbol", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.addWatchlistStock(symbol)
                        Toast.makeText(context, "Following $symbol", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowing) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(horizontal = 16.dp),
                shape = RoundedCornerShape(50), // Pill shape
                modifier = Modifier.height(36.dp)
            ) {
                if (isFollowing) {
                    Icon(Icons.Default.Check, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    Spacer(Modifier.width(4.dp))
                    Text("Following", color = MaterialTheme.colorScheme.onSecondaryContainer, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Add, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(4.dp))
                    Text("Follow", color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
                }
            }
        }

<<<<<<< HEAD
        if (isUiReady) {
            TradeActionBar(
                isDark = isDark,
                canSell = canSell,
                marketStatus = marketStatus,
                onSellComplete = {
                    if (isConnected) {
                        stockDetailViewModel.executePresetTrade(false, true)
                    } else {
                        portfolioViewModel.showMessage("Offline: Cannot trade.", com.apexinvest.app.ui.components.MessageType.ERROR)
                    }
                },
                onBuyComplete = {
                    if (isConnected) {
                        stockDetailViewModel.executePresetTrade(true, true)
                    } else {
                        portfolioViewModel.showMessage("Offline: Cannot trade.", com.apexinvest.app.ui.components.MessageType.ERROR)
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 120.dp)
=======
        // --- TABS ---
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colorScheme.primary,
                    height = 3.dp
                )
            },
            divider = { HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant) }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp) },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = Color.Gray
                )
            }
        }

        // --- CONTENT ---
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> OverviewTab(
                    symbol = symbol,
                    detailState = detailState,
                    currentRange = currentRange,
                    history = history,
                    onRangeSelected = { r -> viewModel.loadStockDetails(symbol, r) },
                    onRetry = { viewModel.loadStockDetails(symbol, currentRange) }
                )

                1 -> NewsTab(newsState)
            }
        }
    }
}
}

// ==========================================
// LOGIC: Currency Helper
// ==========================================
fun getCurrencySymbol(symbol: String): String {
    // 1. Standard Indian Extensions
    if (symbol.endsWith(".NS") || symbol.endsWith(".BO")) return "₹"

    // 2. Nifty / Indices Special Cases (Yahoo Finance Symbols)
    val indianIndices = listOf(
        "^NSEI",       // Nifty 50
        "^NSEBANK",    // Bank Nifty
        "^BSESN",      // Sensex
        "^CNX100",     // Nifty 100
        "NIFTY 50",    // Display names if passed directly
        "BANK NIFTY"
    )
    if (indianIndices.contains(symbol.uppercase())) return "₹"

    // 3. Default to Dollar
    return "$"
}

// ==========================================
// TAB 1: OVERVIEW
// ==========================================
@Composable
fun OverviewTab(
    symbol: String,
    detailState: StockDetailState,
    currentRange: String,
    history: List<TransactionEntity>,
    onRangeSelected: (String) -> Unit,
    onRetry: () -> Unit
) {
    when (detailState) {
        is StockDetailState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        is StockDetailState.Error -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Unable to load data", color = MaterialTheme.colorScheme.error)
                Text(detailState.message, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onRetry) { Text("Retry") }
            }
        }
        is StockDetailState.Success -> {
            StockDetailContent(
                data = detailState.data,
                currentRange = currentRange,
                history = history,
                onRangeSelected = onRangeSelected
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
            )
        }
    }
}

<<<<<<< HEAD
// ... StockDetailContent and other composables remain identical
@Composable
fun StockDetailContent(
    data: StockDetailsResponse,
    livePricing: MarketPricing?,
    liveCandles: List<CandlePoint>,
    holding: StockEntity?,
    isDark: Boolean,
    isChartLoading: Boolean,
    currencyCode: String,
    currentRange: String,
    symbol: String,
    onRangeSelected: (String) -> Unit,
    onPeerClick: (String) -> Unit,
    formatAnalystRating: (String?) -> String,
    financialsState: FinancialsUiState,
    latchedData: StockDetailsResponse, // 🆕 Pass latched data for stable transitions
    isLoadingRange: Boolean = false // 🆕 Lock values during range switch
) {
    val scrollState = rememberScrollState()
    var chartType by remember { mutableStateOf(ChartType.LINE) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val loadedTabs = remember { mutableStateMapOf<Int, Boolean>().apply { put(0, true) } }

    LaunchedEffect(selectedTab) {
        loadedTabs[selectedTab] = true
    }

    // 🚀 ABSOLUTE SOURCE OF TRUTH for Price: Prefer Live WebSocket/Quote Flow
    val lastPrice = remember(livePricing, data) { 
        livePricing?.priceLast ?: data.marketPricing?.priceLast ?: 0.0 
    }

    val dynamicChange = remember(data, currentRange, lastPrice, isLoadingRange) {
        if (isLoadingRange) return@remember latchedData.rangeChangeAbsolute ?: 0.0
        
        val calc = if (currentRange == "1D") {
            // For 1D, use the API's daily change
            livePricing?.changeAbsolute1D ?: data.marketPricing?.changeAbsolute1D ?: 0.0
        } else {
            // For other ranges: Prefer API's calculated range change
            val apiRangeChange = data.rangeChangeAbsolute
            if (apiRangeChange != null && apiRangeChange != 0.0) {
                apiRangeChange
            } else {
                // Fallback: (Current Price - First Chart Open)
                val firstOpen = data.candles.firstOrNull()?.open ?: 0.0
                if (firstOpen > 0.0 && lastPrice > 0.0) lastPrice - firstOpen else 0.0
            }
        }
        
        if (calc == 0.0 && latchedData.rangeChangeAbsolute != null && latchedData.rangeChangeAbsolute != 0.0) {
            latchedData.rangeChangeAbsolute!!
        } else calc
    }

    val dynamicPercent = remember(data, currentRange, lastPrice, isLoadingRange) {
        if (isLoadingRange) return@remember latchedData.rangeChangePercent ?: 0.0

        val calc = if (currentRange == "1D") {
            livePricing?.changePct1D ?: data.marketPricing?.changePct1D ?: 0.0
        } else {
            // Prefer API's calculated range percent
            val apiRangePercent = data.rangeChangePercent
            if (apiRangePercent != null && apiRangePercent != 0.0) {
                apiRangePercent
            } else {
                // Fallback: ((Current - Start) / Start) * 100
                val firstOpen = data.candles.firstOrNull()?.open ?: 0.0
                if (firstOpen > 0.0 && lastPrice > 0.0) ((lastPrice - firstOpen) / firstOpen) * 100.0 else 0.0
            }
        }
        
        // 🚀 LATCHING: If we are refreshing and the new value is 0.0, hold the old one from latchedData
        if (calc == 0.0 && latchedData.rangeChangePercent != null && latchedData.rangeChangePercent != 0.0) {
             latchedData.rangeChangePercent!!
        } else calc
    }
    val isDynamicPositive = remember(dynamicChange) { dynamicChange >= 0 }
    val currencySymbol = getCurrencySymbol(currencyCode)

    Column(Modifier.fillMaxSize().verticalScroll(scrollState)) {

        PriceHeroSection(
            title = data.companyName ?: data.ticker,
            symbol = data.ticker,
            exchangeName = StockMetadataUtils.getExchangeInfo(data.ticker).name,
            price = lastPrice,
            change = dynamicChange,
            percent = dynamicPercent,
            isPositive = isDynamicPositive,
            isDark = isDark,
            isLoading = lastPrice == 0.0,
            currencySymbol = currencySymbol,
            preMarketPrice = livePricing?.preMarketPrice ?: data.marketPricing?.preMarketPrice,
            preMarketChange = livePricing?.preMarketChange ?: data.marketPricing?.preMarketChange,
            postMarketPrice = livePricing?.postMarketPrice ?: data.marketPricing?.postMarketPrice,
            postMarketChange = livePricing?.postMarketChange ?: data.marketPricing?.postMarketChange,
            marketState = livePricing?.marketState ?: data.marketPricing?.marketState,
            hasPrePost = livePricing?.hasPrePost ?: data.marketPricing?.hasPrePost ?: true
        )
        Spacer(Modifier.height(16.dp))

        Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(340.dp).glassCard(isDark)) {
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    TimeframePills(currentRange, isDark) { onRangeSelected(it) }
                    ChartTypeToggle(chartType, isDark) { chartType = it }
                }
                Box(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 4.dp)) {
                    if (isChartLoading && liveCandles.isEmpty()) {
                        GlassShimmer(isDark, 260.dp)
                    } else {
                        // 🚀 CHART SYNC: Ensure the chart uses the same STABLE price as the header
                        OptimizedGlassChart(liveCandles, chartType, lastPrice, isDynamicPositive, isDark, currencyCode, currentRange, symbol)
=======
@Composable
fun StockDetailContent(
    data: StockFullDetail,
    currentRange: String,
    history: List<TransactionEntity>,
    onRangeSelected: (String) -> Unit
) {
    val currencySymbol = getCurrencySymbol(data.symbol)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Price Header
        StockPriceHeader(
            price = data.price,
            change = data.change,
            changePercent = data.changePercent,
            range = currentRange,
            currencySymbol = currencySymbol
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Time Selector
        TimeRangeSelector(currentRange, onRangeSelected)

        Spacer(modifier = Modifier.height(24.dp))

        // Chart
        if (data.historyPoints.isNotEmpty()) {
            InteractiveStockChart(
                points = data.historyPoints,
                previousClose = data.prevClose,
                isPositive = data.change >= 0,
                currencySymbol = currencySymbol
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("No chart data available", color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Key Stats (Rich UI)
        Text("Key Statistics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        StockStatisticsGrid(data, currencySymbol)

        Spacer(modifier = Modifier.height(32.dp))

        // History
        if (history.isNotEmpty()) {
            HorizontalDivider(thickness = 8.dp, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(24.dp))
            TransactionHistorySection(history = history, currencySymbol = currencySymbol)
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ==========================================
// TAB 2: NEWS
// ==========================================
@Composable
fun NewsTab(newsState: NewsUiState) {
    val context = LocalContext.current

    when (newsState) {
        is NewsUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        is NewsUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Unable to fetch news", color = Color.Gray) }
        is NewsUiState.Success -> {
            if (newsState.articles.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Newspaper, null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("No news available", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 16.dp, horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(newsState.articles) { article ->
                        NewsCard(article) {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.link))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Cannot open link", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NewsCard(article: StockNews, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    // Publisher Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = article.publisher,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = article.published,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ==========================================
// HELPER COMPONENTS
// ==========================================

@Composable
fun StockPriceHeader(
    price: Double,
    change: Double,
    changePercent: Double,
    range: String,
    currencySymbol: String
) {
    val isPositive = change >= 0
    val color = if (isPositive) Color(0xFF00C853) else Color(0xFFD32F2F) // Rich Green/Red
    val periodLabel = if (range == "1D") "Today" else range

    // Determine currency code based on symbol
    val currencyCode = if (currencySymbol == "₹") "INR" else "USD"

    Column {
        Row(verticalAlignment = Alignment.Bottom) {
            TickerText(
                value = price,
                prefix = currencySymbol,
                textStyle = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = currencyCode,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isPositive) Icons.Default.Check else Icons.Default.Newspaper, // Placeholder logic
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(0.dp) // Hidden
            )
            Text(
                text = String.format(Locale.US, "%+.2f (%.2f%%)", change, changePercent),
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(color.copy(alpha = 0.1f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$periodLabel",
                    color = color,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Data provided by Yahoo Finance",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun InteractiveStockChart(
    points: List<Pair<String, Double>>,
    previousClose: Double,
    isPositive: Boolean,
    currencySymbol: String
) {
    val chartColor = if (isPositive) Color(0xFF00C853) else Color(0xFFD32F2F)
    val areaColor = chartColor.copy(alpha = 0.1f)
    var touchXRatio by remember { mutableStateOf<Float?>(null) }

    val prices = points.map { it.second }
    val maxVal = (prices.maxOrNull() ?: 0.0).coerceAtLeast(previousClose)
    val minVal = (prices.minOrNull() ?: 0.0).coerceAtMost(previousClose)
    val yRange = if ((maxVal - minVal) == 0.0) 1.0 else (maxVal - minVal) * 1.2
    val dynamicStroke = if (prices.size > 300) 2f else 5f

    Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp, bottom = 12.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onPress = { offset ->
                        touchXRatio = offset.x / size.width
                        tryAwaitRelease()
                        touchXRatio = null
                    })
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset -> touchXRatio = offset.x / size.width },
                        onDragEnd = { touchXRatio = null },
                        onDragCancel = { touchXRatio = null },
                        onHorizontalDrag = { change, _ -> touchXRatio = change.position.x / size.width }
                    )
                }
        ) {
            val width = size.width
            val height = size.height
            fun getY(value: Double): Float { return (height - ((value - minVal) / yRange * height)).toFloat() }

            // Previous Close Line
            val prevCloseY = getY(previousClose)
            drawLine(
                color = Color.Gray.copy(alpha = 0.5f),
                start = Offset(0f, prevCloseY),
                end = Offset(width, prevCloseY),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )

            if (prices.isNotEmpty()) {
                val xStep = width / (prices.size - 1).coerceAtLeast(1)
                val path = Path()
                val fillPath = Path()

                prices.forEachIndexed { index, value ->
                    val x = index * xStep
                    val y = getY(value)
                    if (index == 0) {
                        path.moveTo(x, y); fillPath.moveTo(x, height); fillPath.lineTo(x, y)
                    } else {
                        path.lineTo(x, y); fillPath.lineTo(x, y)
                    }
                }
                fillPath.lineTo(prices.lastIndex * xStep, height)
                fillPath.close()

                drawPath(path = fillPath, brush = Brush.verticalGradient(colors = listOf(areaColor, Color.Transparent), startY = 0f, endY = height))
                drawPath(path = path, color = chartColor, style = Stroke(width = dynamicStroke, cap = StrokeCap.Round, join = StrokeJoin.Round))

                // Touch Indicator
                touchXRatio?.let { ratio ->
                    val touchX = ratio * width
                    val maxHistoryX = (prices.size - 1) * xStep
                    if (touchX <= maxHistoryX) {
                        val index = ((touchX / maxHistoryX) * (prices.size - 1)).roundToInt().coerceIn(0, prices.lastIndex)
                        val x = index * xStep
                        val y = getY(prices[index])

                        // Vertical Line
                        drawLine(color = Color.LightGray, start = Offset(x, 0f), end = Offset(x, height), strokeWidth = 2f)
                        // Outer Glow
                        drawCircle(color = chartColor.copy(alpha = 0.2f), center = Offset(x, y), radius = 20f)
                        // White Dot
                        drawCircle(color = Color.White, center = Offset(x, y), radius = 12f)
                        // Inner Dot
                        drawCircle(color = chartColor, center = Offset(x, y), radius = 8f)
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
                    }
                }
            }
        }

<<<<<<< HEAD
        Spacer(Modifier.height(24.dp))
        if (data.aiAnalystReport != null) {
            AiApexInvestCard(data.aiAnalystReport, isDark)
            Spacer(Modifier.height(24.dp))
        }

        QuickMetricsStrip(data, isDark)
        Spacer(Modifier.height(24.dp))

        DomainTabs(selectedTab, isDark) { selectedTab = it }
        Spacer(Modifier.height(20.dp))

        Crossfade(targetState = selectedTab, label = "TabPanesContent", animationSpec = tween(150)) { tab ->
            Column(Modifier.padding(horizontal = 20.dp)) {
                when (tab) {
                    0 -> OverviewPane(data, holding, isDark)
                    1 -> if (loadedTabs[1] == true) TechnicalsPane(data.advancedTechnicals, isDark) else TabLoadingPlaceholder()
                    2 -> if (loadedTabs[2] == true) ScorecardPane(financialsState, isDark) else TabLoadingPlaceholder()
                    3 -> if (loadedTabs[3] == true) EfficiencyRiskPane(data.efficiencyMargins, data.advancedRiskMetrics, data.analystCoverage, isDark, formatAnalystRating) else TabLoadingPlaceholder()
                    4 -> if (loadedTabs[4] == true) MomentumPane(data.historicalReturns, liveCandles, currentRange, isDark) else TabLoadingPlaceholder()
                    5 -> if (loadedTabs[5] == true) NewsAndPeersPane(data.news, data.similarStocks, getCurrencySymbol(currencyCode), isDark, onPeerClick) else TabLoadingPlaceholder()
                }
                Spacer(Modifier.height(160.dp))
            }
        }
    }
=======
        // Tooltip
        if (touchXRatio != null) {
            val ratio = touchXRatio!!
            var tooltipText = ""
            var subText = ""
            if (ratio <= 1.0f && prices.isNotEmpty()) {
                val index = (ratio * (prices.size - 1)).roundToInt().coerceIn(0, prices.lastIndex)
                tooltipText = String.format(Locale.US, "$currencySymbol%.2f", prices[index])
                subText = points[index].first
            }
            if (tooltipText.isNotEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 0.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.inverseSurface),
                        elevation = CardDefaults.cardElevation(4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = tooltipText, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.inverseOnSurface)
                            Text(text = subText, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimeRangeSelector(currentRange: String, onRangeSelected: (String) -> Unit) {
    val ranges = listOf("1D", "5D", "1M", "6M", "YTD", "1Y", "5Y", "Max")

    // Using a scrollable row just in case screen is small
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ranges.forEach { range ->
            val isSelected = range == currentRange
            val bg = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
            val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onRangeSelected(range) }
                    .background(bg)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = range,
                    color = contentColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// ==========================================
// FIXED: Removed String.format for PE/DivYield/High/Low Strings to fix Crash
// ==========================================
@Composable
fun StockStatisticsGrid(data: StockFullDetail, currencySymbol: String) {
    val stats = listOf(
        "Open" to "$currencySymbol${String.format(Locale.US, "%.2f", data.open)}",
        "Prev Close" to "$currencySymbol${String.format(Locale.US, "%.2f", data.prevClose)}",
        "Day High" to "$currencySymbol${String.format(Locale.US, "%.2f", data.dayHigh)}",
        "Day Low" to "$currencySymbol${String.format(Locale.US, "%.2f", data.dayLow)}",
        "Mkt Cap" to data.marketCap, // e.g. "2.4T"

        // FIX: Removed String.format() - data.peRatio is String
        "P/E Ratio" to data.peRatio,

        // FIX: Removed String.format() - data.dividendYield is String
        "Div Yield" to if(data.dividendYield == "N/A") "N/A" else "${data.dividendYield}%",

        // FIX: Removed String.format() - data.yearHigh is String
        "52W High" to "$currencySymbol${data.yearHigh}",

        // FIX: Removed String.format() - data.yearLow is String
        "52W Low" to "$currencySymbol${data.yearLow}"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            stats.chunked(3).forEachIndexed { index, rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    rowItems.forEach { (label, value) ->
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = label,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = value,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (rowItems.size < 3) {
                        repeat(3 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
                    }
                }
                if (index < stats.chunked(3).lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun TickerText(
    value: Double,
    prefix: String = "",
    textStyle: androidx.compose.ui.text.TextStyle,
    color: Color,
    modifier: Modifier = Modifier
) {
    // 1. Remember previous value to determine direction
    var previousValue by remember { mutableDoubleStateOf(value) }

    // 2. Determine Slide Direction (Up if gained, Down if lost)
    val direction = if (value > previousValue) {
        AnimatedContentTransitionScope.SlideDirection.Up
    } else {
        AnimatedContentTransitionScope.SlideDirection.Down
    }

    // 3. Update memory for next change
    SideEffect { previousValue = value }

    // 4. Animate
    AnimatedContent(
        targetState = value,
        transitionSpec = {
            (slideIntoContainer(direction) + fadeIn()).togetherWith(
                slideOutOfContainer(direction) + fadeOut()
            ).using(SizeTransform(clip = false))
        },
        label = "PriceTicker"
    ) { targetValue ->
        Text(
            text = "$prefix${String.format(Locale.US, "%.2f", targetValue)}",
            style = textStyle,
            color = color,
            modifier = modifier
        )
    }
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
}