package com.apexinvest.app.ui.screens.stockdetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apexinvest.app.api.models.CandlePoint
import com.apexinvest.app.ui.components.glassCard
import com.apexinvest.app.ui.theme.BrandPurple
import com.apexinvest.app.ui.theme.LocalAppColors
import com.apexinvest.app.util.getCurrencySymbol
import com.apexinvest.app.util.StockMetadataUtils
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
fun OptimizedGlassChart(
    candles: List<CandlePoint>,
    type: ChartType,
    currentPrice: Double,
    isPositive: Boolean,
    isDark: Boolean,
    currency: String,
    currentRange: String,
    symbol: String // 🆕 Added symbol to resolve native exchange timezone
) {
    if (candles.isEmpty()) return
    val textMeasurer = rememberTextMeasurer()
    var touchX by remember { mutableStateOf<Float?>(null) }
    val appColors = LocalAppColors.current
    val mainColor = if (isPositive) appColors.trendGreen else appColors.trendRed
    val textColor = MaterialTheme.colorScheme.onSurface
    val gridLineColor = MaterialTheme.colorScheme.onSurface.copy(0.05f)
    val trendGreen = appColors.trendGreen
    val trendRed = appColors.trendRed

    // 🛠️ FIX: Dynamic date formatting based on timeframe AND Exchange Timezone
    val exchangeInfo = remember(symbol) { StockMetadataUtils.getExchangeInfo(symbol) }
    val zoneId = remember(exchangeInfo) { ZoneId.of(exchangeInfo.timezone) }

    val dateFormatter = remember(currentRange, zoneId) {
        val pattern = when (currentRange) {
            "1D" -> "HH:mm"
            "5D", "1M" -> "MMM dd, HH:mm"
            else -> "MMM dd, yyyy"
        }
        DateTimeFormatter.ofPattern(pattern, Locale.getDefault()).withZone(zoneId)
    }

    val displayCandles = remember(candles, currentPrice) {
        val last = candles.last()
        candles.dropLast(1) + last.copy(
            close = currentPrice,
            high = max(last.high, currentPrice),
            low = if (last.low == 0.0) currentPrice else min(last.low, currentPrice)
        )
    }

    Spacer(
        modifier = Modifier.fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { touchX = it.x },
                    onDragEnd = { touchX = null },
                    onHorizontalDrag = { change, _ -> touchX = change.position.x }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(onPress = { touchX = it.x; tryAwaitRelease(); touchX = null })
            }
            .drawWithCache {
                val w = size.width
                val h = size.height
                val priceH = h * 0.78f
                val volH = h * 0.16f
                val maxP = displayCandles.maxOfOrNull { it.high } ?: 0.0
                val minP = displayCandles.minOfOrNull { it.low } ?: 0.0
                val pad = (maxP - minP) * 0.06
                val rangeP = ((maxP + pad) - (minP - pad)).coerceAtLeast(0.1)
                val maxV = displayCandles.maxOfOrNull { it.volume }?.toFloat() ?: 1f
                val stepX = w / displayCandles.size

                val path = Path()
                val fill = Path()

                if (type == ChartType.LINE) {
                    displayCandles.forEachIndexed { i, c ->
                        val x = i * stepX + stepX / 2
                        val y = priceH - ((c.close - (minP - pad)) / rangeP * priceH).toFloat()
                        if (i == 0) {
                            path.moveTo(x, y)
                            fill.moveTo(x, priceH)
                            fill.lineTo(x, y)
                        } else {
                            path.lineTo(x, y)
                            fill.lineTo(x, y)
                        }
                    }
                    fill.lineTo((displayCandles.size - 1) * stepX + stepX / 2, priceH)
                    fill.close()
                }

                val fillBrush = Brush.verticalGradient(listOf(mainColor.copy(0.25f), Color.Transparent), endY = priceH)
                val lineStroke = Stroke(2.2.dp.toPx(), cap = StrokeCap.Round)
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                val crosshairDash = PathEffect.dashPathEffect(floatArrayOf(12f, 12f))

                onDrawBehind {
                    // Draw horizontal background grids
                    for (i in 1..4) {
                        val y = priceH * (i / 4f)
                        drawLine(gridLineColor, Offset(0f, y), Offset(w, y), 1f, pathEffect = dashEffect)
                    }

                    // Draw volume bars at the base
                    displayCandles.forEachIndexed { i, c ->
                        val vh = (c.volume / maxV) * volH
                        val volColor = if (c.close >= c.open) trendGreen.copy(0.18f) else trendRed.copy(0.18f)
                        drawRect(volColor, Offset(i * stepX + 0.5f, h - vh), Size((stepX - 0.5f).coerceAtLeast(1f), vh))
                    }

                    // Draw primary price configurations
                    if (type == ChartType.LINE) {
                        drawPath(fill, fillBrush)
                        drawPath(path, mainColor, style = lineStroke)
                    } else {
                        val cw = (stepX * 0.65f).coerceIn(2f, 12f)
                        displayCandles.forEachIndexed { i, c ->
                            val x = i * stepX + stepX / 2
                            val hy = priceH - ((c.high - (minP - pad)) / rangeP * priceH).toFloat()
                            val ly = priceH - ((c.low - (minP - pad)) / rangeP * priceH).toFloat()
                            val oy = priceH - ((c.open - (minP - pad)) / rangeP * priceH).toFloat()
                            val cy = priceH - ((c.close - (minP - pad)) / rangeP * priceH).toFloat()
                            val color = if (c.close >= c.open) trendGreen else trendRed
                            drawLine(color, Offset(x, hy), Offset(x, ly), 1.2.dp.toPx())
                            drawRect(color, Offset(x - cw / 2, min(oy, cy)), Size(cw, max(1.5f, abs(oy - cy))))
                        }
                    }

                    // Track scrubbing interaction & display tooltip HUD
                    touchX?.let { tx ->
                        val idx = (tx / stepX).toInt().coerceIn(0, displayCandles.lastIndex)
                        val pt = displayCandles[idx]
                        val x = idx * stepX + stepX / 2
                        val y = priceH - ((pt.close - (minP - pad)) / rangeP * priceH).toFloat()

                        drawLine(textColor.copy(0.35f), Offset(x, 0f), Offset(x, h), 1.dp.toPx(), pathEffect = crosshairDash)
                        drawLine(textColor.copy(0.2f), Offset(0f, y), Offset(w, y), 1.dp.toPx(), pathEffect = crosshairDash)
                        drawCircle(mainColor, 6.dp.toPx(), Offset(x, y))
                        drawCircle(Color.White, 2.5.dp.toPx(), Offset(x, y))

                        // 🛠️ ROBUST FORMATTING: Support both epoch seconds and formatted strings
                        val formattedTime = if (pt.time.all { it.isDigit() } && pt.time.length >= 10) {
                            val epochTime = pt.time.toLongOrNull() ?: 0L
                            try {
                                val instant = Instant.ofEpochSecond(epochTime)
                                dateFormatter.format(instant)
                            } catch (e: Exception) {
                                pt.time
                            }
                        } else {
                            // If it's already a string like "09:30" or contains non-digits, show as-is
                            pt.time
                        }

                        val txt = "$formattedTime  •  ${getCurrencySymbol(currency)}${String.format(Locale.US, "%.2f", pt.close)}"
                        val layout = textMeasurer.measure(
                            text = txt,
                            style = TextStyle(color = if (isDark) Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        )
                        val bw = layout.size.width + 24f
                        val bh = layout.size.height + 12f
                        val bx = (w - bw) / 2f
                        val by = 8f

                        drawRoundRect(
                            color = if (isDark) Color.White.copy(0.95f) else Color.Black.copy(0.85f),
                            topLeft = Offset(bx, by),
                            size = Size(bw, bh),
                            cornerRadius = CornerRadius(12f, 12f)
                        )
                        drawText(
                            textMeasurer = textMeasurer,
                            text = txt,
                            topLeft = Offset(bx + 12f, by + 6f),
                            style = TextStyle(color = if (isDark) Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
    )
}

@Composable
fun TimeframePills(current: String, isDark: Boolean, onSelect: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        // Changed "1W" directly to "5D" to match Yahoo data feeds cleanly
        listOf("1D", "5D", "1M", "1Y", "5Y", "MAX").forEach { range ->
            val isSelected = range == current
            val bg = if (isSelected) (if (isDark) Color.White.copy(0.15f) else Color.Black.copy(0.1f)) else Color.Transparent
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(bg)
                    .clickable { onSelect(range) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = range,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ChartTypeToggle(current: ChartType, isDark: Boolean, onSelect: (ChartType) -> Unit) {
    Row(Modifier.glassCard(isDark, RoundedCornerShape(12.dp)).padding(2.dp)) {
        listOf(
            ChartType.LINE to Icons.AutoMirrored.Filled.ShowChart,
            ChartType.CANDLE to Icons.Default.CandlestickChart
        ).forEach { (type, icon) ->
            val isSel = type == current
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSel) BrandPurple else Color.Transparent)
                    .clickable { onSelect(type) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}