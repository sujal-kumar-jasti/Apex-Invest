package com.apexinvest.app.api

import com.apexinvest.app.api.models.PythonStockInfoDto
import retrofit2.http.GET
import retrofit2.http.Path

interface StockApiService {
    @GET("stock/{symbol}/info")
    suspend fun getStockInfo(
        @Path("symbol") symbol: String
    ): PythonStockInfoDto

}