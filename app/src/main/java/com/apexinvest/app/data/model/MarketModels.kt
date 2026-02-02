package com.apexinvest.app.data.model

import com.google.gson.annotations.SerializedName

// 1. News Item (From /news/{symbol})
data class StockNews(
    val title: String,
    val publisher: String,
    val link: String,
    val published: String
)

// 2. Search Result (From /search?q=...)
data class SearchResultDto(
    val symbol: String,
    val name: String,
    val exchange: String,
    val type: String
)

// 3. Trending Stock (From /market/trending)
data class TrendingStockDto(
    val symbol: String,
    val price: Double,
    @SerializedName("changePercent") val changePercent: Double
)

data class CommodityDto(
    val symbol: String,
    val name: String,
    val type: String,
    val currency: String,
    val price: Double,
    val change: Double,
    val changePercent: Double
)