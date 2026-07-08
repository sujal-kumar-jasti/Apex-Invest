package com.apexinvest.app.api

import com.apexinvest.app.api.models.StockAnalysisResponse
import com.apexinvest.app.api.models.StockInfoDto
import retrofit2.http.GET
import retrofit2.http.Path

interface GlobalStockApiService {

    @GET("stock/{symbol}/full")
    suspend fun getGlobalFullData(
        @Path("symbol") symbol: String
    ): StockInfoDto

    @GET("stock/{symbol}/analysis")
    suspend fun getGlobalAiAnalysis(
        @Path("symbol") symbol: String
    ): StockAnalysisResponse

}