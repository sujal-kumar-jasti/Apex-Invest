package com.apexinvest.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE symbol = :symbol ORDER BY timestamp DESC")
    fun getTransactionsForStock(symbol: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(quantity * price) FROM transactions WHERE symbol = :symbol AND type = 'BUY'")
    suspend fun getTotalInvestedForStock(symbol: String): Double?

    @Query("""
        SELECT 
            (SELECT TOTAL(quantity) FROM transactions WHERE symbol = :symbol AND type = 'BUY') - 
            (SELECT TOTAL(quantity) FROM transactions WHERE symbol = :symbol AND type = 'SELL')
    """)
    suspend fun getCurrentHoldingQuantity(symbol: String): Double


    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)


    @Delete
    suspend fun deleteTransactions(transactions: List<TransactionEntity>)


    @Query("DELETE FROM transactions")
    suspend fun clearAllTransactions()
}