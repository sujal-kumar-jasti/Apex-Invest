package com.apexinvest.app.ui.screens.stockdetail.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apexinvest.app.ui.components.glassCard
import com.apexinvest.app.ui.theme.BrandPurple
import com.apexinvest.app.ui.theme.LocalAppColors
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

enum class ChartType { LINE, CANDLE }
enum class Sentiment { POSITIVE, NEGATIVE, NEUTRAL }

@Composable
fun MetricColumn(label: String, value: String?, isDark: Boolean, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(4.dp))
        Text(value ?: "-", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun ExplainerMetricColumn(label: String, value: String?, sentiment: Sentiment, isDark: Boolean, modifier: Modifier = Modifier) {
    val appColors = LocalAppColors.current
    val valueColor = when (sentiment) {
        Sentiment.POSITIVE -> appColors.trendGreen
        Sentiment.NEGATIVE -> appColors.trendRed
        Sentiment.NEUTRAL -> MaterialTheme.colorScheme.onSurface
    }

    Column(modifier.padding(vertical = 4.dp)) {
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(4.dp))
        Text(value ?: "-", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
fun GlassPaneCard(title: String, isDark: Boolean, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().glassCard(isDark).padding(20.dp)) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(16.dp))
        content()
    }
}

@Composable
fun VisualProgressBar(label: String, value: Double?, color: Color, isDark: Boolean) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value.fmtPct(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(Modifier.height(6.dp))
        Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(0.08f))) {
            val progress = ((value ?: 0.0) / 100.0).toFloat().coerceIn(0f, 1f)
            Box(Modifier.fillMaxWidth(progress).fillMaxHeight().clip(CircleShape).background(color))
        }
    }
}

@Composable
fun TradingViewGauge(
    ratingValue: Double,
    title: String,
    subtitle: String,
    isDark: Boolean,
    isAnalyst: Boolean,
    customLabel: String? = null
) {
    val textMeasurer = rememberTextMeasurer()
    val appColors = LocalAppColors.current
    val textColor = MaterialTheme.colorScheme.onSurface
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(0.08f)
    val needleColor = MaterialTheme.colorScheme.onSurface
    val finalRatingValue = remember(ratingValue, customLabel) {
        val value = ratingValue.toFloat()
        if (customLabel == null) return@remember value

        when {
            customLabel.contains("Strong Buy", ignoreCase = true) -> if (value >= 0.6f) value else 0.8f
            customLabel.contains("Buy", ignoreCase = true) -> if (value in 0.2f..0.6f) value else 0.4f
            customLabel.contains("Neutral", ignoreCase = true) || customLabel.contains("Hold", ignoreCase = true) -> if (value in -0.2f..0.2f) value else 0.0f
            customLabel.contains("Strong Sell", ignoreCase = true) -> if (value <= -0.6f) value else -0.8f
            customLabel.contains("Sell", ignoreCase = true) -> if (value in -0.6f..-0.2f) value else -0.4f
            else -> value
        }
    }

    // Needle animation now targets the smart override value
    val animatedRating by animateFloatAsState(
        targetValue = finalRatingValue.coerceIn(-1f, 1f),
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "NeedleAnimation"
    )

    val textLabel = customLabel ?: when {
        ratingValue <= -0.6 -> "Strong Sell"
        ratingValue <= -0.2 -> "Sell"
        ratingValue < 0.2 -> "Neutral"
        ratingValue < 0.6 -> "Buy"
        else -> "Strong Buy"
    }

    val labelColor = when {
        textLabel.contains("Sell", ignoreCase = true) -> appColors.trendRed
        textLabel.contains("Buy", ignoreCase = true) -> appColors.trendGreen
        else -> textColor
    }

    val arcColor = if (isAnalyst) appColors.trendGreen else BrandPurple
    val labelColorSecondary = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f)

    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor)
            Icon(Icons.Default.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 8.dp).size(16.dp))
        }
        Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp, bottom = 16.dp))

        Box(modifier = Modifier.fillMaxWidth().height(210.dp), contentAlignment = Alignment.Center) {
            // 🚀 OPTIMIZATION: Use drawWithCache to cache label layouts and arc geometry.
            Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).drawWithCache {
                val gaugeWidth = 24.dp.toPx()
                val r = (size.width / 2) - 55.dp.toPx()
                val centerX = size.width / 2
                val centerY = size.height - 45.dp.toPx()

                // Pre-measure all static labels
                val labels = listOf("Strong sell", "Sell", "Neutral", "Buy", "Strong buy")
                val labelRadius = r + gaugeWidth / 2 + 32.dp.toPx()
                val labelLayouts = labels.map { text ->
                    textMeasurer.measure(
                        text = text,
                        style = TextStyle(
                            color = labelColorSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                onDrawBehind {
                    // Draw Background Track
                    drawArc(
                        color = trackColor,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        style = Stroke(gaugeWidth, cap = StrokeCap.Round),
                        topLeft = Offset(centerX - r, centerY - r),
                        size = Size(r * 2, r * 2)
                    )

                    // Draw Filled Arc
                    val mappedRating = ((animatedRating + 1f) / 2f).coerceIn(0f, 1f)
                    val sweepAngle = mappedRating * 180f

                    drawArc(
                        color = arcColor,
                        startAngle = 180f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(gaugeWidth, cap = StrokeCap.Round),
                        topLeft = Offset(centerX - r, centerY - r),
                        size = Size(r * 2, r * 2)
                    )

                    // Draw Pre-measured Arc Labels
                    labelLayouts.forEachIndexed { index, layout ->
                        val angle = PI - (index * (PI / 4))
                        val labelX = centerX + (labelRadius * cos(angle)).toFloat()
                        val labelY = centerY - (labelRadius * sin(angle)).toFloat()
                        drawText(layout, topLeft = Offset(labelX - layout.size.width / 2, labelY - layout.size.height / 2))
                    }

                    // Draw Needle
                    val needleAngle = PI - (mappedRating * PI)
                    val needleLength = r - gaugeWidth / 2 + 8f
                    val endX = centerX + (needleLength * cos(needleAngle)).toFloat()
                    val endY = centerY - (needleLength * sin(needleAngle)).toFloat()

                    drawLine(
                        color = needleColor,
                        start = Offset(centerX, centerY),
                        end = Offset(endX, endY),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    drawCircle(color = needleColor, radius = 6.dp.toPx(), center = Offset(centerX, centerY))

                    // Draw Center Text (Measure dynamic label)
                    val valueLayout = textMeasurer.measure(
                        text = textLabel,
                        style = TextStyle(color = labelColor, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    )

                    drawText(
                        valueLayout,
                        topLeft = Offset(centerX - valueLayout.size.width / 2, centerY + 16.dp.toPx())
                    )
                }
            }) {}
        }
    }
}

@Composable
fun GlassShimmer(isDark: Boolean, height: androidx.compose.ui.unit.Dp = 300.dp, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "Shimmer")
    val alpha by transition.animateFloat(initialValue = 0.05f, targetValue = 0.15f, animationSpec = infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "Alpha")
    Box(modifier.fillMaxWidth().height(height).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha)))
}

@Composable
fun DomainTabs(selected: Int, isDark: Boolean, onSelect: (Int) -> Unit) {
    val tabs = listOf("Overview", "Techs", "Finance", "Risk", "Compare", "News")
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp).glassCard(isDark, RoundedCornerShape(50)).padding(4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selected == index
            val animatedBg by animateColorAsState(if (isSelected) (if(isDark) Color.White.copy(0.15f) else Color.Black.copy(0.1f)) else Color.Transparent, label = "TabSel")
            Box(Modifier.weight(1f).height(38.dp).clip(CircleShape).background(animatedBg).clickable { onSelect(index) }, contentAlignment = Alignment.Center) {
                Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

fun getSentiment(value: Double?, minGood: Double, maxGood: Double): Sentiment {
    if (value == null || value.isNaN()) return Sentiment.NEUTRAL
    if (minGood == 0.0 && maxGood == 0.0) return if (value > 0) Sentiment.POSITIVE else if (value < 0) Sentiment.NEGATIVE else Sentiment.NEUTRAL
    return if (value >= minGood && (maxGood == 0.0 || value <= maxGood)) Sentiment.POSITIVE else Sentiment.NEGATIVE
}

fun Double?.fmt(): String = this?.takeIf { !it.isNaN() }?.let { String.format(Locale.US, "%.2f", it) } ?: "-"
fun Double?.fmtPct(): String = this?.takeIf { !it.isNaN() }?.let { String.format(Locale.US, "%.2f%%", it) } ?: "-"
fun Double?.fmtCompact(): String {
    if (this == null || this.isNaN()) return "-"
    val absVal = abs(this)
    return when {
        absVal >= 1_000_000_000 -> String.format(Locale.US, "%.2fB", this / 1_000_000_000)
        absVal >= 10_000_000 -> String.format(Locale.US, "%.2fCr", this / 10_000_000)
        absVal >= 1_000_000 -> String.format(Locale.US, "%.2fM", this / 1_000_000)
        else -> this.fmt()
    }
}
