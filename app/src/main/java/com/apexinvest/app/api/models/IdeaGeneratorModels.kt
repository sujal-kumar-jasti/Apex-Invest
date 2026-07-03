package com.apexinvest.app.api.models

import com.google.gson.annotations.SerializedName

data class PortfolioRequest(
    @SerializedName("summary") val summary: String
)


data class ThemeRequest(
    @SerializedName("theme") val theme: String
)


data class IdeaResponse(
    @SerializedName("response_text") val responseText: String
)