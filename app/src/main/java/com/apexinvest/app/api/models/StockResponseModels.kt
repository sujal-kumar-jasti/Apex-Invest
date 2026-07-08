package com.apexinvest.app.api.models

import androidx.annotation.Keep
import com.apexinvest.app.data.model.StockNews
import com.google.gson.annotations.SerializedName

// ==========================================
// 1. MASTER UI MODEL (Unified App Domain Model)
// ==========================================
@Keep
data class StockDetailsResponse(
    @SerializedName("ticker") val ticker: String,
    @SerializedName("companyName") val companyName: String?,
    @SerializedName("sector") val sector: String?,
    @SerializedName("industry") val industry: String?,
    @SerializedName("assetType") val assetType: String?,
    @SerializedName("logoId") val logoId: String?,
    @SerializedName("employeeCount") val employeeCount: Double?,
    @SerializedName("country") val country: String?,

    @SerializedName("marketPricing") val marketPricing: MarketPricing? = null,
    @SerializedName("valuationMultipliers") val valuationMultipliers: ValuationMultipliers? = null,
    @SerializedName("incomeStatement") val incomeStatement: IncomeStatement? = null,
    @SerializedName("balanceSheet") val balanceSheet: BalanceSheet? = null,
    @SerializedName("cashFlows") val cashFlows: CashFlows? = null,
    @SerializedName("efficiencyMargins") val efficiencyMargins: EfficiencyMargins? = null,
    @SerializedName("historicalReturns") val historicalReturns: HistoricalReturns? = null,
    @SerializedName("advancedTechnicals") val advancedTechnicals: AdvancedTechnicals? = null,
    @SerializedName("analystCoverage") val analystCoverage: AnalystCoverage? = null,
    @SerializedName("advancedRiskMetrics") val advancedRiskMetrics: AdvancedRiskMetrics? = null,

    @SerializedName("rangeChangeAbsolute") val rangeChangeAbsolute: Double? = null,
    @SerializedName("rangeChangePercent") val rangeChangePercent: Double? = null,

    @SerializedName("candles") val candles: List<CandlePoint> = emptyList(),
    @SerializedName("news") val news: List<StockNews> = emptyList(),
    @SerializedName("similarStocks") val similarStocks: List<SimilarStock> = emptyList(),
    @SerializedName("aiAnalystReport") val aiAnalystReport: String? = null
)

// ==========================================
// 2. CHART DATA (Domain Model)
// ==========================================
@Keep
data class CandlePoint(
    @SerializedName("time") val time: String,
    @SerializedName("open") val open: Double,
    @SerializedName("high") val high: Double,
    @SerializedName("low") val low: Double,
    @SerializedName("close") val close: Double,
    @SerializedName("volume") val volume: Long
)

// ==========================================
// 3. MARKET PRICING & VOLUME (/market)
// ==========================================
@Keep
data class MarketPricing(
    @SerializedName("Price_Last") val priceLast: Double?,
    @SerializedName("Price_Open") val priceOpen: Double?,
    @SerializedName("Price_High") val priceHigh: Double?,
    @SerializedName("Price_Low") val priceLow: Double?,
    @SerializedName("Change_Pct_1D") val changePct1D: Double?,
    @SerializedName("Change_Absolute_1D") val changeAbsolute1D: Double?,
    @SerializedName("previousClose") val previousClose: Double = 0.0,
    @SerializedName("High_52Week") val high52Week: Double?,
    @SerializedName("Low_52Week") val low52Week: Double?,
    @SerializedName("Volume_Current") val volumeCurrent: Double?,
    @SerializedName("Traded_Value_Daily") val tradedValueDaily: Double?,
    @SerializedName("Avg_Volume_10D") val avgVolume10D: Double?,
    @SerializedName("Avg_Volume_30D") val avgVolume30D: Double?,
    @SerializedName("Avg_Volume_90D") val avgVolume90D: Double?,
    @SerializedName("Relative_Volume_10D") val relativeVolume10D: Double?,
    @SerializedName("Beta_1Y") val beta1Y: Double?,
    @SerializedName("Beta_5Y") val beta5Y: Double?,

    // Extended Hours Data
    @SerializedName("preMarketPrice") val preMarketPrice: Double? = null,
    @SerializedName("preMarketChange") val preMarketChange: Double? = null,
    @SerializedName("postMarketPrice") val postMarketPrice: Double? = null,
    @SerializedName("postMarketChange") val postMarketChange: Double? = null,
    @SerializedName("marketState") val marketState: String? = null,
    @SerializedName("hasPrePost") val hasPrePost: Boolean = true
)

// ==========================================
// 4. VALUATION MULTIPLIERS (/valuation)
// ==========================================
@Keep
data class ValuationMultipliers(
    @SerializedName("Market_Cap") val marketCap: Double?,
    @SerializedName("Enterprise_Value") val enterpriseValue: Double?,
    @SerializedName("PE_Ratio") val peRatio: Double?,
    @SerializedName("Forward_PE") val forwardPe: Double?,
    @SerializedName("PEG_Ratio") val pegRatio: Double?,
    @SerializedName("PS_Ratio") val psRatio: Double?,
    @SerializedName("PB_Ratio") val pbRatio: Double?,
    @SerializedName("P_FCF_Ratio") val pFcfRatio: Double?,
    @SerializedName("EV_Revenue") val evRevenue: Double?,
    @SerializedName("Enterprise_To_EBITDA") val enterpriseToEbitda: Double?
)

// ==========================================
// 5. INCOME STATEMENT (/income)
// ==========================================
@Keep
data class IncomeStatement(
    @SerializedName("Revenue_TTM") val revenueTtm: Double?,
    @SerializedName("Revenue_Quarterly") val revenueQuarterly: Double?,
    @SerializedName("Revenue_Growth_Yoy_Pct") val revenueGrowthYoyPct: Double?,
    @SerializedName("Earnings_Growth_Yoy_Pct") val earningsGrowthYoyPct: Double?,
    @SerializedName("Gross_Profit_TTM") val grossProfitTtm: Double?,
    @SerializedName("Gross_Profit_Quarterly") val grossProfitQuarterly: Double?,
    @SerializedName("EBITDA_TTM") val ebitdaTtm: Double?,
    @SerializedName("EBITDA_Quarterly") val ebitdaQuarterly: Double?,
    @SerializedName("Net_Income_TTM") val netIncomeTtm: Double?,
    @SerializedName("Net_Income_Quarterly") val netIncomeQuarterly: Double?,
    @SerializedName("EPS_TTM") val epsTtm: Double?,
    @SerializedName("Forward_EPS") val forwardEps: Double?,
    @SerializedName("EPS_Quarterly") val epsQuarterly: Double?
)

// ==========================================
// 6. BALANCE SHEET (/balance-sheet)
// ==========================================
@Keep
data class BalanceSheet(
    @SerializedName("Total_Assets") val totalAssets: Double?,
    @SerializedName("Total_Liabilities") val totalLiabilities: Double?,
    @SerializedName("Equity") val equity: Double?,
    @SerializedName("Total_Debt") val totalDebt: Double?,
    @SerializedName("Net_Debt") val netDebt: Double?,
    @SerializedName("Cash_and_Short_Term_Investments") val cashAndShortTermInvestments: Double?,
    @SerializedName("Total_Cash") val totalCash: Double?,
    @SerializedName("Current_Ratio") val currentRatio: Double?,
    @SerializedName("Quick_Ratio") val quickRatio: Double?,
    @SerializedName("Debt_Equity_Ratio") val debtEquityRatio: Double?,
    @SerializedName("Shares_Outstanding") val sharesOutstanding: Double?
)

// ==========================================
// 7. CASH FLOW (/cash-flow)
// ==========================================
@Keep
data class CashFlows(
    @SerializedName("Cash_From_Operations_TTM") val cashFromOperationsTtm: Double?,
    @SerializedName("Cash_From_Investing_TTM") val cashFromInvestingTtm: Double?,
    @SerializedName("Cash_From_Financing_TTM") val cashFromFinancingTtm: Double?,
    @SerializedName("Free_Cash_Flow_TTM") val freeCashFlowTtm: Double?,
    @SerializedName("Free_Cashflow_YF") val freeCashflowYf: Double?,
    @SerializedName("Capital_Expenditures_TTM") val capitalExpendituresTtm: Double?
)

// ==========================================
// 8. PROFITABILITY & EFFICIENCY (/profitability)
// ==========================================
@Keep
data class EfficiencyMargins(
    @SerializedName("Gross_Margin_Pct") val grossMarginPct: Double?,
    @SerializedName("Operating_Margin_YF") val operatingMarginYf: Double?,
    @SerializedName("Net_Profit_Margin_Pct") val netProfitMarginPct: Double?,
    @SerializedName("EBITDA_Margin_Pct") val ebitdaMarginPct: Double?,
    @SerializedName("ROE_Pct") val roePct: Double?,
    @SerializedName("ROE_YF") val roeYf: Double?,
    @SerializedName("ROA_Pct") val roaPct: Double?,
    @SerializedName("ROA_YF") val roaYf: Double?,
    @SerializedName("Asset_Turnover_Ratio") val assetTurnoverRatio: Double?,
    @SerializedName("Dividend_Yield_Pct") val dividendYieldPct: Double?
)

// ==========================================
// 9. MOMENTUM (/momentum)
// ==========================================
@Keep
data class HistoricalReturns(
    @SerializedName("Return_1W") val return1W: Double?,
    @SerializedName("Return_1M") val return1M: Double?,
    @SerializedName("Return_3M") val return3M: Double?,
    @SerializedName("Return_6M") val return6M: Double?,
    @SerializedName("Return_YTD") val returnYtd: Double?,
    @SerializedName("Return_1Y") val return1Y: Double?,
    @SerializedName("Return_3Y") val return3Y: Double?,
    @SerializedName("Return_5Y") val return5Y: Double?
)

// ==========================================
// 10. TECHNICALS (/technicals)
// ==========================================
@Keep
data class AdvancedTechnicals(
    @SerializedName("Technical_Rating_Overall") val ratingOverall: Double?,
    @SerializedName("Technical_Rating_MovingAverages") val ratingMovingAverages: Double?,
    @SerializedName("Technical_Rating_Oscillators") val ratingOscillators: Double?,
    @SerializedName("RSI_14") val rsi14: Double?,
    @SerializedName("MACD_Line") val macdLine: Double?,
    @SerializedName("MACD_Signal_Line") val macdSignalLine: Double?,
    @SerializedName("EMA_20") val ema20: Double?,
    @SerializedName("SMA_50") val sma50: Double?,
    @SerializedName("SMA_100") val sma100: Double?,
    @SerializedName("SMA_200") val sma200: Double?,
    @SerializedName("ATR_14") val atr14: Double?,
    @SerializedName("Stochastic_K") val stochasticK: Double?,
    @SerializedName("Stochastic_D") val stochasticD: Double?,
    @SerializedName("CCI_20") val cci20: Double?,
    @SerializedName("ADX_14") val adx14: Double?,
    @SerializedName("Awesome_Oscillator") val awesomeOscillator: Double?,
    @SerializedName("Momentum_10") val momentum10: Double?,
    @SerializedName("Bull_Bear_Power") val bullBearPower: Double?,
    @SerializedName("Ultimate_Oscillator") val ultimateOscillator: Double?,
    @SerializedName("Williams_R_Pct") val williamsRPct: Double?,
    @SerializedName("Chaikin_Money_Flow") val chaikinMoneyFlow: Double?
)

// ==========================================
// 11. ANALYST COVERAGE (/analyst)
// ==========================================
@Keep
data class AnalystCoverage(
    @SerializedName("Analyst_Rating_Consensus") val analystRatingConsensus: String?,
    @SerializedName("Target_Price_Mean") val targetPriceMean: Double?,
    @SerializedName("Target_Price_High") val targetPriceHigh: Double?,
    @SerializedName("Target_Price_Low") val targetPriceLow: Double?,
    @SerializedName("Analyst_Count") val analystCount: Double?,
    @SerializedName("Count_Strong_Buy") val countStrongBuy: Int?,
    @SerializedName("Count_Buy") val countBuy: Int?,
    @SerializedName("Count_Hold") val countHold: Int?,
    @SerializedName("Count_Sell") val countSell: Int?,
    @SerializedName("Count_Strong_Sell") val countStrongSell: Int?,
    @SerializedName("Count_Total_Recommendations") val countTotalRecommendations: Int?
)

// ==========================================
// 12. ADVANCED RISK (/advanced-risk)
// ==========================================
@Keep
data class AdvancedRiskMetrics(
    @SerializedName("Piotroski_F_Score") val piotroskiFScore: Double?,
    @SerializedName("Altman_Z_Score") val altmanZScore: Double?,
    @SerializedName("Graham_Number") val grahamNumber: Double?,
    @SerializedName("Shares_Float") val sharesFloat: Double?
)

// ==========================================
// 13. APP UTILITIES & SEARCH & LIVE DTOs
// ==========================================
@Keep
data class SimilarStock(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Double,
    @SerializedName("change_percent") val changePercent: Double,
    @SerializedName("market_cap") val marketCap: Double? = 0.0
)

@Keep
data class StockSearchResult(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String,
    @SerializedName("exch") val exchange: String
)

// ==========================================
// 14. FULL PAYLOAD DTO (Maps exactly to /stock/{symbol}/full)
// ==========================================
@Keep
data class StockInfoDto(
    @SerializedName("Ticker") val ticker: String?,
    @SerializedName("Company_Name") val companyName: String?,
    @SerializedName("Sector") val sector: String?,
    @SerializedName("Industry") val industry: String?,
    @SerializedName("Asset_Type") val assetType: String?,
    @SerializedName("Logo_Id") val logoId: String?,
    @SerializedName("Employee_Count") val employeeCount: Double?,
    @SerializedName("Country") val country: String?,

    @SerializedName("Price_Last") val priceLast: Double?,
    @SerializedName("Price_Open") val priceOpen: Double?,
    @SerializedName("Price_High") val priceHigh: Double?,
    @SerializedName("Price_Low") val priceLow: Double?,
    @SerializedName("Change_Pct_1D") val changePct1D: Double?,
    @SerializedName("Change_Absolute_1D") val changeAbsolute1D: Double?,
    @SerializedName("previousClose") val previousClose: Double = 0.0,
    @SerializedName("High_52Week") val high52Week: Double?,
    @SerializedName("Low_52Week") val low52Week: Double?,
    @SerializedName("Volume_Current") val volumeCurrent: Double?,
    @SerializedName("Traded_Value_Daily") val tradedValueDaily: Double?,
    @SerializedName("Avg_Volume_10D") val avgVolume10D: Double?,
    @SerializedName("Avg_Volume_30D") val avgVolume30D: Double?,
    @SerializedName("Avg_Volume_90D") val avgVolume90D: Double?,
    @SerializedName("Relative_Volume_10D") val relativeVolume10D: Double?,
    @SerializedName("Beta_1Y") val beta1Y: Double?,
    @SerializedName("Beta_5Y") val beta5Y: Double?,

    @SerializedName("Market_Cap") val marketCap: Double?,
    @SerializedName("Enterprise_Value") val enterpriseValue: Double?,
    @SerializedName("PE_Ratio") val peRatio: Double?,
    @SerializedName("Forward_PE") val forwardPe: Double?,
    @SerializedName("PEG_Ratio") val pegRatio: Double?,
    @SerializedName("PS_Ratio") val psRatio: Double?,
    @SerializedName("PB_Ratio") val pbRatio: Double?,
    @SerializedName("P_FCF_Ratio") val pFcfRatio: Double?,
    @SerializedName("EV_Revenue") val evRevenue: Double?,
    @SerializedName("Enterprise_To_EBITDA") val enterpriseToEbitda: Double?,

    @SerializedName("Revenue_TTM") val revenueTtm: Double?,
    @SerializedName("Revenue_Quarterly") val revenueQuarterly: Double?,
    @SerializedName("Revenue_Growth_Yoy_Pct") val revenueGrowthYoyPct: Double?,
    @SerializedName("Earnings_Growth_Yoy_Pct") val earningsGrowthYoyPct: Double?,
    @SerializedName("Gross_Profit_TTM") val grossProfitTtm: Double?,
    @SerializedName("Gross_Profit_Quarterly") val grossProfitQuarterly: Double?,
    @SerializedName("EBITDA_TTM") val ebitdaTtm: Double?,
    @SerializedName("EBITDA_Quarterly") val ebitdaQuarterly: Double?,
    @SerializedName("Net_Income_TTM") val netIncomeTtm: Double?,
    @SerializedName("Net_Income_Quarterly") val netIncomeQuarterly: Double?,
    @SerializedName("EPS_TTM") val epsTtm: Double?,
    @SerializedName("Forward_EPS") val forwardEps: Double?,
    @SerializedName("EPS_Quarterly") val epsQuarterly: Double?,

    @SerializedName("Total_Assets") val totalAssets: Double?,
    @SerializedName("Total_Liabilities") val totalLiabilities: Double?,
    @SerializedName("Equity") val equity: Double?,
    @SerializedName("Total_Debt") val totalDebt: Double?,
    @SerializedName("Net_Debt") val netDebt: Double?,
    @SerializedName("Cash_and_Short_Term_Investments") val cashAndShortTermInvestments: Double?,
    @SerializedName("Total_Cash") val totalCash: Double?,
    @SerializedName("Current_Ratio") val currentRatio: Double?,
    @SerializedName("Quick_Ratio") val quickRatio: Double?,
    @SerializedName("Debt_Equity_Ratio") val debtEquityRatio: Double?,
    @SerializedName("Shares_Outstanding") val sharesOutstanding: Double?,

    @SerializedName("Cash_From_Operations_TTM") val cashFromOperationsTtm: Double?,
    @SerializedName("Cash_From_Investing_TTM") val cashFromInvestingTtm: Double?,
    @SerializedName("Cash_From_Financing_TTM") val cashFromFinancingTtm: Double?,
    @SerializedName("Free_Cash_Flow_TTM") val freeCashFlowTtm: Double?,
    @SerializedName("Free_Cashflow_YF") val freeCashflowYf: Double?,
    @SerializedName("Capital_Expenditures_TTM") val capitalExpendituresTtm: Double?,

    @SerializedName("Gross_Margin_Pct") val grossMarginPct: Double?,
    @SerializedName("Operating_Margin_YF") val operatingMarginYf: Double?,
    @SerializedName("Net_Profit_Margin_Pct") val netProfitMarginPct: Double?,
    @SerializedName("EBITDA_Margin_Pct") val ebitdaMarginPct: Double?,
    @SerializedName("ROE_Pct") val roePct: Double?,
    @SerializedName("ROE_YF") val roeYf: Double?,
    @SerializedName("ROA_Pct") val roaPct: Double?,
    @SerializedName("ROA_YF") val roaYf: Double?,
    @SerializedName("Asset_Turnover_Ratio") val assetTurnoverRatio: Double?,
    @SerializedName("Dividend_Yield_Pct") val dividendYieldPct: Double?,

    @SerializedName("Return_1W") val return1W: Double?,
    @SerializedName("Return_1M") val return1M: Double?,
    @SerializedName("Return_3M") val return3M: Double?,
    @SerializedName("Return_6M") val return6M: Double?,
    @SerializedName("Return_YTD") val returnYtd: Double?,
    @SerializedName("Return_1Y") val return1Y: Double?,
    @SerializedName("Return_3Y") val return3Y: Double?,
    @SerializedName("Return_5Y") val return5Y: Double?,

    @SerializedName("Technical_Rating_Overall") val technicalRatingOverall: Double?,
    @SerializedName("Technical_Rating_MovingAverages") val technicalRatingMovingAverages: Double?,
    @SerializedName("Technical_Rating_Oscillators") val technicalRatingOscillators: Double?,
    @SerializedName("RSI_14") val rsi14: Double?,
    @SerializedName("MACD_Line") val macdLine: Double?,
    @SerializedName("MACD_Signal_Line") val macdSignalLine: Double?,
    @SerializedName("EMA_20") val ema20: Double?,
    @SerializedName("SMA_50") val sma50: Double?,
    @SerializedName("SMA_100") val sma100: Double?,
    @SerializedName("SMA_200") val sma200: Double?,
    @SerializedName("ATR_14") val atr14: Double?,
    @SerializedName("Stochastic_K") val stochasticK: Double?,
    @SerializedName("Stochastic_D") val stochasticD: Double?,
    @SerializedName("CCI_20") val cci20: Double?,
    @SerializedName("ADX_14") val adx14: Double?,
    @SerializedName("Awesome_Oscillator") val awesomeOscillator: Double?,
    @SerializedName("Momentum_10") val momentum10: Double?,
    @SerializedName("Bull_Bear_Power") val bullBearPower: Double?,
    @SerializedName("Ultimate_Oscillator") val ultimateOscillator: Double?,
    @SerializedName("Williams_R_Pct") val williamsRPct: Double?,
    @SerializedName("Chaikin_Money_Flow") val chaikinMoneyFlow: Double?,

    // Extended Hours Data
    @SerializedName("Pre_Market_Price") val preMarketPrice: Double? = null,
    @SerializedName("Pre_Market_Change") val preMarketChange: Double? = null,
    @SerializedName("Post_Market_Price") val postMarketPrice: Double? = null,
    @SerializedName("Post_Market_Change") val postMarketChange: Double? = null,
    @SerializedName("Market_State") val marketState: String? = null,

    @SerializedName("Analyst_Rating_Consensus") val analystRatingConsensus: String?,
    @SerializedName("Target_Price_Mean") val targetPriceMean: Double?,
    @SerializedName("Target_Price_High") val targetPriceHigh: Double?,
    @SerializedName("Target_Price_Low") val targetPriceLow: Double?,
    @SerializedName("Analyst_Count") val analystCount: Double?,
    @SerializedName("Count_Strong_Buy") val countStrongBuy: Int?,
    @SerializedName("Count_Buy") val countBuy: Int?,
    @SerializedName("Count_Hold") val countHold: Int?,
    @SerializedName("Count_Sell") val countSell: Int?,
    @SerializedName("Count_Strong_Sell") val countStrongSell: Int?,
    @SerializedName("Count_Total_Recommendations") val countTotalRecommendations: Int?,

    @SerializedName("Piotroski_F_Score") val piotroskiFScore: Double?,
    @SerializedName("Altman_Z_Score") val altmanZScore: Double?,
    @SerializedName("Graham_Number") val grahamNumber: Double?,
    @SerializedName("Shares_Float") val sharesFloat: Double?,

    @SerializedName("news") val news: List<StockNews> = emptyList(),
    @SerializedName("similar_stocks") val similarStocks: List<SimilarStock> = emptyList()
)

// ==========================================
// 15. SECONDARY ENDPOINT RESPONSE DTOs
// ==========================================
@Keep
data class StockAnalysisResponse(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("company") val company: String?,
    @SerializedName("analysis_report") val analysisReport: String
)

