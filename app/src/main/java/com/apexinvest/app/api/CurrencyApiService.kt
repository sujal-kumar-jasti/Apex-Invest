package com.apexinvest.app.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET

// Data Model for the API Response
data class ExchangeRateResponse(
    @SerializedName("rates") val rates: Map<String, Double>
)

interface CurrencyApiService {
    // We fetch rates relative to USD.
    // Example response: {"rates": {"INR": 83.5, "EUR": 0.92, ...}}
    @GET("latest/USD")
    suspend fun getUsdRates(): ExchangeRateResponse
}