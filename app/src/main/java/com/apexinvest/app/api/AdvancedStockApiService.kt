package com.apexinvest.app.api

import com.apexinvest.app.api.models.AnalystDataResponse
import com.apexinvest.app.api.models.FinancialsResponse
import com.apexinvest.app.api.models.ShareholdingResponse
import com.apexinvest.app.api.models.StockAnalysisResponse
import com.apexinvest.app.api.models.StockInfoDto
import retrofit2.http.GET
import retrofit2.http.Path

interface AdvancedStockApiService {

    @GET("stock/{symbol}/full")
    suspend fun getIndiaFullData(
        @Path("symbol") symbol: String
    ): StockInfoDto

    @GET("stock/{symbol}/profile")
    suspend fun getIndiaProfile(
        @Path("symbol") symbol: String
    ): StockInfoDto

    @GET("stock/{symbol}/market")
    suspend fun getIndiaMarket(
        @Path("symbol") symbol: String
    ): StockInfoDto

    @GET("stock/{symbol}/valuation")
    suspend fun getIndiaValuation(
        @Path("symbol") symbol: String
    ): StockInfoDto

    @GET("stock/{symbol}/income")
    suspend fun getIndiaIncome(
        @Path("symbol") symbol: String
    ): StockInfoDto

    @GET("stock/{symbol}/balance-sheet")
    suspend fun getIndiaBalanceSheet(
        @Path("symbol") symbol: String
    ): StockInfoDto

    @GET("stock/{symbol}/cash-flow")
    suspend fun getIndiaCashFlow(
        @Path("symbol") symbol: String
    ): StockInfoDto

    @GET("stock/{symbol}/profitability")
    suspend fun getIndiaProfitability(
        @Path("symbol") symbol: String
    ): StockInfoDto

    @GET("stock/{symbol}/momentum")
    suspend fun getIndiaPerformance(
        @Path("symbol") symbol: String
    ): StockInfoDto

    @GET("stock/{symbol}/technicals")
    suspend fun getIndiaTechnicals(
        @Path("symbol") symbol: String
    ): StockInfoDto

    @GET("stock/{symbol}/analyst")
    suspend fun getIndiaAnalystCoverage(
        @Path("symbol") symbol: String
    ): StockInfoDto

    @GET("stock/{symbol}/advanced-risk")
    suspend fun getIndiaAdvancedRisk(
        @Path("symbol") symbol: String
    ): StockInfoDto

    @GET("stock/{symbol}/analysis")
    suspend fun getIndiaAiAnalysis(
        @Path("symbol") symbol: String
    ): StockAnalysisResponse

    @GET("stock/{symbol}/financials")
    suspend fun getIndiaFinancials(
        @Path("symbol") symbol: String
    ): FinancialsResponse

    @GET("stock/{symbol}/shareholding")
    suspend fun getIndiaShareholding(
        @Path("symbol") symbol: String
    ): ShareholdingResponse

    @GET("stock/{symbol}/analysts")
    suspend fun getIndiaAnalystRatings(
        @Path("symbol") symbol: String
    ): AnalystDataResponse
}