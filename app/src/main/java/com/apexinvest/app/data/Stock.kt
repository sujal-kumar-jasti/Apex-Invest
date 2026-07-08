package com.apexinvest.app.data

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Portfolio table
@Keep
@Entity(tableName = "portfolio")
data class StockEntity(
    @PrimaryKey
    val symbol: String,
    val companyName: String = "Unknown",
    val sector: String = "Unknown",
    val quantity: Double,
    val buyPrice: Double,
    val currentPrice: Double = 0.0,
    val dailyChange: Double = 0.0,
    val changePercent: Double = 0.0,
    val previousClose: Double = 0.0, // For daily gain calculations
    val preMarketPrice: Double? = null,
    val preMarketChange: Double? = null,
    val postMarketPrice: Double? = null,
    val postMarketChange: Double? = null,
    val marketState: String? = null,
    val lastUpdated: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
    val timestamp: Long = System.currentTimeMillis()
)

// Watchlist table
@Keep
@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey
    val symbol: String = "",
    val companyName: String = "Unknown",
    val sector: String = "Unknown",
    val lastPrice: Double = 0.0,
    val dailyChange: Double = 0.0,
    val changePercent: Double = 0.0,
    val previousClose: Double = 0.0 // Stored for daily gain calculations
)

// Helper model
@Keep
data class StockPortfolioItem(
    val symbol: String = "",
    val quantity: Double = 0.0,
    val buyPrice: Double = 0.0,
    val buyDate: String = ""
) {
    fun toEntity(
        cachedPrice: Double = 0.0,
        cachedChange: Double = 0.0,
        cachedChangePercent: Double = 0.0,
        cachedPrevClose: Double = 0.0, // Mapped from API
        companyName: String = "Unknown",
        sector: String = "Unknown"
    ): StockEntity {
        return StockEntity(
            symbol = this.symbol,
            companyName = companyName,
            sector = sector,
            quantity = this.quantity,
            buyPrice = this.buyPrice,
            currentPrice = cachedPrice,
            dailyChange = cachedChange,
            changePercent = cachedChangePercent,
            previousClose = cachedPrevClose,
            lastUpdated = this.buyDate.ifEmpty {
                LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            }
        )
    }
}