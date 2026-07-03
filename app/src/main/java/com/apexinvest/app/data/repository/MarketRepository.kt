package com.apexinvest.app.data.repository

import com.apexinvest.app.data.model.CommodityDto
import com.apexinvest.app.data.model.SearchResultDto
import com.apexinvest.app.data.model.StockNews
import com.apexinvest.app.data.model.TrendingStockDto
<<<<<<< HEAD
import com.apexinvest.app.data.remote.ApexInvestApiService
=======
import com.apexinvest.app.data.remote.PrognosApiService
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MarketRepository(
<<<<<<< HEAD
    private val api: ApexInvestApiService
) {
    // Fetch News
    suspend fun getNews(symbol: String): List<StockNews> = withContext(Dispatchers.IO) {
        api.getStockNews(symbol)
=======
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
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
    }

    // Search Stocks
    suspend fun search(query: String): List<SearchResultDto> = withContext(Dispatchers.IO) {
<<<<<<< HEAD
        api.searchStocks(query)
=======
        try {
            api.searchStocks(query)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
    }

    // Get Trending
    suspend fun getTrending(): List<TrendingStockDto> = withContext(Dispatchers.IO) {
<<<<<<< HEAD
        api.getTrendingStocks()
    }

    // Get Commodities
    suspend fun getCommodities(): List<CommodityDto> = withContext(Dispatchers.IO) {
        api.getCommodities()
    }

    // Get Global Indices
    suspend fun getGlobalIndices(): List<CommodityDto> = withContext(Dispatchers.IO) {
        api.getGlobalIndices()
=======
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
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
    }
}