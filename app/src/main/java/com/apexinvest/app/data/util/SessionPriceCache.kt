package com.apexinvest.app.data.util

import java.util.concurrent.ConcurrentHashMap

data class PriceData(
    val price: Double,
    val change: Double,
    val changePercent: Double,
    val previousClose: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 🚀 COMMON VARIABLE: Single source of truth for current prices across all screens.
 * This is maintained in-memory for instant access and shared between Dashboard/Details.
 */
object SessionPriceCache {
    private val cache = ConcurrentHashMap<String, PriceData>()

    fun update(symbol: String, price: Double, change: Double, percent: Double, prevClose: Double = 0.0) {
        val s = symbol.uppercase().trim()
        cache[s] = PriceData(price, change, percent, prevClose)
    }

    fun get(symbol: String): PriceData? {
        return cache[symbol.uppercase().trim()]
    }

    fun clear() {
        cache.clear()
    }
}
