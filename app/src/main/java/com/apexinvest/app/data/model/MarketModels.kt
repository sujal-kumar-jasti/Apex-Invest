package com.apexinvest.app.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

// 1. News Item
@Keep
data class StockNews(
    @SerializedName("title") val title: String?,
    @SerializedName("publisher") val publisher: String?,
    @SerializedName("link") val link: String?,
    @SerializedName("published") val published: String?
)

// 2. Search Result
@Keep
data class SearchResultDto(
    @SerializedName("symbol") val symbol: String, // Keep non-nullable ONLY if you are 100% sure the API ALWAYS sends it
    @SerializedName("name") val name: String?,
    @SerializedName("exchange") val exchange: String?,
    @SerializedName("type") val type: String?
)

// 3. Trending Stock
@Keep
data class TrendingStockDto(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("name") val name: String?,
    @SerializedName("price") val price: Double?, // Made nullable so it defaults to null instead of crashing if missing
    @SerializedName("changePercent") val changePercent: Double?,
    @SerializedName("currency") val currency: String?
)

// 4. Commodities & Indices
@Keep
data class CommodityDto(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("name") val name: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("currency") val currency: String?,
    @SerializedName("price") val price: Double?,
    @SerializedName("changePercent") val changePercent: Double?
)