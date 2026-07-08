package com.apexinvest.app.api

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path


@Keep
data class ExchangeRateResponse(
    @SerializedName("rates") val rates: Map<String, Double>
)

interface CurrencyApiService {
    @GET("latest/{base}")
    suspend fun getExchangeRates(@Path("base") base: String): ExchangeRateResponse
}