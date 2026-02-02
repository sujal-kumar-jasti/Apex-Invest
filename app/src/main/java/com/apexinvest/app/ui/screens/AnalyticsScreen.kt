package com.apexinvest.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apexinvest.app.data.StockEntity
import com.apexinvest.app.ui.navigation.Screen
import com.apexinvest.app.util.getConvertedValue
import com.apexinvest.app.util.toCleanString
import com.apexinvest.app.viewmodel.AiUiState
import com.apexinvest.app.viewmodel.AnalyticsUiState
import com.apexinvest.app.viewmodel.PortfolioViewModel
import com.apexinvest.app.viewmodel.StockAllocation

@Composable
fun AnalyticsScreen(
    viewModel: PortfolioViewModel,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    isConnected: Boolean
) {
    // 1. Handle System Back Button
    BackHandler { onBack() }

    // 2. Observe Global Portfolio State
    val portfolioState by viewModel.uiState.collectAsState()

    // 3. LISTEN FOR CHANGES (Triggered whenever Portfolio updates)
    // When the background timer updates the portfolio, this block runs and recalculates analytics
    LaunchedEffect(portfolioState.portfolio) {
        viewModel.loadPortfolioAnalytics()
    }

    // 4. AUTO-REFRESH TIMER (Every 30s)
    // This ensures prices update even if the user stays on the Analytics screen
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(30_000)
            if (isConnected) {
                // This updates the main portfolio list in ViewModel
                viewModel.loadPortfolioAndPrices()
            }
        }
    }

    val state by viewModel.analyticsState.collectAsState()
    val aiState by viewModel.aiState.collectAsState()

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
            // --- HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "Portfolio Intelligence",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // --- CONTENT STATES ---
            when (val uiState = state) {
                is AnalyticsUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                is AnalyticsUiState.Empty -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.PieChart,
                                null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            Spacer(Modifier.height(16.dp))
                            Text("No portfolio data to analyze.", color = Color.Gray)
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { onBack() }) {
                                Text("Add Stocks")
                            }
                        }
                    }
                }

                is AnalyticsUiState.Success -> {
                    AnalyticsContent(
                        data = uiState,
                        isUsd = portfolioState.isUsd,
                        liveRate = portfolioState.liveRate,
                        aiState = aiState,
                        onNavigate = onNavigate,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun AnalyticsContent(
    data: AnalyticsUiState.Success,
    isUsd: Boolean,
    liveRate: Double,
    aiState: AiUiState,
    onNavigate: (String) -> Unit,
    viewModel: PortfolioViewModel
) {
    // Current Symbol to display
    val displaySymbol = if (isUsd) "$" else "₹"

    LazyColumn(
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // 1. HERO CHART
        item {
            // Normalize Total Value for Display
            val totalValConverted = if (isUsd && data.currencySymbol == "₹") data.totalValue / liveRate
            else if (!isUsd && data.currencySymbol == "$") data.totalValue * liveRate
            else data.totalValue

            PortfolioGrowthChart(
                dataPoints = data.historyCurve,
                currentValue = totalValConverted,
                symbol = displaySymbol
            )
            Spacer(Modifier.height(24.dp))
        }

        // 2. TOP MOVERS
        item {
            AnalyticsSectionHeader("Top Movers")
            TopMoversRow(
                gainer = data.topGainer,
                loser = data.topLoser,
                isUsd = isUsd,
                liveRate = liveRate,
                onNavigate = onNavigate
            )
            Spacer(Modifier.height(32.dp))
        }

        // 3. SECTOR DNA
        item {
            AnalyticsSectionHeader("Sector Allocation")
            SectorDonutChart(data.sectors)
            Spacer(Modifier.height(32.dp))
        }

        // 4. HOLDINGS DISTRIBUTION
        item {
            AnalyticsSectionHeader("Holdings Distribution")
            Text(
                "Allocation weight vs. Performance",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            Spacer(Modifier.height(12.dp))

            HoldingsDistributionChart(
                allocations = data.allocations,
                onNavigate = onNavigate
            )
            Spacer(Modifier.height(32.dp))
        }

        // 5. AI INSIGHT CARD
        item {
            AiInsightCard(data) {
                // Check Cache: Only generate if Idle or Error
                if (aiState !is AiUiState.Success && aiState !is AiUiState.Thinking) {
                    viewModel.generateIdeas(null)
                }
                onNavigate(Screen.InvestmentIdeas.route)
            }
        }
    }
}

@Composable
fun AnalyticsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

// ==========================================
// 1. HERO CHART (Interactive + Animated)
// ==========================================
@Composable
fun PortfolioGrowthChart(
    dataPoints: List<Pair<String, Double>>,
    currentValue: Double,
    symbol: String
) {
    var touchX by remember { mutableStateOf<Float?>(null) }
    val prices = remember(dataPoints) { dataPoints.map { it.second } }

    val startValue = prices.firstOrNull() ?: 0.0
    val endValue = prices.lastOrNull() ?: 0.0
    val isPositive = endValue >= startValue
    val chartColor = if (isPositive) Color(0xFF00C853) else Color(0xFFD32F2F)

    // Determine what price/time to show
    val displayPrice = if (touchX != null && dataPoints.isNotEmpty()) {
        val index = ((touchX!!).coerceIn(0f, 1f) * (dataPoints.size - 1)).toInt()
        dataPoints[index].second
    } else {
        currentValue
    }

    val displayTime = if (touchX != null && dataPoints.isNotEmpty()) {
        val index = ((touchX!!).coerceIn(0f, 1f) * (dataPoints.size - 1)).toInt()
        dataPoints[index].first
    } else {
        "Net Worth"
    }

    val textMeasurer = rememberTextMeasurer()

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text("Total Portfolio Value", style = MaterialTheme.typography.labelMedium, color = Color.Gray)

        Row(verticalAlignment = Alignment.Bottom) {

            // --- ANIMATED TICKER ---
            TickerText(
                value = displayPrice,
                prefix = symbol,
                textStyle = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = chartColor
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = displayTime,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = { touchX = it.x / size.width },
                            onDragEnd = { touchX = null },
                            onDragCancel = { touchX = null },
                            onHorizontalDrag = { change, _ -> touchX = (change.position.x / size.width).coerceIn(0f, 1f) }
                        )
                    }
            ) {
                if (prices.isEmpty()) return@Canvas

                val max = prices.maxOrNull() ?: 0.0
                val min = prices.minOrNull() ?: 0.0
                val range = if (max == min) 1.0 else max - min
                val width = size.width
                val height = size.height

                val path = Path()
                val fillPath = Path()

                prices.forEachIndexed { index, value ->
                    val x = (index.toFloat() / (prices.size - 1)) * width
                    val y = height - ((value - min) / range * height).toFloat()
                    if (index == 0) {
                        path.moveTo(x, y); fillPath.moveTo(x, height); fillPath.lineTo(x, y)
                    } else {
                        path.lineTo(x, y); fillPath.lineTo(x, y)
                    }
                }
                fillPath.lineTo(width, height); fillPath.lineTo(0f, height); fillPath.close()

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(chartColor.copy(alpha = 0.3f), chartColor.copy(alpha = 0.0f)),
                        startY = 0f, endY = height
                    )
                )
                drawPath(path = path, color = chartColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

                touchX?.let { ratio ->
                    val index = (ratio * (prices.size - 1)).toInt().coerceIn(0, prices.lastIndex)
                    val x = (index.toFloat() / (prices.size - 1)) * width
                    val y = height - ((prices[index] - min) / range * height).toFloat()

                    val pointTime = dataPoints[index].first
                    val pointPrice = "$symbol${prices[index].toCleanString()}"

                    drawLine(color = Color.LightGray, start = Offset(x, 0f), end = Offset(x, height), strokeWidth = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
                    drawCircle(Color.White, radius = 6.dp.toPx(), center = Offset(x, y))
                    drawCircle(chartColor, radius = 4.dp.toPx(), center = Offset(x, y))

                    val textLayout = textMeasurer.measure(
                        text = AnnotatedString("$pointPrice\n$pointTime"),
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                    )
                    val boxWidth = textLayout.size.width + 30f
                    val boxHeight = textLayout.size.height + 20f
                    var boxX = x - boxWidth / 2
                    if (boxX < 0) boxX = 0f
                    if (boxX + boxWidth > width) boxX = width - boxWidth

                    drawRoundRect(color = Color(0xFF2C2C2C), topLeft = Offset(boxX, 10f), size = Size(boxWidth, boxHeight), cornerRadius = CornerRadius(8.dp.toPx()))
                    drawText(textLayoutResult = textLayout, topLeft = Offset(boxX + 15f, 20f))
                }
            }
        }
    }
}

// ==========================================
// 2. TOP MOVERS (Animated)
// ==========================================
@Composable
fun TopMoversRow(
    gainer: StockEntity?,
    loser: StockEntity?,
    isUsd: Boolean,
    liveRate: Double,
    onNavigate: (String) -> Unit
) {
    val symbol = if (isUsd) "$" else "₹"

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MoverCard(gainer, true, isUsd, liveRate, symbol, Modifier.weight(1f)) {
            gainer?.let { onNavigate(Screen.StockDetail.createRoute(it.symbol)) }
        }
        MoverCard(loser, false, isUsd, liveRate, symbol, Modifier.weight(1f)) {
            loser?.let { onNavigate(Screen.StockDetail.createRoute(it.symbol)) }
        }
    }
}

@Composable
fun MoverCard(
    stock: StockEntity?,
    isWinner: Boolean,
    isUsd: Boolean,
    liveRate: Double,
    symbol: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val bg = if (isWinner) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val textCol = if (isWinner) Color(0xFF00C853) else Color(0xFFD32F2F)
    val label = if (isWinner) "Top Gainer" else "Top Loser"
    val icon = if (isWinner) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown

    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(bg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(label, color = textCol, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.weight(1f))
                Icon(icon, null, tint = textCol, modifier = Modifier.size(16.dp))
            }

            Spacer(Modifier.height(12.dp))

            if (stock != null) {
                val convertedPrice = getConvertedValue(stock.currentPrice, stock.symbol, isUsd, liveRate)

                Text(stock.symbol, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, maxLines = 1)

                // --- ANIMATED PERCENTAGE ---
                TickerText(
                    value = stock.dailyChange,
                    prefix = if (stock.dailyChange > 0) "+" else "",
                    suffix = "%",
                    textStyle = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textCol
                )

                // --- ANIMATED PRICE ---
                TickerText(
                    value = convertedPrice,
                    prefix = symbol,
                    textStyle = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            } else {
                Text("-", style = MaterialTheme.typography.titleMedium)
                Text("--%", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

// ==========================================
// 3. SECTOR DONUT (Standard)
// ==========================================
@Composable
fun SectorDonutChart(sectors: Map<String, Double>) {
    val total = sectors.values.sum()
    val colors = listOf(
        Color(0xFF5C6BC0), Color(0xFF42A5F5), Color(0xFF26C6DA),
        Color(0xFF66BB6A), Color(0xFFFFCA28), Color(0xFFEF5350), Color(0xFFAB47BC)
    )

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(120.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    var startAngle = -90f
                    val strokeWidth = 35.dp.toPx()
                    sectors.entries.forEachIndexed { index, entry ->
                        val sweepAngle = (entry.value / total * 360).toFloat()
                        drawArc(
                            color = colors[index % colors.size],
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                        startAngle += sweepAngle
                    }
                }
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(sectors.size.toString(), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("Sectors", fontSize = 10.sp, color = Color.Gray)
                }
            }

            Spacer(Modifier.width(24.dp))

            Column {
                sectors.entries.sortedByDescending { it.value }.take(5).forEachIndexed { index, entry ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(10.dp).clip(CircleShape).background(colors[index % colors.size])
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(entry.key, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        Text(
                            "${(entry.value / total * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. HOLDINGS DISTRIBUTION (Animated Pill)
// ==========================================
@Composable
fun HoldingsDistributionChart(
    allocations: List<StockAllocation>,
    onNavigate: (String) -> Unit
) {
    if (allocations.isEmpty()) return

    val sortedItems = remember(allocations) { allocations.sortedByDescending { it.percent } }
    val maxPercent = sortedItems.firstOrNull()?.percent ?: 100.0

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            sortedItems.forEachIndexed { index, item ->
                val isProfit = item.changePercent >= 0
                val perfColor = if (isProfit) Color(0xFF00C853) else Color(0xFFD32F2F)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(Screen.StockDetail.createRoute(item.symbol)) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.symbol,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(60.dp)
                    )

                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${((item.percent)*100).toCleanString()}%", fontSize = 10.sp, color = Color.Gray)
                        }
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (item.percent / maxPercent).toFloat().coerceIn(0.01f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    }

                    // --- ANIMATED PERFORMANCE PILL ---
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(perfColor.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        TickerText(
                            value = item.changePercent,
                            prefix = if (isProfit) "+" else "",
                            suffix = "%",
                            textStyle = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = perfColor
                        )
                    }
                }

                if (index < sortedItems.lastIndex) {
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

// ==========================================
// 5. AI INSIGHT CARD (Standard)
// ==========================================
@Composable
fun AiInsightCard(
    data: AnalyticsUiState.Success,
    onClick: () -> Unit
) {
    val (riskLabel, riskColor, riskIcon) = when {
        data.sectors.size < 3 -> Triple("High Risk", Color(0xFFD32F2F), Icons.Default.Warning)
        data.sectors.size in 3..4 -> Triple("Moderate Risk", Color(0xFFFBC02D), Icons.AutoMirrored.Filled.TrendingUp)
        else -> Triple("Balanced", Color(0xFF2E7D32), Icons.Default.CheckCircle)
    }

    val geminiPurple = Color(0xFF6750A4)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = geminiPurple.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, geminiPurple.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, null, tint = geminiPurple, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Gemini Analysis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = geminiPurple)
                        Text("Portfolio Health", style = MaterialTheme.typography.bodySmall, color = geminiPurple.copy(alpha = 0.7f))
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(riskColor.copy(alpha = 0.1f))
                        .border(1.dp, riskColor.copy(alpha = 0.2f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(riskIcon, null, tint = riskColor, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(riskLabel, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = riskColor)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = if (data.sectors.size < 3) "Portfolio is concentrated. Consider diversifying." else "Portfolio is well distributed across sectors.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("View Full Report", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = geminiPurple)
                Spacer(Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = geminiPurple, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ==========================================
// 6. TICKER ANIMATION UTILITY
// ==========================================
@Composable
fun TickerText(
    value: Double,
    prefix: String = "",
    suffix: String = "",
    textStyle: TextStyle,
    fontWeight: FontWeight? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    color: Color,
    modifier: Modifier = Modifier
) {
    var previousValue by remember { mutableStateOf(value) }

    // Up (Green) or Down (Red) direction based on change
    val direction = if (value > previousValue) {
        AnimatedContentTransitionScope.SlideDirection.Up
    } else {
        AnimatedContentTransitionScope.SlideDirection.Down
    }

    SideEffect { previousValue = value }

    AnimatedContent(
        targetState = value,
        transitionSpec = {
            (slideIntoContainer(direction) + fadeIn()).togetherWith(
                slideOutOfContainer(direction) + fadeOut()
            ).using(SizeTransform(clip = false))
        },
        label = "TickerAnimation"
    ) { targetValue ->
        Text(
            text = "$prefix${targetValue.toCleanString()}$suffix",
            style = textStyle,
            fontWeight = fontWeight,
            fontSize = fontSize,
            color = color,
            modifier = modifier
        )
    }
}