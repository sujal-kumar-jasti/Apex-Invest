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

<<<<<<< HEAD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStocks(stocks: List<StockEntity>)

=======
    // Inserts or replaces an existing stock based on the 'symbol' (PrimaryKey)
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStock(stock: StockEntity)

    @Delete
    suspend fun deleteStock(stock: StockEntity)

<<<<<<< HEAD
=======
    // Fetches all stocks and returns a flow to allow Compose to react to changes in real-time
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
    @Query("SELECT * FROM portfolio ORDER BY symbol ASC")
    fun getAllStocks(): Flow<List<StockEntity>>

    @Query("SELECT * FROM portfolio WHERE symbol = :symbol")
    suspend fun getStockBySymbol(symbol: String): StockEntity?

    @Query("DELETE FROM portfolio")
    suspend fun clearPortfolio()
<<<<<<< HEAD

    @Query("SELECT COUNT(*) FROM portfolio")
    suspend fun getPortfolioSize(): Int
}
=======
}
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
