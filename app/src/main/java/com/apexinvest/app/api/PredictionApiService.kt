package com.apexinvest.app.api

import com.apexinvest.app.data.PortfolioAnalysisRequest
import com.apexinvest.app.data.PortfolioSummary
import com.apexinvest.app.data.StockAnalysisRequest
import com.apexinvest.app.data.StockAnalysisResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface PredictionApiService {

    @Headers("Content-Type: application/json")
    @POST("api/v1/analyze/stock")
    suspend fun analyzeStock(
        @Header("X-Firebase-IDToken") idToken: String,
        @Body request: StockAnalysisRequest
    ): StockAnalysisResponse

    @Headers("Content-Type: application/json")
    @POST("api/v1/analyze/portfolio")
    suspend fun analyzePortfolio(
        @Header("X-Firebase-IDToken") idToken: String,
        @Body request: PortfolioAnalysisRequest
    ): PortfolioSummary
}