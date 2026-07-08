package com.apexinvest.app.api

import com.apexinvest.app.api.models.StockAnalysisResponse
import com.apexinvest.app.api.models.StockInfoDto
import retrofit2.http.GET
import retrofit2.http.Path

interface AdvancedStockApiService {

    @GET("stock/{symbol}/full")
    suspend fun getIndiaFullData(
        @Path("symbol") symbol: String
    ): StockInfoDto

    @GET("stock/{symbol}/analysis")
    suspend fun getIndiaAiAnalysis(
        @Path("symbol") symbol: String
    ): StockAnalysisResponse

}