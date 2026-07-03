package com.apexinvest.app.db

import android.content.Context
import androidx.room.Database
<<<<<<< HEAD
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.apexinvest.app.data.NotificationEntity
=======
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
import com.apexinvest.app.data.StockEntity
import com.apexinvest.app.data.TransactionDao
import com.apexinvest.app.data.TransactionEntity
import com.apexinvest.app.data.WatchlistEntity

@Database(
    entities = [
        StockEntity::class,
        WatchlistEntity::class,
        TransactionEntity::class,
<<<<<<< HEAD
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
=======
        ExploreCacheEntity::class // Added Cache Entity
    ],
    version = 3, // Version bumped
    exportSchema = false
)
@TypeConverters(ExploreConverters::class) // Added Converters
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
abstract class AppDatabase : RoomDatabase() {

    abstract fun portfolioDao(): PortfolioDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun transactionDao(): TransactionDao
<<<<<<< HEAD
    abstract fun exploreDao(): ExploreDao
    abstract fun stockStaticDao(): StockStaticDao
    abstract fun stockCacheDao(): StockCacheDao
    abstract fun analysisCacheDao(): AnalysisCacheDao
    abstract fun notificationDao(): NotificationDao
=======
    abstract fun exploreDao(): ExploreDao // Added Explore DAO
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
<<<<<<< HEAD
                val instance = databaseBuilder(
=======
                val instance = Room.databaseBuilder(
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
                    context.applicationContext,
                    AppDatabase::class.java,
                    "apex_invest_db"
                )
<<<<<<< HEAD
                    .fallbackToDestructiveMigration(false)
=======
                    .fallbackToDestructiveMigration()
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}