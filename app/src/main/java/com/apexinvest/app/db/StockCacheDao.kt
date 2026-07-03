package com.apexinvest.app.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

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
