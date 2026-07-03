package com.apexinvest.app.api

<<<<<<< HEAD
import com.apexinvest.app.api.models.CollectionItem
import com.apexinvest.app.api.models.PythonStockInfoDto
import com.apexinvest.app.api.models.ScreenerResult
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
=======
import com.apexinvest.app.api.models.PythonStockResponse
import retrofit2.http.GET
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
import retrofit2.http.Query

interface StockApiService {

<<<<<<< HEAD
    // --- 1. HEAVY INFO (Fundamentals, Charts, Peers) ---
    // Matches: GET /stock/{symbol}/info
    @GET("stock/{symbol}/info")
    suspend fun getStockInfo(
        @Path("symbol") symbol: String
    ): PythonStockInfoDto

    // --- 2. COLLECTIONS (Top Gainers, Losers, etc.) ---
    // Matches: GET /collections/{type}
    @GET("collections/{type}")
    suspend fun getCollection(
        @Path("type") type: String
    ): List<CollectionItem>

    // --- 3. SCREENER (Filter Stocks) ---
    // Matches: POST /screener
    @POST("screener")
    suspend fun runScreener(
        @Query("sector") sector: String? = null,
        @Query("market_cap_min") minMarketCap: Double? = null,
        @Query("pe_min") minPe: Double? = null,
        @Query("pe_max") maxPe: Double? = null
    ): List<ScreenerResult>
=======
    // 1. Fetch Full Data (Price + History)
    // Use this for your Stock Detail Screen (Graph View)
    @GET("stock")
    suspend fun getStockDetails(
        @Query("symbol") symbol: String,
        @Query("range") range: String,
        @Query("charts") charts: Boolean = true // Request full chart history
    ): PythonStockResponse

    // 2. Fetch Live Price Only (No History)
    // Use this for Watchlist, Portfolio, or Home Screen
    // It passes charts=false, making the Python backend skip the heavy history fetch
    @GET("stock")
    suspend fun getLivePrice(
        @Query("symbol") symbol: String,
        @Query("range") range: String = "1D", // Range is ignored by backend if charts=false, but required by API
        @Query("charts") charts: Boolean = false // This triggers the optimization!
    ): PythonStockResponse
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
}