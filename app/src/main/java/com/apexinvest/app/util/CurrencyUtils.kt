package com.apexinvest.app.util

<<<<<<< HEAD
import java.util.Locale

/**
 * Guesses the native currency of a stock based on its ticker symbol.
 */
fun guessCurrencyFromSymbol(rawSymbol: String): String {
    val clean = rawSymbol.uppercase().trim()
    
    // 1. Check for specific common index symbols first
    when {
        clean.startsWith("^NSE") || clean == "NIFTY 50" || clean == "NIFTY_50" -> return "INR"
        clean.startsWith("^BSESN") || clean == "SENSEX" -> return "INR"
        clean.startsWith("^GSPC") || clean.startsWith("^IXIC") || clean.startsWith("^DJI") -> return "USD"
        clean.startsWith("GC=F") || clean.startsWith("SI=F") -> return "USD"
        clean.startsWith("^N225") -> return "JPY"
        clean.startsWith("^HSI") -> return "HKD"
        clean.startsWith("^FTSE") -> return "GBP"
        clean.startsWith("^GDAXI") -> return "EUR"
        clean.startsWith("^FCHI") -> return "EUR"
        clean.startsWith("^STOXX50E") -> return "EUR"
    }
    
    // 2. Use StockMetadataUtils to decode based on suffix
    val exchangeInfo = StockMetadataUtils.getExchangeInfo(clean)
    
    // 🚀 FIX: If getExchangeInfo returns a currency other than the default fallback USD, use it.
    // However, we need to be careful if it falls back to USD for unknown suffixes.
    if (clean.contains(".")) {
        return exchangeInfo.currency
    }

    // Default for US stocks (no dot)
    return "USD"
}

/**
 * Returns the currency symbol for a given currency code.
 */
fun getCurrencySymbol(currency: String?): String = when (currency?.uppercase()) {
    "INR" -> "₹"
    "USD" -> "$"
    "GBP" -> "£"
    "EUR" -> "€"
    "JPY", "CNY" -> "¥"
    "HKD" -> "HK$"
    "CAD", "AUD", "SGD" -> "$"
    "CHF" -> "Fr"
    "BRL" -> "R$"
    "KRW" -> "₩"
    "SAR" -> "ر.س"
    else -> "$"
}

/**
 * Converts a value from its source currency to the target currency (USD or INR).
 * 
 * @param value The value in its source currency.
 * @param symbol The stock ticker symbol to determine source currency if [sourceCurrencyOverride] is null.
 * @param targetIsUsd Whether the target currency is USD.
 * @param rates A map of currency rates relative to USD (USD=1.0).
 * @param sourceCurrencyOverride Optional explicit source currency code.
 */
fun getConvertedValue(
    value: Double,
    symbol: String,
    targetIsUsd: Boolean,
    rates: Map<String, Double>,
    sourceCurrencyOverride: String? = null
): Double {
    val sourceCurrency = sourceCurrencyOverride ?: guessCurrencyFromSymbol(symbol)
    val targetCurrency = if (targetIsUsd) "USD" else "INR"
    
    if (sourceCurrency.uppercase() == targetCurrency.uppercase()) return value
    
    // 1. Convert source to USD
    val valueInUsd = if (sourceCurrency.uppercase() == "USD") {
        value
    } else {
        val sourceRateToUsd = rates[sourceCurrency.uppercase()]
        if (sourceRateToUsd == null || sourceRateToUsd <= 0.0) return value
        value / sourceRateToUsd
    }
    
    // 2. Convert USD to target
    return if (targetCurrency == "USD") {
        valueInUsd
    } else {
        val targetRateFromUsd = rates[targetCurrency] ?: 1.0
        if (targetRateFromUsd <= 0.0) valueInUsd else valueInUsd * targetRateFromUsd
    }
}

/**
 * Legacy support for single rate conversion (USD -> INR).
 */
fun getConvertedValue(value: Double, symbol: String, isUsd: Boolean, rate: Double): Double {
    val sourceCurrency = guessCurrencyFromSymbol(symbol)
    return if (isUsd) {
        if (sourceCurrency == "INR") value / rate else value
    } else {
        if (sourceCurrency == "INR") value else value * rate
    }
}

fun formatPrice(price: Double, symbol: String): String {
    val currencyCode = guessCurrencyFromSymbol(symbol)
    val currencySym = getCurrencySymbol(currencyCode)
    return "$currencySym${String.format(Locale.US, "%.2f", price)}"
}

fun Double.toCleanString(): String {
    val s = String.format(Locale.US, "%.2f", this)
    return if (s.endsWith(".00")) s.substringBefore(".") else s
}
=======
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
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
