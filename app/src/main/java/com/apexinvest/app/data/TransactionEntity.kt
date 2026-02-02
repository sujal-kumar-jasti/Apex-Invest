package com.apexinvest.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val symbol: String,
    val type: TransactionType,// BUY or SELL
    val quantity: Int,        // e.g., 10
    val price: Double,        // Price per share at time of trade
    val timestamp: Long,      // Time of trade (Epoch millis)
    val fees: Double = 0.0,   // Brokerage/Taxes (Professional touch)
    val notes: String = ""    // Optional user notes
)

enum class TransactionType {
    BUY, SELL
}