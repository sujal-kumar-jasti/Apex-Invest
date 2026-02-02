package com.apexinvest.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    // Record a new trade
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    // Get history for a specific stock (Ordered by latest first)
    @Query("SELECT * FROM transactions WHERE symbol = :symbol ORDER BY timestamp DESC")
    fun getTransactionsForStock(symbol: String): Flow<List<TransactionEntity>>

    // Get ALL history (For a global 'Orders' tab)
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    // Calculate Total Invested Amount for a stock (BUYs only)
    // Professional feature: This helps calculate Average Price accurately on the DB side
    @Query("SELECT SUM(quantity * price) FROM transactions WHERE symbol = :symbol AND type = 'BUY'")
    suspend fun getTotalInvestedForStock(symbol: String): Double?

    // Calculate Total Quantity Bought
    @Query("SELECT SUM(quantity) FROM transactions WHERE symbol = :symbol AND type = 'BUY'")
    suspend fun getTotalQtyBought(symbol: String): Int?

    // Delete a specific transaction (e.g., if user made a mistake)
    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
}