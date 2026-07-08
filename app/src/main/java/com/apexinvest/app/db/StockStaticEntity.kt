// 1. Entity: StockStaticEntity.kt
package com.apexinvest.app.db

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "stock_static_info")
data class StockStaticEntity(
    @PrimaryKey val symbol: String,
    val companyName: String,
    val sector: String,
    val industry: String,
    val description: String? = null
)