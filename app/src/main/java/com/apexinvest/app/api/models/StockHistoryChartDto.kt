package com.apexinvest.app.api.models

import com.google.gson.annotations.SerializedName

data class StockHistoryChartDto(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("range") val range: String,
    @SerializedName("candles") val candles: List<CandlePointDto>
)