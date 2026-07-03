package com.apexinvest.app.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
<<<<<<< HEAD
import retrofit2.http.Path


=======

// Data Model for the API Response
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
data class ExchangeRateResponse(
    @SerializedName("rates") val rates: Map<String, Double>
)

interface CurrencyApiService {
<<<<<<< HEAD
    @GET("latest/{base}")
    suspend fun getExchangeRates(@Path("base") base: String): ExchangeRateResponse
=======
    // We fetch rates relative to USD.
    // Example response: {"rates": {"INR": 83.5, "EUR": 0.92, ...}}
    @GET("latest/USD")
    suspend fun getUsdRates(): ExchangeRateResponse
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
}