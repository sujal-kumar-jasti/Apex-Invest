package com.apexinvest.app.ui.screens

import android.app.Activity
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.apexinvest.app.api.models.DeepAnalysisResponse
import com.apexinvest.app.ui.components.CommonScreenHeader
import com.apexinvest.app.ui.components.CommonSearchOverlay
import com.apexinvest.app.ui.components.CommonSearchResultRow
import com.apexinvest.app.ui.components.InsightCard
import com.apexinvest.app.ui.components.StatCard
import com.apexinvest.app.ui.components.glassCard
import com.apexinvest.app.ui.theme.BrandPurple
import com.apexinvest.app.ui.theme.LocalAppColors
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
    onNavigateToPortfolio: () -> Unit, // 🚀 ADDED
    isConnected: Boolean
) {
    LaunchedEffect(Unit) {
        Log.d("PredictionScreen", "🚀 SCREEN_OPENED")
    }

    val portfolioState by portfolioViewModel.uiState.collectAsState()
    val healthState by predictionViewModel.portfolioHealthState.collectAsState()
    val analysisState by predictionViewModel.analysisState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedSymbol by remember { mutableStateOf<String?>(null) }

    val surfaceColor = MaterialTheme.colorScheme.surface
    val isDark = remember(surfaceColor) { surfaceColor.luminance() < 0.5f }

    val meshBrush = remember(isDark) {
        if (isDark) {
            Brush.verticalGradient(listOf(BrandPurple.copy(alpha = 0.12f), Color.Transparent))
        } else {
            Brush.verticalGradient(listOf(BrandPurple.copy(alpha = 0.05f), Color.Transparent))
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    LaunchedEffect(isConnected) {
        if (isConnected) {

            predictionViewModel.scanPortfolio(forceRefresh = false)
        }
    }

    BackHandler(enabled = selectedSymbol != null) {
        selectedSymbol = null
        predictionViewModel.clearAnalysisState()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(meshBrush)
    ) {
        if (selectedSymbol == null) {
            Column {
                CommonScreenHeader(
                    onBackClick = onBack,
                    centerTitle = true,
                    applyStatusBarsPadding = isConnected,
                    leadingContent = {
                        Column {
                            Text("Apex Invest Pro", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge)
                            Text("Advanced Analysis Engine", color = BrandPurple, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        }
                    },
                    title = null,
                    trailingContent = { Spacer(Modifier.width(40.dp)) }
                )

                SecondaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = BrandPurple,
                    indicator = {
                        Box(
                            Modifier
                                .tabIndicatorOffset(selectedTab)
                                .height(3.dp)
                                .padding(horizontal = 50.dp)
                                .clip(CircleShape)
                                .background(BrandPurple)
                        )
                    },
                    divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)) }
                ) {
                    listOf("Portfolio DNA", "Market Scout").forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontWeight = FontWeight.Black) },
                            selectedContentColor = BrandPurple,
                            unselectedContentColor = Color.Gray
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(targetState = selectedSymbol, label = "ScreenTransition") { symbol ->
                if (symbol != null) {
                    DeepAnalysisScreen(
                        symbol = symbol,
                        predictionViewModel = predictionViewModel,
                        isConnected = isConnected,
                        onBack = {
                            selectedSymbol = null
                            predictionViewModel.clearAnalysisState()
                        }
                    )
                } else {
                    AnimatedContent(targetState = selectedTab, label = "TabSwitch") { target ->
                        when (target) {
                            0 -> PortfolioHealthTab(
                                healthState,
                                portfolioState.portfolio.isEmpty(),
                                isDark,
                                onNavigateToPortfolio = onNavigateToPortfolio // 🚀 PASS
                            ) { stockResponse ->
                                predictionViewModel.setAnalysisData(stockResponse)
                                selectedSymbol = stockResponse.symbol
                            }
                            1 -> MarketScoutTab(portfolioViewModel, predictionViewModel, analysisState, isDark) { scoutSymbol ->
                                selectedSymbol = scoutSymbol
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PortfolioHealthTab(
    state: PortfolioHealthState,
    isEmpty: Boolean,
    isDark: Boolean,
    onNavigateToPortfolio: () -> Unit, // 🚀 ADDED
    onStockClick: (DeepAnalysisResponse) -> Unit
) {
    if (isEmpty) {
        EmptyStateView(
            msg = "Your portfolio is empty. Add holdings to unlock deep health metrics and AI risk analysis.",
            actionText = "Start Investing",
            onAction = onNavigateToPortfolio
        )
        return
    }

    val summaryData = when (state) {
        is PortfolioHealthState.Success -> state.summary
        is PortfolioHealthState.Loading -> state.summary
        is PortfolioHealthState.Error -> state.summary
        else -> null
    }

    if (summaryData != null) {
        Column(Modifier.fillMaxSize()) {
            if (state is PortfolioHealthState.Loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = BrandPurple, trackColor = Color.Transparent)
                Text(state.message, color = BrandPurple, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), textAlign = TextAlign.Center)
            }

            LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item { SentimentGaugeCard(score = summaryData.totalSentimentScore, mood = summaryData.marketMood, isDark = isDark) }
                item {
                    val appColors = LocalAppColors.current
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard("Alpha Leader", summaryData.topPick ?: "--", Icons.AutoMirrored.Filled.TrendingUp, appColors.trendGreen, Modifier.weight(1f),isDark )
                        StatCard("Analyzed", "${summaryData.stockBreakdowns.size} Assets", Icons.Default.QueryStats, BrandPurple, Modifier.weight(1f),isDark)
                    }
                }
                if (summaryData.riskWarning != null) { item { ProRiskBanner(summaryData.riskWarning) } }
                item { Text("Portfolio Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)) }

                items(summaryData.stockBreakdowns) { stock ->
                    val symbol = stock.symbol
                    val verdict = stock.agentSynthesis.finalVerdict
                    val healthScore = stock.financialHealthScore
                    
                    ProStockAiCard(
                        symbol = symbol,
                        verdict = verdict,
                        financialHealthScore = healthScore,
                        isDark = isDark,
                        onClick = remember(stock.symbol) { { onStockClick(stock) } }
                    )
                }
            }
        }
    } else {
        when (state) {
            is PortfolioHealthState.Loading -> ProLoadingState(state.message)
            is PortfolioHealthState.Error -> ProErrorView(state.message)
            else -> {}
        }
    }
}

@Composable
fun MarketScoutTab(
    pViewModel: PortfolioViewModel,
    predViewModel: PredictionViewModel,
    analysisState: AnalysisState,
    isDark: Boolean,
    onSymbolSelected: (String) -> Unit
) {
    var showSearchPopup by remember { mutableStateOf(value = false) }

    BackHandler(enabled = showSearchPopup) {
        showSearchPopup = false
    }

    Column(Modifier.fillMaxSize()) {
        ProSearchTrigger(onClick = { showSearchPopup = true }, isDark = isDark, modifier = Modifier.padding(16.dp))
        Box(Modifier.fillMaxSize()) {
            when (analysisState) {
                is AnalysisState.Idle -> EmptyStateView("Scout any global asset for real-time AI analysis.")
                is AnalysisState.Loading -> if (analysisState.data == null) ProLoadingState(analysisState.message)
                is AnalysisState.Error -> if (analysisState.data == null) ProErrorView(analysisState.message)
                else -> {}
            }
        }
    }

    if (showSearchPopup) {
        var searchQuery by remember { mutableStateOf("") }
        val searchResults by pViewModel.searchResults.collectAsState()
        val focusManager = LocalFocusManager.current

        CommonSearchOverlay(
            query = searchQuery,
            onQueryChange = { searchQuery = it; if (it.length >= 2) pViewModel.searchStocks(it) },
            onDismiss = { showSearchPopup = false },
            isDark = isDark,
            placeholder = "Enter ticker (e.g., AAPL)"
        ) {
            if (searchQuery.isEmpty()) {
                Box(Modifier.align(Alignment.Center)) {
                    Text("Analyze any symbol...", color = Color.Gray, fontSize = 15.sp)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(searchResults, key = { it.symbol }) { stock ->
                        CommonSearchResultRow(
                            symbol = stock.symbol,
                            name = stock.name,
                            exchange = stock.exchange
                        ) {
                            focusManager.clearFocus()
                            showSearchPopup = false
                            predViewModel.analyzeStock(stock.symbol, forceRefresh = false)
                            onSymbolSelected(stock.symbol)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProLoadingState(message: String) = Box(Modifier.fillMaxSize(), Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(color = BrandPurple, strokeWidth = 3.dp)
        Spacer(Modifier.height(16.dp))
        Text(message, fontWeight = FontWeight.Bold, color = BrandPurple)
    }
}

@Composable
fun ProErrorView(msg: String) {
    val appColors = LocalAppColors.current
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Text(msg, color = appColors.trendRed, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(32.dp))
    }
}

@Composable
fun EmptyStateView(msg: String, actionText: String? = null, onAction: (() -> Unit)? = null) = Box(Modifier.fillMaxSize().padding(32.dp), Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(msg, color = Color.Gray, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
        if (actionText != null && onAction != null) {
            Spacer(Modifier.height(16.dp))
            androidx.compose.material3.Button(
                onClick = onAction,
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = BrandPurple)
            ) {
                Text(actionText, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SentimentGaugeCard(score: Double, mood: String, isDark: Boolean) {
    val progress = ((score + 1.0) / 2.0).toFloat().coerceIn(0f, 1f)
    val animatedValue = animateFloatAsState(targetValue = progress, animationSpec = tween(durationMillis = 2500, easing = FastOutSlowInEasing), label = "SentimentProgress")

    val appColors = LocalAppColors.current
    val gaugeColor: Color = when { progress < 0.4f -> appColors.trendRed; progress > 0.6f -> appColors.trendGreen; else -> Color(0xFFFFB300) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(isDark, RoundedCornerShape(30.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                Spacer(
                    Modifier.size(190.dp).drawWithCache {
                        val gradient = Brush.horizontalGradient(listOf(appColors.trendRed, Color(0xFFFFB300), appColors.trendGreen))
                        val backgroundTrack = Color.Gray.copy(alpha = 0.1f)
                        val stroke = Stroke(18.dp.toPx(), cap = StrokeCap.Round)

                        onDrawBehind {
                            drawArc(backgroundTrack, 140f, 260f, false, style = stroke)
                            drawArc(gradient, 140f, 260f * animatedValue.value, false, style = stroke)
                        }
                    }
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = (score * 100).toInt().toString(), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black, color = gaugeColor)
                    Text("AI SCORE", style = MaterialTheme.typography.labelSmall, color = Color.Gray, letterSpacing = 2.sp)
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(mood.uppercase(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = gaugeColor, letterSpacing = 1.sp)
        }
    }
}

@Composable
fun ProStockAiCard(
    symbol: String,
    verdict: String,
    financialHealthScore: String,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val isBull = remember(verdict) { verdict.contains("Buy", true) }
    val isBear = remember(verdict) { verdict.contains("Sell", true) }
    val appColors = LocalAppColors.current
    val cardColor: Color = remember(isBull, isBear) {
        if (isBull) appColors.trendGreen else if (isBear) appColors.trendRed else Color(0xFFFFB300)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(isDark, RoundedCornerShape(24.dp))
            .clickable { onClick() }
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).background(cardColor.copy(alpha = 0.15f), RoundedCornerShape(16.dp)), Alignment.Center) {
                Text(symbol.take(1), fontWeight = FontWeight.Black, color = cardColor, fontSize = 20.sp)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1.1f)) {
                Text(symbol, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.bodyLarge)
                Text(financialHealthScore, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Box(Modifier.background(cardColor.copy(alpha = 0.15f), RoundedCornerShape(16.dp)).border(1.dp, cardColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))) {
                    Text(if(isBull) "BUY" else if (isBear) "SELL" else "HOLD", color = cardColor, fontWeight = FontWeight.Black, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                }
            }
        }
    }
}

@Composable
fun ProRiskBanner(msg: String) {
    val appColors = LocalAppColors.current
    InsightCard(title = "Risk Alert", description = msg, icon = Icons.Default.WarningAmber, color = appColors.trendRed, isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f)
}

@Composable
fun ProSearchTrigger(onClick: () -> Unit, isDark: Boolean, modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(12.dp, RoundedCornerShape(30.dp), spotColor = BrandPurple.copy(alpha = 0.2f))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = if(isDark) 0.8f else 1f), RoundedCornerShape(30.dp))
            .clickable { onClick() }
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(30.dp))
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Search, null, tint = BrandPurple)
            Spacer(Modifier.width(12.dp))
            Text("Search global markets...", color = Color.Gray, fontWeight = FontWeight.Medium, fontSize = 15.sp)
        }
    }
}
