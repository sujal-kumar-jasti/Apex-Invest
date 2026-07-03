package com.apexinvest.app.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.apexinvest.app.data.NotificationEntity
import com.apexinvest.app.data.StockEntity
import com.apexinvest.app.data.TransactionDao
import com.apexinvest.app.data.TransactionEntity
import com.apexinvest.app.data.WatchlistEntity

@Database(
    entities = [
        StockEntity::class,
        WatchlistEntity::class,
        TransactionEntity::class,
        ExploreCacheEntity::class,
        StockStaticEntity::class,
        StockCacheEntity::class,
        AnalysisCacheEntity::class,
        NotificationEntity::class,
    ],
    version = 10, // 🆕 Bumped version for extended hours fields in StockEntity and StockCacheEntity
    exportSchema = false
)
@TypeConverters(AppConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun portfolioDao(): PortfolioDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun transactionDao(): TransactionDao
    abstract fun exploreDao(): ExploreDao
    abstract fun stockStaticDao(): StockStaticDao
    abstract fun stockCacheDao(): StockCacheDao
    abstract fun analysisCacheDao(): AnalysisCacheDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "apex_invest_db"
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}