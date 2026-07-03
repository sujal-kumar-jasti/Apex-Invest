package com.apexinvest.app.ui.components

<<<<<<< HEAD
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
=======
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
<<<<<<< HEAD
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
=======
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2

@Composable
fun PremiumLineChart(
    dataPoints: List<Double>,
    isPositive: Boolean,
    modifier: Modifier = Modifier,
<<<<<<< HEAD
    strokeColor: Color? = null,
    fillColor: Color? = null
) {
    // Return an empty Spacer to prevent the layout from collapsing if data is missing
    if (dataPoints.size < 2) {
        Spacer(modifier = modifier)
        return
    }

    val defaultGreen = Color(0xFF00E676)
    val defaultRed = Color(0xFFFF3D00)
    val baseTrendColor = strokeColor ?: if (isPositive) defaultGreen else defaultRed
    val areaColor = fillColor ?: baseTrendColor
    val isMiniChart = strokeColor != null

    // 🚀 CRITICAL OPTIMIZATION: Data Downsampling
    // Prevents the drawWithCache block from calculating thousands of cubic bezier
    // curves at once during the initial composition spike.
    val optimizedData = remember(dataPoints) {
        val maxPoints = 150
        if (dataPoints.size > maxPoints) {
            val step = dataPoints.size.toFloat() / maxPoints
            List(maxPoints) { i -> dataPoints[(i * step).toInt()] }
        } else {
            dataPoints
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

        val brush = Brush.verticalGradient(
            colors = listOf(areaColor.copy(alpha = if (isMiniChart) 0.2f else 0.3f), Color.Transparent),
            startY = 0f,
            endY = height
        )

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
=======
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
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
}