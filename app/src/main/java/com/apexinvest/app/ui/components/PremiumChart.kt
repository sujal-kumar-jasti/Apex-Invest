package com.apexinvest.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun PremiumLineChart(
    dataPoints: List<Double>,
    isPositive: Boolean,
    modifier: Modifier = Modifier,
    strokeColor: Color? = null, // <--- New Parameter
    fillColor: Color? = null    // <--- New Parameter
) {
    if (dataPoints.isEmpty()) return

    // 1. Determine Colors (Use passed custom color OR default Green/Red logic)
    val defaultColor = if (isPositive) Color(0xFF00C853) else Color(0xFFFF3D00)
    val finalLineColor = strokeColor ?: defaultColor
    val finalFillColor = fillColor ?: defaultColor.copy(alpha = 0.3f)

    // 2. Gradient Brush
    val brush = remember(finalFillColor) {
        Brush.verticalGradient(
            colors = listOf(
                finalFillColor,                 // Top opacity (from param)
                finalFillColor.copy(alpha = 0.05f), // Fades out at bottom
                Color.Transparent
            )
        )
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Add padding so graph doesn't clip at top/bottom edges
        val verticalPadding = 20f
        val drawHeight = height - (verticalPadding * 2)

        val minVal = dataPoints.minOrNull() ?: 0.0
        val maxVal = dataPoints.maxOrNull() ?: 1.0
        val range = (maxVal - minVal).coerceAtLeast(1.0)

        // Helper to map Value -> Y Coordinate
        fun getY(value: Double): Float {
            return height - verticalPadding - ((value - minVal) / range * drawHeight).toFloat()
        }

        // 3. Draw Baseline (Only if using default colors - optional context)
        if (strokeColor == null) {
            val startY = getY(dataPoints.first())
            drawLine(
                color = Color.Gray.copy(alpha = 0.5f),
                start = Offset(0f, startY),
                end = Offset(width, startY),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }

        // 4. Calculate Points
        val points = dataPoints.mapIndexed { index, value ->
            val x = (index.toFloat() / (dataPoints.size - 1)) * width
            val y = getY(value)
            Offset(x, y)
        }

        // 5. Create Smooth Path (Bezier)
        val path = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points.first().x, points.first().y)
                for (i in 0 until points.size - 1) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    val controlPoint1 = Offset((p0.x + p1.x) / 2, p0.y)
                    val controlPoint2 = Offset((p0.x + p1.x) / 2, p1.y)
                    cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p1.x, p1.y)
                }
            }
        }

        // 6. Draw Fill
        val fillPath = Path().apply {
            addPath(path)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(fillPath, brush)

        // 7. Draw Line
        drawPath(
            path = path,
            color = finalLineColor,
            style = Stroke(
                width = 6f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}