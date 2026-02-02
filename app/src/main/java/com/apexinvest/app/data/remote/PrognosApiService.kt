package com.apexinvest.app.data.remote

import com.apexinvest.app.data.model.CommodityDto
import com.apexinvest.app.data.model.SearchResultDto
import com.apexinvest.app.data.model.StockNews
import com.apexinvest.app.data.model.TrendingStockDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PrognosApiService {

    @GET("/search")
    suspend fun searchStocks(@Query("q") query: String): List<SearchResultDto>

    @GET("/news/{symbol}")
    suspend fun getStockNews(@Path("symbol") symbol: String): List<StockNews>

    @GET("/market/trending")
    suspend fun getTrendingStocks(): List<TrendingStockDto>

    // NEW: Live Commodities & Indices
    @GET("/market/commodities")
    suspend fun getCommodities(): List<CommodityDto>
}