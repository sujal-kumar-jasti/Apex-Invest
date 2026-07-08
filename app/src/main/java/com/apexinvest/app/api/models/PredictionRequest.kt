package com.apexinvest.app.api.models

import androidx.annotation.Keep
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

// --- 1. REQUEST MODELS (Added for Direct App-to-Backend Sync) ---

// --- 2. JOB POLLING MODELS (The Async Handlers) ---

@Keep
data class JobInitResponse(
    @SerializedName("job_id") val jobId: String,
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)

@Keep
data class JobStatusResponse(
    @SerializedName("status") val status: String,
    @SerializedName("error") val error: String? = null,
    // Using JsonElement so we can parse it later based on whether it's a Stock or Portfolio
    @SerializedName("data") val data: JsonElement? = null
)

// --- 3. THE MAIN RESPONSES (The Heavy Payloads) ---

@Keep
data class DeepAnalysisResponse(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("current_price") val currentPrice: Double,
    @SerializedName("financial_health_score") val financialHealthScore: String,
    @SerializedName("historical_chart_data") val historicalChartData: List<HistoricalPricePoint>,
    @SerializedName("monte_carlo_forecast") val monteCarloForecast: List<MonteCarloPoint>,
    @SerializedName("fundamentals") val fundamentals: FundamentalMetrics,
    @SerializedName("sentiment") val sentiment: SentimentAnalysis,
    @SerializedName("agent_synthesis") val agentSynthesis: AgentSynthesis
)

@Keep
data class PortfolioSummary(
    @SerializedName("user_id") val userId: String,
    @SerializedName("total_sentiment_score") val totalSentimentScore: Double,
    @SerializedName("market_mood") val marketMood: String,
    @SerializedName("risk_warning") val riskWarning: String?,
    @SerializedName("top_pick") val topPick: String?,
    @SerializedName("stock_breakdowns") val stockBreakdowns: List<DeepAnalysisResponse>
)

// --- 4. CHARTING & ARRAY DATA (Ready for X,Y Graphing) ---

@Keep
data class HistoricalPricePoint(
    @SerializedName("date") val date: String, // String format: "YYYY-MM-DD"
    @SerializedName("close") val close: Double,
    @SerializedName("volume") val volume: Double,
    @SerializedName("ma_50") val ma50: Double?,
    @SerializedName("rsi_14") val rsi14: Double?,
    @SerializedName("macd") val macd: Double?,
    @SerializedName("macd_signal") val macdSignal: Double?
)

@Keep
data class MonteCarloPoint(
    @SerializedName("date") val date: String, // X-Axis Coordinate
    @SerializedName("mean_price") val meanPrice: Double, // Y-Axis Primary Line
    @SerializedName("bull_case_90th") val bullCase90th: Double, // Y-Axis Upper Bound
    @SerializedName("bear_case_10th") val bearCase10th: Double // Y-Axis Lower Bound
)

@Keep
data class ChartPoint(
    @SerializedName("date") val date: String,
    @SerializedName("value") val value: Double
)

// --- 5. FUNDAMENTALS & AI NESTED OBJECTS ---

@Keep
data class FundamentalMetrics(
    @SerializedName("market_cap") val marketCap: String,
    @SerializedName("pe_ratio") val peRatio: Double,
    @SerializedName("debt_to_equity") val debtToEquity: Double,
    @SerializedName("put_call_ratio") val putCallRatio: Double,
    @SerializedName("institutional_ownership") val institutionalOwnership: Double,
    @SerializedName("revenue_history") val revenueHistory: List<ChartPoint>,
    @SerializedName("free_cash_flow_history") val freeCashFlowHistory: List<ChartPoint>
)

@Keep
data class SentimentAnalysis(
    @SerializedName("overall_score") val overallScore: Double,
    @SerializedName("label") val label: String,
    @SerializedName("news_articles") val newsArticles: List<NewsItem>
)

@Keep
data class NewsItem(
    @SerializedName("title") val title: String,
    @SerializedName("link") val link: String,
    @SerializedName("publisher") val publisher: String,
    @SerializedName("sentiment_label") val sentimentLabel: String,
    @SerializedName("score") val score: Double
)

@Keep
data class AgentSynthesis(
    @SerializedName("fundamental_thesis") val fundamentalThesis: String,
    @SerializedName("macro_news_thesis") val macroNewsThesis: String,
    @SerializedName("final_verdict") val finalVerdict: String
)