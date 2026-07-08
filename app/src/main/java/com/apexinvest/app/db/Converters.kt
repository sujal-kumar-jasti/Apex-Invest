package com.apexinvest.app.db

import androidx.room.TypeConverter
import com.apexinvest.app.data.TransactionType

class AppConverters {
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = enumValueOf<TransactionType>(value)

}