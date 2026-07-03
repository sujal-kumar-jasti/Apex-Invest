package com.apexinvest.app.api.util

import com.apexinvest.app.api.models.CandlePointDto
import com.apexinvest.app.api.models.StockLiveQuoteDto
import com.apexinvest.app.api.models.yahoo.YahooChartResponse
import com.apexinvest.app.api.models.yahoo.YahooQuoteResponse

object YahooParser {

    /**
     * 🚀 Lightweight Parser for v7/finance/quote results
     */
    fun parseV7Response(response: YahooQuoteResponse): List<StockLiveQuoteDto> {
        return response.quoteResponse?.result?.map { data ->
            StockLiveQuoteDto(
                symbol = data.symbol,
                price = data.regularMarketPrice ?: 0.0,
                change = data.regularMarketChange ?: 0.0,
                changePercent = data.regularMarketChangePercent ?: 0.0,
                previousClose = data.regularMarketPreviousClose ?: 0.0,
                open = data.regularMarketOpen ?: 0.0,
                dayHigh = data.regularMarketDayHigh ?: 0.0,
                dayLow = data.regularMarketDayLow ?: 0.0,
                yearHigh = data.fiftyTwoWeekHigh ?: 0.0,
                yearLow = data.fiftyTwoWeekLow ?: 0.0,
                prePrice = data.preMarketPrice,
                preChange = data.preMarketChange,
                postPrice = data.postMarketPrice,
                postChange = data.postMarketChange,
                marketState = data.marketState ?: "CLOSED",
                hasPrePost = data.hasPrePostMarketData ?: true
            )
        } ?: emptyList()
    }

    fun parseToQuote(symbol: String, response: YahooChartResponse, candlesJson: String? = null): StockLiveQuoteDto {
        val result = response.chart?.result?.firstOrNull()
        val meta = result?.meta

        // 1. Extract the deep numerical data arrays
        val quoteBlock = result?.indicators?.quote?.firstOrNull()
        val timestamps = result?.timestamp ?: emptyList()

        // 2. Fetch the absolute latest market close tick from the live chart series array
        val lastSequencePrice = quoteBlock?.close?.lastOrNull { it != null && it > 0.0 }

        // 3. Fallback priority for true live current price handling
        // 🚀 CRITICAL FIX: price should ONLY be the Regular Market price. 
        // We MUST NOT use lastSequencePrice here because it contains After-Hours data.
        val price = meta?.regularMarketPrice ?: meta?.currentPrice ?: 0.0
        val prevClose = meta?.previousClose ?: meta?.chartPreviousClose ?: meta?.officialPreviousClose ?: 0.0

        val changePercent = if (meta?.percentageChange == 0.0 && price != 0.0 && prevClose != 0.0) {
            ((price - prevClose) / prevClose) * 100.0
        } else {
            meta?.percentageChange ?: 0.0
        }
        val change = if (price != 0.0 && prevClose != 0.0) price - prevClose else 0.0

        val dayHigh = meta?.regularMarketDayHigh ?: 0.0
        val dayLow = meta?.regularMarketDayLow ?: 0.0
        val yearHigh = meta?.fiftyTwoWeekHigh ?: 0.0
        val yearLow = meta?.fiftyTwoWeekLow ?: 0.0

        // 🚀 SMART STATE DETECTION: If marketState is missing, infer it from exchange hours
        val detectedState = meta?.marketState ?: run {
            val (isOpen, _) = com.apexinvest.app.util.StockMetadataUtils.isMarketOpen(symbol)
            if (isOpen) "REGULAR" else "CLOSED"
        }

        android.util.Log.d("YahooParser", "Meta for $symbol: price=$price, lastSequencePrice=$lastSequencePrice, state=$detectedState (was ${meta?.marketState})")

        val exactOpenPrice = meta?.regularMarketOpen
            ?: quoteBlock?.open?.firstOrNull { it != null && it > 0.0 }
            ?: prevClose

        val isPre = detectedState == "PRE" || detectedState == "PREPRE"
        val isPost = detectedState == "POST" || detectedState == "POSTPOST" || detectedState == "CLOSED"

        // 🚀 DIRECT EXTRACTION: Extract the pre/post extended market calculations from the sequence data
        val prePrice = meta?.preMarketPrice
            ?: (if (isPre) (lastSequencePrice ?: meta?.extendedMarketPrice) else null)
        val preChange = meta?.preMarketChange
            ?: (if (isPre && prePrice != null) prePrice - prevClose else null)

        val postPrice = meta?.postMarketPrice
            ?: (if (isPost) (lastSequencePrice ?: meta?.extendedMarketPrice) else null)
        val postChange = meta?.postMarketChange
            ?: (if (isPost && postPrice != null) postPrice - (meta?.regularMarketPrice ?: price) else null)

        android.util.Log.d("YahooParser", "Final Result for $symbol: price=$price, pre=$prePrice, post=$postPrice, state=$detectedState")

        return StockLiveQuoteDto(
            symbol = symbol,
            price = price,
            change = change,
            changePercent = changePercent,
            previousClose = prevClose,
            open = exactOpenPrice,
            dayHigh = dayHigh,
            dayLow = dayLow,
            yearHigh = yearHigh,
            yearLow = yearLow,
            prePrice = prePrice,
            preChange = preChange,
            postPrice = postPrice,
            postChange = postChange,
            marketState = detectedState,
            hasPrePost = meta?.hasPrePostMarketData ?: true,
            candlesJson = candlesJson
        )
    }

    fun parseToCandles(response: YahooChartResponse): List<CandlePointDto> {
        val result = response.chart?.result?.firstOrNull() ?: return emptyList()
        val timestamps = result.timestamp ?: return emptyList()
        val quote = result.indicators?.quote?.firstOrNull() ?: return emptyList()

        val candles = mutableListOf<CandlePointDto>()
        for (i in timestamps.indices) {
            val o = quote.open?.getOrNull(i) ?: continue
            val h = quote.high?.getOrNull(i) ?: o
            val l = quote.low?.getOrNull(i) ?: o
            val c = quote.close?.getOrNull(i) ?: o
            val v = quote.volume?.getOrNull(i) ?: 0L // Restored missing volume declaration

            candles.add(CandlePointDto(
                time = timestamps[i].toString(),
                open = o, high = h, low = l, close = c, volume = v
            ))
        }
        return candles
    }

    fun filterLast24h(candles: List<CandlePointDto>): List<CandlePointDto> {
        if (candles.isEmpty()) return emptyList()
        // 🛠️ FIX: Anchor to the LATEST data point in the set (not current system time)
        // This ensures charts remain populated on weekends/holidays.
        val lastTimestamp = candles.last().time.toLongOrNull() ?: return candles

        // 🛠️ WINDOW: 24 Hours (86400 seconds)
        val rollingStart = lastTimestamp - 86400

        val filtered = candles.filter { candle ->
            val timestamp = candle.time.toLongOrNull() ?: 0L
            timestamp >= rollingStart
        } // Restored missing brace

        return if (filtered.size < 40 && candles.size > filtered.size) {
            candles.takeLast(100.coerceAtMost(candles.size))
        } else {
            filtered
        }
    } // Restored missing brace

    fun filterRegularHours(symbol: String, candles: List<CandlePointDto>): List<CandlePointDto> {
        if (candles.isEmpty()) return emptyList() // Restored missing empty check

        val exchangeInfo = com.apexinvest.app.util.StockMetadataUtils.getExchangeInfo(symbol)
        val zoneId = try { java.time.ZoneId.of(exchangeInfo.timezone) } catch (e: Exception) { java.time.ZoneId.of("UTC") }
        val openTime = try { java.time.LocalTime.parse(exchangeInfo.openTime) } catch (e: Exception) { java.time.LocalTime.of(9, 30) }
        val closeTime = try { java.time.LocalTime.parse(exchangeInfo.closeTime) } catch (e: Exception) { java.time.LocalTime.of(16, 0) }

        return candles.filter { candle ->
            val timestamp = candle.time.toLongOrNull() ?: 0L // Restored missing timestamp declaration
            val zdt = java.time.ZonedDateTime.ofInstant(java.time.Instant.ofEpochSecond(timestamp), zoneId)
            val day = zdt.dayOfWeek.value
            if (day > 5) return@filter false // Exclude weekends if they somehow snuck in

            val time = zdt.toLocalTime()
            time >= openTime && time <= closeTime
        }
    }
}