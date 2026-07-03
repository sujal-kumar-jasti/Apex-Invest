package com.apexinvest.app.api.models

import com.google.gson.annotations.SerializedName

data class CandlePointDto(
    @SerializedName("time") val time: String, // Formatted as "yyyy-MM-dd HH:mm" or "yyyy-MM-dd"
    @SerializedName("open") val open: Double,
    @SerializedName("high") val high: Double,
    @SerializedName("low") val low: Double,
    @SerializedName("close") val close: Double,
    @SerializedName("volume") val volume: Long
)