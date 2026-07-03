package com.apexinvest.app.api.models

import com.google.gson.annotations.SerializedName

// ==========================================
// NEW: CHART-READY FINANCIAL MODELS
// ==========================================

data class ChartFinancialPeriod(
    val date: String,
    val revenue: Double,
    val cogs: Double,
    @SerializedName("gross_profit") val grossProfit: Double,
    @SerializedName("operating_expenses") val operatingExpenses: Double,
    @SerializedName("operating_income") val operatingIncome: Double,
    @SerializedName("net_income") val netIncome: Double,
    @SerializedName("total_debt") val totalDebt: Double,
    @SerializedName("cash_and_equivalents") val cashAndEquivalents: Double,
    @SerializedName("free_cash_flow") val freeCashFlow: Double
)

data class ChartEarningsPeriod(
    val date: String,
    val actual: Double,
    val estimate: Double
)


data class FinancialsDto(
    val annual: List<ChartFinancialPeriod> = emptyList(),
    val quarterly: List<ChartFinancialPeriod> = emptyList(),

    @SerializedName("earnings_annual")
    val earningsAnnual: List<ChartEarningsPeriod> = emptyList(),

    @SerializedName("earnings_quarterly")
    val earningsQuarterly: List<ChartEarningsPeriod> = emptyList()
)

data class StockFundamentalsDto(
    @SerializedName("market_cap") val marketCap: Double,
    @SerializedName("market_cap_class") val marketCapClass: String,
    @SerializedName("pe_ratio") val peRatio: Double?,
    @SerializedName("pb_ratio") val pbRatio: Double?,
    val roe: Double?,
    @SerializedName("debt_to_equity") val debtToEquity: Double?,
    @SerializedName("book_value") val bookValue: Double?,
    @SerializedName("dividend_yield") val dividendYield: Double?,
    val eps: Double?,
    @SerializedName("industry_pe") val industryPe: Double?
)

data class ShareholdingDto(
    val promoters: Double,
    val retail: Double,
    val institutions: Double,
    @SerializedName("foreign_institutions") val foreignInstitutions: Double,
    @SerializedName("domestic_institutions") val domesticInstitutions: Double
)

data class PythonStockInfoDto(
    val name: String,
    val currency: String,
    val sector: String?,
    val industry: String?,
    val description: String?,
    val fundamentals: StockFundamentalsDto?,
    val financials: FinancialsDto?,
    val shareholding: ShareholdingDto?,
    @SerializedName("similar_stocks") val similarStocks: List<SimilarStock> = emptyList()
)