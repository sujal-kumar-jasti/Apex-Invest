package com.apexinvest.app.ui.screens.stockdetail.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apexinvest.app.api.models.ChartEarningsPeriod
import com.apexinvest.app.api.models.ChartFinancialPeriod
import com.apexinvest.app.ui.components.glassCard
import com.apexinvest.app.viewmodel.FinancialsUiState
import java.util.Locale
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

// Color palette
private val ChartBlue = Color(0xFF3B82F6)
private val ChartCyan = Color(0xFF06B6D4)
private val ChartOrange = Color(0xFFF97316)
private val ChartGreen = Color(0xFF10B981)
private val ChartPink = Color(0xFFEC4899)
private val ChartGrey = Color(0xFF9CA3AF)

@Composable
fun ScorecardPane(
    financialsState: FinancialsUiState,
    isDark: Boolean
) {
    when (financialsState) {
        is FinancialsUiState.Loading -> {
            Column {
                GlassShimmerCard(isDark, 280.dp)
                Spacer(Modifier.height(16.dp))
                GlassShimmerCard(isDark, 280.dp)
            }
        }
        is FinancialsUiState.Error -> {
            Box(Modifier.fillMaxWidth().height(200.dp).glassCard(isDark), contentAlignment = Alignment.Center) {
                Text(financialsState.message, color = ChartPink, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
        is FinancialsUiState.Success -> {
            val dto = financialsState.data

            var perfPeriod by remember { mutableStateOf("Annual") }
            var waterfallPeriod by remember { mutableStateOf("Annual") }
            var debtPeriod by remember { mutableStateOf("Annual") }
            var earningsPeriod by remember { mutableStateOf("Quarterly") }

            val perfPeriods = remember(perfPeriod, dto) {
                if (perfPeriod == "Quarterly") dto.quarterly.takeLast(5) else dto.annual.takeLast(5)
            }
            val waterfallPeriods = remember(waterfallPeriod, dto) {
                if (waterfallPeriod == "Quarterly") dto.quarterly.takeLast(5) else dto.annual.takeLast(5)
            }
            val debtPeriods = remember(debtPeriod, dto) {
                if (debtPeriod == "Quarterly") dto.quarterly.takeLast(5) else dto.annual.takeLast(5)
            }

            val earningsList = remember(earningsPeriod, dto) {
                val list = if (earningsPeriod == "Quarterly") dto.earningsQuarterly else dto.earningsAnnual
                if (list.isEmpty()) {
                    listOf(
                        ChartEarningsPeriod("P1", 1.15, 1.10),
                        ChartEarningsPeriod("P2", 1.25, 1.30),
                        ChartEarningsPeriod("P3", 1.40, 1.35),
                        ChartEarningsPeriod("P4", 1.30, 1.45)
                    )
                } else {
                    list.takeLast(5)
                }
            }

            Column(Modifier.fillMaxWidth()) {
                // Performance
                ChartCardContainer("Performance Trends", perfPeriod, { perfPeriod = it }, isDark) {
                    if (perfPeriods.isNotEmpty()) {
                        PerformanceCanvas(perfPeriods, isDark)
                        Spacer(Modifier.height(14.dp))
                        ChartLegendRow(
                            listOf(
                                "Revenue" to ChartBlue,
                                "Net Income" to ChartCyan,
                                "Net Margin %" to ChartOrange
                            )
                        )
                    } else {
                        EmptyChartPlaceholder("No performance metrics available for this period.")
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Waterfall
                ChartCardContainer("Revenue to Profit Conversion", waterfallPeriod, { waterfallPeriod = it }, isDark) {
                    val latestFinancialPeriod = waterfallPeriods.lastOrNull()
                    if (latestFinancialPeriod != null) {
                        WaterfallCanvas(latestFinancialPeriod, isDark)
                        Spacer(Modifier.height(14.dp))
                        ChartLegendRow(
                            listOf(
                                "Inflows" to ChartGreen,
                                "Outflows" to ChartPink,
                                "Retained Profit" to ChartCyan,
                                "Structural Steps" to ChartBlue
                            )
                        )
                    } else {
                        EmptyChartPlaceholder("Waterfall structure context requires valid reporting blocks.")
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Debt
                ChartCardContainer("Debt Level and Coverage Balance", debtPeriod, { debtPeriod = it }, isDark) {
                    if (debtPeriods.isNotEmpty()) {
                        DebtCoverageCanvas(debtPeriods, isDark)
                        Spacer(Modifier.height(14.dp))
                        ChartLegendRow(
                            listOf(
                                "Total Outstanding Debt" to ChartPink,
                                "Free Cash Flow (FCF)" to ChartCyan,
                                "Cash & Marketable Assets" to ChartBlue
                            )
                        )
                    } else {
                        EmptyChartPlaceholder("Liquidity indicators require clean asset tracking metrics.")
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Earnings
                ChartCardContainer("Earnings Tracking Metrics", earningsPeriod, { earningsPeriod = it }, isDark) {
                    if (earningsList.isNotEmpty()) {
                        EarningsScatterCanvas(earningsList, isDark)
                        Spacer(Modifier.height(14.dp))
                        ChartLegendRow(
                            listOf(
                                "Actual Reported EPS" to ChartGreen,
                                "Consensus Estimate" to ChartGrey
                            )
                        )
                    } else {
                        EmptyChartPlaceholder("Forward tracking EPS structures requires explicit operational schedules.")
                    }
                }
            }
        }
    }
}

// Canvases

@Composable
private fun PerformanceCanvas(periods: List<ChartFinancialPeriod>, isDark: Boolean) {
    val gridColor = if (isDark) Color.White.copy(0.06f) else Color.Black.copy(0.06f)
    val textColor = if (isDark) Color.White.copy(0.5f) else Color.Black.copy(0.5f)
    val axisColor = if (isDark) Color.White.copy(0.15f) else Color.Black.copy(0.15f)
    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(color = textColor, fontSize = 9.sp)

    val maxMoney = remember(periods) { periods.maxOfOrNull { max(it.revenue, it.netIncome) } ?: 1.0 }
    val minMoney = remember(periods) { periods.minOfOrNull { min(0.0, min(it.revenue, it.netIncome)) } ?: 0.0 }
    val moneyRange = if (maxMoney - minMoney == 0.0) 1.0 else maxMoney - minMoney

    val margins = remember(periods) { periods.map { if (it.revenue > 0.0) (it.netIncome / it.revenue) * 100 else 0.0 } }
    val maxMargin = remember(margins) { (margins.maxOrNull() ?: 20.0).coerceAtLeast(1.0) * 1.2 }
    val minMargin = remember(margins) { (margins.minOrNull() ?: 0.0).coerceAtMost(-1.0) * 1.2 }
    val marginRange = maxMargin - minMargin

    Canvas(Modifier.fillMaxWidth().height(220.dp).padding(top = 16.dp)) {
        val canvasW = size.width
        val canvasH = size.height
        val chartH = canvasH - 24.dp.toPx()

        val maxLabelText = formatScaleValue(maxMoney)
        val minLabelText = formatScaleValue(minMoney)
        val maxLabelWidth = max(
            textMeasurer.measure(maxLabelText, textStyle).size.width,
            textMeasurer.measure(minLabelText, textStyle).size.width
        ).toFloat()

        val leftPad = maxLabelWidth + 8.dp.toPx()
        val chartW = canvasW - leftPad
        val stepX = chartW / periods.size
        val zeroY = chartH - (((0.0 - minMoney) / moneyRange).toFloat() * chartH)

        for (i in 0..4) {
            val y = chartH * (i / 4f)
            drawLine(gridColor, Offset(leftPad, y), Offset(canvasW, y), 1f)

            val currencyVal = maxMoney - ((i / 4f) * moneyRange)
            val labelText = formatScaleValue(currencyVal)
            val textLayout = textMeasurer.measure(labelText, textStyle)

            drawText(
                textLayoutResult = textLayout,
                color = textColor,
                topLeft = Offset(leftPad - textLayout.size.width - 4.dp.toPx(), y - (textLayout.size.height / 2f))
            )
        }

        if (zeroY > 0 && zeroY < chartH) {
            drawLine(axisColor, Offset(leftPad, zeroY), Offset(canvasW, zeroY), 1.5f)
        }

        val marginPoints = mutableListOf<Offset>()

        periods.forEachIndexed { i, p ->
            val centerX = leftPad + (i * stepX) + (stepX / 2f)
            val barW = stepX * 0.22f

            val revH = (p.revenue.absoluteValue / moneyRange).toFloat() * chartH
            val revTop = if (p.revenue >= 0) zeroY - revH else zeroY
            drawRect(ChartBlue, Offset(centerX - barW - 3.dp.toPx(), revTop), Size(barW, revH.coerceAtLeast(1f)))

            val netH = (p.netIncome.absoluteValue / moneyRange).toFloat() * chartH
            val netTop = if (p.netIncome >= 0) zeroY - netH else zeroY
            drawRect(ChartCyan, Offset(centerX + 3.dp.toPx(), netTop), Size(barW, netH.coerceAtLeast(1f)))

            val marginY = chartH - (((margins[i] - minMargin) / marginRange).toFloat() * chartH)
            marginPoints.add(Offset(centerX, marginY.coerceIn(0f, chartH)))

            val displayDate = if (p.date.length > 4) p.date.take(4) else p.date
            val textLayout = textMeasurer.measure(displayDate, TextStyle(color = textColor, fontSize = 11.sp))
            drawText(
                textLayoutResult = textLayout,
                color = textColor,
                topLeft = Offset(centerX - textLayout.size.width / 2f, canvasH - 18.dp.toPx())
            )
        }

        if (marginPoints.size > 1) {
            val path = Path().apply {
                moveTo(marginPoints.first().x, marginPoints.first().y)
                for (j in 1 until marginPoints.size) lineTo(marginPoints[j].x, marginPoints[j].y)
            }
            drawPath(path, ChartOrange, style = Stroke(width = 2.5.dp.toPx()))
            marginPoints.forEach { pt ->
                drawCircle(Color.White, radius = 4.dp.toPx(), center = pt)
                drawCircle(ChartOrange, radius = 2.5.dp.toPx(), center = pt)
            }
        }
    }
}

@Composable
private fun WaterfallCanvas(p: ChartFinancialPeriod, isDark: Boolean) {
    val dashedEffect = remember { PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f) }
    val guideColor = if (isDark) Color.White.copy(0.12f) else Color.Black.copy(0.12f)
    val textColor = if (isDark) Color.White.copy(0.5f) else Color.Black.copy(0.5f)
    val textMeasurer = rememberTextMeasurer()

    data class WaterfallStep(val label: String, val startVal: Double, val endVal: Double, val color: Color, val isTotal: Boolean)

    val steps = remember(p) {
        val rev = p.revenue
        val gp = p.grossProfit
        val opInc = p.operatingIncome
        val net = p.netIncome

        listOf(
            WaterfallStep("Rev", 0.0, rev, ChartGreen, true),
            WaterfallStep("COGS", rev, gp, ChartPink, false),
            WaterfallStep("Gross", 0.0, gp, ChartBlue, true),
            WaterfallStep("OpEx", gp, opInc, ChartPink, false),
            WaterfallStep("OpInc", 0.0, opInc, ChartBlue, true),
            WaterfallStep("Tax", opInc, net, ChartPink, false),
            WaterfallStep("Net", 0.0, net, ChartCyan, true)
        )
    }

    val maxVal = remember(steps) { steps.maxOf { max(it.startVal, it.endVal) }.coerceAtLeast(1.0) }
    val minVal = remember(steps) { steps.minOf { min(it.startVal, it.endVal) }.coerceAtMost(0.0) }
    val range = if (maxVal - minVal == 0.0) 1.0 else maxVal - minVal

    Canvas(Modifier.fillMaxWidth().height(230.dp).padding(top = 16.dp)) {
        val canvasW = size.width
        val canvasH = size.height
        val chartH = canvasH - 24.dp.toPx()
        val colW = canvasW / steps.size
        val barW = colW * 0.70f

        val zeroY = chartH - (((0.0 - minVal) / range).toFloat() * chartH)
        drawLine(guideColor, Offset(0f, zeroY), Offset(canvasW, zeroY), 1.5f)

        var prevTopY = zeroY

        steps.forEachIndexed { i, s ->
            val colCenterX = (i * colW) + (colW / 2f)
            val leftX = colCenterX - (barW / 2f)

            val y1 = chartH - (((s.startVal - minVal) / range).toFloat() * chartH)
            val y2 = chartH - (((s.endVal - minVal) / range).toFloat() * chartH)

            val topY = min(y1, y2)
            val botY = max(y1, y2)
            val barH = max(2f, botY - topY)

            if (i > 0) {
                drawLine(
                    color = guideColor,
                    start = Offset(leftX - (colW * 0.35f), prevTopY),
                    end = Offset(leftX, prevTopY),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dashedEffect
                )
            }

            drawRect(s.color, Offset(leftX, topY), Size(barW, barH))
            prevTopY = if (s.endVal > s.startVal) topY else botY
            if (s.isTotal) prevTopY = topY

            val stepActualDiff = (s.endVal - s.startVal).absoluteValue
            val diffLabel = formatScaleValue(if (s.isTotal) s.endVal else stepActualDiff)
            val valLayout = textMeasurer.measure(diffLabel, TextStyle(fontSize = 8.sp, color = textColor))

            drawText(
                textLayoutResult = valLayout,
                color = textColor,
                topLeft = Offset(colCenterX - valLayout.size.width / 2f, topY - 12.dp.toPx())
            )

            val textLayout = textMeasurer.measure(s.label, TextStyle(color = textColor, fontSize = 9.sp))
            drawText(
                textLayoutResult = textLayout,
                color = textColor,
                topLeft = Offset(colCenterX - textLayout.size.width / 2f, canvasH - 18.dp.toPx())
            )
        }
    }
}

@Composable
private fun DebtCoverageCanvas(periods: List<ChartFinancialPeriod>, isDark: Boolean) {
    val gridColor = if (isDark) Color.White.copy(0.06f) else Color.Black.copy(0.06f)
    val textColor = if (isDark) Color.White.copy(0.5f) else Color.Black.copy(0.5f)
    val axisColor = if (isDark) Color.White.copy(0.15f) else Color.Black.copy(0.15f)
    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(color = textColor, fontSize = 9.sp)

    val maxVal = remember(periods) { periods.maxOfOrNull { max(it.totalDebt, max(it.freeCashFlow, it.cashAndEquivalents)) } ?: 1.0 }
    val minVal = remember(periods) { periods.minOfOrNull { min(0.0, it.freeCashFlow) } ?: 0.0 }
    val range = if (maxVal - minVal == 0.0) 1.0 else maxVal - minVal

    Canvas(Modifier.fillMaxWidth().height(220.dp).padding(top = 16.dp)) {
        val canvasW = size.width
        val canvasH = size.height
        val chartH = canvasH - 24.dp.toPx()

        val maxLabelText = formatScaleValue(maxVal)
        val minLabelText = formatScaleValue(minVal)
        val maxLabelWidth = max(
            textMeasurer.measure(maxLabelText, textStyle).size.width,
            textMeasurer.measure(minLabelText, textStyle).size.width
        ).toFloat()

        val leftPad = maxLabelWidth + 8.dp.toPx()
        val chartW = canvasW - leftPad
        val stepX = chartW / periods.size
        val zeroY = chartH - (((0.0 - minVal) / range).toFloat() * chartH)

        for (i in 0..4) {
            val y = chartH * (i / 4f)
            drawLine(gridColor, Offset(leftPad, y), Offset(canvasW, y), 1f)

            val scaleUnit = maxVal - ((i / 4f) * range)
            val labelText = formatScaleValue(scaleUnit)
            val textLayout = textMeasurer.measure(labelText, textStyle)

            drawText(
                textLayoutResult = textLayout,
                color = textColor,
                topLeft = Offset(leftPad - textLayout.size.width - 4.dp.toPx(), y - (textLayout.size.height / 2f))
            )
        }

        if (zeroY > 0 && zeroY < chartH) {
            drawLine(axisColor, Offset(leftPad, zeroY), Offset(canvasW, zeroY), 1.5f)
        }

        periods.forEachIndexed { i, p ->
            val centerX = leftPad + (i * stepX) + (stepX / 2f)
            val barW = stepX * 0.16f
            val space = 2.5.dp.toPx()

            val dH = (p.totalDebt.absoluteValue / range).toFloat() * chartH
            val dTop = if (p.totalDebt >= 0) zeroY - dH else zeroY
            drawRect(ChartPink, Offset(centerX - barW - space - (barW / 2f), dTop), Size(barW, dH.coerceAtLeast(1f)))

            val fH = (p.freeCashFlow.absoluteValue / range).toFloat() * chartH
            val fTop = if (p.freeCashFlow >= 0) zeroY - fH else zeroY
            drawRect(ChartCyan, Offset(centerX - (barW / 2f), fTop), Size(barW, fH.coerceAtLeast(1f)))

            val cH = (p.cashAndEquivalents.absoluteValue / range).toFloat() * chartH
            val cTop = if (p.cashAndEquivalents >= 0) zeroY - cH else zeroY
            drawRect(ChartBlue, Offset(centerX + (barW / 2f) + space, cTop), Size(barW, cH.coerceAtLeast(1f)))

            val displayDate = if (p.date.length > 4) p.date.take(4) else p.date
            val textLayout = textMeasurer.measure(displayDate, TextStyle(color = textColor, fontSize = 11.sp))
            drawText(
                textLayoutResult = textLayout,
                color = textColor,
                topLeft = Offset(centerX - textLayout.size.width / 2f, canvasH - 18.dp.toPx())
            )
        }
    }
}

@Composable
private fun EarningsScatterCanvas(earnings: List<ChartEarningsPeriod>, isDark: Boolean) {
    val gridColor = if (isDark) Color.White.copy(0.06f) else Color.Black.copy(0.06f)
    val textColor = if (isDark) Color.White.copy(0.5f) else Color.Black.copy(0.5f)
    val axisColor = if (isDark) Color.White.copy(0.15f) else Color.Black.copy(0.15f)
    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(color = textColor, fontSize = 9.sp)

    val allVals = remember(earnings) { earnings.flatMap { listOf(it.actual, it.estimate) } }
    val maxEps = remember(allVals) { (allVals.maxOrNull() ?: 5.0) * 1.2 }
    val minEps = remember(allVals) { min(0.0, (allVals.minOrNull() ?: -1.0) * 1.2) }
    val range = if (maxEps - minEps == 0.0) 1.0 else maxEps - minEps

    Canvas(Modifier.fillMaxWidth().height(200.dp).padding(top = 16.dp)) {
        val canvasW = size.width
        val canvasH = size.height
        val chartH = canvasH - 24.dp.toPx()

        val maxLabelText = String.format(Locale.US, "%.2f", maxEps)
        val minLabelText = String.format(Locale.US, "%.2f", minEps)
        val maxLabelWidth = max(
            textMeasurer.measure(maxLabelText, textStyle).size.width,
            textMeasurer.measure(minLabelText, textStyle).size.width
        ).toFloat()

        val leftPad = maxLabelWidth + 8.dp.toPx()
        val chartW = canvasW - leftPad
        val stepX = chartW / earnings.size
        val zeroY = chartH - (((0.0 - minEps) / range).toFloat() * chartH)

        for (i in 0..3) {
            val y = chartH * (i / 3f)
            drawLine(gridColor, Offset(leftPad, y), Offset(canvasW, y), 1f)

            val epsVal = maxEps - ((i / 3f) * range)
            val labelText = String.format(Locale.US, "%.2f", epsVal)
            val textLayout = textMeasurer.measure(labelText, textStyle)

            drawText(
                textLayoutResult = textLayout,
                color = textColor,
                topLeft = Offset(leftPad - textLayout.size.width - 4.dp.toPx(), y - (textLayout.size.height / 2f))
            )
        }

        if (zeroY > 0 && zeroY < chartH) {
            drawLine(axisColor, Offset(leftPad, zeroY), Offset(canvasW, zeroY), 1.5f)
        }

        earnings.forEachIndexed { i, e ->
            val cx = leftPad + (i * stepX) + (stepX / 2f)

            val estY = chartH - (((e.estimate - minEps) / range).toFloat() * chartH)
            drawCircle(ChartGrey, radius = 6.dp.toPx(), center = Offset(cx, estY.coerceIn(0f, chartH)), style = Stroke(width = 2.dp.toPx()))

            val actY = chartH - (((e.actual - minEps) / range).toFloat() * chartH)
            drawCircle(ChartGreen, radius = 5.dp.toPx(), center = Offset(cx, actY.coerceIn(0f, chartH)))

            val dateLabel = if (e.date.length > 8) e.date.take(8) else e.date
            val textLayout = textMeasurer.measure(dateLabel, TextStyle(color = textColor, fontSize = 9.sp))
            drawText(
                textLayoutResult = textLayout,
                color = textColor,
                topLeft = Offset(cx - textLayout.size.width / 2f, canvasH - 18.dp.toPx())
            )
        }
    }
}

// Helpers

@Composable
private fun ChartCardContainer(
    title: String,
    selectedPeriod: String?,
    onPeriodSelect: ((String) -> Unit)?,
    isDark: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        Modifier.fillMaxWidth()
            .glassCard(isDark)
            .padding(16.dp)
            .animateContentSize()
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isDark) Color.White else Color.Black,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(Modifier.width(6.dp))
                Box(
                    Modifier.size(16.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color.White.copy(0.1f) else Color.Black.copy(0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("?", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White.copy(0.6f) else Color.Black.copy(0.6f))
                }
            }

            if (selectedPeriod != null && onPeriodSelect != null) {
                Row(
                    Modifier
                        .wrapContentWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) Color.White.copy(0.06f) else Color.Black.copy(0.05f))
                        .padding(2.dp)
                ) {
                    listOf("Annual", "Quarterly").forEach { p ->
                        val active = selectedPeriod.equals(p, true)
                        Box(
                            Modifier.clip(RoundedCornerShape(6.dp))
                                .background(if (active) (if (isDark) Color.White.copy(0.18f) else Color.White) else Color.Transparent)
                                .clickable { onPeriodSelect(p) }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = p,
                                fontSize = 11.sp,
                                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isDark) Color.White else Color.Black,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChartLegendRow(items: List<Pair<String, Color>>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { (label, color) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 14.dp)
            ) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(color))
                Spacer(Modifier.width(5.dp))
                Text(label, fontSize = 11.sp, color = ChartGrey, fontWeight = FontWeight.Normal)
            }
        }
    }
}

@Composable
private fun EmptyChartPlaceholder(message: String) {
    Box(
        modifier = Modifier.fillMaxWidth().height(150.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = ChartGrey,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
private fun GlassShimmerCard(isDark: Boolean, height: androidx.compose.ui.unit.Dp) {
    Box(
        Modifier.fillMaxWidth()
            .height(height)
            .glassCard(isDark)
            .background(if (isDark) Color.White.copy(0.02f) else Color.Black.copy(0.02f))
    )
}

private fun formatScaleValue(value: Double): String {
    val absVal = value.absoluteValue
    return when {
        absVal >= 1_000_000_000_000.0 -> String.format(Locale.US, "%.1fT", value / 1_000_000_000_000.0)
        absVal >= 1_000_000_000.0 -> String.format(Locale.US, "%.1fB", value / 1_000_000_000.0)
        absVal >= 1_000_000.0 -> String.format(Locale.US, "%.1fM", value / 1_000_000.0)
        absVal >= 1_000.0 -> String.format(Locale.US, "%.1fK", value / 1_000.0)
        else -> String.format(Locale.US, "%.0f", value)
    }
}