package com.apexinvest.app.ui.screens.stockdetail.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apexinvest.app.api.models.StockDetailsResponse
import com.apexinvest.app.data.StockEntity

@Composable
fun OverviewPane(
    data: StockDetailsResponse,
    holding: StockEntity?,
    isDark: Boolean
) {
    Column {
        if (holding != null) {
            StockHoldingsCard(holding, isDark)
            Spacer(Modifier.height(16.dp))
        }

        GlassPaneCard("Corporate Profile", isDark) {
        Row(Modifier.fillMaxWidth()) {
            MetricColumn("Sector", data.sector, isDark, Modifier.weight(1f))
            MetricColumn("Industry", data.industry, isDark, Modifier.weight(1f))
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth()) {
            MetricColumn("Country", data.country, isDark, Modifier.weight(1f))
            MetricColumn("Employees", data.employeeCount?.fmtCompact(), isDark, Modifier.weight(1f))
        }
    }
    Spacer(Modifier.height(16.dp))
    data.marketPricing?.let { m ->
        GlassPaneCard("Market Pricing Metrics", isDark) {
            Row(Modifier.fillMaxWidth()) {
                MetricColumn("Open", m.priceOpen.fmt(), isDark, Modifier.weight(1f))
                MetricColumn("Day High", m.priceHigh.fmt(), isDark, Modifier.weight(1f))
                MetricColumn("Day Low", m.priceLow.fmt(), isDark, Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth()) {
                MetricColumn("52W High", m.high52Week.fmt(), isDark, Modifier.weight(1f))
                MetricColumn("52W Low", m.low52Week.fmt(), isDark, Modifier.weight(1f))
                MetricColumn("Daily Traded Val", m.tradedValueDaily.fmtCompact(), isDark, Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth()) {
                MetricColumn("Avg Vol 10D", m.avgVolume10D.fmtCompact(), isDark, Modifier.weight(1f))
                MetricColumn("Avg Vol 90D", m.avgVolume90D.fmtCompact(), isDark, Modifier.weight(1f))
                MetricColumn("Beta 5Y", m.beta5Y.fmt(), isDark, Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth()) {
                MetricColumn("Relative Vol 10D", m.relativeVolume10D.fmt(), isDark, Modifier.weight(1f))
                Spacer(Modifier.weight(2f))
            }
        }
        Spacer(Modifier.height(16.dp))
    }
    data.valuationMultipliers?.let { v ->
        GlassPaneCard("Valuation Multipliers", isDark) {
            Row(Modifier.fillMaxWidth()) {
                MetricColumn("Enterprise Value", v.enterpriseValue.fmtCompact(), isDark, Modifier.weight(1f))
                MetricColumn("P/E Ratio", v.peRatio.fmt(), isDark, Modifier.weight(1f))
                MetricColumn("Forward P/E", v.forwardPe.fmt(), isDark, Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth()) {
                MetricColumn("P/B Ratio", v.pbRatio.fmt(), isDark, Modifier.weight(1f))
                MetricColumn("P/S Ratio", v.psRatio.fmt(), isDark, Modifier.weight(1f))
                MetricColumn("P/FCF", v.pFcfRatio.fmt(), isDark, Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth()) {
                MetricColumn("EV / EBITDA", v.enterpriseToEbitda.fmt(), isDark, Modifier.weight(1f))
                MetricColumn("EV / Revenue", v.evRevenue.fmt(), isDark, Modifier.weight(1f))
                MetricColumn("PEG Ratio", v.pegRatio.fmt(), isDark, Modifier.weight(1f))
            }
        }
    }
}
}
