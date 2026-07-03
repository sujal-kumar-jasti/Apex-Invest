package com.apexinvest.app.ui.screens.stockdetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apexinvest.app.api.models.CandlePoint
import com.apexinvest.app.api.models.HistoricalReturns
import com.apexinvest.app.ui.theme.LocalAppColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

@Composable
fun MomentumPane(
    ret: HistoricalReturns?,
    candles: List<CandlePoint>,
    currentRange: String,
    isDark: Boolean
) {
    if (ret == null) {
        // Fallback or Shimmer state loader placeholder block
        Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
            Text("Loading Performance Metrics...", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    // Modern glass-styled metric summary grid layout
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            ExplainerMetricColumn("1 Week", ret.return1W.fmtPct(), getSentiment(ret.return1W, 0.0, 0.0), isDark, Modifier.weight(1f))
            ExplainerMetricColumn("1 Month", ret.return1M.fmtPct(), getSentiment(ret.return1M, 0.0, 0.0), isDark, Modifier.weight(1f))
            ExplainerMetricColumn("3 Months", ret.return3M.fmtPct(), getSentiment(ret.return3M, 0.0, 0.0), isDark, Modifier.weight(1f))
            ExplainerMetricColumn("6 Months", ret.return6M.fmtPct(), getSentiment(ret.return6M, 0.0, 0.0), isDark, Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth()) {
            ExplainerMetricColumn("YTD", ret.returnYtd.fmtPct(), getSentiment(ret.returnYtd, 0.0, 0.0), isDark, Modifier.weight(1f))
            ExplainerMetricColumn("1 Year", ret.return1Y.fmtPct(), getSentiment(ret.return1Y, 0.0, 0.0), isDark, Modifier.weight(1f))
            ExplainerMetricColumn("3 Years", ret.return3Y.fmtPct(), getSentiment(ret.return3Y, 0.0, 0.0), isDark, Modifier.weight(1f))
            ExplainerMetricColumn("5 Years", ret.return5Y.fmtPct(), getSentiment(ret.return5Y, 0.0, 0.0), isDark, Modifier.weight(1f))
        }
    }

    Spacer(Modifier.height(24.dp))

    // Yearly Overlapping seasonality view panel
    Text(
        text = "Yearly Comparison Trends",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
    )

    // Accept either 5D or 1W based adjustments comfortably
    if (currentRange != "MAX" && currentRange != "5Y") {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(MaterialTheme.colorScheme.onSurface.copy(0.04f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Select '5Y' or 'MAX' above to view overlapping yearly trends.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                modifier = Modifier.padding(24.dp)
            )
        }
    } else {
        YearlySeasonalityChart(candles = candles, isDark = isDark)
    }
}

@Composable
fun YearlySeasonalityChart(candles: List<CandlePoint>, isDark: Boolean) {
    if (candles.size < 15) return

    val appColors = LocalAppColors.current
    val yearFormatter = remember { SimpleDateFormat("yyyy", Locale.getDefault()) }

    // Fixes blank render problem by validating raw epochs along with standard ISO string styles
    val yearlyData = remember(candles) {
        val groups = mutableMapOf<String, MutableList<CandlePoint>>()
        candles.forEach { c ->
            val epochSeconds = c.time.toLongOrNull()
            val year = if (epochSeconds != null) {
                val millis = if (epochSeconds < 100000000000L) epochSeconds * 1000 else epochSeconds
                yearFormatter.format(Date(millis))
            } else {
                c.time.take(4)
            }

            if (year.length == 4 && year.toIntOrNull() != null && year.startsWith("20")) {
                groups.getOrPut(year) { mutableListOf() }.add(c)
            }
        }

        // Pick the 3 most recent sequential calendar years to prevent overlapping graph clutter
        groups.entries
            .sortedByDescending { it.key }
            .take(3)
            .reversed()
            .map { (year, list) ->
                val startPrice = if (list.first().close == 0.0) 1.0 else list.first().close
                val percentages = list.map { ((it.close - startPrice) / startPrice * 100).toFloat() }
                year to percentages
            }
    }

    if (yearlyData.isEmpty()) return

    var touchX by remember { mutableStateOf<Float?>(null) }
    val textMeasurer = rememberTextMeasurer()
    val colors = listOf(appColors.trendOrange, appColors.trendGreen, appColors.trendBlue)

    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(240.dp)) {
            Spacer(
                Modifier.fillMaxSize()
                    .padding(top = 20.dp, bottom = 20.dp, start = 8.dp, end = 55.dp)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = { touchX = it.x },
                            onDragEnd = { touchX = null },
                            onDragCancel = { touchX = null },
                            onHorizontalDrag = { change, _ -> touchX = change.position.x }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(onPress = { touchX = it.x; tryAwaitRelease(); touchX = null })
                    }
                    .drawWithCache {
                        val w = size.width
                        val h = size.height

                        var minPct = 0f
                        var maxPct = 0f
                        var maxDaysInYear = 1

                        yearlyData.forEach { (_, pcts) ->
                            minPct = min(minPct, pcts.minOrNull() ?: 0f)
                            maxPct = max(maxPct, pcts.maxOrNull() ?: 0f)
                            maxDaysInYear = max(maxDaysInYear, pcts.size)
                        }

                        val pad = (maxPct - minPct) * 0.12f
                        minPct -= pad
                        maxPct += pad
                        val rangeP = (maxPct - minPct).coerceAtLeast(1f)
                        val stepX = w / (maxDaysInYear - 1).coerceAtLeast(1).toFloat()

                        val zeroY = h - ((0f - minPct) / rangeP * h)
                        val crosshairDash = PathEffect.dashPathEffect(floatArrayOf(12f, 12f))

                        val yearPaths = yearlyData.map { (_, pcts) ->
                            val path = Path()
                            pcts.forEachIndexed { j, pct ->
                                val x = j * stepX
                                val y = h - ((pct - minPct) / rangeP * h)
                                if (j == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }
                            path
                        }

                        onDrawBehind {
                            // Baseline zero reference indicator line
                            drawLine(color = onSurface.copy(0.15f), start = Offset(0f, zeroY), end = Offset(w, zeroY), strokeWidth = 1.dp.toPx())

                            // Y-Axis Metric scale labels
                            val textStyle = TextStyle(color = onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                            drawText(
                                textMeasurer = textMeasurer,
                                text = "${maxPct.toInt()}%",
                                topLeft = Offset(w + 12f, 0f),
                                style = textStyle,
                                softWrap = false
                            )
                            drawText(
                                textMeasurer = textMeasurer,
                                text = "0%",
                                topLeft = Offset(w + 12f, zeroY - 7.dp.toPx()),
                                style = textStyle,
                                softWrap = false
                            )
                            drawText(
                                textMeasurer = textMeasurer,
                                text = "${minPct.toInt()}%",
                                topLeft = Offset(w + 12f, h - 14.dp.toPx()),
                                style = textStyle,
                                softWrap = false
                            )

                            // Draw normalized relative curves
                            yearlyData.forEachIndexed { i, (_, pcts) ->
                                val color = colors[i % colors.size]
                                drawPath(yearPaths[i], color, style = Stroke(2.2.dp.toPx(), cap = StrokeCap.Round))

                                if (pcts.isNotEmpty()) {
                                    val lastX = (pcts.size - 1) * stepX
                                    val lastY = h - ((pcts.last() - minPct) / rangeP * h)
                                    drawCircle(color, 4.dp.toPx(), Offset(lastX, lastY))
                                }
                            }

                            // Interaction scrub overlay
                            touchX?.let { tX ->
                                val clampedTx = tX.coerceIn(0f, w)
                                val index = (clampedTx / stepX).toInt().coerceIn(0, maxDaysInYear - 1)
                                val crosshairX = index * stepX

                                drawLine(color = onSurface.copy(0.25f), start = Offset(crosshairX, 0f), end = Offset(crosshairX, h), strokeWidth = 1.dp.toPx(), pathEffect = crosshairDash)

                                val tooltipRows = yearlyData.mapIndexed { i, (year, pcts) ->
                                    val clr = colors[i % colors.size]
                                    if (index < pcts.size) {
                                        val pct = pcts[index]
                                        val y = h - ((pct - minPct) / rangeP * h)
                                        drawCircle(clr, 5.dp.toPx(), Offset(crosshairX, y))
                                        drawCircle(Color.White, 2.dp.toPx(), Offset(crosshairX, y))
                                        Triple(clr, year, pct)
                                    } else {
                                        Triple(clr, year, null)
                                    }
                                }.reversed()

                                val tooltipStyle = TextStyle(color = if (isDark) Color.White else Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                val valueStyle = TextStyle(color = if (isDark) Color.White.copy(0.8f) else Color.Black.copy(0.8f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)

                                var maxCalculatedWidth = 0f
                                tooltipRows.forEach { (_, year, pct) ->
                                    val valStr = pct?.let { String.format(Locale.US, "${if(it > 0) "+" else "" }%.2f%%", it) } ?: "--"
                                    val yearLayout = textMeasurer.measure(year, tooltipStyle)
                                    val valLayout = textMeasurer.measure(valStr, valueStyle)
                                    val rowWidth = 24f + 16f + yearLayout.size.width + 32f + valLayout.size.width + 24f
                                    maxCalculatedWidth = max(maxCalculatedWidth, rowWidth)
                                }

                                val boxWidth = maxCalculatedWidth.coerceAtLeast(145f)
                                val boxHeight = (tooltipRows.size * 30f) + 16f
                                val boxX = if (crosshairX + boxWidth + 20f > w) crosshairX - boxWidth - 20f else crosshairX + 20f
                                val boxY = 12f

                                drawRoundRect(
                                    color = (if (isDark) Color(0xFF16161A) else Color(0xFFF0F1F5)).copy(0.96f),
                                    topLeft = Offset(boxX, boxY),
                                    size = Size(boxWidth, boxHeight),
                                    cornerRadius = CornerRadius(16f, 16f)
                                )

                                var currentY = boxY + 10f
                                tooltipRows.forEach { (color, year, pct) ->
                                    drawCircle(color, 8f, Offset(boxX + 16f, currentY + 14f))
                                    drawText(textMeasurer, year, Offset(boxX + 30f, currentY + 4f), tooltipStyle)

                                    val valStr = pct?.let { String.format(Locale.US, "${if(it > 0) "+" else "" }%.2f%%", it) } ?: "--"
                                    val valLayout = textMeasurer.measure(valStr, valueStyle)
                                    drawText(textMeasurer, valStr, Offset(boxX + boxWidth - valLayout.size.width - 16f, currentY + 4f), valueStyle)
                                    currentY += 30f
                                }
                            }
                        }
                    }
            )
        }

        // Inline color legends row
        Row(
            modifier = Modifier.padding(start = 8.dp, top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            yearlyData.reversed().forEachIndexed { i, (yr, _) ->
                val colorIndex = (yearlyData.size - 1 - i) % colors.size
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(colors[colorIndex]))
                    Spacer(Modifier.width(6.dp))
                    Text(text = yr, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable fun ExplainerMetricColumn(l: String, v: String, s: Int, d: Boolean, m: Modifier) { Column(m) { Text(l, fontSize = 11.sp); Text(v, fontWeight = FontWeight.Bold) } }
fun Double.fmtPct(): String = String.format(Locale.US, "%.2f%%", this)
fun getSentiment(v: Double, low: Double, high: Double): Int = 0