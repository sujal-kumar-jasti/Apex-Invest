package com.apexinvest.app.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.apexinvest.app.data.WatchlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStocks(stocks: List<WatchlistEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStock(stock: WatchlistEntity)

    @Delete
    suspend fun deleteStock(stock: WatchlistEntity)

    @Query("SELECT * FROM watchlist ORDER BY symbol ASC")
    fun getAllWatchlistStocks(): Flow<List<WatchlistEntity>>

    @Query("SELECT symbol FROM watchlist")
    suspend fun getAllWatchlistSymbols(): List<String>

    @Query("SELECT * FROM watchlist WHERE symbol = :symbol")
    suspend fun getStockBySymbol(symbol: String): WatchlistEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE symbol = :symbol)")
    fun isStockWatched(symbol: String): Flow<Boolean>

    @Query("DELETE FROM watchlist")
    suspend fun clearWatchlist()
}