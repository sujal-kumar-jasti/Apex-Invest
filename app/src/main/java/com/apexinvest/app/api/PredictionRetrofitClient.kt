package com.apexinvest.app.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object PredictionRetrofitClient {

    // Your Hugging Face Space URL
    private const val BASE_URL ="https://jsujalkumar7899-prognosai-fastapi-backend-1.hf.space/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        // --- TIMEOUT UPDATES START ---
        // Sets the time allowed to establish a connection to the server
        .connectTimeout(5, TimeUnit.MINUTES)
        // Sets the time allowed to wait for the backend to process and return data
        .readTimeout(10, TimeUnit.MINUTES)
        // Sets the time allowed to send data to the server
        .writeTimeout(5, TimeUnit.MINUTES)
        // --- TIMEOUT UPDATES END ---
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Publicly exposed service instance
    val predictionApiService: PredictionApiService by lazy {
        retrofit.create(PredictionApiService::class.java)
    }
}