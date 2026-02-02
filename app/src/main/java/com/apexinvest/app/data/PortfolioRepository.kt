package com.apexinvest.app.data

import android.annotation.SuppressLint
import com.apexinvest.app.api.CurrencyApiService
import com.apexinvest.app.api.GlobalStockApiService
import com.apexinvest.app.api.StockApiService
import com.apexinvest.app.db.PortfolioDao
import com.apexinvest.app.db.WatchlistDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

// --- Data Models ---
data class StockFullDetail(
    val symbol: String,
    val price: Double,
    val change: Double,
    val changePercent: Double,
    val prevClose: Double,
    val open: Double,
    val dayHigh: Double,
    val dayLow: Double,
    val marketCap: String,
    val peRatio: String,
    val dividendYield: String,
    val yearHigh: String,
    val yearLow: String,
    val historyPoints: List<Pair<String, Double>>
)

data class StockSearchResult(
    val symbol: String,
    val shortName: String?,
    val longName: String?,
    val exchange: String?,
    val type: String?
)

class PortfolioRepository(
    private val portfolioDao: PortfolioDao,
    private val watchlistDao: WatchlistDao,
    private val transactionDao: TransactionDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,

    // --- UPDATED SERVICES ---
    private val stockApiService: StockApiService,       // Indian Stocks (Existing)
    private val globalApiService: GlobalStockApiService, // Global Stocks (NEW Python Backend)
    private val currencyApiService: CurrencyApiService
) {

    private fun getPortfolioRef() = auth.currentUser?.let { firestore.collection("users").document(it.uid).collection("portfolio") }
    private fun getWatchlistRef() = auth.currentUser?.let { firestore.collection("users").document(it.uid).collection("watchlist") }

    // Helper to distinguish Indian stocks
    private fun isIndian(symbol: String): Boolean = symbol.endsWith(".NS") || symbol.endsWith(".BO")

    // ---------------------------------------------------------
    // --- 1. SEARCH (Consolidated) ---
    // ---------------------------------------------------------
    suspend fun searchStocks(query: String): List<StockSearchResult> {
        return withContext(Dispatchers.IO) {
            try {
                // Now using the Global API which wraps Yahoo Search (covers US, India, Crypto, etc.)
                globalApiService.searchStocks(query).map { item ->
                    StockSearchResult(
                        symbol = item.symbol,
                        shortName = item.name,
                        longName = item.name,
                        exchange = item.exchange,
                        type = item.type
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    // ---------------------------------------------------------
    // --- 2. FULL DETAILS (Unified Logic) ---
    // ---------------------------------------------------------
    @SuppressLint("DefaultLocale")
    suspend fun getFullStockDetails(symbol: String, range: String): Result<StockFullDetail> {
        return withContext(Dispatchers.IO) {
            try {
                // We define variables to hold data from EITHER API
                val symbolClean = symbol.uppercase()

                val price: Double
                val change: Double
                val changePercent: Double
                val prevClose: Double
                val open: Double
                val dayHigh: Double
                val dayLow: Double
                val marketCap: String
                val peRatio: String
                val divYield: String
                val yHigh: String
                val yLow: String
                val rawHistory: List<List<Any>> // Raw format from Python APIs

                // --- SWITCH: INDIAN vs GLOBAL ---
                if (isIndian(symbolClean)) {

                    val response = stockApiService.getStockDetails(symbolClean, range, charts = true)
                    price = response.price
                    change = response.change
                    changePercent = response.changePercent
                    prevClose = response.prevClose
                    open = response.open
                    dayHigh = response.dayHigh
                    dayLow = response.dayLow
                    marketCap = formatIndianMarketCap(response.marketCap)
                    peRatio = response.peRatio
                    divYield = response.dividendYield
                    yHigh = response.yearHigh
                    yLow = response.yearLow
                    rawHistory = response.historyPoints
                } else {
                    // B. Use NEW Global API (Replaces Finnhub/Tiingo)
                    val response = globalApiService.getStockDetails(symbolClean, range,charts=true)
                    price = response.price
                    change = response.change
                    changePercent = response.changePercent
                    prevClose = response.prevClose
                    open = response.open
                    dayHigh = response.dayHigh
                    dayLow = response.dayLow
                    marketCap = response.marketCap
                    peRatio = response.peRatio
                    divYield = response.dividendYield
                    yHigh = response.yearHigh
                    yLow = response.yearLow
                    rawHistory = response.historyPoints
                }

                // --- PROCESS HISTORY (Standardize Format) ---
                // Python returns [["Date", 123.45], ...]. converting to List<Pair<String, Double>>
                val cleanHistory = rawHistory.mapNotNull { point ->
                    try {
                        val date = point[0] as? String
                        val p = (point[1] as? Number)?.toDouble()
                        if (date != null && p != null) Pair(date, p) else null
                    } catch (_: Exception) { null }
                }

                // --- BUILD MODEL ---
                val detail = StockFullDetail(
                    symbol = symbolClean,
                    price = price,
                    change = change,
                    changePercent = changePercent,
                    prevClose = prevClose,
                    open = open,
                    dayHigh = dayHigh,
                    dayLow = dayLow,
                    marketCap = marketCap,
                    peRatio = peRatio,
                    dividendYield = divYield,
                    yearHigh = yHigh,
                    yearLow = yLow,
                    historyPoints = cleanHistory
                )

                // --- 3. RECALCULATE CHANGE BASED ON RANGE ---
                // "1D" -> Standard Daily Change (Current - PrevClose)
                // "1M", "1Y", etc. -> (Current - First Point in Graph)
                val calculatedDetail = if (range == "1D") {
                    detail
                } else {
                    if (detail.historyPoints.isNotEmpty()) {
                        val firstPrice = detail.historyPoints.first().second
                        val lastPrice = detail.price
                        val rangeChange = lastPrice - firstPrice
                        val rangeChangePct = if (firstPrice != 0.0) (rangeChange / firstPrice) * 100 else 0.0

                        detail.copy(
                            change = rangeChange,
                            changePercent = rangeChangePct
                        )
                    } else {
                        detail
                    }
                }

                Result.success(calculatedDetail)

            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    private suspend fun fetchAndUpdatePrice(symbol: String) {
        try {
            val symbolClean = symbol.uppercase()
            var price = 0.0
            var change = 0.0

            if (isIndian(symbolClean)) {
                try {
                    val data = stockApiService.getLivePrice(symbolClean, "1D", charts = false)
                    price = data.price
                    change = data.changePercent
                } catch (_: Exception) { }
            } else {
                try {
                    val data = globalApiService.getStockDetails(symbolClean, "1D", charts = false)
                    price = data.price
                    change = data.changePercent
                } catch (_: Exception) { }
            }

            if (price > 0.0) {
                // Update Local Database
                val pStock = portfolioDao.getStockBySymbol(symbolClean)
                if (pStock != null) {
                    portfolioDao.insertStock(pStock.copy(currentPrice = price, dailyChange = change))
                }
                val wStock = watchlistDao.getStockBySymbol(symbolClean)
                if (wStock != null) {
                    watchlistDao.insertStock(wStock.copy(lastPrice = price))
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    // ---------------------------------------------------------
    // --- 4. DATA SYNC & UTILS (Unchanged) ---
    // ---------------------------------------------------------

    private fun formatIndianMarketCap(rawCap: String?): String {
        if (rawCap.isNullOrEmpty()) return "-"
        return try {
            val sanitized = rawCap.replace(",", "")
            val value = sanitized.toDouble()
            val inMillions = value / 1_000_000.0
            String.format(Locale.US, "%.2fM", inMillions)
        } catch (_: Exception) {
            rawCap
        }
    }

    suspend fun syncAllDataAndPrices() {
        withContext(Dispatchers.IO) {
            try {
                if (auth.currentUser == null) return@withContext
                val (cloudPortfolio, cloudWatchlist) = coroutineScope {
                    val pDef = async { getPortfolioRef()!!.get().await().toObjects(StockPortfolioItem::class.java) }
                    val wDef = async { getWatchlistRef()!!.get().await() }
                    Pair(pDef.await(), wDef.await())
                }
                // Sync to DB
                cloudPortfolio.forEach { item ->
                    val existing = portfolioDao.getStockBySymbol(item.symbol)
                    portfolioDao.insertStock(item.toEntity().copy(
                        currentPrice = existing?.currentPrice ?: 0.0,
                        lastUpdated = System.currentTimeMillis()
                    ))
                }
                val watchItems = cloudWatchlist.documents.mapNotNull { doc ->
                    doc.getString("symbol")?.let { WatchlistEntity(it, 0.0) }
                }
                watchlistDao.clearWatchlist()
                watchItems.forEach { watchlistDao.insertStock(it) }

                // Fetch Prices
                val allSymbols = (cloudPortfolio.map { it.symbol } + watchItems.map { it.symbol }).distinct()
                allSymbols.forEach { symbol -> launch { fetchAndUpdatePrice(symbol) } }

            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private var cachedUsdToInrRate: Double = 84.0

    suspend fun getConversionRate(): Double {
        return withContext(Dispatchers.IO) {
            try {
                val response = currencyApiService.getUsdRates()
                val rate = response.rates["INR"] ?: 84.0
                cachedUsdToInrRate = rate
                rate
            } catch (e: Exception) {
                e.printStackTrace()
                cachedUsdToInrRate
            }
        }
    }

    // ---------------------------------------------------------
    // --- 5. TRANSACTION ENGINE (Real Ledger Logic) ---
    // ---------------------------------------------------------

    suspend fun recordTrade(
        symbol: String,
        type: TransactionType,
        quantity: Int,
        price: Double,
        dateString: String
    ) = withContext(Dispatchers.IO) {
        val cleanSymbol = symbol.uppercase().trim()
        val timestamp = System.currentTimeMillis()

        // 1. Log the transaction in history
        val transaction = TransactionEntity(
            symbol = cleanSymbol,
            type = type,
            quantity = quantity,
            price = price,
            timestamp = timestamp
        )
        transactionDao.insertTransaction(transaction)

        // 2. Update the Portfolio Snapshot
        val currentHolding = portfolioDao.getStockBySymbol(cleanSymbol)

        if (type == TransactionType.BUY) {
            if (currentHolding == null) {
                val newStock = StockEntity(
                    symbol = cleanSymbol,
                    quantity = quantity,
                    buyPrice = price,
                    currentPrice = price,
                    buyDate = dateString,
                    dailyChange = 0.0
                )
                portfolioDao.insertStock(newStock)
                getPortfolioRef()?.document(cleanSymbol)?.set(StockPortfolioItem(cleanSymbol, quantity, price, dateString))
            } else {
                // Weighted Average Logic
                val totalOldCost = currentHolding.quantity * currentHolding.buyPrice
                val totalNewCost = quantity * price
                val newTotalQty = currentHolding.quantity + quantity
                val newAvgPrice = (totalOldCost + totalNewCost) / newTotalQty

                val updatedStock = currentHolding.copy(
                    quantity = newTotalQty,
                    buyPrice = newAvgPrice
                )
                portfolioDao.insertStock(updatedStock)
                getPortfolioRef()?.document(cleanSymbol)?.set(StockPortfolioItem(cleanSymbol, newTotalQty, newAvgPrice, currentHolding.buyDate))
            }
        } else {
            if (currentHolding != null) {
                val newQty = currentHolding.quantity - quantity

                if (newQty <= 0) {
                    portfolioDao.deleteStock(currentHolding)
                    getPortfolioRef()?.document(cleanSymbol)?.delete()
                } else {
                    val updatedStock = currentHolding.copy(quantity = newQty)
                    portfolioDao.insertStock(updatedStock)
                    getPortfolioRef()?.document(cleanSymbol)?.set(StockPortfolioItem(cleanSymbol, newQty, currentHolding.buyPrice, currentHolding.buyDate))
                }
            }
        }
        Unit
    }

    // --- Legacy Wrappers ---
    suspend fun saveStock(item: StockPortfolioItem) {
        recordTrade(item.symbol, TransactionType.BUY, item.quantity, item.buyPrice, item.buyDate)
    }

    suspend fun deleteStock(item: StockPortfolioItem) {
        recordTrade(item.symbol, TransactionType.SELL, item.quantity, item.buyPrice, item.buyDate)
    }

    suspend fun addWatchlistStock(symbol: String) {
        val s = symbol.uppercase()
        getWatchlistRef()?.document(s)?.set(mapOf("symbol" to s, "lastPrice" to 0.0))?.await()
        syncAllDataAndPrices()
    }
    suspend fun deleteWatchlistStock(symbol: String) {
        val s = symbol.uppercase()
        getWatchlistRef()?.document(s)?.delete()?.await()
        watchlistDao.deleteStock(WatchlistEntity(s, 0.0))
    }

    fun getLocalPortfolio(): Flow<List<StockEntity>> = portfolioDao.getAllStocks()
    fun getLocalWatchlist(): Flow<List<WatchlistEntity>> = watchlistDao.getAllWatchlistStocks()

    suspend fun clearAllLocalData() {
        withContext(Dispatchers.IO) {
            portfolioDao.clearPortfolio()
            watchlistDao.clearWatchlist()
        }
    }

    // --- CSV GENERATION ---
    suspend fun generatePortfolioCsv(): String {
        return withContext(Dispatchers.IO) {
            val stocks = portfolioDao.getAllStocks().first()
            val sb = StringBuilder()
            sb.append('\uFEFF')
            sb.append("Symbol,Shares,Buy Price,Current Price,Total Invested,Total Value,Gain/Loss\n")

            stocks.forEach { stock ->
                val isIndian = stock.symbol.endsWith(".NS") || stock.symbol.endsWith(".BO")
                val currency = if (isIndian) "Rs. " else "$"

                val invested = stock.buyPrice * stock.quantity
                val value = stock.currentPrice * stock.quantity
                val gain = value - invested

                fun fmt(amount: Double): String {
                    return "$currency${String.format(Locale.US, "%.2f", amount)}"
                }

                sb.append("${stock.symbol},")
                sb.append("${stock.quantity},")
                sb.append("${fmt(stock.buyPrice)},")
                sb.append("${fmt(stock.currentPrice)},")
                sb.append("${fmt(invested)},")
                sb.append("${fmt(value)},")
                sb.append("${fmt(gain)}\n")
            }
            sb.toString()
        }
    }

    private suspend fun validateSymbolExists(symbol: String): Boolean {
        return try {
            val query = symbol.uppercase()
            // Updated to use Global API search
            val searchResponse = globalApiService.searchStocks(query)
            searchResponse.any { it.symbol.equals(query, ignoreCase = true) }
        } catch (e: Exception) {
            false
        }
    }

    fun getTransactionHistory(symbol: String): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsForStock(symbol.uppercase())
    }

    suspend fun getTotalInvested(symbol: String): Double {
        return transactionDao.getTotalInvestedForStock(symbol.uppercase()) ?: 0.0
    }
}