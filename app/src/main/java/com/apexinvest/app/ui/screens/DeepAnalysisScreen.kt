package com.apexinvest.app.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import com.apexinvest.app.api.models.DeepAnalysisResponse
import com.apexinvest.app.api.models.HistoricalPricePoint
import com.apexinvest.app.api.models.MonteCarloPoint
import com.apexinvest.app.api.models.NewsItem
import com.apexinvest.app.ui.components.CommonScreenHeader
import com.apexinvest.app.ui.components.glassCard
import com.apexinvest.app.ui.theme.BrandPurple
import com.apexinvest.app.ui.theme.LocalAppColors
import com.apexinvest.app.util.StockMetadataUtils
import com.apexinvest.app.util.formatPrice
import com.apexinvest.app.util.getCurrencySymbol
import com.apexinvest.app.util.guessCurrencyFromSymbol
import com.apexinvest.app.viewmodel.AnalysisState
import com.apexinvest.app.viewmodel.PredictionViewModel
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeepAnalysisScreen(
    symbol: String,
    predictionViewModel: PredictionViewModel,
    onBack: () -> Unit,
    isConnected: Boolean
) {
    val analysisState by predictionViewModel.analysisState.collectAsState()
    val view = LocalView.current
    val surfaceColor = MaterialTheme.colorScheme.surface
    val isDark = remember(surfaceColor) { surfaceColor.luminance() < 0.5f }

    val meshBrush = remember(isDark) {
        if (isDark) {
            Brush.verticalGradient(listOf(BrandPurple.copy(alpha = 0.12f), Color.Transparent))
        } else {
            Brush.verticalGradient(listOf(BrandPurple.copy(alpha = 0.05f), Color.Transparent))
        }
    }

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    LaunchedEffect(symbol) {
        val current = predictionViewModel.analysisState.value
        if ((current !is AnalysisState.Success) || (current.data.symbol != symbol)) {
            predictionViewModel.analyzeStock(symbol)
        }
    }

    BackHandler(onBack = onBack)

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
                    Text(symbol, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge)
                    Text("Advanced Analysis Engine", color = BrandPurple, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                }
            }
        )

        Box(modifier = Modifier.weight(1f)) {
            val state = analysisState

            AnimatedContent(targetState = state, label = "AnalysisStateTransition") { targetState ->
                when (targetState) {
                    is AnalysisState.Loading -> {
                        if (targetState.data != null) {
                            AnalysisContent(targetState.data, isDark)
                        } else {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = BrandPurple, strokeWidth = 3.dp)
                                    Spacer(Modifier.height(16.dp))
                                    Text(targetState.message, fontWeight = FontWeight.Bold, color = BrandPurple)
                                }
                            }
                        }
                    }
                    is AnalysisState.Error -> {
                        val appColors = LocalAppColors.current
                        if (targetState.data != null) {
                            AnalysisContent(targetState.data, isDark)
                        } else {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                                    Text(targetState.message, color = appColors.trendRed, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                    Spacer(Modifier.height(16.dp))
                                    Surface(
                                        modifier = Modifier.clickable { predictionViewModel.analyzeStock(symbol, forceRefresh = true) },
                                        color = appColors.trendRed.copy(0.1f),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, appColors.trendRed.copy(0.3f))
                                    ) {
                                        Text("Retry Analysis", color = appColors.trendRed, modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                    is AnalysisState.Success -> {
                        AnalysisContent(targetState.data, isDark)
                    }
                    is AnalysisState.Idle -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = BrandPurple)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalysisContent(stock: DeepAnalysisResponse, isDark: Boolean) {
    val appColors = LocalAppColors.current
    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column {
                    val priceStr = formatPrice(stock.currentPrice, stock.symbol)
                    Text(priceStr, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
                    Text("Current Trading Price", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                }
                val isBull = stock.agentSynthesis.finalVerdict.contains("Buy", true)
                Box(
                    modifier = Modifier
                        .background(if (isBull) appColors.trendGreen.copy(0.15f) else appColors.trendRed.copy(0.15f), RoundedCornerShape(20.dp))
                        .border(1.dp, if (isBull) appColors.trendGreen.copy(0.3f) else appColors.trendRed.copy(0.3f), RoundedCornerShape(20.dp))
                ) {
                    Text(
                        if (isBull) "BUY" else "HOLD/SELL",
                        Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        color = if (isBull) appColors.trendGreen else appColors.trendRed,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        if (stock.historicalChartData.isNotEmpty() || stock.monteCarloForecast.isNotEmpty()) {
            item {
                Text("Historical Trend & AI Forecast (1 Yr)", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium, letterSpacing = (-0.5).sp)
                Spacer(Modifier.height(12.dp))
                PremiumForecastChart(
                    history = stock.historicalChartData,
                    forecast = stock.monteCarloForecast,
                    currentPrice = stock.currentPrice,
                    symbol = stock.symbol,
                    isDark = isDark
                )
            }
        }

        item {
            Box(Modifier.fillMaxWidth().glassCard(isDark)) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(32.dp).background(BrandPurple.copy(0.2f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Memory, null, tint = BrandPurple, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Text("AI Quant Synthesis", fontWeight = FontWeight.Black, color = BrandPurple, fontSize = 18.sp)
                    }
                    Spacer(Modifier.height(20.dp))
                    Text("Fundamentals", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                    Text(stock.agentSynthesis.fundamentalThesis, style = MaterialTheme.typography.bodyMedium, color = if(isDark) Color.White.copy(0.9f) else Color.Black)

                    Spacer(Modifier.height(16.dp))
                    Text("Macro & News", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                    Text(stock.agentSynthesis.macroNewsThesis, style = MaterialTheme.typography.bodyMedium, color = if(isDark) Color.White.copy(0.9f) else Color.Black)

                    Spacer(Modifier.height(20.dp))
                    Box(modifier = Modifier.fillMaxWidth().background(BrandPurple.copy(0.1f), RoundedCornerShape(12.dp)).border(1.dp, BrandPurple.copy(0.2f), RoundedCornerShape(12.dp))) {
                        Text("Verdict: ${stock.agentSynthesis.finalVerdict}", color = BrandPurple, fontWeight = FontWeight.Bold, modifier = Modifier.padding(14.dp))
                    }
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                val rsi = stock.historicalChartData.lastOrNull()?.rsi14 ?: 0.0
                val macd = stock.historicalChartData.lastOrNull()?.macd ?: 0.0
                DeepFundamentalChip("RSI (14)", String.format(Locale.US, "%.1f", rsi), Modifier.weight(1f), if(rsi > 70) appColors.trendRed else if(rsi < 30) appColors.trendGreen else if(isDark) Color.White else Color.Black, isDark)
                DeepFundamentalChip("MACD", String.format(Locale.US, "%.2f", macd), Modifier.weight(1f), if(macd > 0) appColors.trendGreen else appColors.trendRed, isDark)
                DeepFundamentalChip("P/E Ratio", stock.fundamentals.peRatio.toString(), Modifier.weight(1f), if(isDark) Color.White else Color.Black, isDark)
            }
        }

        item {
            Box(Modifier.fillMaxWidth().glassCard(isDark)) {
                Column(Modifier.padding(20.dp)) {
                    Text("Real-Time Sentiment Engine", fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Spacer(Modifier.height(16.dp))
                    DeepLinearSentimentMeter(stock.sentiment.overallScore, isDark)
                }
            }
        }

        item {
            Text("Market Intel Feed", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium, letterSpacing = (-0.5).sp)
            Spacer(Modifier.height(8.dp))
        }

        items(stock.sentiment.newsArticles) { news ->
            val context = LocalContext.current
            val onClick = remember(news.link) {
                {
                    if (news.link.isNotEmpty()) {
                        try { context.startActivity(Intent(Intent.ACTION_VIEW, news.link.toUri())) } catch (e: Exception) { e.printStackTrace() }
                    }
                }
            }
            DeepNewsItemCard(news, isDark, onClick)
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun PremiumForecastChart(
    history: List<HistoricalPricePoint>,
    forecast: List<MonteCarloPoint>,
    currentPrice: Double,
    symbol: String,
    isDark: Boolean
) {
    val currencySymbol = getCurrencySymbol(guessCurrencyFromSymbol(symbol))
    val appColors = LocalAppColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            // 🚀 OPTIMIZATION: Reduced shadow depth for faster scrolling.
            .shadow(if (isDark) 8.dp else 2.dp, RoundedCornerShape(24.dp), spotColor = BrandPurple.copy(0.2f))
            .glassCard(isDark)
    ) {
        val textMeasurer = rememberTextMeasurer()
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp)
                // 🚀 OPTIMIZATION: Cache paths and text layouts.
                .drawWithCache {
                    val w = size.width
                    val h = size.height

                    val histPrices = history.map { it.close }
                    val forecastHighs = forecast.map { it.bullCase90th }
                    val forecastLows = forecast.map { it.bearCase10th }

                    val allPrices = histPrices + forecastHighs + forecastLows + currentPrice
                    val maxPrice = allPrices.maxOrNull() ?: currentPrice
                    val minPrice = allPrices.minOrNull() ?: currentPrice

                    val range = (maxPrice - minPrice).coerceAtLeast(0.1)
                    val paddedMax = maxPrice + (range * 0.05)
                    val paddedMin = minPrice - (range * 0.05)
                    val paddedRange = paddedMax - paddedMin

                    val gridYPositions = listOf(0f, h / 2f, h)
                    val gridValues = listOf(paddedMax, (paddedMax + paddedMin) / 2, paddedMin)

                    val midX = w * 0.5f
                    val histPath = Path()
                    if (history.isNotEmpty()) {
                        val histStep = midX / (history.size.coerceAtLeast(2) - 1)
                        history.forEachIndexed { i, p ->
                            val x = i * histStep
                            val y = (h - ((p.close - paddedMin) / paddedRange * h)).toFloat()
                            if (i == 0) histPath.moveTo(x, y) else histPath.lineTo(x, y)
                        }
                    }

                    val conePath = Path()
                    val meanPath = Path()
                    if (forecast.isNotEmpty()) {
                        val forecastWidth = w - midX
                        val forecastStep = forecastWidth / (forecast.size.coerceAtLeast(2) - 1)

                        forecast.forEachIndexed { i, p ->
                            val x = midX + (i * forecastStep)
                            val y = (h - ((p.bullCase90th - paddedMin) / paddedRange * h)).toFloat()
                            if (i == 0) conePath.moveTo(x, y) else conePath.lineTo(x, y)
                        }
                        for (i in forecast.indices.reversed()) {
                            val p = forecast[i]
                            val x = midX + (i * forecastStep)
                            val y = (h - ((p.bearCase10th - paddedMin) / paddedRange * h)).toFloat()
                            conePath.lineTo(x, y)
                        }
                        conePath.close()

                        forecast.forEachIndexed { i, p ->
                            val x = midX + (i * forecastStep)
                            val y = (h - ((p.meanPrice - paddedMin) / paddedRange * h)).toFloat()
                            if (i == 0) meanPath.moveTo(x, y) else meanPath.lineTo(x, y)
                        }
                    }

                    val currentY = (h - ((currentPrice - paddedMin) / paddedRange * h)).toFloat()
                    
                    val gridColor = if (isDark) Color.White.copy(0.1f) else Color.Black.copy(0.1f)
                    val coneBrush = Brush.verticalGradient(listOf(appColors.trendGreen.copy(0.25f), appColors.trendRed.copy(0.1f)))
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                    // Pre-measure labels
                    val labelStyle = TextStyle(
                        color = if (isDark) Color.LightGray else Color.DarkGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    val gridLabels = gridValues.map { v ->
                        textMeasurer.measure("$currencySymbol${String.format(Locale.US, "%.1f", v)}", labelStyle)
                    }

                    onDrawBehind {
                        gridYPositions.forEachIndexed { index, yPos ->
                            drawLine(color = gridColor, start = Offset(0f, yPos), end = Offset(w, yPos), strokeWidth = 2f, pathEffect = dashEffect)
                            drawText(gridLabels[index], topLeft = Offset(0f, yPos - gridLabels[index].size.height))
                        }

                        if (history.isNotEmpty()) {
                            drawPath(histPath, color = Color.Gray.copy(0.8f), style = Stroke(4.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                        }

                        if (forecast.isNotEmpty()) {
                            drawPath(conePath, brush = coneBrush, style = Fill)
                            drawPath(meanPath, color = BrandPurple, style = Stroke(5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                        }

                        drawLine(color = BrandPurple.copy(0.6f), start = Offset(0f, currentY), end = Offset(w, currentY), strokeWidth = 3f, pathEffect = dashEffect)

                        // Draw Footer Labels
                        val footerStyle = labelStyle.copy(fontSize = 10.sp)
                        val exchangeInfo = StockMetadataUtils.getExchangeInfo(symbol)
                        val zoneId = java.time.ZoneId.of(exchangeInfo.timezone)
                        
                        // 🛠️ Precision: Format labels using native exchange timezone
                        val displayFormatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm", Locale.getDefault())

                        history.firstOrNull()?.date?.let { rawDate ->
                            try {
                                val zdt = ZonedDateTime.parse(rawDate).withZoneSameInstant(zoneId)
                                drawText(textMeasurer, zdt.format(displayFormatter), Offset(0f, h + 10f), footerStyle)
                            } catch (e: Exception) {
                                drawText(textMeasurer, rawDate, Offset(0f, h + 10f), footerStyle)
                            }
                        }

                        drawText(textMeasurer, "Today (${exchangeInfo.tvPrefix})", Offset(midX - 30f, h + 10f), footerStyle)

                        forecast.lastOrNull()?.date?.let { rawDate ->
                            try {
                                val zdt = ZonedDateTime.parse(rawDate).withZoneSameInstant(zoneId)
                                drawText(textMeasurer, zdt.format(displayFormatter), Offset(w - 80f, h + 10f), footerStyle)
                            } catch (e: Exception) {
                                drawText(textMeasurer, rawDate, Offset(w - 40f, h + 10f), footerStyle)
                            }
                        }
                    }
                }
        )
    }
}

@Composable
private fun DeepFundamentalChip(label: String, value: String, modifier: Modifier, valColor: Color, isDark: Boolean) {
    Box(modifier = modifier.glassCard(isDark, RoundedCornerShape(16.dp))) {
        Column(Modifier.padding(14.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Black, style = MaterialTheme.typography.bodyLarge, color = valColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun DeepLinearSentimentMeter(score: Double, isDark: Boolean) {
    val appColors = LocalAppColors.current
    val normalized = ((score + 1.0) / 2.0).toFloat().coerceIn(0f, 1f)
    Column {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text("Bearish", fontSize = 11.sp, color = appColors.trendRed, fontWeight = FontWeight.Bold)
            Text("Neutral", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Text("Bullish", fontSize = 11.sp, color = appColors.trendGreen, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(14.dp).clip(CircleShape).background(if(isDark) Color.White.copy(0.1f) else Color.Black.copy(0.05f))) {
            Box(Modifier.fillMaxHeight().fillMaxWidth(normalized).background(Brush.horizontalGradient(listOf(appColors.trendRed, Color(0xFFFFB300), appColors.trendGreen))))
        }
    }
}

@Composable
private fun DeepNewsItemCard(news: NewsItem, isDark: Boolean, onClick: () -> Unit) {
    val appColors = LocalAppColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(isDark, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(text = news.title, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium, color = if(isDark) Color.White else Color.Black)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = news.publisher, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Spacer(Modifier.width(10.dp))
                    val color = when(news.sentimentLabel) {
                        "Bullish" -> appColors.trendGreen
                        "Bearish" -> appColors.trendRed
                        else -> Color.Gray
                    }
                    Box(modifier = Modifier.size(6.dp).background(color, CircleShape))
                    Spacer(Modifier.width(6.dp))
                    Text(text = news.sentimentLabel, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
                }
            }
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
        }
    }
}
