package com.apexinvest.app.data

import android.util.Log
import com.apexinvest.app.api.ApexAuthApiService
import com.apexinvest.app.api.CurrencyApiService
import com.apexinvest.app.api.IdeasApi
import com.apexinvest.app.api.PortfolioAnalysisRequest
import com.apexinvest.app.api.PredictionApiService
import com.apexinvest.app.api.TradingViewStockApiService
import com.apexinvest.app.api.YahooFinanceApiService
import com.apexinvest.app.api.models.CandlePointDto
import com.apexinvest.app.api.models.DeepAnalysisResponse
import com.apexinvest.app.api.models.PortfolioSummary
import com.apexinvest.app.api.models.StockLiveQuoteDto
import com.apexinvest.app.api.models.StockSearchResult
import com.apexinvest.app.api.models.SyncResponse
import com.apexinvest.app.api.models.TransactionItem
import com.apexinvest.app.api.models.WatchlistItem
import com.apexinvest.app.api.util.YahooParser
import com.apexinvest.app.data.remote.ApexInvestApiService
import com.apexinvest.app.data.util.SessionPriceCache
import com.apexinvest.app.db.AnalysisCacheDao
import com.apexinvest.app.db.AnalysisCacheEntity
import com.apexinvest.app.db.NotificationDao
import com.apexinvest.app.db.PortfolioDao
import com.apexinvest.app.db.StockCacheDao
import com.apexinvest.app.db.StockCacheEntity
import com.apexinvest.app.db.WatchlistDao
import com.apexinvest.app.utils.SessionManager
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

data class PriceUpdate(val symbol: String, val price: Double, val change: Double, val changePercent: Double, val previousClose: Double = 0.0)

class PortfolioRepository(
    private val portfolioDao: PortfolioDao,
    private val watchlistDao: WatchlistDao,
    private val transactionDao: TransactionDao,
    private val stockCacheDao: StockCacheDao,
    private val analysisCacheDao: AnalysisCacheDao,
    private val notificationDao: NotificationDao,
    private val sessionManager: SessionManager,
    private val authApiService: ApexAuthApiService,
    private val yahooFinanceApiService: YahooFinanceApiService,
    private val currencyApiService: CurrencyApiService,
    private val predictionApiService: PredictionApiService,
    private val ideasApi: IdeasApi,
    private val tradingViewApiService: TradingViewStockApiService,
    private val apexInvestApiService: ApexInvestApiService
) {

    private val tag = "PortfolioRepository"
    private val gson = Gson()

    val globalPriceUpdates = MutableSharedFlow<PriceUpdate>(extraBufferCapacity = 128)
    private var lastPriceSyncTime = 0L
    private var cachedRatesMap: Map<String, Double> = mapOf("INR" to 84.0, "USD" to 1.0)

    private val sparklineMemCache = java.util.concurrent.ConcurrentHashMap<String, List<Double>>()

    suspend fun hydrateSparklineCache() = withContext(Dispatchers.IO) {
        val allPortfolio = portfolioDao.getAllStocks().firstOrNull() ?: emptyList()
        val allWatchlist = watchlistDao.getAllWatchlistStocks().firstOrNull() ?: emptyList()
        val allSymbols = (allPortfolio.map { it.symbol.uppercase().trim() } +
                allWatchlist.map { it.symbol.uppercase().trim() }).distinct()

        if (allSymbols.isEmpty()) return@withContext

        val cachedList = stockCacheDao.getStocksCache(allSymbols)

        cachedList.chunked(15).forEach { chunk ->
            chunk.map { cached ->
                async {
                    val s = cached.symbol.uppercase().trim()
                    if (cached.candlesJson != "[]" && cached.candlesJson.isNotEmpty()) {
                        try {
                            val candles = gson.fromJson<List<CandlePointDto>>(cached.candlesJson, object : com.google.gson.reflect.TypeToken<List<CandlePointDto>>() {}.type)
                            sparklineMemCache[s] = candles.map { it.close }
                        } catch (_: Exception) {}
                    }
                }
            }.awaitAll()
        }
    }

    fun getCachedSparklineSync(symbol: String): List<Double> {
        return sparklineMemCache[symbol.uppercase().trim()] ?: emptyList()
    }

    fun getCachedSparklineDtoSync(symbol: String): List<CandlePointDto> {
        val s = symbol.uppercase().trim()
        val cachedJson = stockCacheDao.getCachedCandlesSync(s)
        return if (cachedJson != null && cachedJson != "[]") {
            try {
                gson.fromJson(cachedJson, object : com.google.gson.reflect.TypeToken<List<CandlePointDto>>() {}.type)
            } catch (_: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    suspend fun prefetchSparklines(symbols: List<String>) = withContext(Dispatchers.IO) {
        if (symbols.isEmpty()) return@withContext
        val normalized = symbols.map { it.uppercase().trim() }
        val caches = stockCacheDao.getStocksCache(normalized)

        caches.forEach { cached ->
            if (cached.candlesJson.isNotBlank() && cached.candlesJson != "[]") {
                try {
                    val candles = gson.fromJson<List<CandlePointDto>>(
                        cached.candlesJson,
                        object : com.google.gson.reflect.TypeToken<List<CandlePointDto>>() {}.type
                    )
                    sparklineMemCache[cached.symbol.uppercase().trim()] = candles.map { it.close }
                } catch (_: Exception) {}
            }
        }
    }

    val allPortfolioStocks: Flow<List<StockEntity>> = portfolioDao.getAllStocks()
    val allWatchlistStocks: Flow<List<WatchlistEntity>> = watchlistDao.getAllWatchlistStocks()

    suspend fun refreshMissingStockMetadata() = withContext(Dispatchers.IO) {
        val currentPortfolio = portfolioDao.getAllStocks().firstOrNull() ?: emptyList()
        val currentWatchlist = watchlistDao.getAllWatchlistStocks().firstOrNull() ?: emptyList()

        // Safely check strings bypassing the Gson null trap
        val symbolsToFetch = (
                currentPortfolio.filter { it.companyName == "Unknown" || it.sector == "Unknown" || it.companyName.isBlank() || it.sector.isBlank() }.map { it.symbol } +
                        currentWatchlist.filter { it.companyName == "Unknown" || it.sector == "Unknown" || it.companyName.isBlank() || it.sector.isBlank() }.map { it.symbol }
                ).toSet()

        if (symbolsToFetch.isEmpty()) return@withContext

        val fetchedMetaMap = symbolsToFetch.chunked(10).flatMap { chunk ->
            coroutineScope {
                chunk.map { symbol ->
                    async {
                        try {
                            val formattedSymbol = com.apexinvest.app.util.StockMetadataUtils.getFormattedSymbol(symbol)
                            val meta = tradingViewApiService.getStockMeta(symbol = formattedSymbol)

                            val safeName = meta.companyName
                            val safeSector = meta.sector

                            if (safeName.isNotEmpty() || safeSector.isNotEmpty()) {
                                symbol to meta
                            } else null
                        } catch (_: Exception) { null }
                    }
                }.awaitAll().filterNotNull()
            }
        }.toMap()

        if (fetchedMetaMap.isEmpty()) return@withContext

        val portfolioBatch = mutableListOf<StockEntity>()
        currentPortfolio.forEach { stock ->
            fetchedMetaMap[stock.symbol]?.let { tvMeta ->
                if (stock.companyName == "Unknown" || stock.companyName.isBlank() || stock.sector == "Unknown" || stock.sector.isBlank()) {
                    val safeTvName = tvMeta.companyName
                    val safeTvSector = tvMeta.sector

                    portfolioBatch.add(
                        stock.copy(
                            companyName = safeTvName.ifEmpty { stock.companyName },
                            sector = safeTvSector.ifEmpty { "Other" }
                        )
                    )
                }
            }
        }

        val watchlistBatch = mutableListOf<WatchlistEntity>()
        currentWatchlist.forEach { watchItem ->
            fetchedMetaMap[watchItem.symbol]?.let { tvMeta ->
                if (watchItem.companyName == "Unknown" || watchItem.companyName.isBlank() || watchItem.sector == "Unknown" || watchItem.sector.isBlank()) {
                    val safeTvName = tvMeta.companyName
                    val safeTvSector = tvMeta.sector

                    watchlistBatch.add(
                        watchItem.copy(
                            companyName = safeTvName.ifEmpty { watchItem.companyName },
                            sector = safeTvSector.ifEmpty { "Other" }
                        )
                    )
                }
            }
        }

        if (portfolioBatch.isNotEmpty()) portfolioDao.insertStocks(portfolioBatch)
        if (watchlistBatch.isNotEmpty()) watchlistDao.insertStocks(watchlistBatch)
    }

    suspend fun addStockToPortfolio(item: StockPortfolioItem, cachedPrice: Double = 0.0, cachedChange: Double = 0.0, cachedChangePercent: Double = 0.0, cachedPrevClose: Double = 0.0) = withContext(Dispatchers.IO) {
        val existingStock = portfolioDao.getStockBySymbol(item.symbol)
        var companyName = existingStock?.companyName ?: "Unknown"
        var sector = existingStock?.sector ?: "Unknown"

        if (existingStock == null || companyName == "Unknown" || companyName.isBlank() || sector == "Unknown" || sector.isBlank()) {
            try {
                val formattedSymbol = com.apexinvest.app.util.StockMetadataUtils.getFormattedSymbol(item.symbol)
                val tvMeta = tradingViewApiService.getStockMeta(symbol = formattedSymbol)

                val safeName = tvMeta.companyName
                val safeSector = tvMeta.sector

                companyName = safeName.ifEmpty { companyName }
                sector = safeSector.ifEmpty { "Other" }
            } catch (_: Exception) {}
        }

        portfolioDao.insertStock(item.toEntity(cachedPrice, cachedChange, cachedChangePercent, cachedPrevClose, companyName, sector).copy(timestamp = System.currentTimeMillis(), lastUpdated = item.buyDate.ifEmpty { LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) }))
    }

    suspend fun addStockToWatchlist(symbol: String, lastPrice: Double = 0.0, dailyChange: Double = 0.0, changePercent: Double = 0.0, prevClose: Double = 0.0) = withContext(Dispatchers.IO) {
        val existingWatch = watchlistDao.getStockBySymbol(symbol)
        var companyName = existingWatch?.companyName ?: "Unknown"
        var sector = existingWatch?.sector ?: "Unknown"

        if (existingWatch == null || companyName == "Unknown" || companyName.isBlank() || sector == "Unknown" || sector.isBlank()) {
            try {
                val formattedSymbol = com.apexinvest.app.util.StockMetadataUtils.getFormattedSymbol(symbol)
                val tvMeta = tradingViewApiService.getStockMeta(symbol = formattedSymbol)

                val safeName = tvMeta.companyName
                val safeSector = tvMeta.sector

                companyName = safeName.ifEmpty { companyName }
                sector = safeSector.ifEmpty { "Other" }
            } catch (_: Exception) {}
        }

        watchlistDao.insertStock(WatchlistEntity(symbol, companyName, sector, lastPrice, dailyChange, changePercent, prevClose))

        val authHeader = sessionManager.getAuthHeader()
        CoroutineScope(Dispatchers.IO).launch {
            if (authHeader != null) {
                try { authApiService.updateCloudWatchlist(authHeader, WatchlistItem(symbol)) } catch (_: Exception) {}
            }
        }
    }

    suspend fun fullCloudSync(forceLoginSync: Boolean = false) = withContext(Dispatchers.IO) {
        val hasLocalData = (portfolioDao.getPortfolioSize() > 0) ||
                (watchlistDao.getAllWatchlistStocks().firstOrNull()?.isNotEmpty() == true) ||
                (transactionDao.getAllTransactions().firstOrNull()?.isNotEmpty() == true)

        if (!forceLoginSync && hasLocalData) {
            Log.d(tag, "Skipping cloud sync: Local data exists.")
            return@withContext
        }

        val rawToken = sessionManager.getAuthHeader() ?: return@withContext
        try {
            val header = if (rawToken.startsWith("Bearer ")) rawToken else "Bearer $rawToken"
            val response = authApiService.syncUserData(header)

            if (response.isSuccessful && response.body() != null) {
                val cloudData: SyncResponse = response.body()!!
                val currentStocks = portfolioDao.getAllStocks().first().associateBy { it.symbol }
                val currentWatchlist = watchlistDao.getAllWatchlistStocks().first().associateBy { it.symbol }

                portfolioDao.clearPortfolio()
                watchlistDao.clearWatchlist()
                transactionDao.clearAllTransactions()

                cloudData.portfolio.forEach { item ->
                    val existing = currentStocks[item.symbol]
                    addStockToPortfolio(StockPortfolioItem(item.symbol, item.quantity, item.averageBuyPrice, item.lastUpdated), existing?.currentPrice ?: 0.0, existing?.dailyChange ?: 0.0, existing?.changePercent ?: 0.0, existing?.previousClose ?: 0.0)
                }

                cloudData.watchlist.forEach { item ->
                    val existing = currentWatchlist[item.symbol]
                    addStockToWatchlist(item.symbol, existing?.lastPrice ?: 0.0, existing?.dailyChange ?: 0.0, existing?.changePercent ?: 0.0, existing?.previousClose ?: 0.0)
                }

                cloudData.transactions.forEach { item ->
                    transactionDao.insertTransaction(
                        TransactionEntity(
                            cloudId = item.id,
                            symbol = item.symbol,
                            type = TransactionType.valueOf(item.type.uppercase()),
                            quantity = item.quantity,
                            price = item.price,
                            timestamp = item.timestamp,
                            fees = item.fees,
                            notes = item.notes ?: ""
                        )
                    )
                }

                analysisCacheDao.clearAll()
                refreshMissingStockMetadata()
                syncAllDataAndPrices()
            }
        } catch (e: Exception) { Log.e(tag, "SYNC CRASHED", e) }
    }

    suspend fun searchStocks(query: String): List<StockSearchResult> = withContext(Dispatchers.IO) {
        if (query.length < 2) return@withContext emptyList()
        try {
            val results = apexInvestApiService.searchStocks(query)
            results.map {
                StockSearchResult(
                    symbol = it.symbol,
                    name = it.name ?: "",
                    type = it.type ?: "Unknown",
                    exchange = it.exchange ?: ""
                )
            }
        } catch (e: Exception) {
            Log.e(tag, "Search failed", e)
            emptyList()
        }
    }

    suspend fun recordTrade(symbol: String, type: TransactionType, quantity: Double, price: Double, dateString: String) = withContext(Dispatchers.IO) {
        val cleanSymbol = symbol.uppercase().trim()
        val timestamp = System.currentTimeMillis()
        val authHeader = sessionManager.getAuthHeader()

        transactionDao.insertTransaction(TransactionEntity(symbol = cleanSymbol, type = type, quantity = quantity, price = price, timestamp = timestamp, fees = 0.0, notes = "Manual Trade on $dateString"))

        val current = portfolioDao.getStockBySymbol(cleanSymbol)
        if (type == TransactionType.BUY) {
            if (current == null) {
                addStockToPortfolio(StockPortfolioItem(cleanSymbol, quantity, price, dateString), price, 0.0, 0.0, price)
            } else {
                val newQty = current.quantity + quantity
                val newAvg = ((current.quantity * current.buyPrice) + (quantity * price)) / newQty
                portfolioDao.insertStock(current.copy(quantity = newQty, buyPrice = newAvg, lastUpdated = dateString))
            }
        } else {
            current?.let {
                val newQty = it.quantity - quantity
                if (newQty <= 0.0001) portfolioDao.deleteStock(it)
                else portfolioDao.insertStock(it.copy(quantity = newQty, lastUpdated = dateString))
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            if (authHeader != null) {
                try { authApiService.recordCloudTrade(authHeader, TransactionItem(id = null, cleanSymbol, type.name, quantity, price, 0.0, "Manual Trade on $dateString", timestamp)) } catch (_: Exception) {}
            }
        }

        analysisCacheDao.clearAll()
        // Ensure Dashboard has data after first trade
        fetchAndUpdatePrice(cleanSymbol)
    }

    suspend fun deleteWatchlistStock(symbol: String) = withContext(Dispatchers.IO) {
        val s = symbol.uppercase().trim()
        val authHeader = sessionManager.getAuthHeader()

        watchlistDao.getStockBySymbol(s)?.let { watchlistDao.deleteStock(it) }

        CoroutineScope(Dispatchers.IO).launch {
            if (authHeader != null) {
                try { authApiService.deleteFromCloudWatchlist(authHeader, WatchlistItem(s)) } catch (_: Exception) {}
            }
        }
    }

    suspend fun updateStockPricesInDb(symbol: String, price: Double, change: Double, changePercent: Double = 0.0, prevClose: Double = 0.0) {
        val s = symbol.uppercase().trim()

        updatePriceRAM(s, price, change, changePercent, prevClose)

        val portfolio = portfolioDao.getStockBySymbol(s)
        val watchlist = watchlistDao.getStockBySymbol(s)
        val cache = stockCacheDao.getStockCache(s)

        if (portfolio != null) portfolioDao.insertStock(portfolio.copy(currentPrice = price, dailyChange = change, changePercent = changePercent, previousClose = prevClose))
        if (watchlist != null) watchlistDao.insertStock(watchlist.copy(lastPrice = price, dailyChange = change, changePercent = changePercent, previousClose = prevClose))
        if (cache != null) stockCacheDao.insertStockCache(cache.copy(price = price, change = change, changePercent = changePercent, previousClose = prevClose, timestamp = System.currentTimeMillis()))
    }

    suspend fun updatePriceRAM(symbol: String, price: Double, change: Double, pct: Double, prevClose: Double = 0.0) {
        val s = symbol.uppercase().trim()

        SessionPriceCache.update(s, price, change, pct, prevClose)

        val chart = sparklineMemCache[s]?.toMutableList()
        if (!chart.isNullOrEmpty()) {
            chart[chart.size - 1] = price
            sparklineMemCache[s] = chart
        }

        globalPriceUpdates.emit(PriceUpdate(s, price, change, pct, prevClose))
    }

    suspend fun updateStockPricesBatch(quotes: List<StockLiveQuoteDto>) = withContext(Dispatchers.IO) {
        if (quotes.isEmpty()) return@withContext
        val symbols = quotes.map { it.symbol.uppercase().trim() }

        val allPortfolio = portfolioDao.getAllStocks().first().associateBy { it.symbol.uppercase().trim() }
        val allWatchlist = watchlistDao.getAllWatchlistStocks().first().associateBy { it.symbol.uppercase().trim() }
        val allCache = stockCacheDao.getStocksCache(symbols).associateBy { it.symbol.uppercase().trim() }

        val portfolioBatch = mutableListOf<StockEntity>()
        val watchlistBatch = mutableListOf<WatchlistEntity>()
        val cacheBatch = mutableListOf<StockCacheEntity>()

        quotes.forEach { quote ->
            val s = quote.symbol.uppercase().trim()

            updatePriceRAM(s, quote.price, quote.change, quote.changePercent, quote.previousClose)

            allPortfolio[s]?.let { portfolioBatch.add(it.copy(currentPrice = quote.price, dailyChange = quote.change, changePercent = quote.changePercent, previousClose = quote.previousClose)) }
            allWatchlist[s]?.let { watchlistBatch.add(it.copy(lastPrice = quote.price, dailyChange = quote.change, changePercent = quote.changePercent, previousClose = quote.previousClose)) }

            val existingCache = allCache[s]
            cacheBatch.add(
                StockCacheEntity(
                    symbol = s,
                    price = quote.price,
                    change = quote.change,
                    changePercent = quote.changePercent,
                    previousClose = quote.previousClose,
                    candlesJson = quote.candlesJson ?: existingCache?.candlesJson ?: "[]",
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        if (portfolioBatch.isNotEmpty()) portfolioDao.insertStocks(portfolioBatch)
        if (watchlistBatch.isNotEmpty()) watchlistDao.insertStocks(watchlistBatch)
        if (cacheBatch.isNotEmpty()) stockCacheDao.insertStocksCache(cacheBatch)
    }

    suspend fun fetchAndUpdatePrice(symbol: String) = withContext(Dispatchers.IO) {
        val s = symbol.uppercase().trim()
        val now = System.currentTimeMillis()
        val cached = stockCacheDao.getStockCache(s)

        val marketStatus = com.apexinvest.app.util.StockMetadataUtils.isMarketOpen(s)
        if (!marketStatus.first && cached != null) return@withContext

        var candlesJsonToSave: String = cached?.candlesJson ?: "[]"
        val isSessionOutdated = cached != null && com.apexinvest.app.util.StockMetadataUtils.isNewSessionStarted(s, cached.timestamp)

        if (!isSessionOutdated && cached != null && (now - cached.timestamp < 9_000L)) {
            if (!sparklineMemCache.containsKey(s) && candlesJsonToSave != "[]") {
                try {
                    val candles = gson.fromJson<List<CandlePointDto>>(candlesJsonToSave, object : com.google.gson.reflect.TypeToken<List<CandlePointDto>>() {}.type)
                    sparklineMemCache[s] = candles.map { it.close }
                } catch (_: Exception) {}
            }
        } else {
            try {
                val shouldFetchHeavyChart = isSessionOutdated || candlesJsonToSave == "[]" || (now - (cached?.timestamp ?: 0L) > 1_800_000L)

                val yahooResponse = if (shouldFetchHeavyChart) {
                    yahooFinanceApiService.getLivePriceAndChart(s, "5m", "5d") // Increased to 5d
                } else {
                    yahooFinanceApiService.getQuotes(s)
                }

                val quoteDto = if (shouldFetchHeavyChart) {
                    YahooParser.parseToQuote(s, yahooResponse as com.apexinvest.app.api.models.yahoo.YahooChartResponse)
                } else {
                    YahooParser.parseV7Response(yahooResponse as com.apexinvest.app.api.models.yahoo.YahooQuoteResponse)
                        .firstOrNull { it.symbol.equals(s, ignoreCase = true) } ?: throw Exception("No quote")
                }

                if (quoteDto.price > 0.0) {
                    updateStockPricesInDb(s, quoteDto.price, quoteDto.change, quoteDto.changePercent, quoteDto.previousClose)
                }

                if (shouldFetchHeavyChart) {
                    val fullCandles = YahooParser.parseToCandles(yahooResponse as com.apexinvest.app.api.models.yahoo.YahooChartResponse)
                    
                    // Filter regular hours before rolling window
                    val regularCandles = YahooParser.filterRegularHours(s, fullCandles)
                    val candles = YahooParser.filterRollingWindow(s, regularCandles)

                    if (candles.isNotEmpty()) {
                        val lastCandle = candles.last()
                        val updatedCandles = if (kotlin.math.abs(lastCandle.close - quoteDto.price) > 0.001) {
                            candles.dropLast(1) + lastCandle.copy(close = quoteDto.price)
                        } else candles

                        candlesJsonToSave = gson.toJson(updatedCandles)
                        sparklineMemCache[s] = updatedCandles.map { it.close }
                    }
                } else {
                    try {
                        val listType = object : com.google.gson.reflect.TypeToken<MutableList<CandlePointDto>>() {}.type
                        val existingCandles: MutableList<CandlePointDto> =
                            gson.fromJson(candlesJsonToSave, listType)

                        existingCandles.add(CandlePointDto((now / 1000).toString(), quoteDto.price, quoteDto.price, quoteDto.price, quoteDto.price, 0))
                        
                        // Ensure consistency between full and partial updates
                        val filtered = YahooParser.filterRollingWindow(s, existingCandles)
                        candlesJsonToSave = gson.toJson(filtered)
                        sparklineMemCache[s] = filtered.map { it.close }
                    } catch (_: Exception) {}
                }

                stockCacheDao.insertStockCache(
                    StockCacheEntity(
                        symbol = s,
                        price = quoteDto.price,
                        change = quoteDto.change,
                        changePercent = quoteDto.changePercent,
                        previousClose = quoteDto.previousClose,
                        candlesJson = candlesJsonToSave,
                        timestamp = now
                    )
                )
            } catch (_: Exception) {}
        }
    }

    suspend fun fetchLivePriceOnly(symbol: String, updateDb: Boolean = true, forceNetwork: Boolean = false): StockLiveQuoteDto? = withContext(Dispatchers.IO) {
        val s = symbol.uppercase().trim()
        val cached = stockCacheDao.getStockCache(s)
        val now = System.currentTimeMillis()

        val isMarketActive = com.apexinvest.app.util.StockMetadataUtils.isExtendedMarketActive(s)
        val hasGoodSparkline = cached != null && cached.candlesJson.isNotBlank() && cached.candlesJson != "[]" && cached.candlesJson.length > 500

        if (!isMarketActive && hasGoodSparkline && !forceNetwork) {
            return@withContext StockLiveQuoteDto(
                s, cached.price, cached.change, cached.changePercent,
                cached.previousClose, 0.0, 0.0, 0.0, 0.0, 0.0,
                candlesJson = cached.candlesJson
            )
        }

        val isSessionOutdated = cached != null && com.apexinvest.app.util.StockMetadataUtils.isNewSessionStarted(s, cached.timestamp)

        if (!isSessionOutdated && cached != null && (now - cached.timestamp < 4_000L) && !forceNetwork) {
            if (!sparklineMemCache.containsKey(s) && cached.candlesJson
                .isNotBlank() && cached.candlesJson != "[]") {
                try {
                    val candles = gson.fromJson<List<CandlePointDto>>(cached.candlesJson, object : com.google.gson.reflect.TypeToken<List<CandlePointDto>>() {}.type)
                    sparklineMemCache[s] = candles.map { it.close }
                } catch (_: Exception) {}
            }

            return@withContext StockLiveQuoteDto(
                s, cached.price, cached.change, cached.changePercent,
                cached.previousClose, 0.0, 0.0, 0.0, 0.0, 0.0,
                candlesJson = cached.candlesJson
            )
        }

        try {
            val shouldFetchHeavyChart = cached == null || cached.candlesJson.isBlank() || cached.candlesJson == "[]" || cached.candlesJson.length < 300 || isSessionOutdated

            val yahooResponse = if (shouldFetchHeavyChart) {
                yahooFinanceApiService.getLivePriceAndChart(s, interval = "5m", range = "2d")
            } else {
                yahooFinanceApiService.getQuotes(s)
            }

            val quoteData = if (shouldFetchHeavyChart) {
                YahooParser.parseToQuote(s, yahooResponse as com.apexinvest.app.api.models.yahoo.YahooChartResponse)
            } else {
                YahooParser.parseV7Response(yahooResponse as com.apexinvest.app.api.models.yahoo.YahooQuoteResponse)
                    .firstOrNull { it.symbol.equals(s, ignoreCase = true) } ?: return@withContext null
            }

            if (quoteData.price <= 0.0) return@withContext null

            var candlesJsonToSave = cached?.candlesJson ?: "[]"

            if (shouldFetchHeavyChart) {
                val fullCandles = YahooParser.parseToCandles(yahooResponse as com.apexinvest.app.api.models.yahoo.YahooChartResponse)
                val regular = YahooParser.filterRegularHours(s, fullCandles)
                val filtered = YahooParser.filterRollingWindow(s, regular)
                if (filtered.isNotEmpty()) {
                    candlesJsonToSave = gson.toJson(filtered)
                    sparklineMemCache[s] = filtered.map { candle -> candle.close }
                }
            } else {
                try {
                    val isRegularSession = com.apexinvest.app.util.StockMetadataUtils.isMarketOpen(s).first

                    if (isRegularSession) {
                        val listType = object : com.google.gson.reflect.TypeToken<MutableList<CandlePointDto>>() {}.type
                        val existingCandles: MutableList<CandlePointDto> = if (candlesJsonToSave.isNotEmpty()) {
                            gson.fromJson(candlesJsonToSave, listType)
                        } else mutableListOf()

                        val newPoint = CandlePointDto(
                            time = (now / 1000).toString(),
                            open = quoteData.price,
                            high = quoteData.price,
                            low = quoteData.price,
                            close = quoteData.price,
                            volume = 0
                        )

                        existingCandles.add(newPoint)

                        val rollingStart = (now / 1000) - 86400
                        val filtered = existingCandles.filter { (it.time.toLongOrNull() ?: 0L) >= rollingStart }

                        candlesJsonToSave = gson.toJson(filtered)
                        sparklineMemCache[s] = filtered.map { it.close }
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Incremental chart update failed for $s", e)
                }
            }

            if (updateDb) {
                stockCacheDao.insertStockCache(
                    StockCacheEntity(
                        symbol = s, price = quoteData.price, change = quoteData.change,
                        changePercent = quoteData.changePercent, previousClose = quoteData.previousClose,
                        candlesJson = candlesJsonToSave, timestamp = now
                    )
                )
                updateStockPricesInDb(s, quoteData.price, quoteData.change, quoteData.changePercent, quoteData.previousClose)
            }
            quoteData.copy(candlesJson = candlesJsonToSave)
        } catch (e: Exception) {
            Log.e(tag, "fetchLivePriceOnly failed for $s", e)
            null
        }
    }

    suspend fun syncAllDataAndPrices(forceFullRefresh: Boolean = false) = withContext(Dispatchers.IO) {
        if (!sessionManager.isLoggedIn()) return@withContext
        val allPortfolio = portfolioDao.getAllStocks().first()
        val allWatchlist = watchlistDao.getAllWatchlistStocks().first()
        val allSymbols = (allPortfolio.map { it.symbol } + allWatchlist.map { it.symbol }).distinct()

        if (allSymbols.isEmpty()) return@withContext

        if (forceFullRefresh) {
            allSymbols.chunked(5).forEach { chunk ->
                chunk.map { async { fetchLivePriceOnly(it, forceNetwork = true) } }.awaitAll()
            }
        } else {
            val activeMarkets = allSymbols.filter { com.apexinvest.app.util.StockMetadataUtils.isExtendedMarketActive(it) }
            if (activeMarkets.isNotEmpty()) {
                val results = mutableListOf<StockLiveQuoteDto>()
                activeMarkets.chunked(20).forEach { chunk ->
                    try {
                        val symbolsString = chunk.joinToString(",")
                        val yahooResponse = yahooFinanceApiService.getQuotes(symbolsString)
                        val fetched = YahooParser.parseV7Response(yahooResponse)
                        results.addAll(fetched)
                    } catch (e: Exception) {
                        Log.e(tag, "Failed to fetch batch quotes for $chunk", e)
                    }
                }
                updateStockPricesBatch(results)
            }
        }
        lastPriceSyncTime = System.currentTimeMillis()
    }

    suspend fun getConversionRates(): Map<String, Double> = withContext(Dispatchers.IO) {
        try {
            val response = currencyApiService.getExchangeRates("USD")
            cachedRatesMap = response.rates
            cachedRatesMap
        } catch (_: Exception) {
            cachedRatesMap
        }
    }

    suspend fun getConversionRate(): Double = getConversionRates()["INR"] ?: 84.0

    fun getDeepAnalysisFlow(symbol: String, forceRefresh: Boolean = false): Flow<Pair<String, DeepAnalysisResponse?>> = flow {
        val s = symbol.uppercase().trim()
        val cacheKey = "DEEP_$s"
        val cached = analysisCacheDao.getAnalysisCache(cacheKey)
        val data = cached?.let { gson.fromJson(it.dataJson, DeepAnalysisResponse::class.java) }

        val isExpired = cached == null || (System.currentTimeMillis() - cached.timestamp > 86_400_000L)

        if (data != null && !forceRefresh && !isExpired) {
            emit("COMPLETED" to data)
            return@flow
        }

        try {
            emit((if (data != null) "Refreshing AI Data..." else "Initializing AI Engine...") to data)
            val jobId = predictionApiService.analyzeStock(s).jobId
            while(currentCoroutineContext().isActive) {
                val status = predictionApiService.checkJobStatus(jobId)
                if (status.status == "COMPLETED") {
                    val res = gson.fromJson(gson.toJson(status.data), DeepAnalysisResponse::class.java)
                    analysisCacheDao.insertAnalysisCache(AnalysisCacheEntity(cacheKey, gson.toJson(res), "", System.currentTimeMillis()))
                    emit("COMPLETED" to res)
                    break
                }
                else if (status.status == "FAILED") {
                    emit("Error: Analysis failed" to data)
                    break
                } else emit(status.status to data)
                delay(3000.milliseconds)
            }
        } catch (e: Exception) {
            emit("Error: ${e.localizedMessage}" to data)
        }
    }

    fun getPortfolioAnalysisFlow(forceRefresh: Boolean = false): Flow<Pair<String, PortfolioSummary?>> = flow {
        val cacheKey = "PORTFOLIO_SUMMARY"
        val cached = analysisCacheDao.getAnalysisCache(cacheKey)

        val data = cached?.let {
            try { gson.fromJson(it.dataJson, PortfolioSummary::class.java) } catch (_: Exception) { null }
        }

        val portfolio = portfolioDao.getAllStocks().first()
        val currentSignature = portfolio.sortedBy { it.symbol }
            .joinToString("|") { "${it.symbol}:${it.quantity}" }

        val isExpired = cached == null || (System.currentTimeMillis() - cached.timestamp > 86_400_000L)
        val signatureMatches = cached?.signature == currentSignature

        if (data != null && !forceRefresh && !isExpired && signatureMatches) {
            emit("COMPLETED" to data)
            return@flow
        }

        try {
            emit("Gathering Portfolio Data..." to data)
            val symbols = portfolio.map { it.symbol }

            if (symbols.isEmpty()) {
                emit("EMPTY_PORTFOLIO" to null)
                return@flow
            }

            val jobId = predictionApiService.analyzePortfolio(PortfolioAnalysisRequest(symbols)).jobId
            while(currentCoroutineContext().isActive) {
                val status = predictionApiService.checkJobStatus(jobId)
                if (status.status == "COMPLETED") {
                    val res = gson.fromJson(gson.toJson(status.data), PortfolioSummary::class.java)
                    analysisCacheDao.insertAnalysisCache(
                        AnalysisCacheEntity(cacheKey, gson.toJson(res), currentSignature, System.currentTimeMillis())
                    )
                    emit("COMPLETED" to res)
                    break
                } else if (status.status == "FAILED") {
                    emit("Error: Backend failure" to data)
                    break
                } else {
                    emit(status.status to data)
                }
                delay(3000.milliseconds)
            }
        } catch (e: Exception) {
            emit("Error: ${e.localizedMessage}" to data)
        }
    }

    suspend fun getAiPortfolioInsights(summary: String): String = withContext(Dispatchers.IO) {
        try {
            val res = ideasApi.getPortfolioAnalysis(com.apexinvest.app.api.models.PortfolioRequest(summary)).body()?.responseText
            if (!res.isNullOrBlank()) return@withContext res

            val cleanTickersOnly = summary.split(";").map { it.substringBefore("(").trim() }.filter { it.isNotEmpty() }.joinToString(", ")
            ideasApi.getPortfolioAnalysis(com.apexinvest.app.api.models.PortfolioRequest(cleanTickersOnly)).body()?.responseText ?: getOfflineFallback()
        } catch (_: Exception) {
            try {
                val cleanTickersOnly = summary.split(";").map { it.substringBefore("(").trim() }.filter { it.isNotEmpty() }.joinToString(", ")
                ideasApi.getPortfolioAnalysis(com.apexinvest.app.api.models.PortfolioRequest(cleanTickersOnly)).body()?.responseText ?: getOfflineFallback()
            } catch (_: Exception) {
                getOfflineFallback()
            }
        }
    }

    suspend fun getAiThematicInsights(theme: String): String = withContext(Dispatchers.IO) { try { ideasApi.getThematicAnalysis(com.apexinvest.app.api.models.ThemeRequest(theme)).body()?.responseText ?: getOfflineFallback() } catch (_: Exception) { getOfflineFallback() } }

    suspend fun getCachedAiInsights(key: String): String? = withContext(Dispatchers.IO) {
        val cached = analysisCacheDao.getAnalysisCache(key)?.dataJson
        if (cached == getOfflineFallback()) null else cached
    }

    suspend fun saveAiInsights(key: String, data: String) = withContext(Dispatchers.IO) {
        if (data != getOfflineFallback()) {
            analysisCacheDao.insertAnalysisCache(AnalysisCacheEntity(key, data))
        }
    }

    private fun getOfflineFallback(): String = "[RISK] Offline Mode: Unable to reach server.\n[SUGGESTION] SPY | QQQ | GLD"

    fun getLocalWatchlist() = watchlistDao.getAllWatchlistStocks()
    fun getAllTransactionHistory() = transactionDao.getAllTransactions()
    fun getRecentTransactions(limit: Int) = transactionDao.getRecentTransactions(limit)

    suspend fun deleteTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        val authHeader = sessionManager.getAuthHeader()

        transactionDao.deleteTransaction(transaction)

        if (authHeader != null && transaction.cloudId != null) {
            try {
                val response = authApiService.deleteCloudTransaction(authHeader, transaction.cloudId)
                if (!response.isSuccessful) {
                    Log.e("SyncError", "Failed to delete from cloud. Code: ${response.code()}, Body: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("SyncError", "Network error while deleting transaction: ${e.message}")
            }
        } else if (authHeader != null) {
            Log.w("SyncError", "Cannot delete from cloud: transaction.cloudId is null for ${transaction.symbol}")
        }
    }

    suspend fun clearAllLocalData() = withContext(Dispatchers.IO) {
        portfolioDao.clearPortfolio()
        watchlistDao.clearWatchlist()
        transactionDao.clearAllTransactions()
        stockCacheDao.clearAll()
        analysisCacheDao.clearAll()
        notificationDao.clearAllNotifications()
        sessionManager.clearSession()
        lastPriceSyncTime = 0L
        sparklineMemCache.clear()
    }

    suspend fun generatePortfolioCsv(): String = withContext(Dispatchers.IO) {
        val stocks = portfolioDao.getAllStocks().first()
        val sb = StringBuilder()

        sb.append("\uFEFF") // UTF-8 BOM

        sb.append("Symbol,Shares,Currency,Buy Price,Current Price,Total Invested,Total Value,Gain/Loss\n")

        stocks.forEach { stock ->
            val code = com.apexinvest.app.util.guessCurrencyFromSymbol(stock.symbol)

            val inv = stock.buyPrice * stock.quantity
            val currentVal = stock.currentPrice * stock.quantity
            val gain = currentVal - inv

            val qtyFormat = String.format(Locale.US, "%.4f", stock.quantity)
            val buyFormat = String.format(Locale.US, "%.2f", stock.buyPrice)
            val curFormat = String.format(Locale.US, "%.2f", stock.currentPrice)
            val invFormat = String.format(Locale.US, "%.2f", inv)
            val valFormat = String.format(Locale.US, "%.2f", currentVal)
            val gainFormat = String.format(Locale.US, "%.2f", gain)

            sb.append("${stock.symbol},$qtyFormat,$code(${com.apexinvest.app.util.getCurrencySymbol(code)}),$buyFormat,$curFormat,$invFormat,$valFormat,$gainFormat\n")
        }

        sb.toString()
    }
}