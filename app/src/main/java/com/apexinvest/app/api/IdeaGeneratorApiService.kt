package com.apexinvest.app.api

import com.apexinvest.app.api.models.IdeaResponse
import com.apexinvest.app.api.models.PortfolioRequest
import com.apexinvest.app.api.models.ThemeRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface IdeasApi {

    @POST("generate/portfolio-ideas")
    suspend fun getPortfolioAnalysis(
        @Body request: PortfolioRequest
    ): Response<IdeaResponse>

    @POST("generate/thematic-ideas")
    suspend fun getThematicAnalysis(
        @Body request: ThemeRequest
    ): Response<IdeaResponse>
}