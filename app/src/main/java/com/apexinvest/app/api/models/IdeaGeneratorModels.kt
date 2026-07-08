package com.apexinvest.app.api.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class PortfolioRequest(
    @SerializedName("summary") val summary: String
)

@Keep
data class ThemeRequest(
    @SerializedName("theme") val theme: String
)

@Keep
data class IdeaResponse(
    @SerializedName("response_text") val responseText: String
)