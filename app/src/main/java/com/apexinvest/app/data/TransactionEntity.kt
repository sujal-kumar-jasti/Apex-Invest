package com.apexinvest.app.data

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val cloudId: String? = null,  // 🚀 ADD THIS: Maps to the backend UUID
    val symbol: String,
    val type: TransactionType,    // BUY or SELL
    val quantity: Double,
    val price: Double,            // Price per share at time of trade
    val fees: Double = 0.0,       // Brokerage/Taxes
    val notes: String = "",       // Optional user notes
    val timestamp: Long = System.currentTimeMillis()
)

enum class TransactionType {
    BUY, SELL
}