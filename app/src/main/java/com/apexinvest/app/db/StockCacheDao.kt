package com.apexinvest.app.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StockCacheDao {
    @Query("SELECT * FROM stock_cache WHERE symbol = :symbol")
    suspend fun getStockCache(symbol: String): StockCacheEntity?

    @Query("SELECT * FROM stock_cache WHERE symbol IN (:symbols)")
    suspend fun getStocksCache(symbols: List<String>): List<StockCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockCache(cache: StockCacheEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStocksCache(stocks: List<StockCacheEntity>)

    @Query("SELECT candlesJson FROM stock_cache WHERE symbol = :symbol")
    fun getCachedCandlesSync(symbol: String): String?

    @Query("DELETE FROM stock_cache")
    suspend fun clearAll()
}

@Dao
interface AnalysisCacheDao {
    @Query("SELECT * FROM analysis_cache WHERE `key` = :key")
    suspend fun getAnalysisCache(key: String): AnalysisCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysisCache(cache: AnalysisCacheEntity)

    @Query("DELETE FROM analysis_cache")
    suspend fun clearAll()
}
