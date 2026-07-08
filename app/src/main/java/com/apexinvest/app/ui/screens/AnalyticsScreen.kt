package com.apexinvest.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.apexinvest.app.viewmodel.ImpactDriver
import com.apexinvest.app.viewmodel.PortfolioViewModel
import java.util.Locale
import kotlin.math.abs

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
                    onNavigate = onNavigate,
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
    onNavigate: (String) -> Unit,
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

        item(key = "performance_grid", contentType = "summary") {
            SectionTitle("Performance Snapshot")
            PerformanceGrid(data.winRate, data.topGainer, data.topLoser, isUsd, onNavigate, isDark)
            Spacer(Modifier.height(32.dp))
        }

        item(key = "influence_matrix_header", contentType = "header") {
            SectionTitle("Top Impact Drivers")
        }

        item(key = "influence_matrix", contentType = "chart") {
            PortfolioInfluenceMatrix(
                topDrivers = data.topDrivers,
                hiddenCount = data.hiddenImpactCount,
                isDark = isDark
            )
            Spacer(Modifier.height(32.dp))
        }

        item(key = "sector_dna", contentType = "summary") {
            SectionTitle("Sector Distribution")
            SectorRingChart(data.sectors, isDark)
            Spacer(Modifier.height(32.dp))
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
    var selectedIndex by remember { mutableIntStateOf(-1) }

    val displayValue = if (selectedIndex != -1 && selectedIndex < chartData.size) chartData[selectedIndex] else totalValue
    val isInteracting = selectedIndex != -1
    val displayGain = if (isInteracting) displayValue - investedAmount else totalGain
    val displayPercent = if (isInteracting) { if (investedAmount != 0.0) (displayGain / investedAmount) * 100 else 0.0 } else gainPercent

    val isPositive = displayGain >= 0
    val accentColor = if (isPositive) appColors.trendGreen else appColors.trendRed

    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Text(if (isInteracting) "POINT VALUE" else "NET WORTH", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
        AppTickerText(value = displayValue, currencySymbol = currencySymbol, textStyle = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black))
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Invested", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$currencySymbol${String.format(Locale.US, "%,.2f", investedAmount)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            GlassPill(color = accentColor, icon = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown, text = "${if (isPositive) "+" else ""}${String.format(Locale.US, "%,.2f", displayGain)} (${String.format(Locale.US, "%,.2f", displayPercent)}%)")
        }
        Spacer(Modifier.height(24.dp))
        OptimizedGrowthChart(
            chartData = chartData,
            color = accentColor,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp), // 🚀 CRITICAL FIX: Add height to Chart!
            onPointSelected = { selectedIndex = it },
            onSelectionCleared = { selectedIndex = -1 }
        )
    }
}

@Composable
fun GlassPill(color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, color.copy(alpha = 0.3f))) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(text, color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun PerformanceGrid(winRate: Double, topGainer: StockEntity?, topLoser: StockEntity?, isUsd: Boolean,
                    onNavigate: (String) -> Unit, isDark: Boolean) {
    val appColors = LocalAppColors.current
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), Arrangement.spacedBy(16.dp)) {
        Box(modifier = Modifier.weight(1f).height(130.dp).glassCard(isDark, RoundedCornerShape(24.dp))) {
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

        Box(modifier = Modifier.weight(1f).height(130.dp).glassCard(isDark, RoundedCornerShape(24.dp)).clickable(enabled = displayStock != null) { displayStock?.let { onNavigate(Screen.StockDetail.createRoute(it.symbol, if (isUsd) "USD" else "INR")) } }) {
            Column(Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(moverIcon, null, tint = moverColor, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(moverTitle, style = MaterialTheme.typography.labelSmall, color = moverColor, fontWeight = FontWeight.Bold)
                }
                if (displayStock != null) {
                    Column {
                        Text(displayStock.symbol, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                        Text(String.format(Locale.US, "%+.2f%%", displayStock.changePercent), color = moverColor, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                } else {
                    Text("--", fontWeight = FontWeight.Black, fontSize = 28.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// 🚀 REBUILT: Highly Optimized Memoized Ring Chart
// Calculates drawing vectors exactly once in memory to prevent scroll lag
@Composable
fun SectorRingChart(sectors: Map<String, Double>, isDark: Boolean) {
    val colors = remember { listOf(Color(0xFF5C6BC0), Color(0xFF26C6DA), Color(0xFF66BB6A), Color(0xFFFFCA28), Color(0xFFEF5350)) }

    // Memoizing the math: This prevents O(N) sorting and angle math during lazy column recomposition
    val (slices, legendItems) = remember(sectors) {
        val sorted = sectors.entries.sortedByDescending { it.value }
        val total = sorted.sumOf { it.value }.coerceAtLeast(1.0)

        var currentStartAngle = -90f
        val calcSlices = sorted.mapIndexed { index, entry ->
            val sweep = (entry.value / total).toFloat() * 360f
            val color = colors[index % colors.size]
            val slice = Triple(color, currentStartAngle, sweep)
            currentStartAngle += sweep
            slice
        }

        val calcLegend = sorted.take(4).mapIndexed { index, entry ->
            Triple(entry.key, colors[index % colors.size], (entry.value / total * 100).toInt())
        }

        Pair(calcSlices, calcLegend)
    }

    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).glassCard(isDark, RoundedCornerShape(30.dp))) {
        Row(Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(110.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    // Pure primitive drawing. Zero math overhead here.
                    slices.forEach { (color, start, sweep) ->
                        drawArc(
                            color = color,
                            startAngle = start,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(width = 28.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }
            Spacer(Modifier.width(24.dp))
            Column {
                legendItems.forEach { (name, color, percent) ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "$percent%",
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
fun PortfolioInfluenceMatrix(topDrivers: List<ImpactDriver>, hiddenCount: Int, isDark: Boolean) {
    val maxAbsImpact = remember(topDrivers) {
        topDrivers.maxOfOrNull { abs(it.impactScore) }?.coerceAtLeast(0.01) ?: 1.0
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .glassCard(isDark, RoundedCornerShape(32.dp))
            .padding(24.dp)
    ) {
        Column {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Asset Influence", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                Icon(Icons.Default.AccountBalanceWallet, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                "Assets actively driving your portfolio today.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            if (topDrivers.isEmpty()) {
                Text("No data available", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                topDrivers.forEach { driver ->
                    ImpactDriverRow(
                        driver = driver,
                        maxAbsImpact = maxAbsImpact
                    )
                }

                if (hiddenCount > 0) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "+ $hiddenCount other assets with minimal net impact",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun ImpactDriverRow(
    driver: ImpactDriver,
    maxAbsImpact: Double
) {
    var expanded by remember { mutableStateOf(false) }
    val isPositive = driver.impactScore >= 0
    val baseColor = if (isPositive) Color(0xFF00BFA5) else Color(0xFFFF8A65)
    val barColor = baseColor.copy(alpha = 0.2f)

    // Rendered instantly. No animation delay, no state transition.
    val fraction = (abs(driver.impactScore) / maxAbsImpact).toFloat()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { expanded = !expanded }
    ) {
        Box(Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = fraction)
                    .height(48.dp)
                    .background(barColor)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = driver.symbol,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = String.format(Locale.US, "%+.2f%%", driver.impactScore),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = baseColor
                )
            }
        }

        // Details panel expansion kept for UI interactivity, but chart bar rendering is instant
        AnimatedVisibility(visible = expanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Weight", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${(driver.allocationPercent * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Text("×", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Return", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(String.format(Locale.US, "%+.2f%%", driver.changePercent), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Text("=", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Column(horizontalAlignment = Alignment.End) {
                    Text("Impact", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(String.format(Locale.US, "%+.2f%%", driver.impactScore), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = baseColor)
                }
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