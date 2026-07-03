package com.apexinvest.app.api

import com.apexinvest.app.api.models.JobInitResponse
import com.apexinvest.app.api.models.JobStatusResponse
import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// Add this data class to format the JSON payload
data class PortfolioAnalysisRequest(
    @SerializedName("symbols") val symbols: List<String>
)

interface PredictionApiService {

    @POST("api/v1/analyze/stock")
    suspend fun analyzeStock(
        @Query("symbol") symbol: String
    ): JobInitResponse

    // REVERTED to use user_id query parameter to match your Python code!
    // In PredictionApiService.kt
    @POST("api/v1/analyze/portfolio")
    suspend fun analyzePortfolio(
        @Body request: PortfolioAnalysisRequest // <-- Changed this!
    ): JobInitResponse

    @GET("api/v1/jobs/{job_id}")
    suspend fun checkJobStatus(
        @Path("job_id") jobId: String
    ): JobStatusResponse
}