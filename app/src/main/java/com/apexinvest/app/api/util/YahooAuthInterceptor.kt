package com.apexinvest.app.api.util

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class YahooAuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url
        
        // Yahoo queries only
        if (!url.host.contains("yahoo.com")) {
            return chain.proceed(originalRequest)
        }

        // Skip blocking crumb fetch for chart v8 requests
        val isChartRequest = url.encodedPath.contains("v8/finance/chart")
        
        // Get crumb
        val crumb = if (isChartRequest) {
            // Check if we already have it, but don't block for it
            YahooAuthManager.getCrumbAsync()
        } else {
            runBlocking { YahooAuthManager.getCrumb() }
        }

        val newUrl = if (crumb != null) {
            url.newBuilder()
                .addQueryParameter("crumb", crumb)
                .build()
        } else {
            url
        }

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
            .header("Accept", "application/json")
            .build()

        return chain.proceed(newRequest)
    }
}
