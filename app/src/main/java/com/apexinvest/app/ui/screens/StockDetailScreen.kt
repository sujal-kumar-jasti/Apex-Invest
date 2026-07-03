package com.apexinvest.app.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
            }
        }
    }

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
                }
            }
        }

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
            )
        }
    }
}

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
                    }
                }
            }
        }

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
}