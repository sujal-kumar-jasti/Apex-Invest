package com.apexinvest.app.data

import com.google.gson.annotations.SerializedName

// --- REQUESTS ---
data class StockAnalysisRequest(
    val symbol: String,
    @SerializedName("duration_days") val durationDays: Int = 7
)

data class PortfolioAnalysisRequest(
    val symbols: List<String>
)

// --- RESPONSES ---

// 1. Single Stock Analysis
data class StockAnalysisResponse(
    val symbol: String,
    @SerializedName("current_price") val currentPrice: Double,
    val recommendation: String,
    val fundamentals: FundamentalData,
    val sentiment: SentimentData,
    val forecast: List<PredictionPoint>
)

// 2. Portfolio Summary
data class PortfolioSummary(
    @SerializedName("total_sentiment_score") val totalScore: Double,
    @SerializedName("market_mood") val marketMood: String,
    @SerializedName("risk_warning") val riskWarning: String?,
    @SerializedName("top_pick") val topPick: String?,
    @SerializedName("analyzed_count") val analyzedCount: Int,
    @SerializedName("stock_breakdowns") val stockBreakdowns: List<StockAnalysisResponse>
)

data class SentimentData(
    val score: Double,
    val label: String,
    val summary: String,
    val news: List<NewsItem>
)

data class NewsItem(
    val title: String,
    val link: String,
    val publisher: String,
    @SerializedName("sentiment_label") val sentimentLabel: String,
    @SerializedName("sentiment_score") val sentimentScore: Double
)

data class FundamentalData(
    @SerializedName("market_cap") val marketCap: String,
    @SerializedName("pe_ratio") val peRatio: Double,
    val sector: String
)

data class PredictionPoint(
    val date: String,
    @SerializedName("predicted_price") val predictedPrice: Double
)