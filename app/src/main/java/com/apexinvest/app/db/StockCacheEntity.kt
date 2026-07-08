package com.apexinvest.app.db

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "stock_cache")
data class StockCacheEntity(
    @PrimaryKey val symbol: String,
    val price: Double,
    val change: Double,
    val changePercent: Double,
    val previousClose: Double = 0.0, // Maintain session consistency
    val preMarketPrice: Double? = null,
    val preMarketChange: Double? = null,
    val postMarketPrice: Double? = null,
    val postMarketChange: Double? = null,
    val marketState: String? = null,
    val candlesJson: String, // Storing as JSON string for simplicity in cache
    val timestamp: Long = System.currentTimeMillis()
)

@Keep
@Entity(tableName = "analysis_cache")
data class AnalysisCacheEntity(
    @PrimaryKey val key: String, // e.g., "DEEP_NVDA" or "PORTFOLIO_SUMMARY"
    val dataJson: String,
    val signature: String = "", // Track state changes
    val timestamp: Long = System.currentTimeMillis()
)
