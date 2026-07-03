package com.apexinvest.app.api

import com.apexinvest.app.api.models.CollectionItem
import com.apexinvest.app.api.models.PythonStockInfoDto
import com.apexinvest.app.api.models.ScreenerResult
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface StockApiService {

    // --- 1. HEAVY INFO (Fundamentals, Charts, Peers) ---
    // Matches: GET /stock/{symbol}/info
    @GET("stock/{symbol}/info")
    suspend fun getStockInfo(
        @Path("symbol") symbol: String
    ): PythonStockInfoDto

    // --- 2. COLLECTIONS (Top Gainers, Losers, etc.) ---
    // Matches: GET /collections/{type}
    @GET("collections/{type}")
    suspend fun getCollection(
        @Path("type") type: String
    ): List<CollectionItem>

    // --- 3. SCREENER (Filter Stocks) ---
    // Matches: POST /screener
    @POST("screener")
    suspend fun runScreener(
        @Query("sector") sector: String? = null,
        @Query("market_cap_min") minMarketCap: Double? = null,
        @Query("pe_min") minPe: Double? = null,
        @Query("pe_max") maxPe: Double? = null
    ): List<ScreenerResult>
}