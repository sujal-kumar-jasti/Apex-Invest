package com.apexinvest.app.ui.screens.stockdetail.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apexinvest.app.api.models.AdvancedTechnicals

@Composable
fun TechnicalsPane(tech: AdvancedTechnicals?, isDark: Boolean) {
    if (tech == null) {
        GlassShimmer(300.dp)
        return
    }
    GlassPaneCard("Technical Summary", isDark) {
        val rating = tech.ratingOverall ?: 0.0
        TradingViewGauge(rating, "Technicals", "Summarizing what the indicators are suggesting.",
            isAnalyst = false)
    }
    Spacer(Modifier.height(16.dp))
    GlassPaneCard("Oscillators & Momentum", isDark) {
        Row(Modifier.fillMaxWidth()) {
            ExplainerMetricColumn("RSI (14)", tech.rsi14.fmt(), getSentiment(tech.rsi14, 40.0, 60.0),
                Modifier.weight(1f))
            ExplainerMetricColumn("MACD", tech.macdLine.fmt(), getSentiment(tech.macdLine, 0.0, 0.0),
                Modifier.weight(1f))
            MetricColumn("MACD Signal", tech.macdSignalLine.fmt(), Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth()) {
            MetricColumn("ADX (14)", tech.adx14.fmt(), Modifier.weight(1f))
            ExplainerMetricColumn("CCI (20)", tech.cci20.fmt(), getSentiment(tech.cci20, -100.0, 100.0),
                Modifier.weight(1f))
            MetricColumn("ATR (14)", tech.atr14.fmt(), Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth()) {
            ExplainerMetricColumn("Awesome Osc", tech.awesomeOscillator.fmt(), getSentiment(tech.awesomeOscillator, 0.0, 0.0),
                Modifier.weight(1f))
            MetricColumn("Stochastic K", tech.stochasticK.fmt(), Modifier.weight(1f))
            MetricColumn("Stochastic D", tech.stochasticD.fmt(), Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth()) {
            ExplainerMetricColumn("Momentum (10)", tech.momentum10.fmt(), getSentiment(tech.momentum10, 0.0, 0.0),
                Modifier.weight(1f))
            ExplainerMetricColumn("Williams %R", tech.williamsRPct.fmt(), getSentiment(tech.williamsRPct, -80.0, -20.0),
                Modifier.weight(1f))
            ExplainerMetricColumn("Chaikin MF", tech.chaikinMoneyFlow.fmt(), getSentiment(tech.chaikinMoneyFlow, 0.0, 0.0),
                Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth()) {
            ExplainerMetricColumn("Bull/Bear Power", tech.bullBearPower.fmt(), getSentiment(tech.bullBearPower, 0.0, 0.0),
                Modifier.weight(1f))
            MetricColumn("Ultimate Osc", tech.ultimateOscillator.fmt(), Modifier.weight(1f))
            MetricColumn("Rating Osc", tech.ratingOscillators.fmt(), Modifier.weight(1f))
        }
    }
    Spacer(Modifier.height(16.dp))
    GlassPaneCard("Moving Averages", isDark) {
        Row(Modifier.fillMaxWidth()) {
            MetricColumn("EMA 20", tech.ema20.fmt(), Modifier.weight(1f))
            MetricColumn("SMA 50", tech.sma50.fmt(), Modifier.weight(1f))
            MetricColumn("SMA 100", tech.sma100.fmt(), Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth()) {
            MetricColumn("SMA 200", tech.sma200.fmt(), Modifier.weight(1f))
            MetricColumn("Rating MAs", tech.ratingMovingAverages.fmt(), Modifier.weight(1f))
            Spacer(Modifier.weight(1f))
        }
    }
}
