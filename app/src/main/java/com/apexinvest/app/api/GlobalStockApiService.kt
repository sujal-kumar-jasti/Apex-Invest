package com.apexinvest.app.api

import retrofit2.http.GET
import retrofit2.http.Query

// Response models matching your Python JSON structure
data class GlobalStockResponse(
    val symbol: String,
    val price: Double,
    val change: Double,
    val changePercent: Double,
    val prevClose: Double,
    val open: Double,
    val dayHigh: Double,
    val dayLow: Double,
    val marketCap: String,
    val peRatio: String,
    val dividendYield: String,
    val yearHigh: String,
    val yearLow: String,
    val historyPoints: List<List<Any>>
)

data class GlobalSearchResponse(
    val symbol: String,
    val name: String,
    val type: String,
    val exchange: String
)

interface GlobalStockApiService {

    @GET("stock")
    suspend fun getStockDetails(
        @Query("symbol") symbol: String,
        @Query("range") range: String,
        @Query("charts") charts: Boolean = true
    ): GlobalStockResponse

    @GET("search")
    suspend fun searchStocks(
        @Query("q") query: String
    ): List<GlobalSearchResponse>
}