package com.apexinvest.app.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.TypeConverter
import com.apexinvest.app.data.model.TrendingStockDto
import com.apexinvest.app.viewmodel.CommodityUiModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow

// --- ENTITY ---
@Entity(tableName = "explore_cache")
data class ExploreCacheEntity(
    @PrimaryKey val id: Int = 0, // Singleton row
    val indices: List<CommodityUiModel>,
    val trendingStocks: List<TrendingStockDto>,
    val commodities: List<CommodityUiModel>,
    val globalIndices: List<CommodityUiModel>,
    val lastUpdated: Long = System.currentTimeMillis()
)

// --- DAO ---
@Dao
interface ExploreDao {
    @Query("SELECT * FROM explore_cache WHERE id = 0")
    fun getExploreData(): Flow<ExploreCacheEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertData(data: ExploreCacheEntity)

    @Query("DELETE FROM explore_cache")
    suspend fun clearCache()
}

// --- CONVERTERS ---
class ExploreConverters {
    private val gson = Gson()

    // Commodity List
    @TypeConverter
    fun fromCommodityList(list: List<CommodityUiModel>?): String {
        return gson.toJson(list ?: emptyList<CommodityUiModel>())
    }

    @TypeConverter
    fun toCommodityList(data: String?): List<CommodityUiModel> {
        if (data.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<CommodityUiModel>>() {}.type
        return gson.fromJson(data, type)
    }

    // Trending Stock List
    @TypeConverter
    fun fromTrendingList(list: List<TrendingStockDto>?): String {
        return gson.toJson(list ?: emptyList<TrendingStockDto>())
    }

    @TypeConverter
    fun toTrendingList(data: String?): List<TrendingStockDto> {
        if (data.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<TrendingStockDto>>() {}.type
        return gson.fromJson(data, type)
    }
}