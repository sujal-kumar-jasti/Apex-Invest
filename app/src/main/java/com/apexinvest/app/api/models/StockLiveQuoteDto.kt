package com.apexinvest.app.api.models

import com.google.gson.annotations.SerializedName

data class StockLiveQuoteDto(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("price") val price: Double,
    @SerializedName("change") val change: Double,
    @SerializedName("changePercent") val changePercent: Double,
    @SerializedName("previousClose") val previousClose: Double,
    @SerializedName("open") val open: Double,
    @SerializedName("dayHigh") val dayHigh: Double,
    @SerializedName("dayLow") val dayLow: Double,
    @SerializedName("yearHigh") val yearHigh: Double,
    @SerializedName("yearLow") val yearLow: Double,

    // 🚀 Extended Hours Data
    @SerializedName("prePrice") val prePrice: Double? = null,
    @SerializedName("preChange") val preChange: Double? = null,
    @SerializedName("postPrice") val postPrice: Double? = null,
    @SerializedName("postChange") val postChange: Double? = null,
    @SerializedName("marketState") val marketState: String? = null,
    @SerializedName("hasPrePost") val hasPrePost: Boolean = true,

    @SerializedName("candlesJson") val candlesJson: String? = null // 🆕 Optional sparkline data
)