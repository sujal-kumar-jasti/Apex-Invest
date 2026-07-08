package com.apexinvest.app.api.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class StockHistoryChartDto(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("range") val range: String,
    @SerializedName("candles") val candles: List<CandlePointDto>
)