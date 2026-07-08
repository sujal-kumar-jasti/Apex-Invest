package com.apexinvest.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlin.math.abs

@Composable
fun rememberShimmerAlpha(): Float {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "alpha",
    )
    return alpha
}

@Composable
fun AppTickerText(
    value: Double,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
    currencySymbol: String = "",
    showExplicitSign: Boolean = false,
    suffix: String = ""
) {
    val formattedValue = remember(value) { String.format(Locale.US, "%,.2f", abs(value)) }
    val targetString = remember(formattedValue, suffix) { formattedValue + suffix }

    val signPrefix = remember(value, showExplicitSign) {
        when {
            value < 0 -> "-"
            (showExplicitSign && value > 0) -> "+"
            else -> ""
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = modifier
    ) {
        if (currencySymbol.isNotEmpty() || signPrefix.isNotEmpty()) {
            Text(
                text = "$signPrefix$currencySymbol",
                style = textStyle.copy(fontFeatureSettings = "tnum"),
                maxLines = 1,
                softWrap = false
            )
        }
        Text(
            text = targetString,
            style = textStyle.copy(fontFeatureSettings = "tnum"),
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AppSparkline(data: List<Double>, color: Color, modifier: Modifier = Modifier) {
    if (data.isEmpty()) return

    // 🚀 Robustness: If only one point, create a horizontal line
    val plotData = if (data.size == 1) listOf(data[0], data[0]) else data

    // 🚀 OPTIMIZATION: Use drawWithCache to cache the Path object.
    // This prevents allocating a new Path on every single draw frame (e.g. during scroll).
    Spacer(
        modifier = modifier.drawWithCache {
            val path = Path()
            val max = plotData.maxOrNull() ?: 1.0
            val min = plotData.minOrNull() ?: 0.0
            val range = (max - min).coerceAtLeast(0.001)
            
            val w = size.width
            val h = size.height
            val step = w / (plotData.size - 1)

            plotData.forEachIndexed { i, v ->
                val x = i * step
                val y = (h - ((v - min) / range * h)).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            onDrawBehind {
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    )
}

fun Modifier.glassCard(isDark: Boolean, shape: RoundedCornerShape = RoundedCornerShape(24.dp)) = this
    .clip(shape)
    .background(if (isDark) Color(0xFF1E1E1E) else Color(0xFFF5F5F5))
    .border(1.dp, if (isDark) Color(0xFF2C2C2C) else Color(0xFFEEEEEE), shape)
