package com.apexinvest.app.data.repository

import com.apexinvest.app.data.model.CommodityDto
import com.apexinvest.app.data.model.SearchResultDto
import com.apexinvest.app.data.model.StockNews
import com.apexinvest.app.data.model.TrendingStockDto
import com.apexinvest.app.data.remote.PrognosApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MarketRepository(
    private val api: PrognosApiService
) {
    // Fetch News with Error Handling
    suspend fun getNews(symbol: String): List<StockNews> = withContext(Dispatchers.IO) {
        try {
            api.getStockNews(symbol)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Search Stocks
    suspend fun search(query: String): List<SearchResultDto> = withContext(Dispatchers.IO) {
        try {
            api.searchStocks(query)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Get Trending
    suspend fun getTrending(): List<TrendingStockDto> = withContext(Dispatchers.IO) {
        try {
            api.getTrendingStocks()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    suspend fun getCommodities(): List<CommodityDto> = withContext(Dispatchers.IO) {
        try {
            api.getCommodities()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}