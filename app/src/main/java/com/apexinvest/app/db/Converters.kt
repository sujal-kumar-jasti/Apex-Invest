package com.apexinvest.app.db

import androidx.room.TypeConverter
import com.apexinvest.app.data.TransactionType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AppConverters {
    private val gson = Gson()

    // --- 1. Transaction Type Enum Converters ---
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = enumValueOf<TransactionType>(value)

    // --- 2. Explore Cache Converters (JSON to String) ---
    // If your ExploreCache stores a List of objects, Room needs this:
    @TypeConverter
    fun fromStringList(value: List<String>?): String = gson.toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String>? {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }
}