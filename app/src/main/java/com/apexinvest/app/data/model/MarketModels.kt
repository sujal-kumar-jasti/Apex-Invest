package com.apexinvest.app.data.model

import com.google.gson.annotations.SerializedName

// 1. News Item
data class StockNews(
    val title: String?,
    val publisher: String?,
    val link: String?,
    val published: String?
)

// 2. Search Result
data class SearchResultDto(
    val symbol: String, // Keep non-nullable ONLY if you are 100% sure the API ALWAYS sends it
    val name: String?,
    val exchange: String?,
    val type: String?
)

// 3. Trending Stock
data class TrendingStockDto(
    val symbol: String,
    val name: String?,
    val price: Double?, // Made nullable so it defaults to null instead of crashing if missing
    @SerializedName("changePercent") val changePercent: Double?,
    val currency: String?
)

// 4. Commodities & Indices
data class CommodityDto(
    val symbol: String,
    val name: String?,
    val type: String?,
    val currency: String?,
    val price: Double?,
    @SerializedName("changePercent") val changePercent: Double?
)