package com.apexinvest.app.util

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
    
    // 🚀 FIX: Reliable detection via exchange metadata
    if (clean.contains(".") || exchangeInfo.currency != "USD") {
        return exchangeInfo.currency
    }

    // 3. Fallback for common patterns if suffix is missing
    return when {
        clean.endsWith(".NS") || clean.endsWith(".BO") -> "INR"
        else -> "USD"
    }
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
    // 🚀 FIX: Prioritize exchange metadata for the most accurate source currency
    val sourceCurrency = sourceCurrencyOverride ?: run {
        val info = StockMetadataUtils.getExchangeInfo(symbol)
        if (info.currency != "USD" || symbol.contains(".")) {
            info.currency
        } else {
            guessCurrencyFromSymbol(symbol)
        }
    }
    val targetCurrency = if (targetIsUsd) "USD" else "INR"
    
    if (sourceCurrency.equals(targetCurrency, ignoreCase = true)) return value
    
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

fun formatPrice(price: Double, symbol: String): String {
    val currencyCode = guessCurrencyFromSymbol(symbol)
    val currencySym = getCurrencySymbol(currencyCode)
    return "$currencySym${String.format(Locale.US, "%.2f", price)}"
}

fun Double.toCleanString(): String {
    val s = String.format(Locale.US, "%.2f", this)
    return if (s.endsWith(".00")) s.substringBefore(".") else s
}
