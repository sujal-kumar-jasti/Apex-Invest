package com.apexinvest.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.apexinvest.app.data.NewsItem
import com.apexinvest.app.data.PredictionPoint
import com.apexinvest.app.data.StockAnalysisResponse
import com.apexinvest.app.data.StockSearchResult
import com.apexinvest.app.ui.navigation.Screen
import com.apexinvest.app.viewmodel.AnalysisState
import com.apexinvest.app.viewmodel.PortfolioHealthState
import com.apexinvest.app.viewmodel.PortfolioViewModel
import com.apexinvest.app.viewmodel.PredictionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionScreen(
    portfolioViewModel: PortfolioViewModel,
    predictionViewModel: PredictionViewModel,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    isConnected: Boolean
) {
    val portfolioState by portfolioViewModel.uiState.collectAsState()
    val healthState by predictionViewModel.portfolioHealthState.collectAsState()
    val analysisState by predictionViewModel.analysisState.collectAsState()

    // We don't need searchResults here anymore, they are handled in the Popup
    // val searchResults by portfolioViewModel.searchResults.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Portfolio Health", "Market Scout")

    LaunchedEffect(portfolioState.portfolio) {
        val symbols = portfolioState.portfolio.map { it.symbol }
        if (symbols.isNotEmpty()) {
            predictionViewModel.scanPortfolio(symbols)
        }
    }

    Scaffold(
        contentWindowInsets = if (!isConnected) WindowInsets(0.dp) else ScaffoldDefaults.contentWindowInsets,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(Modifier.background(MaterialTheme.colorScheme.background)) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "PrognosAI Platinum",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) },
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            height = 3.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontWeight = FontWeight.SemiBold) },
                            unselectedContentColor = Color.Gray
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (selectedTab) {
                0 -> PortfolioHealthTab(healthState, portfolioState.portfolio.isEmpty(), onNavigate)
                1 -> MarketScoutTab(
                    portfolioViewModel = portfolioViewModel,
                    predictionViewModel = predictionViewModel,
                    analysisState = analysisState
                )
            }
        }
    }
}

@Composable
fun PortfolioHealthTab(
    state: PortfolioHealthState,
    isPortfolioEmpty: Boolean,
    onNavigate: (String) -> Unit
) {
    if (isPortfolioEmpty) {
        EmptyState("Add stocks to your portfolio to enable AI Analysis.")
        return
    }

    when (state) {
        is PortfolioHealthState.Loading -> LoadingState()
        is PortfolioHealthState.Error -> ErrorState(state.message)
        is PortfolioHealthState.Success -> {
            val data = state.summary
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item { SentimentGaugeCard(score = data.totalScore, mood = data.marketMood) }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        AnalyticsCard("Top Performer", data.topPick ?: "N/A", Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF4CAF50), Modifier.weight(1f))
                        AnalyticsCard("Stocks Analyzed", "${data.stockBreakdowns.size}", Icons.Default.QueryStats, Color(0xFF2196F3), Modifier.weight(1f))
                    }
                }
                if (data.riskWarning != null) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFFFCDD2)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WarningAmber, null, tint = Color(0xFFD32F2F))
                                Spacer(Modifier.width(16.dp))
                                Text(data.riskWarning, color = Color(0xFFB71C1C), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
                item { Text("Detailed Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp, top = 8.dp)) }
                items(data.stockBreakdowns) { stock ->
                    StockAiCard(stock) { onNavigate(Screen.StockDetail.createRoute(stock.symbol)) }
                }
                item { Spacer(Modifier.height(30.dp)) }
            }
        }
        else -> {}
    }
}

// -------------------------------------------------------------------------
// REWRITTEN MARKET SCOUT TAB (Better Search UI)
// -------------------------------------------------------------------------

@Composable
fun MarketScoutTab(
    portfolioViewModel: PortfolioViewModel,
    predictionViewModel: PredictionViewModel,
    analysisState: AnalysisState
) {
    // Local state for the popup
    var showSearchPopup by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {

        // 1. CLEAN SEARCH TRIGGER (Not DockedSearchBar)
        SearchBarTrigger(
            onClick = { showSearchPopup = true },
            modifier = Modifier.padding(16.dp)
        )

        // 2. MAIN CONTENT AREA
        Box(Modifier.fillMaxSize()) {
            when (analysisState) {
                is AnalysisState.Idle -> EmptyState("Search above to analyze any stock or crypto.")
                is AnalysisState.Loading -> LoadingState()
                is AnalysisState.Error -> ErrorState(analysisState.message)
                is AnalysisState.Success -> DetailedStockView(analysisState.data)
            }
        }
    }

    // 3. FULL SCREEN POPUP (Identical to Explore Screen)
    if (showSearchPopup) {
        StockSearchPopup(
            viewModel = portfolioViewModel,
            onDismiss = { showSearchPopup = false },
            onResultSelected = { symbol ->
                showSearchPopup = false
                // Trigger the AI analysis
                predictionViewModel.analyzeStock(symbol)
            }
        )
    }
}

@Composable
fun SearchBarTrigger(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        Text(
            text = "Search Global Stocks (e.g. NVDA)",
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontSize = 16.sp
        )
    }
}

@Composable
fun StockSearchPopup(
    viewModel: PortfolioViewModel,
    onDismiss: () -> Unit,
    onResultSelected: (String) -> Unit
) {
    // Use search state from PortfolioViewModel (reuse existing logic)
    val query = remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Auto-focus when dialog opens
    LaunchedEffect(Unit) {
        try { focusRequester.requestFocus() } catch (_: Exception) {}
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }

                TextField(
                    value = query.value,
                    onValueChange = {
                        query.value = it
                        if (it.length >= 2) viewModel.searchStocks(it)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    placeholder = { Text("Type symbol...") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    trailingIcon = {
                        if (query.value.isNotEmpty()) {
                            IconButton(onClick = {
                                query.value = ""
                                viewModel.searchStocks("")
                            }) {
                                Icon(Icons.Default.Clear, null, tint = Color.Gray)
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                )
            }

            HorizontalDivider()

            // Results List
            Box(modifier = Modifier.fillMaxSize()) {
                if (query.value.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Type to search...", color = Color.Gray)
                    }
                } else if (searchResults.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (query.value.length < 2) {
                            Text("Type at least 2 characters", color = Color.Gray)
                        } else {
                            Text("No results found", color = Color.Gray)
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(searchResults) { stock ->
                            SearchResultItemRow(stock) {
                                onResultSelected(stock.symbol)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// EXISTING COMPONENTS (Kept mostly same, just optimized)
// -------------------------------------------------------------------------

@Composable
fun SearchResultItemRow(stock: StockSearchResult, onClick: () -> Unit) {
    val safeSymbol = stock.symbol.orEmpty()
    val safeName = stock.shortName ?: stock.longName ?: ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            val char = if (safeSymbol.isNotEmpty()) safeSymbol.take(1) else "?"
            Text(
                char,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(safeSymbol, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(safeName, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(6.dp)
        ) {
            val exch = stock.exchange ?: stock.type ?: "US"
            Text(
                text = exch,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun DetailedStockView(stock: StockAnalysisResponse) {
    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(stock.symbol, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
                    Text(
                        text = formatPrice(stock.symbol, stock.currentPrice),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Gray
                    )
                }

                val isBullish = stock.recommendation.contains("Bullish", ignoreCase = true) || stock.recommendation.contains("Buy", ignoreCase = true)
                val badgeColor = if(isBullish) Color(0xFF2E7D32) else Color(0xFFC62828)
                val badgeBg = if(isBullish) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)

                Surface(color = badgeBg, shape = RoundedCornerShape(12.dp)) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            stock.recommendation.uppercase(),
                            color = badgeColor,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FundamentalChip("Mkt Cap", stock.fundamentals.marketCap, Modifier.weight(1f))
                FundamentalChip("P/E", stock.fundamentals.peRatio.toString(), Modifier.weight(1f))
                FundamentalChip("Sector", stock.fundamentals.sector, Modifier.weight(1f))
            }
            Spacer(Modifier.height(30.dp))
        }

        if (stock.forecast.isNotEmpty()) {
            item {
                Text("AI 7-Day Forecast", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))
                PredictionChart(stock.forecast, stock.currentPrice)
                Spacer(Modifier.height(30.dp))
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Sentiment Analysis", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    LinearSentimentMeter(stock.sentiment.score)
                    Spacer(Modifier.height(12.dp))
                    Text(stock.sentiment.summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        item {
            Text("Key Headlines", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
        }

        items(stock.sentiment.news) { news ->
            NewsItemCard(news)
            Spacer(Modifier.height(12.dp))
        }
        item { Spacer(Modifier.height(50.dp)) }
    }
}

@Composable
fun SentimentGaugeCard(score: Double, mood: String) {
    val gaugeValue = ((score + 1.0) / 2.0).toFloat().coerceIn(0f, 1f)
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(gaugeValue) {
        animatedProgress.animateTo(gaugeValue, animationSpec = tween(1500))
    }

    val currentColor = when {
        gaugeValue < 0.4f -> Color(0xFFEF5350)
        gaugeValue > 0.6f -> Color(0xFF4CAF50)
        else -> Color(0xFFFFC107)
    }

    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp)
            .wrapContentHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(180.dp)) {
                    drawArc(
                        color = Color.LightGray.copy(alpha = 0.2f),
                        startAngle = 140f,
                        sweepAngle = 260f,
                        useCenter = false,
                        style = Stroke(width = 60f, cap = StrokeCap.Round)
                    )

                    drawArc(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFFEF5350), Color(0xFFFFC107), Color(0xFF4CAF50))
                        ),
                        startAngle = 140f,
                        sweepAngle = 260f * animatedProgress.value,
                        useCenter = false,
                        style = Stroke(width = 60f, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%.2f", score),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = currentColor
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "MARKET MOOD",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                mood.uppercase(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = currentColor
            )
        }
    }
}

@Composable
fun StockAiCard(stock: StockAnalysisResponse, onClick: () -> Unit) {
    val color = getSentimentColor(stock.recommendation)
    val bgColor = getSentimentBackgroundColor(stock.recommendation)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.5f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stock.symbol,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Surface(
                        color = bgColor,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            stock.recommendation,
                            color = color,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = stock.sentiment.summary.ifEmpty { "Analyzing market data..." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun PredictionChart(points: List<PredictionPoint>, currentPrice: Double) {
    if (points.isEmpty()) return
    val prices = points.map { it.predictedPrice }
    val maxPrice = prices.maxOrNull() ?: currentPrice
    val minPrice = prices.minOrNull() ?: currentPrice
    val trendColor = if ((prices.lastOrNull() ?: 0.0) >= currentPrice) Color(0xFF4CAF50) else Color(0xFFEF5350)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.5f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().height(220.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                val width = size.width
                val height = size.height
                val range = (maxPrice - minPrice).coerceAtLeast(0.1)

                val path = Path()
                val xStep = width / (points.size - 1).coerceAtLeast(1)

                points.forEachIndexed { index, point ->
                    val x = index * xStep
                    val y = height - ((point.predictedPrice - minPrice) / range * height).toFloat()
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                drawPath(
                    path = path,
                    color = trendColor,
                    style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                points.forEachIndexed { index, point ->
                    val x = index * xStep
                    val y = height - ((point.predictedPrice - minPrice) / range * height).toFloat()
                    drawCircle(color = Color.White, radius = 8f, center = Offset(x, y))
                    drawCircle(color = trendColor, radius = 6f, center = Offset(x, y))
                }
            }

            Text(
                "Forecast",
                modifier = Modifier.padding(16.dp).align(Alignment.TopStart),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun NewsItemCard(news: NewsItem) {
    val context = LocalContext.current
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth().clickable {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(news.link))
                context.startActivity(intent)
            } catch (e: Exception) { e.printStackTrace() }
        }
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(
                    news.title,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(news.publisher, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Spacer(Modifier.width(8.dp))
                    val color = when(news.sentimentLabel) {
                        "Bullish" -> Color(0xFF4CAF50)
                        "Bearish" -> Color(0xFFE53935)
                        else -> Color.Gray
                    }
                    Box(Modifier.size(6.dp).clip(CircleShape).background(color))
                    Spacer(Modifier.width(4.dp))
                    Text(news.sentimentLabel, style = MaterialTheme.typography.labelSmall, color = color)
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.Gray)
        }
    }
}

@Composable
fun LinearSentimentMeter(score: Double) {
    val normalized = ((score + 1.0) / 2.0).toFloat().coerceIn(0f, 1f)

    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Bearish", style = MaterialTheme.typography.labelSmall, color = Color(0xFFEF5350))
            Text("Neutral", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFFC107))
            Text("Bullish", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50))
        }

        Spacer(Modifier.height(8.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(Color.LightGray.copy(alpha = 0.2f))
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(normalized)
                    .clip(RoundedCornerShape(7.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFEF5350), Color(0xFFFFC107), Color(0xFF4CAF50))
                        )
                    )
            )
        }
    }
}

@Composable
fun AnalyticsCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            Modifier.padding(vertical = 20.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(12.dp))
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
fun FundamentalChip(label: String, value: String, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.5f))
    ) {
        Column(Modifier.padding(vertical = 12.dp, horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun ErrorState(msg: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.ErrorOutline, null, tint = Color.Red, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(16.dp))
            Text(msg, color = Color.Red, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun EmptyState(msg: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Search, null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text(msg, color = Color.Gray, textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium)
        }
    }
}

fun getSentimentColor(recommendation: String): Color {
    return when {
        recommendation.contains("Bullish", ignoreCase = true) || recommendation.contains("Buy", ignoreCase = true) -> Color(0xFF4CAF50)
        recommendation.contains("Bearish", ignoreCase = true) || recommendation.contains("Sell", ignoreCase = true) -> Color(0xFFEF5350)
        else -> Color(0xFFFFC107)
    }
}

fun getSentimentBackgroundColor(recommendation: String): Color {
    return when {
        recommendation.contains("Bullish", ignoreCase = true) -> Color(0xFFE8F5E9)
        recommendation.contains("Bearish", ignoreCase = true) -> Color(0xFFFFEBEE)
        else -> Color(0xFFFFF8E1)
    }
}

fun formatPrice(symbol: String, price: Double): String {
    val isIndian = symbol.uppercase().endsWith(".NS") || symbol.uppercase().endsWith(".BO")
    val currency = if (isIndian) "₹" else "$"
    return "$currency$price"
}