package com.apexinvest.app.api

import com.apexinvest.app.api.models.PythonStockResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface StockApiService {

    // 1. Fetch Full Data (Price + History)
    // Use this for your Stock Detail Screen (Graph View)
    @GET("stock")
    suspend fun getStockDetails(
        @Query("symbol") symbol: String,
        @Query("range") range: String,
        @Query("charts") charts: Boolean = true // Request full chart history
    ): PythonStockResponse

    // 2. Fetch Live Price Only (No History)
    // Use this for Watchlist, Portfolio, or Home Screen
    // It passes charts=false, making the Python backend skip the heavy history fetch
    @GET("stock")
    suspend fun getLivePrice(
        @Query("symbol") symbol: String,
        @Query("range") range: String = "1D", // Range is ignored by backend if charts=false, but required by API
        @Query("charts") charts: Boolean = false // This triggers the optimization!
    ): PythonStockResponse
}