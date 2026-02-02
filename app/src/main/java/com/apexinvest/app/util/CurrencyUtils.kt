package com.apexinvest.app.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.Locale

suspend fun fetchRealTimeUsdToInrRate(): Double {
    return withContext(Dispatchers.IO) {
        try {
            val jsonString = URL("https://api.frankfurter.app/latest?from=USD&to=INR").readText()
            val jsonObject = JSONObject(jsonString)
            jsonObject.getJSONObject("rates").getDouble("INR")
        } catch (e: Exception) {
            e.printStackTrace()
            84.0
        }
    }
}

fun Double.toCleanString(): String {
    return String.format(Locale.US, "%,.2f", this)
}

fun isIndianStock(symbol: String): Boolean {
    val s = symbol.uppercase(Locale.getDefault())
    return s.endsWith(".NS") || s.endsWith(".BO")
}

fun getConvertedValue(value: Double, symbol: String, targetIsUsd: Boolean, rate: Double): Double {
    if (rate == 0.0) return 0.0
    val stockIsInr = isIndianStock(symbol)
    return when {
        targetIsUsd -> if (stockIsInr) value / rate else value
        else -> if (stockIsInr) value else value * rate
    }
}
// Add this to CurrencyUtils.kt
// Add this to CurrencyUtils.kt

fun getCurrencySymbol(symbol: String): String {
    // Check if the stock symbol ends with Indian exchange suffixes
    val s = symbol.uppercase()
    return if (s.endsWith(".NS") || s.endsWith(".BO")) "₹" else "$"
}