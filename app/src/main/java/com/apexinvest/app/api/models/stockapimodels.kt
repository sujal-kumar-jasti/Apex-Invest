package com.apexinvest.app.api.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

// ==========================================
// NEW: CHART-READY FINANCIAL MODELS
// ==========================================

@Keep
data class ChartFinancialPeriod(
    @SerializedName("date") val date: String,
    @SerializedName("revenue") val revenue: Double,
    @SerializedName("cogs") val cogs: Double,
    @SerializedName("gross_profit") val grossProfit: Double,
    @SerializedName("operating_expenses") val operatingExpenses: Double,
    @SerializedName("operating_income") val operatingIncome: Double,
    @SerializedName("net_income") val netIncome: Double,
    @SerializedName("total_debt") val totalDebt: Double,
    @SerializedName("cash_and_equivalents") val cashAndEquivalents: Double,
    @SerializedName("free_cash_flow") val freeCashFlow: Double
)

@Keep
data class ChartEarningsPeriod(
    @SerializedName("date") val date: String,
    @SerializedName("actual") val actual: Double,
    @SerializedName("estimate") val estimate: Double
)

@Keep
data class FinancialsDto(
    @SerializedName("annual")
    val annual: List<ChartFinancialPeriod> = emptyList(),

    @SerializedName("quarterly")
    val quarterly: List<ChartFinancialPeriod> = emptyList(),

    @SerializedName("earnings_annual")
    val earningsAnnual: List<ChartEarningsPeriod> = emptyList(),

    @SerializedName("earnings_quarterly")
    val earningsQuarterly: List<ChartEarningsPeriod> = emptyList()
)

@Keep
data class StockFundamentalsDto(
    @SerializedName("market_cap") val marketCap: Double,
    @SerializedName("market_cap_class") val marketCapClass: String,
    @SerializedName("pe_ratio") val peRatio: Double?,
    @SerializedName("pb_ratio") val pbRatio: Double?,
    @SerializedName("roe") val roe: Double?,
    @SerializedName("debt_to_equity") val debtToEquity: Double?,
    @SerializedName("book_value") val bookValue: Double?,
    @SerializedName("dividend_yield") val dividendYield: Double?,
    @SerializedName("eps") val eps: Double?,
    @SerializedName("industry_pe") val industryPe: Double?
)

@Keep
data class ShareholdingDto(
    @SerializedName("promoters") val promoters: Double,
    @SerializedName("retail") val retail: Double,
    @SerializedName("institutions") val institutions: Double,
    @SerializedName("foreign_institutions") val foreignInstitutions: Double,
    @SerializedName("domestic_institutions") val domesticInstitutions: Double
)

@Keep
data class PythonStockInfoDto(
    @SerializedName("name") val name: String,
    @SerializedName("currency") val currency: String,
    @SerializedName("sector") val sector: String?,
    @SerializedName("industry") val industry: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("fundamentals") val fundamentals: StockFundamentalsDto?,
    @SerializedName("financials") val financials: FinancialsDto?,
    @SerializedName("shareholding") val shareholding: ShareholdingDto?,
    @SerializedName("similar_stocks") val similarStocks: List<SimilarStock> = emptyList()
)