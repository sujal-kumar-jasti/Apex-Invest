package com.apexinvest.app.api.models

import com.google.gson.annotations.SerializedName


data class AuthRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class VerifyOtpRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("otp") val otp: String
)

data class AuthResponse(
    @SerializedName("token") val token: String? = null,
    @SerializedName("message") val message: String
)

data class ForgotPasswordRequest(
    @SerializedName("email") val email: String
)

data class ResetPasswordRequest(
    @SerializedName("email") val email: String,
    @SerializedName("otp") val otp: String,
    @SerializedName("newPassword") val newPassword: String
)

data class GoogleAuthRequest(
    @SerializedName("idToken") val idToken: String
)

data class ChangePasswordRequest(
    @SerializedName("email") val email: String, // Kept for DTO consistency, though backend extracts user from Token
    @SerializedName("oldPassword") val oldPassword: String,
    @SerializedName("newPassword") val newPassword: String
)

// --- 2. CLOUD SYNC & DATA MODELS ---

data class PortfolioItem(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("quantity") val quantity: Double,
    @SerializedName("averageBuyPrice") val averageBuyPrice: Double,
    @SerializedName("lastUpdated") val lastUpdated: String
)

data class WatchlistItem(
    @SerializedName("symbol") val symbol: String
)

data class TransactionItem(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("type") val type: String, // "BUY" or "SELL"
    @SerializedName("quantity") val quantity: Double,
    @SerializedName("price") val price: Double,
    @SerializedName("fees") val fees: Double = 0.0,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("timestamp") val timestamp: Long
)

data class SyncResponse(
    @SerializedName("portfolio") val portfolio: List<PortfolioItem>,
    @SerializedName("watchlist") val watchlist: List<WatchlistItem>,
    @SerializedName("transactions") val transactions: List<TransactionItem>
)