package com.apexinvest.app.api

import com.apexinvest.app.api.models.AnalystDataResponse
import com.apexinvest.app.api.models.FinancialsResponse
import com.apexinvest.app.api.models.ShareholdingResponse
import com.apexinvest.app.api.models.StockAnalysisResponse
import com.apexinvest.app.api.models.StockInfoDto
import retrofit2.http.GET
import retrofit2.http.Path

interface GlobalStockApiService {

    @GET("stock/{symbol}/full")
    suspend fun getGlobalFullData(
        @Path("symbol") symbol: String
    ): StockInfoDto

    @GET("stock/{symbol}/profile")
    suspend fun getGlobalProfile(
        @Path("symbol") symbol: String
    ): StockInfoDto

    @GET("stock/{symbol}/market")
    suspend fun getGlobalMarket(
        @Path("symbol") symbol: String
    ): StockInfoDto

    @GET("stock/{symbol}/valuation")
    suspend fun getGlobalValuation(
        @Path("symbol") symbol: String
    ): StockInfoDto

    @GET("stock/{symbol}/income")
    suspend fun getGlobalIncome(
        @Path("symbol") symbol: String
    ): StockInfoDto

    @GET("stock/{symbol}/balance-sheet")
    suspend fun getGlobalBalanceSheet(
        @Path("symbol") symbol: String
    ): StockInfoDto

    @GET("stock/{symbol}/cash-flow")
    suspend fun getGlobalCashFlow(
        @Path("symbol") symbol: String
    ): StockInfoDto

    @GET("stock/{symbol}/profitability")
    suspend fun getGlobalProfitability(
        @Path("symbol") symbol: String
    ): StockInfoDto

    @GET("stock/{symbol}/momentum")
    suspend fun getGlobalPerformance(
        @Path("symbol") symbol: String
    ): StockInfoDto

    @GET("stock/{symbol}/technicals")
    suspend fun getGlobalTechnicals(
        @Path("symbol") symbol: String
    ): StockInfoDto

    @GET("stock/{symbol}/analyst")
    suspend fun getGlobalAnalystCoverage(
        @Path("symbol") symbol: String
    ): StockInfoDto

    @GET("stock/{symbol}/advanced-risk")
    suspend fun getGlobalAdvancedRisk(
        @Path("symbol") symbol: String
    ): StockInfoDto

    @GET("stock/{symbol}/analysis")
    suspend fun getGlobalAiAnalysis(
        @Path("symbol") symbol: String
    ): StockAnalysisResponse

    @GET("stock/{symbol}/financials")
    suspend fun getGlobalFinancials(
        @Path("symbol") symbol: String
    ): FinancialsResponse

    @GET("stock/{symbol}/shareholding")
    suspend fun getGlobalShareholding(
        @Path("symbol") symbol: String
    ): ShareholdingResponse

    @GET("stock/{symbol}/analysts")
    suspend fun getGlobalAnalystRatings(
        @Path("symbol") symbol: String
    ): AnalystDataResponse
}