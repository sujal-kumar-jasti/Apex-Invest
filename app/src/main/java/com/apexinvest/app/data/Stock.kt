package com.apexinvest.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
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
        )
    }
}