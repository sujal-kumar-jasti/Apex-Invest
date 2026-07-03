package com.apexinvest.app.ui.screens.stockdetail.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apexinvest.app.api.models.AdvancedRiskMetrics
import com.apexinvest.app.api.models.AnalystCoverage
import com.apexinvest.app.api.models.EfficiencyMargins
import com.apexinvest.app.ui.theme.BrandPurple
import com.apexinvest.app.ui.theme.LocalAppColors

@Composable
fun EfficiencyRiskPane(
    eff: EfficiencyMargins?,
    rsk: AdvancedRiskMetrics?,
    ana: AnalystCoverage?,
    isDark: Boolean,
    formatAnalystRating: (String?) -> String = { it ?: "-" }
) {
    if (eff == null || rsk == null || ana == null) {
        GlassShimmer(isDark, 300.dp)
        return
    }

    val appColors = LocalAppColors.current

    GlassPaneCard("Analyst Ratings Consensus", isDark) {
        val total = ana.countTotalRecommendations?.toDouble() ?: 0.0
        val gaugeScore = if (total > 0) {
            val score = ((ana.countStrongBuy ?: 0) * 1.0 + (ana.countBuy ?: 0) * 0.75 + (ana.countHold ?: 0) * 0.5 + (ana.countSell ?: 0) * 0.25) / total
            (score * 2) - 1
        } else 0.0

        TradingViewGauge(
            ratingValue = gaugeScore,
            title = "Analyst rating",
            subtitle = "An aggregate view of professional's ratings.",
            isDark = isDark,
            isAnalyst = true,
            customLabel = formatAnalystRating(ana.analystRatingConsensus)
        )
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth()) {
            MetricColumn("Target High", ana.targetPriceHigh.fmt(), isDark, Modifier.weight(1f))
            MetricColumn("Target Mean", ana.targetPriceMean.fmt(), isDark, Modifier.weight(1f))
            MetricColumn("Target Low", ana.targetPriceLow.fmt(), isDark, Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth()) {
            MetricColumn("Strong Buy", ana.countStrongBuy?.toString() ?: "-", isDark, Modifier.weight(1f))
            MetricColumn("Buy", ana.countBuy?.toString() ?: "-", isDark, Modifier.weight(1f))
            MetricColumn("Hold", ana.countHold?.toString() ?: "-", isDark, Modifier.weight(1f))
            MetricColumn("Sell", ana.countSell?.toString() ?: "-", isDark, Modifier.weight(1f))
            MetricColumn("Strong Sell", ana.countStrongSell?.toString() ?: "-", isDark, Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth()) {
            // NEW: Apply the formatter directly to analystRatingConsensus right here in the UI
            MetricColumn("Consensus Key", formatAnalystRating(ana.analystRatingConsensus), isDark, Modifier.weight(1f))
            MetricColumn("Analyst Count", ana.analystCount.fmt(), isDark, Modifier.weight(1f))
            MetricColumn("Total Recs", ana.countTotalRecommendations?.toString() ?: "-", isDark, Modifier.weight(1f))
        }
    }
    Spacer(Modifier.height(16.dp))
    GlassPaneCard("Profitability Margins", isDark) {
        VisualProgressBar("Gross Margin", eff.grossMarginPct, appColors.trendGreen, isDark)
        Spacer(Modifier.height(12.dp))
        VisualProgressBar("Operating Margin", eff.operatingMarginYf, appColors.trendOrange, isDark)
        Spacer(Modifier.height(12.dp))
        VisualProgressBar("EBITDA Margin", eff.ebitdaMarginPct, appColors.trendBlue, isDark)
        Spacer(Modifier.height(12.dp))
        VisualProgressBar("Net Profit Margin", eff.netProfitMarginPct, BrandPurple, isDark)
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth()) {
            ExplainerMetricColumn("ROE", eff.roePct.fmtPct(), getSentiment(eff.roePct, 0.0, 15.0), isDark, Modifier.weight(1f))
            MetricColumn("ROE (YF)", eff.roeYf.fmtPct(), isDark, Modifier.weight(1f))
            ExplainerMetricColumn("ROA", eff.roaPct.fmtPct(), getSentiment(eff.roaPct, 0.0, 5.0), isDark, Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth()) {
            MetricColumn("ROA (YF)", eff.roaYf.fmtPct(), isDark, Modifier.weight(1f))
            MetricColumn("Asset Turnover", eff.assetTurnoverRatio.fmt(), isDark, Modifier.weight(1f))
            MetricColumn("Div Yield", eff.dividendYieldPct.fmtPct(), isDark, Modifier.weight(1f))
        }
    }
    Spacer(Modifier.height(16.dp))
    GlassPaneCard("Advanced Risk Metrics", isDark) {
        Row(Modifier.fillMaxWidth()) {
            ExplainerMetricColumn("Piotroski F-Score", rsk.piotroskiFScore.fmt(), getSentiment(rsk.piotroskiFScore, 4.0, 7.0), isDark, Modifier.weight(1f))
            ExplainerMetricColumn("Altman Z-Score", rsk.altmanZScore.fmt(), getSentiment(rsk.altmanZScore, 1.8, 3.0), isDark, Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth()) {
            MetricColumn("Graham Number", rsk.grahamNumber.fmt(), isDark, Modifier.weight(1f))
            MetricColumn("Shares Float", rsk.sharesFloat.fmtCompact(), isDark, Modifier.weight(1f))
        }
    }
}