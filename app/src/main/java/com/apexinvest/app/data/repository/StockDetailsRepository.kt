package com.apexinvest.app.data.repository

import com.apexinvest.app.api.AdvancedStockApiService
import com.apexinvest.app.api.GlobalStockApiService
import com.apexinvest.app.api.StockApiService
import com.apexinvest.app.api.YahooFinanceApiService
import com.apexinvest.app.api.models.AdvancedRiskMetrics
import com.apexinvest.app.api.models.AdvancedTechnicals
import com.apexinvest.app.api.models.AnalystCoverage
import com.apexinvest.app.api.models.BalanceSheet
import com.apexinvest.app.api.models.CandlePoint
import com.apexinvest.app.api.models.CashFlows
import com.apexinvest.app.api.models.EfficiencyMargins
import com.apexinvest.app.api.models.FinancialsDto
import com.apexinvest.app.api.models.HistoricalReturns
import com.apexinvest.app.api.models.IncomeStatement
import com.apexinvest.app.api.models.MarketPricing
import com.apexinvest.app.api.models.PythonStockInfoDto
import com.apexinvest.app.api.models.StockDetailsResponse
import com.apexinvest.app.api.models.StockHistoryChartDto
import com.apexinvest.app.api.models.StockInfoDto
import com.apexinvest.app.api.models.StockLiveQuoteDto
import com.apexinvest.app.api.models.ValuationMultipliers
import com.apexinvest.app.api.util.YahooParser
import com.apexinvest.app.data.model.StockNews
import com.apexinvest.app.data.remote.ApexInvestApiService
import com.apexinvest.app.db.StockStaticDao
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Collections
import kotlin.math.abs

data class SessionCache<T>(
    val data: T?,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false
)

class StockDetailsRepository(
    private val staticDao: StockStaticDao,
    private val analysisCacheDao: com.apexinvest.app.db.AnalysisCacheDao,
    private val stockCacheDao: com.apexinvest.app.db.StockCacheDao,
    private val yahooFinanceApiService: YahooFinanceApiService,
    private val advancedApiService: AdvancedStockApiService,
    private val globalApiService: GlobalStockApiService,
    private val apexInvestApiService: ApexInvestApiService,
    private val stockApiService: StockApiService // Python Backend API
) {
    private val TAG = "StockDetailsRepository"
    private val gson = com.google.gson.Gson()

    private fun <K, V> createSessionCache() =
        Collections.synchronizedMap(LinkedHashMap<K, V>(50, 0.75f, true))

    private val liveQuoteCache = createSessionCache<String, SessionCache<StockLiveQuoteDto>>()
    private val chartCache = createSessionCache<String, SessionCache<StockHistoryChartDto>>()
    private val fullDataCache = createSessionCache<String, SessionCache<StockInfoDto>>()
    private val aiAnalysisCache = createSessionCache<String, SessionCache<String>>()
    private val newsCache = createSessionCache<String, SessionCache<List<StockNews>>>()

    // Track symbols fetched in this session
    private val sessionFetchedSymbols = Collections.synchronizedSet(mutableSetOf<String>())

    // Dedicated Cache for Python Financial Charts and Info
    private val pythonInfoCache = createSessionCache<String, SessionCache<PythonStockInfoDto>>()

    private fun isIndianMarket(symbol: String): Boolean {
        val s = symbol.uppercase()
        return s.endsWith(".NS") || s.endsWith(".BO") || s.startsWith("^NSE") || s.startsWith("NSE:")
    }

    fun getMergedStockDetailsFlow(
        symbol: String,
        range: String,
        forceRefresh: Boolean = false
    ): Flow<Pair<Result<StockDetailsResponse>, Boolean>> = channelFlow {
        val symClean = symbol.uppercase().trim()
        val isIndian = isIndianMarket(symClean)

        // Force fresh load on first session fetch
        val isFirstSessionFetch = sessionFetchedSymbols.add(symClean)
        val finalForceRefresh = forceRefresh || isFirstSessionFetch

        var staticInfo = staticDao.getStaticInfo(symClean)

        var liveQuote: StockLiveQuoteDto? = liveQuoteCache[symClean]?.data
        var chartData: StockHistoryChartDto? = chartCache["${symClean}_$range"]?.data
        var fullMatrix: StockInfoDto? = fullDataCache[symClean]?.data
        var aiReport: String? = aiAnalysisCache[symClean]?.data
        var newsData: List<StockNews> = newsCache[symClean]?.data ?: emptyList()

        if (liveQuote == null || fullMatrix == null || chartData == null) {
            // Check Stock Cache for most recent live price
            val recentCache = stockCacheDao.getStockCache(symClean)
            
            analysisCacheDao.getAnalysisCache("DETAIL_QUOTE_$symClean")?.let {
                if (liveQuote == null) {
                    try { 
                        val cachedQuote = gson.fromJson(it.dataJson, StockLiveQuoteDto::class.java)
                        // Only use if it's newer than our DB cache or DB cache is missing
                        if (recentCache != null && recentCache.timestamp > it.timestamp) {
                            liveQuote = StockLiveQuoteDto(
                                symbol = symClean,
                                price = recentCache.price,
                                change = recentCache.change,
                                changePercent = recentCache.changePercent,
                                previousClose = recentCache.previousClose,
                                open = recentCache.price, // Fallback
                                dayHigh = recentCache.price,
                                dayLow = recentCache.price,
                                yearHigh = recentCache.price,
                                yearLow = recentCache.price,
                                prePrice = recentCache.preMarketPrice,
                                preChange = recentCache.preMarketChange,
                                postPrice = recentCache.postMarketPrice,
                                postChange = recentCache.postMarketChange,
                                marketState = recentCache.marketState
                            )
                        } else {
                            liveQuote = cachedQuote
                        }
                    } catch (_: Exception) {}
                }
            }
            
            // If still null but we have DB cache, use it
            if (liveQuote == null && recentCache != null) {
                liveQuote = StockLiveQuoteDto(
                    symbol = symClean,
                    price = recentCache.price,
                    change = recentCache.change,
                    changePercent = recentCache.changePercent,
                    previousClose = recentCache.previousClose,
                    open = recentCache.price,
                    dayHigh = recentCache.price,
                    dayLow = recentCache.price,
                    yearHigh = recentCache.price,
                    yearLow = recentCache.price,
                    prePrice = recentCache.preMarketPrice,
                    preChange = recentCache.preMarketChange,
                    postPrice = recentCache.postMarketPrice,
                    postChange = recentCache.postMarketChange,
                    marketState = recentCache.marketState
                )
            }

            analysisCacheDao.getAnalysisCache("DETAIL_MATRIX_$symClean")?.let {
                if (fullMatrix == null) {
                    try { fullMatrix = gson.fromJson(it.dataJson, StockInfoDto::class.java) } catch (_: Exception) {}
                }
            }
            analysisCacheDao.getAnalysisCache("DETAIL_CHART_${symClean}_$range")?.let {
                if (chartData == null) {
                    try { chartData = gson.fromJson(it.dataJson, StockHistoryChartDto::class.java) } catch (_: Exception) {}
                }
            }
            analysisCacheDao.getAnalysisCache("DETAIL_NEWS_$symClean")?.let {
                if (newsData.isEmpty()) {
                    try {
                        val type = object : com.google.gson.reflect.TypeToken<List<StockNews>>() {}.type
                        newsData = gson.fromJson(it.dataJson, type)
                    } catch (_: Exception) {}
                }
            }
            analysisCacheDao.getAnalysisCache("DETAIL_AI_$symClean")?.let {
                if (aiReport == null) aiReport = it.dataJson
            }
        }

        suspend fun pushCurrentState(isComplete: Boolean) {
            if (liveQuote == null && fullMatrix == null && chartData == null && !isComplete) return

            val response = StockDetailsResponse(
                ticker = symClean,
                companyName = staticInfo?.companyName ?: fullMatrix?.companyName ?: symClean,
                sector = staticInfo?.sector ?: fullMatrix?.sector,
                industry = staticInfo?.industry ?: fullMatrix?.industry,
                assetType = fullMatrix?.assetType,
                logoId = fullMatrix?.logoId,
                employeeCount = fullMatrix?.employeeCount,
                country = fullMatrix?.country,
                marketPricing = mapMarketPricing(liveQuote, fullMatrix),
                valuationMultipliers = fullMatrix?.let { mapValuation(it) },
                incomeStatement = fullMatrix?.let { mapIncomeStatement(it) },
                balanceSheet = fullMatrix?.let { mapBalanceSheet(it) },
                cashFlows = fullMatrix?.let { mapCashFlows(it) },
                efficiencyMargins = fullMatrix?.let { mapEfficiencyMargins(it) },
                historicalReturns = fullMatrix?.let { mapHistoricalReturns(it) },
                advancedTechnicals = fullMatrix?.let { mapAdvancedTechnicals(it) },
                analystCoverage = fullMatrix?.let { mapAnalystCoverage(it) },
                advancedRiskMetrics = fullMatrix?.let { mapAdvancedRiskMetrics(it) },
                rangeChangeAbsolute = calculateRangeChangeAbsolute(range, liveQuote, chartData),
                rangeChangePercent = calculateRangeChangePercent(range, liveQuote, chartData),
                candles = chartData?.candles?.map { dto ->
                    CandlePoint(
                        time = dto.time, open = dto.open, high = dto.high,
                        low = dto.low, close = dto.close, volume = dto.volume
                    )
                } ?: emptyList(),
                news = newsData,
                similarStocks = fullMatrix?.similarStocks ?: emptyList(),
                aiAnalystReport = aiReport
            )

            send(Pair(Result.success(response), isComplete))
        }

        pushCurrentState(isComplete = false)

        coroutineScope {
            launch {
                // Skip Yahoo quote if market is open and data exists
                val (isOpen, _) = com.apexinvest.app.util.StockMetadataUtils.isMarketOpen(symClean)
                if (!isOpen || liveQuote == null) {
                    fetchLiveQuote(symClean, finalForceRefresh).onSuccess {
                        liveQuote = it
                        pushCurrentState(isComplete = false)
                    }
                }
            }

            launch {
                fetchChartData(symClean, range, finalForceRefresh).onSuccess {
                    chartData = it
                    pushCurrentState(isComplete = false)
                }
            }

            launch {
                fetchFullMatrix(symClean, isIndian, finalForceRefresh).onSuccess { fetchedMatrix ->
                    fullMatrix = fetchedMatrix
                    if (staticInfo == null && !fetchedMatrix.companyName.isNullOrEmpty()) {
                        staticInfo = com.apexinvest.app.db.StockStaticEntity(
                            symbol = symClean,
                            companyName = fetchedMatrix.companyName,
                            sector = fetchedMatrix.sector ?: "Unknown",
                            industry = fetchedMatrix.industry ?: "Unknown"
                        )
                        staticDao.insertStaticInfo(staticInfo)
                    }
                    pushCurrentState(isComplete = false)
                }
            }

            launch {
                fetchAiAnalysis(symClean, isIndian, finalForceRefresh).onSuccess {
                    aiReport = it
                    pushCurrentState(isComplete = false)
                }
            }

            launch {
                fetchNews(symClean, finalForceRefresh).onSuccess {
                    newsData = it
                    pushCurrentState(isComplete = false)
                }
            }

            // NEW: Fetch Python data simultaneously so the ViewModel has immediate access
            launch {
                fetchPythonStockInfo(symClean, finalForceRefresh)
            }
        }

        if (liveQuote == null && fullMatrix == null && chartData == null) {
            send(Pair(Result.failure(Exception("Failed to establish main data connections.")), true))
        } else {
            pushCurrentState(isComplete = true)
        }
        awaitClose { }
    }

    suspend fun fetchLiveQuote(symbol: String, forceRefresh: Boolean = false): Result<StockLiveQuoteDto> {
        val cached = liveQuoteCache[symbol]
        // Short 10s TTL for live quotes
        val now = System.currentTimeMillis()
        if (!forceRefresh && cached?.isError == false && (now - cached.timestamp < 10_000L)) {
            cached.data?.let { return Result.success(it) }
        }
        return try {
            // Use lightweight v7/finance/quote for live price updates
            val yahooResponse = yahooFinanceApiService.getQuotes(symbol)
            val res = YahooParser.parseV7Response(yahooResponse).firstOrNull { it.symbol.equals(symbol, ignoreCase = true) }
                ?: throw Exception("No quote found for $symbol")

            liveQuoteCache[symbol] = SessionCache(res)
            analysisCacheDao.insertAnalysisCache(com.apexinvest.app.db.AnalysisCacheEntity("DETAIL_QUOTE_$symbol", gson.toJson(res)))
            Result.success(res)
        } catch (e: Exception) {
            val dbCached = analysisCacheDao.getAnalysisCache("DETAIL_QUOTE_$symbol")
            if (dbCached != null) {
                try {
                    val res = gson.fromJson(dbCached.dataJson, StockLiveQuoteDto::class.java)
                    liveQuoteCache[symbol] = SessionCache(res)
                    return Result.success(res)
                } catch (_: Exception) {}
            }
            liveQuoteCache[symbol] = SessionCache(null, isError = true)
            Result.failure(e)
        }
    }

    suspend fun fetchChartData(symbol: String, range: String, forceRefresh: Boolean = false): Result<StockHistoryChartDto> {
        val key = "${symbol}_$range"
        val cached = chartCache[key]
        if (!forceRefresh && cached?.isError == false) {
            cached.data?.let { return Result.success(it) }
        }
        return try {
            val (interval, fetchRange) = when(range) {
                "1d" -> "1m" to "5d" // Increased to 5d for gap coverage
                "5d" -> "5m" to "5d"
                "1mo" -> "15m" to "1mo"
                "3mo", "6mo" -> "1d" to range
                "1y", "2y", "ytd" -> "1d" to range
                else -> "1wk" to range
            }

            val yahooResponse = yahooFinanceApiService.getLivePriceAndChart(symbol, interval, fetchRange, includePrePost = false)
            val quoteDto = YahooParser.parseToQuote(symbol, yahooResponse, null)
            val fullCandles = YahooParser.parseToCandles(yahooResponse)
            
            android.util.Log.d(TAG, "Fetched chart for $symbol range $range: ${fullCandles.size} candles")
            
            // Filter out pre/post market points
            // Only filter regular hours for intra-day ranges
            // 5y and MAX use weekly/monthly candles which fall outside specific daily regular hours.
            val regularCandles = if (range in listOf("1d", "5d", "1mo", "3mo", "6mo")) {
                YahooParser.filterRegularHours(symbol, fullCandles)
            } else {
                fullCandles
            }
            val candles = if (range == "1d") YahooParser.filterRollingWindow(symbol, regularCandles) else regularCandles

            val finalCandles = if (range == "1d" && candles.isNotEmpty() && quoteDto.price > 0) {
                val last = candles.last()
                if (abs(last.close - quoteDto.price) > 0.001) {
                    candles.dropLast(1) + last.copy(close = quoteDto.price)
                } else candles
            } else candles

            val res = StockHistoryChartDto(symbol = symbol, range = range, candles = finalCandles)

            chartCache[key] = SessionCache(res)
            analysisCacheDao.insertAnalysisCache(com.apexinvest.app.db.AnalysisCacheEntity("DETAIL_CHART_$key", gson.toJson(res)))
            Result.success(res)
        } catch (e: Exception) {
            chartCache[key] = SessionCache(null, isError = true)
            Result.failure(e)
        }
    }

    suspend fun fetchFullMatrix(symbol: String, isIndian: Boolean, forceRefresh: Boolean = false): Result<StockInfoDto> {
        val cached = fullDataCache[symbol]
        if (!forceRefresh && cached?.isError == false) {
            cached.data?.let { return Result.success(it) }
        }
        return try {
            val formattedSymbol = com.apexinvest.app.util.StockMetadataUtils.getFormattedSymbol(symbol)
            val res = if (isIndian) advancedApiService.getIndiaFullData(formattedSymbol) else globalApiService.getGlobalFullData(formattedSymbol)

            fullDataCache[symbol] = SessionCache(res)
            analysisCacheDao.insertAnalysisCache(com.apexinvest.app.db.AnalysisCacheEntity("DETAIL_MATRIX_$symbol", gson.toJson(res)))
            Result.success(res)
        } catch (e: Exception) {
            fullDataCache[symbol] = SessionCache(null, isError = true)
            Result.failure(e)
        }
    }

    suspend fun fetchAiAnalysis(symbol: String, isIndian: Boolean, forceRefresh: Boolean = false): Result<String> {
        val cached = aiAnalysisCache[symbol]
        if (!forceRefresh && cached?.isError == false) {
            cached.data?.let { return Result.success(it) }
        }
        return try {
            val formattedSymbol = com.apexinvest.app.util.StockMetadataUtils.getFormattedSymbol(symbol)
            val res = if (isIndian) {
                advancedApiService.getIndiaAiAnalysis(formattedSymbol).analysisReport
            } else {
                globalApiService.getGlobalAiAnalysis(formattedSymbol).analysisReport
            }

            aiAnalysisCache[symbol] = SessionCache(res)
            analysisCacheDao.insertAnalysisCache(com.apexinvest.app.db.AnalysisCacheEntity("DETAIL_AI_$symbol", res))
            Result.success(res)
        } catch (e: Exception) {
            aiAnalysisCache[symbol] = SessionCache(null, isError = true)
            Result.failure(e)
        }
    }

    suspend fun fetchNews(symbol: String, forceRefresh: Boolean = false): Result<List<StockNews>> {
        val cached = newsCache[symbol]
        if (!forceRefresh && cached?.isError == false) {
            cached.data?.let { return Result.success(it) }
        }
        return try {
            val res = apexInvestApiService.getStockNews(symbol)
            newsCache[symbol] = SessionCache(res)
            analysisCacheDao.insertAnalysisCache(com.apexinvest.app.db.AnalysisCacheEntity("DETAIL_NEWS_$symbol", gson.toJson(res)))
            Result.success(res)
        } catch (e: Exception) {
            newsCache[symbol] = SessionCache(null, isError = true)
            Result.failure(e)
        }
    }

    // ==========================================
    // PYTHON BACKEND INTEGRATION (Financial Charts)
    // ==========================================

    // ADD THIS PROPERTY at the top of your repository class (near your caches)
    private val pythonFetchMutex = Mutex()

    // REPLACE your entire fetchPythonStockInfo function with this:
    private suspend fun fetchPythonStockInfo(symbol: String, forceRefresh: Boolean = false): Result<PythonStockInfoDto> {
        val symClean = symbol.uppercase().trim()

        // Wrap the logic in a lock
        return pythonFetchMutex.withLock {
            val cached = pythonInfoCache[symClean]

            if (!forceRefresh && cached?.isError == false && cached.data != null) {
                return@withLock Result.success(cached.data)
            }

            try {
                val res = stockApiService.getStockInfo(symClean)

                pythonInfoCache[symClean] = SessionCache(res)
                analysisCacheDao.insertAnalysisCache(com.apexinvest.app.db.AnalysisCacheEntity("DETAIL_PYTHON_$symClean", gson.toJson(res)))

                Result.success(res)
            } catch (e: Exception) {
                val dbCached = analysisCacheDao.getAnalysisCache("DETAIL_PYTHON_$symClean")
                if (dbCached != null) {
                    try {
                        val res = gson.fromJson(dbCached.dataJson, PythonStockInfoDto::class.java)
                        pythonInfoCache[symClean] = SessionCache(res)
                        return@withLock Result.success(res)
                    } catch (_: Exception) {}
                }
                pythonInfoCache[symClean] = SessionCache(null, isError = true)
                Result.failure(e)
            }
        }
    }

    // Exposes the entire FinancialsDto (Annual, Quarterly, Earnings) to the UI
    suspend fun getFinancialsChartSlice(symbol: String, forceRefresh: Boolean = false): Result<FinancialsDto> {
        return fetchPythonStockInfo(symbol, forceRefresh).mapCatching {
            it.financials ?: throw Exception("Historical chart data is missing from backend")
        }
    }

    // ==========================================
    // GRANULAR DOMAIN EXTRACTIONS (ViewModel Slices)
    // ==========================================

    suspend fun getAnalystCoverageSlice(symbol: String, forceRefresh: Boolean = false): Result<AnalystCoverage> {
        return fetchFullMatrix(symbol, isIndianMarket(symbol), forceRefresh).map { mapAnalystCoverage(it) }
    }

    private fun mapMarketPricing(live: StockLiveQuoteDto?, dto: StockInfoDto?): MarketPricing {
        return MarketPricing(
            priceLast = live?.price ?: dto?.priceLast ?: 0.0,
            priceOpen = live?.open ?: dto?.priceOpen,
            priceHigh = live?.dayHigh ?: dto?.priceHigh,
            priceLow = live?.dayLow ?: dto?.priceLow,
            changePct1D = live?.changePercent ?: dto?.changePct1D,
            changeAbsolute1D = live?.change ?: dto?.changeAbsolute1D,
            previousClose = live?.previousClose ?: 0.0,
            high52Week = live?.yearHigh ?: dto?.high52Week,
            low52Week = live?.yearLow ?: dto?.low52Week,
            volumeCurrent = dto?.volumeCurrent,
            tradedValueDaily = dto?.tradedValueDaily,
            avgVolume10D = dto?.avgVolume10D,
            avgVolume30D = dto?.avgVolume30D,
            avgVolume90D = dto?.avgVolume90D,
            relativeVolume10D = dto?.relativeVolume10D,
            beta1Y = dto?.beta1Y,
            beta5Y = dto?.beta5Y,
            preMarketPrice = live?.prePrice ?: dto?.preMarketPrice,
            preMarketChange = live?.preChange ?: dto?.preMarketChange,
            postMarketPrice = live?.postPrice ?: dto?.postMarketPrice,
            postMarketChange = live?.postChange ?: dto?.postMarketChange,
            marketState = live?.marketState ?: dto?.marketState,
            hasPrePost = live?.hasPrePost ?: true
        )
    }

    private fun mapValuation(dto: StockInfoDto) = ValuationMultipliers(
        marketCap = dto.marketCap, enterpriseValue = dto.enterpriseValue, peRatio = dto.peRatio,
        forwardPe = dto.forwardPe, pegRatio = dto.pegRatio, psRatio = dto.psRatio, pbRatio = dto.pbRatio,
        pFcfRatio = dto.pFcfRatio, evRevenue = dto.evRevenue, enterpriseToEbitda = dto.enterpriseToEbitda
    )

    private fun mapIncomeStatement(dto: StockInfoDto) = IncomeStatement(
        revenueTtm = dto.revenueTtm, revenueQuarterly = dto.revenueQuarterly,
        revenueGrowthYoyPct = dto.revenueGrowthYoyPct, earningsGrowthYoyPct = dto.earningsGrowthYoyPct,
        grossProfitTtm = dto.grossProfitTtm, grossProfitQuarterly = dto.grossProfitQuarterly,
        ebitdaTtm = dto.ebitdaTtm, ebitdaQuarterly = dto.ebitdaQuarterly,
        netIncomeTtm = dto.netIncomeTtm, netIncomeQuarterly = dto.netIncomeQuarterly,
        epsTtm = dto.epsTtm, forwardEps = dto.forwardEps, epsQuarterly = dto.epsQuarterly
    )

    private fun mapBalanceSheet(dto: StockInfoDto) = BalanceSheet(
        totalAssets = dto.totalAssets, totalLiabilities = dto.totalLiabilities, equity = dto.equity,
        totalDebt = dto.totalDebt, netDebt = dto.netDebt, cashAndShortTermInvestments = dto.cashAndShortTermInvestments,
        totalCash = dto.totalCash, currentRatio = dto.currentRatio, quickRatio = dto.quickRatio,
        debtEquityRatio = dto.debtEquityRatio, sharesOutstanding = dto.sharesOutstanding
    )

    private fun mapCashFlows(dto: StockInfoDto) = CashFlows(
        cashFromOperationsTtm = dto.cashFromOperationsTtm, cashFromInvestingTtm = dto.cashFromInvestingTtm,
        cashFromFinancingTtm = dto.cashFromFinancingTtm, freeCashFlowTtm = dto.freeCashFlowTtm,
        freeCashflowYf = dto.freeCashflowYf, capitalExpendituresTtm = dto.capitalExpendituresTtm
    )

    private fun mapEfficiencyMargins(dto: StockInfoDto) = EfficiencyMargins(
        grossMarginPct = dto.grossMarginPct, operatingMarginYf = dto.operatingMarginYf,
        netProfitMarginPct = dto.netProfitMarginPct, ebitdaMarginPct = dto.ebitdaMarginPct,
        roePct = dto.roePct, roeYf = dto.roeYf, roaPct = dto.roaPct, roaYf = dto.roaYf,
        assetTurnoverRatio = dto.assetTurnoverRatio, dividendYieldPct = dto.dividendYieldPct
    )

    private fun mapHistoricalReturns(dto: StockInfoDto) = HistoricalReturns(
        return1W = dto.return1W, return1M = dto.return1M, return3M = dto.return3M, return6M = dto.return6M,
        returnYtd = dto.returnYtd, return1Y = dto.return1Y, return3Y = dto.return3Y, return5Y = dto.return5Y
    )

    private fun mapAdvancedTechnicals(dto: StockInfoDto) = AdvancedTechnicals(
        ratingOverall = dto.technicalRatingOverall, ratingMovingAverages = dto.technicalRatingMovingAverages,
        ratingOscillators = dto.technicalRatingOscillators, rsi14 = dto.rsi14, macdLine = dto.macdLine,
        macdSignalLine = dto.macdSignalLine, ema20 = dto.ema20, sma50 = dto.sma50, sma100 = dto.sma100,
        sma200 = dto.sma200, atr14 = dto.atr14, stochasticK = dto.stochasticK, stochasticD = dto.stochasticD,
        cci20 = dto.cci20, adx14 = dto.adx14, awesomeOscillator = dto.awesomeOscillator,
        momentum10 = dto.momentum10, bullBearPower = dto.bullBearPower, ultimateOscillator = dto.ultimateOscillator,
        williamsRPct = dto.williamsRPct, chaikinMoneyFlow = dto.chaikinMoneyFlow
    )

    private fun mapAnalystCoverage(dto: StockInfoDto) = AnalystCoverage(
        analystRatingConsensus = dto.analystRatingConsensus, targetPriceMean = dto.targetPriceMean,
        targetPriceHigh = dto.targetPriceHigh, targetPriceLow = dto.targetPriceLow, analystCount = dto.analystCount,
        countStrongBuy = dto.countStrongBuy, countBuy = dto.countBuy, countHold = dto.countHold,
        countSell = dto.countSell, countStrongSell = dto.countStrongSell, countTotalRecommendations = dto.countTotalRecommendations
    )

    private fun mapAdvancedRiskMetrics(dto: StockInfoDto) = AdvancedRiskMetrics(
        piotroskiFScore = dto.piotroskiFScore, altmanZScore = dto.altmanZScore,
        grahamNumber = dto.grahamNumber, sharesFloat = dto.sharesFloat
    )

    private fun calculateRangeChangeAbsolute(range: String, live: StockLiveQuoteDto?, chart: StockHistoryChartDto?): Double? {
        if (range == "1d") return live?.change
        val candles = chart?.candles ?: return null
        if (candles.size < 2) return null
        return (live?.price ?: candles.last().close) - candles.first().open
    }

    private fun calculateRangeChangePercent(range: String, live: StockLiveQuoteDto?, chart: StockHistoryChartDto?): Double? {
        if (range == "1d") return live?.changePercent
        val candles = chart?.candles ?: return null
        if (candles.size < 2) return null
        val startPrice = candles.first().open
        if (startPrice <= 0.0) return null
        val currentPrice = live?.price ?: candles.last().close
        return ((currentPrice - startPrice) / startPrice) * 100.0
    }
}