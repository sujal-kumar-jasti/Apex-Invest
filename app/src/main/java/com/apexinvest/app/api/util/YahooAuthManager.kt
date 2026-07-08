package com.apexinvest.app.api.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object YahooAuthManager {
    private const val TAG = "YahooAuthManager"
    private var cachedCrumb: String? = null
    private val mutex = Mutex()

    // Memory CookieJar
    private val cookieJar = object : CookieJar {
        private val cookieStore = mutableMapOf<String, List<Cookie>>()

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            cookieStore[url.host] = cookies
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return cookieStore[url.host] ?: emptyList()
        }
    }

    private val authClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                .build()
            chain.proceed(request)
        }
        .build()

    suspend fun getCrumb(): String? = mutex.withLock {
        if (cachedCrumb != null) return cachedCrumb

        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Step 1: Fetching session cookies...")
                // Visit a quote page to get cookies (e.g., NVDA)
                val initRequest = Request.Builder()
                    .url("https://finance.yahoo.com/quote/NVDA")
                    .build()
                
                authClient.newCall(initRequest).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e(TAG, "Failed to get cookies: ${response.code}")
                    }
                }

                Log.d(TAG, "Step 2: Fetching crumb...")
                val crumbRequest = Request.Builder()
                    .url("https://query1.finance.yahoo.com/v1/test/getcrumb")
                    .build()

                authClient.newCall(crumbRequest).execute().use { response ->
                    if (response.isSuccessful) {
                        val crumb = response.body.string().trim()
                        if (crumb.isNotBlank()) {
                            Log.d(TAG, "Successfully retrieved crumb: $crumb")
                            cachedCrumb = crumb
                            return@withContext crumb
                        }
                    } else {
                        Log.e(TAG, "Failed to get crumb: ${response.code} ${response.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching Yahoo crumb", e)
            }
            null
        }
    }

    /**
     * Non-blocking getter for crumb.
     */
    fun getCrumbAsync(): String? = cachedCrumb

    fun getCookieJar(): CookieJar = cookieJar
}
