package com.apexinvest.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
fun AppCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = containerColor
    ) {
        Column(Modifier.padding(16.dp)) {
            content()
        }
    }
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
            showExplicitSign && value > 0 -> "+"
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
    if (data.size < 2) return

    // 🚀 OPTIMIZATION: Use drawWithCache to cache the Path object.
    // This prevents allocating a new Path on every single draw frame (e.g. during scroll).
    Spacer(
        modifier = modifier.drawWithCache {
            val path = Path()
            val max = data.maxOrNull() ?: 1.0
            val min = data.minOrNull() ?: 0.0
            val range = (max - min).coerceAtLeast(0.001)
            
            val w = size.width
            val h = size.height
            val step = w / (data.size - 1)

            data.forEachIndexed { i, v ->
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

@Composable
fun AppSkeleton(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    )
}

fun Modifier.glassCard(isDark: Boolean, shape: RoundedCornerShape = RoundedCornerShape(24.dp)) = this
    .clip(shape)
    .background(if (isDark) Color(0xFF1E1E1E) else Color(0xFFF5F5F5))
    .border(1.dp, if (isDark) Color(0xFF2C2C2C) else Color(0xFFEEEEEE), shape)
