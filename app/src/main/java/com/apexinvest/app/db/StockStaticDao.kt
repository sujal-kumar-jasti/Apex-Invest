package com.apexinvest.app.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StockStaticDao {
    @Query("SELECT * FROM stock_static_info WHERE symbol = :symbol LIMIT 1")
    suspend fun getStaticInfo(symbol: String): StockStaticEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaticInfo(info: StockStaticEntity)
}