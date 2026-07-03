package com.apexinvest.app.data.repository

import com.apexinvest.app.data.model.CommodityDto
import com.apexinvest.app.data.model.SearchResultDto
import com.apexinvest.app.data.model.StockNews
import com.apexinvest.app.data.model.TrendingStockDto
import com.apexinvest.app.data.remote.ApexInvestApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MarketRepository(
    private val api: ApexInvestApiService
) {
    // Fetch News
    suspend fun getNews(symbol: String): List<StockNews> = withContext(Dispatchers.IO) {
        api.getStockNews(symbol)
    }

    // Search Stocks
    suspend fun search(query: String): List<SearchResultDto> = withContext(Dispatchers.IO) {
        api.searchStocks(query)
    }

    // Get Trending
    suspend fun getTrending(): List<TrendingStockDto> = withContext(Dispatchers.IO) {
        api.getTrendingStocks()
    }

    // Get Commodities
    suspend fun getCommodities(): List<CommodityDto> = withContext(Dispatchers.IO) {
        api.getCommodities()
    }

    // Get Global Indices
    suspend fun getGlobalIndices(): List<CommodityDto> = withContext(Dispatchers.IO) {
        api.getGlobalIndices()
    }
}