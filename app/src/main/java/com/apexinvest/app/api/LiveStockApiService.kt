package com.apexinvest.app.api

import com.apexinvest.app.api.models.StockHistoryChartDto
import com.apexinvest.app.api.models.StockLiveQuoteDto
import com.apexinvest.app.api.models.StockSearchResult
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LiveStockApiService {


    @GET("search")
    suspend fun searchStocks(
        @Query("q") query: String
    ): List<StockSearchResult>


    @GET("stock/{symbol}/live")
    suspend fun getStockLive(
        @Path("symbol") symbol: String
    ): StockLiveQuoteDto


    @GET("stock/{symbol}/chart")
    suspend fun getStockChart(
        @Path("symbol") symbol: String,
        @Query("range") range: String = "1d"
    ): StockHistoryChartDto
}