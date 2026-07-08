package com.apexinvest.app.api

import com.apexinvest.app.api.models.yahoo.YahooChartResponse
import com.apexinvest.app.api.models.yahoo.YahooQuoteResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface YahooFinanceApiService {

    @GET("v8/finance/chart/{symbol}")
    suspend fun getLivePriceAndChart(
        @Path("symbol") symbol: String,
        @Query("interval") interval: String = "1m",
        @Query("range") range: String = "1d",
        @Query("includePrePost") includePrePost: Boolean = false,
        @Query("crumb") crumb: String? = null
    ): YahooChartResponse

    /**
     * Fetch live quotes for multiple symbols.
     */
    @GET("v7/finance/quote")
    suspend fun getQuotes(
        @Query("symbols") symbols: String,
        @Query("crumb") crumb: String? = null
    ): YahooQuoteResponse

    /**
     * Fetch current quote only.
     */
    @Deprecated("Use getQuotes(symbol) for more efficient price updates")
    @GET("v8/finance/chart/{symbol}")
    suspend fun getLivePriceOnly(
        @Path("symbol") symbol: String,
        @Query("interval") interval: String = "1m",
        @Query("range") range: String = "1d",
        @Query("includePrePost") includePrePost: Boolean = true
    ): YahooChartResponse
}
