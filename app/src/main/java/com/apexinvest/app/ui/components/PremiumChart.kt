package com.apexinvest.app.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun PremiumLineChart(
    dataPoints: List<Double>,
    isPositive: Boolean,
    modifier: Modifier = Modifier,
    strokeColor: Color? = null
) {
    // Return an empty Spacer to prevent the layout from collapsing if data is missing
    if (dataPoints.isEmpty()) {
        Spacer(modifier = modifier)
        return
    }

    // Handle single data point
    val processedPoints = if (dataPoints.size == 1) listOf(dataPoints[0], dataPoints[0]) else dataPoints

    val defaultGreen = Color(0xFF00E676)
    val defaultRed = Color(0xFFFF3D00)
    val baseTrendColor = strokeColor ?: if (isPositive) defaultGreen else defaultRed
    val isMiniChart = strokeColor != null

    // Data Downsampling
    // Prevents the drawWithCache block from calculating thousands of cubic bezier
    // curves at once during the initial composition spike.
    val optimizedData = remember(processedPoints) {
        val maxPoints = 150
        if (processedPoints.size > maxPoints) {
            val step = processedPoints.size.toFloat() / maxPoints
            List(maxPoints) { i -> processedPoints[(i * step).toInt()] }
        } else {
            processedPoints
        }
    }

    val pathMeasure = remember { PathMeasure() }

    // Pre-calculate min/max using the optimized, smaller dataset
    val minVal = remember(optimizedData) { (optimizedData.minOrNull() ?: 0.0).toFloat() }
    val maxVal = remember(optimizedData) { (optimizedData.maxOrNull() ?: 0.0).toFloat() }
    val range = remember(minVal, maxVal) { (maxVal - minVal).coerceAtLeast(0.01f) }

    Spacer(modifier = modifier.drawWithCache {
        val width = size.width
        val height = size.height

        // Safety check to prevent drawing before layout finishes measuring
        if (width <= 0f || height <= 0f) {
            return@drawWithCache onDrawBehind {}
        }

        val verticalPadding = if (isMiniChart) 2.dp.toPx() else 8.dp.toPx()
        val drawHeight = height - (verticalPadding * 2)

        val strokePath = Path()
        val fillPath = Path()

        // 1. Build the smooth Bezier path EXACTLY ONCE using optimizedData
        val startX = 0f
        val startY = height - verticalPadding - ((optimizedData.first().toFloat() - minVal) / range * drawHeight)
        strokePath.moveTo(startX, startY)

        var prevX = startX
        var prevY = startY
        val stepX = width / (optimizedData.size - 1)

        for (i in 1 until optimizedData.size) {
            val x = i * stepX
            val y = height - verticalPadding - ((optimizedData[i].toFloat() - minVal) / range * drawHeight)

            val controlX = (prevX + x) / 2f
            strokePath.cubicTo(controlX, prevY, controlX, y, x, y)

            prevX = x
            prevY = y
        }

        fillPath.addPath(strokePath)
        fillPath.lineTo(width, height)
        fillPath.lineTo(0f, height)
        fillPath.close()

        // Calculate length ONCE here
        pathMeasure.setPath(strokePath, false)
        val pathLength = pathMeasure.length

        val baselineY = height - verticalPadding - ((optimizedData.first().toFloat() - minVal) / range * drawHeight)

        // Hoist all object allocations outside the RenderThread block
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
        val strokeStyle = Stroke(
            width = if (isMiniChart) 2.dp.toPx() else 3.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
        val dotOuterRadius = 5.dp.toPx()
        val dotInnerRadius = 3.dp.toPx()
        val baselineColor = Color.Gray.copy(alpha = 0.2f)

        onDrawBehind {

            if (!isMiniChart) {
                drawLine(
                    color = baselineColor,
                    start = Offset(0f, baselineY),
                    end = Offset(width, baselineY),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = dashEffect
                )
            }

            drawPath(
                path = strokePath,
                color = baseTrendColor,
                style = strokeStyle
            )

            if (!isMiniChart && pathLength > 0f) {
                val dotPos = pathMeasure.getPosition(pathLength)
                if (dotPos != Offset.Unspecified) {
                    drawCircle(
                        color = Color.White,
                        radius = dotOuterRadius,
                        center = dotPos
                    )
                    drawCircle(
                        color = baseTrendColor,
                        radius = dotInnerRadius,
                        center = dotPos
                    )
                }
            }
        }
    })
}