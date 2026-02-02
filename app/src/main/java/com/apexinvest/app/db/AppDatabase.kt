package com.apexinvest.app.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.apexinvest.app.data.StockEntity
import com.apexinvest.app.data.TransactionDao
import com.apexinvest.app.data.TransactionEntity
import com.apexinvest.app.data.WatchlistEntity

@Database(
    entities = [
        StockEntity::class,
        WatchlistEntity::class,
        TransactionEntity::class,
        ExploreCacheEntity::class // Added Cache Entity
    ],
    version = 3, // Version bumped
    exportSchema = false
)
@TypeConverters(ExploreConverters::class) // Added Converters
abstract class AppDatabase : RoomDatabase() {

    abstract fun portfolioDao(): PortfolioDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun transactionDao(): TransactionDao
    abstract fun exploreDao(): ExploreDao // Added Explore DAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "apex_invest_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}