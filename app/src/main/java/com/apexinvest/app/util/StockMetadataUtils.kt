package com.apexinvest.app.util

import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

object StockMetadataUtils {
    data class ExchangeInfo(
        val name: String,
        val currency: String,
        val tvPrefix: String,
        val timezone: String,
        val openTime: String, // "HH:mm"
        val closeTime: String,
        val lunchStart: String? = null, // "HH:mm"
        val lunchEnd: String? = null
    )

    // Map of all major global stock exchange suffixes
    private val exchangeMap = mapOf(
        // North America
        "TO" to ExchangeInfo("Toronto Stock Exchange", "CAD", "TSX", "America/Toronto", "09:30", "16:00"),
        "V"  to ExchangeInfo("TSX Venture Exchange", "CAD", "TSXV", "America/Toronto", "09:30", "16:00"),
        "MX" to ExchangeInfo("Bourse de Montreal", "CAD", "BM", "America/Toronto", "09:30", "16:00"),
        
        // Asia / Pacific
        "T"  to ExchangeInfo("Tokyo Stock Exchange", "JPY", "TSE", "Asia/Tokyo", "09:00", "15:30", "11:30", "12:30"),
        "HK" to ExchangeInfo("Hong Kong Stock Exchange", "HKD", "HKEX", "Asia/Hong_Kong", "09:30", "16:00", "12:00", "13:00"),
        "SS" to ExchangeInfo("Shanghai Stock Exchange", "CNY", "SSE", "Asia/Shanghai", "09:30", "15:00", "11:30", "13:00"),
        "SZ" to ExchangeInfo("Shenzhen Stock Exchange", "CNY", "SZSE", "Asia/Shanghai", "09:30", "15:00", "11:30", "13:00"),
        "KS" to ExchangeInfo("Korea Stock Exchange (KOSPI)", "KRW", "KRX", "Asia/Seoul", "09:00", "15:30"),
        "AX" to ExchangeInfo("Australian Securities Exchange", "AUD", "ASX", "Australia/Sydney", "10:00", "16:00"),
        "SI" to ExchangeInfo("Singapore Exchange", "SGD", "SGX", "Asia/Singapore", "09:00", "17:00", "12:00", "13:00"),
        "TW" to ExchangeInfo("Taiwan Stock Exchange", "TWD", "TWSE", "Asia/Taipei", "09:00", "13:30"),
        "NZ" to ExchangeInfo("New Zealand Exchange", "NZD", "NZX", "Pacific/Auckland", "10:00", "16:45"),
        
        // Europe
        "L"  to ExchangeInfo("London Stock Exchange", "GBP", "LSE", "Europe/London", "08:00", "16:30"),
        "DE" to ExchangeInfo("Xetra (Germany)", "EUR", "XETR", "Europe/Berlin", "09:00", "17:30"),
        "F"  to ExchangeInfo("Frankfurt Stock Exchange", "EUR", "FWB", "Europe/Berlin", "09:00", "20:00"),
        "PA" to ExchangeInfo("Euronext Paris", "EUR", "EURONEXT", "Europe/Paris", "09:00", "17:30"),
        "AS" to ExchangeInfo("Euronext Amsterdam", "EUR", "EURONEXT", "Europe/Amsterdam", "09:00", "17:30"),
        "BR" to ExchangeInfo("Euronext Brussels", "EUR", "EURONEXT", "Europe/Brussels", "09:00", "17:30"),
        "LS" to ExchangeInfo("Euronext Lisbon", "EUR", "EURONEXT", "Europe/Lisbon", "08:00", "16:30"),
        "MI" to ExchangeInfo("Italian Stock Exchange", "EUR", "MIL", "Europe/Rome", "09:00", "17:30"),
        "CH" to ExchangeInfo("Six Swiss Exchange", "CHF", "SIX", "Europe/Zurich", "09:00", "17:30"),
        "MC" to ExchangeInfo("Bolsa de Madrid", "EUR", "BME", "Europe/Madrid", "09:00", "17:30"),
        "ST" to ExchangeInfo("Nasdaq Stockholm", "SEK", "OMX", "Europe/Stockholm", "09:00", "17:30"),
        "OL" to ExchangeInfo("Oslo Stock Exchange", "NOK", "OSE", "Europe/Oslo", "09:00", "16:20"),
        "CO" to ExchangeInfo("Nasdaq Copenhagen", "DKK", "OMX", "Europe/Copenhagen", "09:00", "17:00"),
        "HE" to ExchangeInfo("Nasdaq Helsinki", "EUR", "OMX", "Europe/Helsinki", "10:00", "18:30"),
        "IC" to ExchangeInfo("Nasdaq Iceland", "ISK", "OMX", "Atlantic/Reykjavik", "09:30", "15:30"),
        
        // India & Middle East
        "BO" to ExchangeInfo("BSE India", "INR", "BSE", "Asia/Kolkata", "09:15", "15:30"),
        "NS" to ExchangeInfo("NSE India", "INR", "NSE", "Asia/Kolkata", "09:15", "15:30"),
        "SR" to ExchangeInfo("Saudi Exchange (Tadawul)", "SAR", "TADAWUL", "Asia/Riyadh", "10:00", "15:00"),
        "QA" to ExchangeInfo("Qatar Stock Exchange", "QAR", "QSE", "Asia/Qatar", "09:30", "13:15"),
        "IS" to ExchangeInfo("Borsa Istanbul", "TRY", "BIST", "Europe/Istanbul", "10:00", "18:00"),
        "TA" to ExchangeInfo("Tel Aviv Stock Exchange", "ILS", "TASE", "Asia/Jerusalem", "09:00", "17:25"),
        
        // South America
        "SA" to ExchangeInfo("B3 Brazil", "BRL", "BMFBOVESPA", "America/Sao_Paulo", "10:00", "17:00"),
        "MX" to ExchangeInfo("Mexican Stock Exchange", "MXN", "BMV", "America/Mexico_City", "08:30", "15:00"),
        "BA" to ExchangeInfo("Buenos Aires Stock Exchange", "ARS", "BCBA", "America/Argentina/Buenos_Aires", "11:00", "17:00")
    )

    /**
     * Decodes a raw ticker symbol (e.g., "1322.T", "AAPL", "^NSEI") into its full exchange information.
     */
    fun getExchangeInfo(symbol: String): ExchangeInfo {
        val s = symbol.uppercase().trim()
        
        // 🛠️ FIX: Explicit Index Mapping (Bypass suffix check for known indices)
        when {
            s == "^NSEI" || s == "NIFTY 50" || s == "NIFTY_50" -> 
                return ExchangeInfo("NSE India Index", "INR", "NSE", "Asia/Kolkata", "09:15", "15:30")
            s == "^NSEBANK" || s == "BANKNIFTY" -> 
                return ExchangeInfo("NSE Bank Index", "INR", "NSE", "Asia/Kolkata", "09:15", "15:30")
            s == "^BSESN" || s == "SENSEX" -> 
                return ExchangeInfo("BSE India Index", "INR", "BSE", "Asia/Kolkata", "09:15", "15:30")
            s == "^GDAXI" || s == "DAX" -> 
                return ExchangeInfo("DAX Performance Index", "EUR", "XETR", "Europe/Berlin", "09:00", "17:30")
            s == "^FTSE" || s == "FTSE 100" -> 
                return ExchangeInfo("FTSE 100 Index", "GBP", "LSE", "Europe/London", "08:00", "16:30")
            s == "^FCHI" || s == "CAC 40" -> 
                return ExchangeInfo("CAC 40 Index", "EUR", "EURONEXT", "Europe/Paris", "09:00", "17:30")
            s == "^N225" || s == "NIKKEI" -> 
                return ExchangeInfo("Nikkei 225", "JPY", "TSE", "Asia/Tokyo", "09:00", "15:00")
            s == "^HSI" || s == "HANG SENG" -> 
                return ExchangeInfo("Hang Seng Index", "HKD", "HKEX", "Asia/Hong_Kong", "09:30", "16:00")
            s.startsWith("GC=F") -> 
                return ExchangeInfo("Gold Comex", "USD", "COMEX", "America/New_York", "00:00", "23:59")
            s.startsWith("SI=F") -> 
                return ExchangeInfo("Silver Comex", "USD", "COMEX", "America/New_York", "00:00", "23:59")
        }

        // If there is no dot suffix, it defaults to the main US markets
        if (!symbol.contains(".")) {
            return ExchangeInfo("US Exchanges (NYSE/NASDAQ)", "USD", "NASDAQ", "America/New_York", "09:30", "16:00")
        }

        // Extract the market suffix after the last dot
        val suffix = symbol.substringAfterLast(".").uppercase()
        
        // Return the matched exchange info, or a generic fallback
        return exchangeMap[suffix] ?: ExchangeInfo("Other International Exchange ($suffix)", "USD", "NASDAQ", "America/New_York", "09:30", "16:00")
    }

    /**
     * Checks if the market is currently open for the given symbol.
     */
    fun isMarketOpen(symbol: String): Pair<Boolean, String> {
        val info = getExchangeInfo(symbol)
        val zoneId = ZoneId.of(info.timezone)
        val now = ZonedDateTime.now(zoneId)
        
        val day = now.dayOfWeek.value
        if (day > 5) return false to "Market Closed (Weekend)"
        
        val openTime = LocalTime.parse(info.openTime)
        val closeTime = LocalTime.parse(info.closeTime)
        val currentTime = now.toLocalTime()
        
        val isOpen = currentTime in openTime..closeTime
        
        if (isOpen && info.lunchStart != null && info.lunchEnd != null) {
            val lunchStart = LocalTime.parse(info.lunchStart)
            val lunchEnd = LocalTime.parse(info.lunchEnd)
            if (
                currentTime in lunchStart..lunchEnd) {
                return false to "Market Closed (Lunch Break - ${info.name})"
            }
        }

        return isOpen to if (isOpen) "Market Open (${info.name})" else "Market Closed (${info.name})"
    }

    /**
     * 🚀 NEW: Check if the market is currently in extended hours (Pre/Post) or recently closed.
     * This is used to keep polling for after-hours prices and final close prices.
     */
    fun isExtendedMarketActive(symbol: String, graceMinutes: Long = 15): Boolean {
        val info = getExchangeInfo(symbol)
        val zoneId = ZoneId.of(info.timezone)
        val now = ZonedDateTime.now(zoneId)
        
        val day = now.dayOfWeek.value
        if (day > 5) return false
        
        val openTime = LocalTime.parse(info.openTime)
        val closeTime = LocalTime.parse(info.closeTime)
        val currentTime = now.toLocalTime()
        
        // 1. Regular market is open
        if (currentTime in openTime..closeTime) return true
        
        // 2. Grace period after close (to fetch final close price)
        val gracePeriodEnd = closeTime.plusMinutes(graceMinutes)
        if (currentTime > closeTime && currentTime <= gracePeriodEnd) return true
        
        // 3. Extended hours for US markets
        if (!symbol.contains(".")) {
            val preMarketStart = LocalTime.of(4, 0)
            val postMarketEnd = LocalTime.of(20, 0)
            if (currentTime in preMarketStart..postMarketEnd) return true
        }
        
        return false
    }

    /**
     * Formats a ticker symbol for backend APIs (e.g., TradingView, Live Stock).
     * Format: "ticker:exchange" where ticker includes its original suffix.
     * Example: "RELIANCE.NS:NSE", "AAPL:NASDAQ", "1322.T:TSE"
     */
    fun getFormattedSymbol(symbol: String): String {
        val info = getExchangeInfo(symbol)
        val tvExchange = info.tvPrefix.uppercase()
        return "${symbol.uppercase()}:$tvExchange"
    }

    /**
     * Determines if a new trading session has started since the last update.
     * This is used to invalidate caches when a market opens.
     */
    fun isNewSessionStarted(symbol: String, lastUpdateMillis: Long): Boolean {
        val info = getExchangeInfo(symbol)
        val zoneId = ZoneId.of(info.timezone)
        
        val now = ZonedDateTime.now(zoneId)
        val lastUpdate = ZonedDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(lastUpdateMillis), 
            zoneId
        )

        // If it's a different calendar day, we should probably check further
        if (now.toLocalDate().isAfter(lastUpdate.toLocalDate())) {
            val openTime = LocalTime.parse(info.openTime)
            
            // If now is past the open time of the current day, and last update was before that
            if (now.toLocalTime() >= openTime) {
                return true
            }
            
            // If there's more than a 24h gap, it's definitely a new session
            if (java.time.Duration.between(lastUpdate, now).toHours() > 24) {
                return true
            }
        }
        
        return false
    }
}
