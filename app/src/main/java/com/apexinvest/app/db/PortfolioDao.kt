package com.apexinvest.app.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.apexinvest.app.data.StockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PortfolioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStocks(stocks: List<StockEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStock(stock: StockEntity)

    @Delete
    suspend fun deleteStock(stock: StockEntity)

    @Query("SELECT * FROM portfolio ORDER BY symbol ASC")
    fun getAllStocks(): Flow<List<StockEntity>>

    @Query("SELECT * FROM portfolio WHERE symbol = :symbol")
    suspend fun getStockBySymbol(symbol: String): StockEntity?

    @Query("DELETE FROM portfolio")
    suspend fun clearPortfolio()

    @Query("SELECT COUNT(*) FROM portfolio")
    suspend fun getPortfolioSize(): Int
}