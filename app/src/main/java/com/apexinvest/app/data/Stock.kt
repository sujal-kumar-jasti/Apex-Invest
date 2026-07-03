package com.apexinvest.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
<<<<<<< HEAD
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// --- 1. PORTFOLIO TABLE (Local Database) ---
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
    val previousClose: Double = 0.0, // 🆕 Stored for daily gain calculations
    val preMarketPrice: Double? = null,
    val preMarketChange: Double? = null,
    val postMarketPrice: Double? = null,
    val postMarketChange: Double? = null,
    val marketState: String? = null,
    val lastUpdated: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
    val timestamp: Long = System.currentTimeMillis()
)

// --- 2. WATCHLIST TABLE ---
@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey
    val symbol: String = "",
    val companyName: String = "Unknown",
    val sector: String = "Unknown",
    val lastPrice: Double = 0.0,
    val dailyChange: Double = 0.0,
    val changePercent: Double = 0.0,
    val previousClose: Double = 0.0 // 🆕 Stored for daily gain calculations
)

// --- 3. HELPER MODEL ---
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
        cachedPrevClose: Double = 0.0, // 🆕 Mapped from API
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
=======
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// --- 1. ROOM Entity (Local Database) ---
@Entity(tableName = "portfolio")
data class StockEntity(
    @PrimaryKey
    val symbol: String,         // e.g., "AAPL" - Unique identifier
    val quantity: Int,          // Number of shares owned
    val buyPrice: Double,       // Price at the time of purchase
    val buyDate: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE), // ADDED: Buy Date (YYYY-MM-DD)
    val lastUpdated: Long = 0,  // Timestamp for conflict resolution/syncing
    // Fields below will be updated by the external API
    val currentPrice: Double = 0.0,
    val dailyChange: Double = 0.0
)

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey
    val symbol: String = "", // e.g., "TSLA"
    val lastPrice: Double = 0.0
)
// NOTE: Removed the secondary constructor. A data class with default values is often
// sufficient for Firebase/Room when using Kotlin data classes, and it relies on
// the primary constructor's default values. If Firebase gives issues, you may
// need to add @JvmOverloads to the primary constructor.

// --- 2. Firestore/API Model (Cloud/External Data) ---
@IgnoreExtraProperties
data class StockPortfolioItem(
    val symbol: String = "",
    val quantity: Int = 0,
    val buyPrice: Double = 0.0,
    val buyDate: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE), // ADDED: Buy Date (YYYY-MM-DD)

    // Fields excluded from serialization but tracked locally in the entity
    @get:Exclude var currentPrice: Double = 0.0,
    @get:Exclude var dailyChange: Double = 0.0
) {

    // Helper to convert Firestore Model to Room Entity
    fun toEntity(): StockEntity {
        return StockEntity(
            symbol = this.symbol,
            quantity = this.quantity,
            buyPrice = this.buyPrice,
            buyDate = this.buyDate
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
        )
    }
}