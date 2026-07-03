package com.apexinvest.app.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.TypeConverter
<<<<<<< HEAD
import androidx.room.TypeConverters
import com.apexinvest.app.data.model.CommodityDto
import com.apexinvest.app.data.model.TrendingStockDto
=======
import com.apexinvest.app.data.model.TrendingStockDto
import com.apexinvest.app.viewmodel.CommodityUiModel
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow

// --- ENTITY ---
@Entity(tableName = "explore_cache")
<<<<<<< HEAD
@TypeConverters(ExploreConverters::class)
data class ExploreCacheEntity(
    @PrimaryKey val id: Int = 0, // Singleton row

    // Store Raw DTOs, NOT UI Models
    val indices: List<CommodityDto>,
    val trendingStocks: List<TrendingStockDto>,
    val commodities: List<CommodityDto>,
    val globalIndices: List<CommodityDto>,

    // Store the rate used at the time of fetch (Default fallback)
    val conversionRate: Double = 84.0,

=======
data class ExploreCacheEntity(
    @PrimaryKey val id: Int = 0, // Singleton row
    val indices: List<CommodityUiModel>,
    val trendingStocks: List<TrendingStockDto>,
    val commodities: List<CommodityUiModel>,
    val globalIndices: List<CommodityUiModel>,
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
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

<<<<<<< HEAD
    // Converter for CommodityDto (Raw Data)
    @TypeConverter
    fun fromCommodityList(list: List<CommodityDto>?): String {
        return gson.toJson(list ?: emptyList<CommodityDto>())
    }

    @TypeConverter
    fun toCommodityList(data: String?): List<CommodityDto> {
        if (data.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<CommodityDto>>() {}.type
=======
    // Commodity List
    @TypeConverter
    fun fromCommodityList(list: List<CommodityUiModel>?): String {
        return gson.toJson(list ?: emptyList<CommodityUiModel>())
    }

    @TypeConverter
    fun toCommodityList(data: String?): List<CommodityUiModel> {
        if (data.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<CommodityUiModel>>() {}.type
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
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