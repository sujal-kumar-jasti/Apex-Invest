package com.apexinvest.app.ui.components

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun OptimizedGrowthChart(
    chartData: List<Double>,
    color: Color,
    modifier: Modifier = Modifier,
    onPointSelected: (Int) -> Unit = {},
    onSelectionCleared: () -> Unit = {}
) {
    var touchX by remember { mutableFloatStateOf(-1f) }
    val currentData by rememberUpdatedState(chartData)

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { 
                        if (currentData.isEmpty()) return@detectHorizontalDragGestures
                        touchX = (it.x / size.width).coerceIn(0f, 1f)
                        val idx = (touchX * (currentData.size - 1)).toInt().coerceIn(0, currentData.lastIndex)
                        onPointSelected(idx)
                    },
                    onDragEnd = { 
                        touchX = -1f
                        onSelectionCleared()
                    },
                    onHorizontalDrag = { change, _ ->
                        if (currentData.isEmpty()) return@detectHorizontalDragGestures
                        touchX = (change.position.x / size.width).coerceIn(0f, 1f)
                        val idx = (touchX * (currentData.size - 1)).toInt().coerceIn(0, currentData.lastIndex)
                        onPointSelected(idx)
                    }
                )
            }
            .drawWithCache {
                if (chartData.isEmpty()) return@drawWithCache onDrawBehind {}

                val w = size.width
                val h = size.height
                val max = chartData.maxOrNull() ?: 1.0
                val min = chartData.minOrNull() ?: 0.0
                val r = (max - min).coerceAtLeast(1.0)
                val step = w / (chartData.size - 1).coerceAtLeast(1)

                fun calcY(v: Double) = (h - ((v - min) / r * h)).toFloat()

                // Cache paths
                val linePath = Path()
                val fillPath = Path()

                chartData.forEachIndexed { i, v ->
                    val x = i * step
                    val y = calcY(v)
                    if (i == 0) {
                        linePath.moveTo(x, y)
                        fillPath.moveTo(x, h)
                        fillPath.lineTo(x, y)
                    } else {
                        linePath.lineTo(x, y)
                        fillPath.lineTo(x, y)
                    }
                }
                fillPath.lineTo(w, h)
                fillPath.close()

                // Pre-allocate objects
                val gradientBrush = Brush.verticalGradient(listOf(color.copy(alpha = 0.2f), Color.Transparent))
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f))
                val cursorDashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                val strokeStyle = Stroke(3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

                val baseLineColor = Color.Gray.copy(alpha = 0.2f)
                val cursorLineColor = Color.Gray.copy(alpha = 0.5f)
                val startY = calcY(chartData.first())

                onDrawBehind {
                    // Static elements
                    drawLine(baseLineColor, Offset(0f, startY), Offset(w, startY), 2f, pathEffect = dashEffect)
                    drawPath(fillPath, gradientBrush)
                    drawPath(linePath, color, style = strokeStyle)

                    // Interaction redraw
                    if (touchX != -1f) {
                        val idx = (touchX * (chartData.size - 1)).toInt().coerceIn(0, chartData.lastIndex)
                        val x = idx * step
                        val y = calcY(chartData[idx])

                        drawLine(cursorLineColor, Offset(x, 0f), Offset(x, h), 2f, pathEffect = cursorDashEffect)
                        drawCircle(Color.White, 6.dp.toPx(), Offset(x, y))
                        drawCircle(color, 4.dp.toPx(), Offset(x, y))
                    }
                }
            }
    )
}