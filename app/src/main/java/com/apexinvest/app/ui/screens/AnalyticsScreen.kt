package com.apexinvest.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apexinvest.app.data.StockEntity
import com.apexinvest.app.ui.components.AppTickerText
import com.apexinvest.app.ui.components.CommonScreenHeader
import com.apexinvest.app.ui.components.OptimizedGrowthChart
import com.apexinvest.app.ui.components.glassCard
import com.apexinvest.app.ui.navigation.Screen
import com.apexinvest.app.ui.theme.BrandPurple
import com.apexinvest.app.ui.theme.LocalAppColors
import com.apexinvest.app.viewmodel.AnalyticsUiState
import com.apexinvest.app.viewmodel.PortfolioViewModel
import com.apexinvest.app.viewmodel.StockAllocation
import java.util.Locale

@Composable
fun AnalyticsScreen(
    viewModel: PortfolioViewModel,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    isConnected: Boolean,
) {
    BackHandler { onBack() }

    val portfolioState by viewModel.uiState.collectAsStateWithLifecycle()
    val state by viewModel.analyticsState.collectAsStateWithLifecycle()
    val portfolioStats by viewModel.portfolioStats.collectAsStateWithLifecycle()

    val isUsd = portfolioState.isUsd
    val liveRate = portfolioState.liveRate

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, isConnected) {
        val observer = LifecycleEventObserver { _, event ->
            if ((event == Lifecycle.Event.ON_RESUME) && isConnected) {
                viewModel.startPeriodicUpdates()
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.stopPeriodicUpdates()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stopPeriodicUpdates()
        }
    }

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val meshBrush = remember(isDark) {
        if (isDark) {
            Brush.verticalGradient(listOf(BrandPurple.copy(alpha = 0.08f), Color.Transparent))
        } else {
            Brush.verticalGradient(listOf(BrandPurple.copy(alpha = 0.03f), Color.Transparent))
        }
    }

    val preCalculatedChart = portfolioStats?.chartData ?: emptyList()
    val investedAmount = portfolioStats?.totalInvested ?: 0.0
    val totalGain = portfolioStats?.totalGain ?: 0.0
    val gainPercent = portfolioStats?.totalPercent ?: 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(meshBrush)
    ) {
        CommonScreenHeader(
            onBackClick = onBack,
            applyStatusBarsPadding = isConnected,
            leadingContent = {
                Column {
                    Text("Analytics", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Text("Deep Data Visualization", style = MaterialTheme.typography.labelSmall, color = BrandPurple)
                }
            }
        )
        Box(modifier = Modifier.weight(1f)) {
            when (val uiState = state) {
                is AnalyticsUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = BrandPurple)
                }
                is AnalyticsUiState.Empty -> EmptyAnalyticsView(onBack)
                is AnalyticsUiState.Success -> AnalyticsProContent(
                    data = uiState,
                    aggregatedChart = preCalculatedChart,
                    investedAmount = investedAmount,
                    totalGain = totalGain,
                    gainPercent = gainPercent,
                    isUsd = isUsd,
                    liveRate = liveRate,
                    onNavigate = onNavigate,
                    viewModel = viewModel,
                    isDark = isDark
                )
            }
        }
    }
}

@Composable
fun AnalyticsProContent(
    data: AnalyticsUiState.Success,
    aggregatedChart: List<Double>,
    investedAmount: Double,
    totalGain: Double,
    gainPercent: Double,
    isUsd: Boolean,
    liveRate: Double,
    onNavigate: (String) -> Unit,
    viewModel: PortfolioViewModel,
    isDark: Boolean
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 100.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item(key = "hero_chart") {
            PortfolioHeroSection(
                totalValue = data.totalValue,
                investedAmount = investedAmount,
                totalGain = totalGain,
                gainPercent = gainPercent,
                chartData = aggregatedChart,
                currencySymbol = data.currencySymbol
            )
            Spacer(Modifier.height(24.dp))
        }

        item(key = "performance_grid") {
            SectionTitle("Performance Snapshot")
            PerformanceGrid(
                winRate = data.winRate,
                topGainer = data.topGainer,
                topLoser = data.topLoser,
                isUsd = isUsd,
                liveRate = liveRate,
                onNavigate = onNavigate,
                isDark = isDark
            )
            Spacer(Modifier.height(32.dp))
        }

        item(key = "sector_dna") {
            SectionTitle("Sector Distribution")
            SectorRingChart(data.sectors, isDark)
            Spacer(Modifier.height(32.dp))
        }

        item(key = "holdings_header") {
            SectionTitle("Asset Allocation Map")
        }

        itemsIndexed(data.allocations, key = { _, item -> item.symbol }) { index, item ->
            HoldingsPremiumRow(
                item = item,
                viewModel = viewModel,
                isLast = index == data.allocations.lastIndex,
                onNavigate = onNavigate,
                isDark = isDark
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
    )
}

@Composable
fun PortfolioHeroSection(
    totalValue: Double,
    investedAmount: Double,
    totalGain: Double,
    gainPercent: Double,
    chartData: List<Double>,
    currencySymbol: String
) {
    val appColors = LocalAppColors.current
    val isPositive = totalGain >= 0
    val accentColor = if (isPositive) appColors.trendGreen else appColors.trendRed

    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Text(
            "NET WORTH",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )
        AppTickerText(
            value = totalValue,
            currencySymbol = currencySymbol,
            textStyle = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black)
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Invested", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "$currencySymbol${String.format(Locale.US, "%,.2f", investedAmount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            GlassPill(
                color = accentColor,
                icon = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                text = "${if (isPositive) "+" else ""}${String.format(Locale.US, "%,.2f", totalGain)} (${String.format(Locale.US, "%,.2f", gainPercent)}%)"
            )
        }

        Spacer(Modifier.height(24.dp))
        OptimizedGrowthChart(chartData = chartData, color = accentColor)
    }
}

@Composable
fun GlassPill(color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(text, color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun PerformanceGrid(
    winRate: Double,
    topGainer: StockEntity?,
    topLoser: StockEntity?,
    isUsd: Boolean,
    liveRate: Double,
    onNavigate: (String) -> Unit,
    isDark: Boolean
) {
    val appColors = LocalAppColors.current

    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), Arrangement.spacedBy(16.dp)) {
        Box(
            modifier = Modifier.weight(1f).height(130.dp).glassCard(isDark, RoundedCornerShape(24.dp))
        ) {
            Column(Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.ShowChart, null, tint = BrandPurple, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Win Rate", style = MaterialTheme.typography.labelSmall, color = BrandPurple, fontWeight = FontWeight.Bold)
                }
                Text("${winRate.toInt()}%", fontWeight = FontWeight.Black, fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("Profitable Trades", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        val displayStock = topGainer ?: topLoser
        val isGainer = displayStock?.dailyChange?.let { it >= 0 } ?: true
        val moverColor = if (isGainer) appColors.trendGreen else appColors.trendRed
        val moverIcon = if (isGainer) Icons.Default.EmojiEvents else Icons.Default.Warning
        val moverTitle = if (isGainer) "Top Gainer" else "Top Loser"

        Box(
            modifier = Modifier.weight(1f).height(130.dp).glassCard(isDark, RoundedCornerShape(24.dp)).clickable(enabled = displayStock != null) {
                displayStock?.let { onNavigate(Screen.StockDetail.createRoute(it.symbol, if (isUsd) "USD" else "INR")) }
            }
        ) {
            Column(Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(moverIcon, null, tint = moverColor, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(moverTitle, style = MaterialTheme.typography.labelSmall, color = moverColor, fontWeight = FontWeight.Bold)
                }
                if (displayStock != null) {
                    Column {
                        Text(displayStock.symbol, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                        Text(
                            String.format(Locale.US, "%+.2f%%", displayStock.changePercent),
                            color = moverColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }
                } else {
                    Text("--", fontWeight = FontWeight.Black, fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun SectorRingChart(sectors: Map<String, Double>, isDark: Boolean) {
    val colors = remember { listOf(Color(0xFF5C6BC0), Color(0xFF26C6DA), Color(0xFF66BB6A), Color(0xFFFFCA28), Color(0xFFEF5350)) }
    val sorted = remember(sectors) { sectors.entries.sortedByDescending { it.value } }
    val total = remember(sectors) { sectors.values.sum().coerceAtLeast(1.0) }

    var animationPlayed by remember { mutableStateOf(value = false) }
    val sweepAngle by animateFloatAsState(
        targetValue = if (animationPlayed) 360f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "ring_animation"
    )

    LaunchedEffect(Unit) { animationPlayed = true }

    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).glassCard(isDark, RoundedCornerShape(30.dp))
    ) {
        Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(110.dp), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.PieChart, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f), modifier = Modifier.size(32.dp))
                // 🚀 OPTIMIZATION: Use drawWithCache to avoid allocations on every frame of the animation.
                Canvas(Modifier.fillMaxSize().drawWithCache {
                    onDrawBehind {
                        var start = -90f
                        sorted.forEachIndexed { i, e ->
                            val ratio = (e.value / total).toFloat()
                            val sweep = ratio * sweepAngle
                            drawArc(
                                color = colors[i % colors.size],
                                startAngle = start,
                                sweepAngle = sweep,
                                useCenter = false,
                                style = Stroke(28.dp.toPx(), cap = StrokeCap.Round)
                            )
                            start += (ratio * 360f)
                        }
                    }
                }) {}
            }
            Spacer(Modifier.width(24.dp))
            Column {
                sorted.take(4).forEachIndexed { i, e ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(colors[i % colors.size]))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = e.key,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${(e.value / total * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HoldingsPremiumRow(
    item: StockAllocation,
    viewModel: PortfolioViewModel,
    isLast: Boolean,
    onNavigate: (String) -> Unit,
    isDark: Boolean
) {
    val appColors = LocalAppColors.current
    val color = if (item.changePercent >= 0) appColors.trendGreen else appColors.trendRed

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = if (isLast) 0.dp else 8.dp)
            .glassCard(isDark, RoundedCornerShape(20.dp))
            .clickable { onNavigate(Screen.StockDetail.createRoute(item.symbol, "")) }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(42.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(item.symbol.take(1), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(item.symbol, fontWeight = FontWeight.Black, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                Text(
                    text = viewModel.getCompanyNameForSymbol(item.symbol),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { item.percent.toFloat().coerceIn(0.05f, 1f) },
                    modifier = Modifier.fillMaxWidth(0.7f).height(6.dp).clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = color.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
                ) {
                    val signedChange = String.format(Locale.US, "%+.2f%%", item.changePercent)
                    Text(
                        signedChange,
                        Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = color
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "${(item.percent * 100).toInt()}% of Port",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun EmptyAnalyticsView(onBack: () -> Unit) {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.AccountBalanceWallet, null, Modifier.size(72.dp), MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
            Spacer(Modifier.height(16.dp))
            Text("No Analytics Available", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            Text("Add holdings to view performance metrics", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
            Button(onClick = onBack) { Text("Back to Portfolio") }
        }
    }
}
