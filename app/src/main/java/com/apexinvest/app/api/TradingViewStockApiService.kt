package com.apexinvest.app.api

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

// --- NEW ISOLATED MICRO-METADATA DTOs ---

@Keep
data class TvMetaResponse(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("resolved_ticker") val resolvedTicker: String, // 🆕 Added to capture the backend's matched key
    @SerializedName("company_name") val companyName: String,
    @SerializedName("sector") val sector: String,
    @SerializedName("industry") val industry: String
)

// --- RETROFIT INTERFACE ---
interface TradingViewStockApiService {

    @GET("stock/meta")
    suspend fun getStockMeta(
        @Query("symbol") symbol: String,
        @Query("exchange") exchange: String? = null
    ): TvMetaResponse

}