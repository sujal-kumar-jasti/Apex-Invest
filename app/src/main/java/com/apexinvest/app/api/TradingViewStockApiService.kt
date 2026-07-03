package com.apexinvest.app.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

// --- NEW ISOLATED MICRO-METADATA DTOs ---

data class TvMetaResponse(
    val symbol: String,
    @SerializedName("resolved_ticker") val resolvedTicker: String, // 🆕 Added to capture the backend's matched key
    @SerializedName("company_name") val companyName: String,
    val sector: String,
    val industry: String
)

data class TvNameResponse(
    val symbol: String,
    @SerializedName("company_name") val companyName: String
)

data class TvSectorResponse(
    val symbol: String,
    val sector: String
)

// --- PRE-EXISTING DATA MODELS ---

data class TvProfileResponse(
    val symbol: String,
    val exchange: String,
    @SerializedName("company_name") val companyName: String,
    val sector: String,
    val industry: String,
    val employees: Any? // Can be string text description or integer count
)

data class TvValuationMetrics(
    @SerializedName("pe_ratio_ttm") val peRatioTtm: Double?,
    @SerializedName("price_to_sales") val priceToSales: Double?,
    @SerializedName("price_to_book") val priceToBook: Double?,
    @SerializedName("eps_ttm") val epsTtm: Double?,
    @SerializedName("dividend_yield_pct") val dividendYieldPct: Double
)

data class TvFinancialsResponse(
    val symbol: String,
    @SerializedName("valuation_metrics") val valuationMetrics: TvValuationMetrics
)

data class TvRawScores(
    val overall: Double,
    @SerializedName("moving_averages") val movingAverages: Double,
    val oscillators: Double
)

data class TvRatingsResponse(
    val symbol: String,
    @SerializedName("composite_rating") val compositeRating: String,
    @SerializedName("moving_averages_rating") val movingAveragesRating: String,
    @SerializedName("oscillators_rating") val oscillatorsRating: String,
    @SerializedName("raw_scores") val rawScores: TvRawScores
)

// Combined Core Unified Mapping DTO
data class TvCompleteDetailsResponse(
    val identity: TvProfileResponse,
    val fundamentals: TvValuationMetrics,
    @SerializedName("sentiment_ratings") val sentimentRatings: TvRatingsSummary
)

data class TvRatingsSummary(
    val summary: String,
    @SerializedName("moving_averages") val movingAverages: String,
    val oscillators: String
)

// Bulk market endpoint response
data class TvMarketData(
    val price: Double?,
    @SerializedName("change_percent") val changePercent: Double,
    val volume: Long,
    val currency: String,
    val type: String
)

data class TvBulkFundamentalStock(
    val symbol: String,
    @SerializedName("company_name") val companyName: String,
    val sector: String,
    val industry: String,
    @SerializedName("technical_rating") val technicalRating: String,
    val fundamentals: TvValuationMetrics,
    val market_data: TvMarketData
)

// --- RETROFIT INTERFACE ---
interface TradingViewStockApiService {

    @GET("stock/meta")
    suspend fun getStockMeta(
        @Query("symbol") symbol: String,
        @Query("exchange") exchange: String? = null
    ): TvMetaResponse

    @GET("stock/name")
    suspend fun getStockName(
        @Query("symbol") symbol: String,
        @Query("exchange") exchange: String? = null
    ): TvNameResponse

    @GET("stock/sector")
    suspend fun getStockSector(
        @Query("symbol") symbol: String,
        @Query("exchange") exchange: String? = null
    ): TvSectorResponse

    @GET("stock/profile")
    suspend fun getStockProfile(
        @Query("symbol") symbol: String,
        @Query("exchange") exchange: String? = null
    ): TvProfileResponse

    @GET("stock/financials")
    suspend fun getStockFinancials(
        @Query("symbol") symbol: String,
        @Query("exchange") exchange: String? = null
    ): TvFinancialsResponse

    @GET("stock/ratings")
    suspend fun getStockRatings(
        @Query("symbol") symbol: String,
        @Query("exchange") exchange: String? = null
    ): TvRatingsResponse

    @GET("stock/all-details")
    suspend fun getCompleteStockDetails(
        @Query("symbol") symbol: String,
        @Query("exchange") exchange: String? = null
    ): TvCompleteDetailsResponse

    @GET("market/stocks/fundamentals")
    suspend fun getNiftyFundamentals(): List<TvBulkFundamentalStock>
}