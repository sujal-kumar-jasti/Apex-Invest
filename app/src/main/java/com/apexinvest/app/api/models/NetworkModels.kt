package com.apexinvest.app.api.models


data class PythonStockResponse(
    val symbol: String,
    val price: Double,
    val change: Double,
    val changePercent: Double,
    val prevClose: Double,
    val open: Double,
    val dayHigh: Double,
    val dayLow: Double,
    val marketCap: String,
    val peRatio: String,
    val dividendYield: String,
    val yearHigh: String,
    val yearLow: String,
    val historyPoints: List<List<Any>>
)