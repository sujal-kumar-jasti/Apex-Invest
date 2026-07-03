package com.apexinvest.app.api.models.yahoo

import com.google.gson.annotations.SerializedName

data class YahooChartResponse(
    @SerializedName("chart") val chart: YahooChartResultWrapper?
)

data class YahooChartResultWrapper(
    @SerializedName("result") val result: List<YahooChartData>?
)

data class YahooChartData(
    @SerializedName("meta") val meta: YahooChartMeta?,
    @SerializedName("timestamp") val timestamp: List<Long>?,
    @SerializedName("indicators") val indicators: YahooIndicators?
)

data class YahooChartMeta(
    @SerializedName("symbol") val symbol: String?,
    @SerializedName("regularMarketPrice") val regularMarketPrice: Double?,
    @SerializedName("chartPreviousClose") val chartPreviousClose: Double?,
    @SerializedName("regularMarketPreviousClose") val regularMarketPreviousClose: Double?,
    @SerializedName("previousClose") val previousClose: Double?,
    @SerializedName("regularMarketOpen") val regularMarketOpen: Double?,
    @SerializedName("regularMarketDayHigh") val regularMarketDayHigh: Double?,
    @SerializedName("regularMarketDayLow") val regularMarketDayLow: Double?,
    @SerializedName("fiftyTwoWeekHigh") val fiftyTwoWeekHigh: Double?,
    @SerializedName("fiftyTwoWeekLow") val fiftyTwoWeekLow: Double?,
    @SerializedName("hasPrePostMarketData") val hasPrePostMarketData: Boolean? = null,

    // 🚀 Extended Hours Data
    @SerializedName("preMarketPrice") val preMarketPrice: Double? = null,
    @SerializedName("postMarketPrice") val postMarketPrice: Double? = null,
    @SerializedName("preMarketChange") val preMarketChange: Double? = null,
    @SerializedName("postMarketChange") val postMarketChange: Double? = null,
    @SerializedName("extendedMarketPrice") val extendedMarketPrice: Double? = null,
    @SerializedName("extendedMarketChange") val extendedMarketChange: Double? = null,
    @SerializedName("extendedMarketChangePercent") val extendedMarketChangePercent: Double? = null,
    @SerializedName("exchangeTimezoneName") val exchangeTimezoneName: String? = null,
    @SerializedName("marketState") val marketState: String? = null
) {
    val currentPrice: Double
        get() = regularMarketPrice ?: previousClose ?: chartPreviousClose ?: 0.0

    val officialPreviousClose: Double
        get() = regularMarketPreviousClose ?: previousClose ?: chartPreviousClose ?: 0.0

    val percentageChange: Double
        get() {
            val current = regularMarketPrice ?: return 0.0
            val previous = officialPreviousClose
            if (previous == 0.0) return 0.0
            return ((current - previous) / previous) * 100.0
        }
}

data class YahooIndicators(
    @SerializedName("quote") val quote: List<YahooQuote>?
)

data class YahooQuote(
    @SerializedName("open") val open: List<Double?>?,
    @SerializedName("close") val close: List<Double?>?,
    @SerializedName("high") val high: List<Double?>?,
    @SerializedName("low") val low: List<Double?>?,
    @SerializedName("volume") val volume: List<Long?>?
)

// 🚀 Lightweight Quote Models (v7/finance/quote)
data class YahooQuoteResponse(
    @SerializedName("quoteResponse") val quoteResponse: YahooQuoteResultWrapper?
)

data class YahooQuoteResultWrapper(
    @SerializedName("result") val result: List<YahooQuoteData>?,
    @SerializedName("error") val error: Any? = null
)

data class YahooQuoteData(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("regularMarketPrice") val regularMarketPrice: Double?,
    @SerializedName("regularMarketChange") val regularMarketChange: Double?,
    @SerializedName("regularMarketChangePercent") val regularMarketChangePercent: Double?,
    @SerializedName("regularMarketPreviousClose") val regularMarketPreviousClose: Double?,
    @SerializedName("regularMarketOpen") val regularMarketOpen: Double?,
    @SerializedName("regularMarketDayHigh") val regularMarketDayHigh: Double?,
    @SerializedName("regularMarketDayLow") val regularMarketDayLow: Double?,
    @SerializedName("fiftyTwoWeekHigh") val fiftyTwoWeekHigh: Double?,
    @SerializedName("fiftyTwoWeekLow") val fiftyTwoWeekLow: Double?,
    @SerializedName("marketState") val marketState: String?,
    @SerializedName("hasPrePostMarketData") val hasPrePostMarketData: Boolean?,

    // Extended Hours
    @SerializedName("preMarketPrice") val preMarketPrice: Double? = null,
    @SerializedName("preMarketChange") val preMarketChange: Double? = null,
    @SerializedName("postMarketPrice") val postMarketPrice: Double? = null,
    @SerializedName("postMarketChange") val postMarketChange: Double? = null
)
