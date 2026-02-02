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

    // Inserts or replaces an existing stock based on the 'symbol' (PrimaryKey)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStock(stock: StockEntity)

    @Delete
    suspend fun deleteStock(stock: StockEntity)

    // Fetches all stocks and returns a flow to allow Compose to react to changes in real-time
    @Query("SELECT * FROM portfolio ORDER BY symbol ASC")
    fun getAllStocks(): Flow<List<StockEntity>>

    @Query("SELECT * FROM portfolio WHERE symbol = :symbol")
    suspend fun getStockBySymbol(symbol: String): StockEntity?

    @Query("DELETE FROM portfolio")
    suspend fun clearPortfolio()
}
