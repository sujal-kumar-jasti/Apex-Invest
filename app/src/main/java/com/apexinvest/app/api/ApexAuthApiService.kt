package com.apexinvest.app.api

import com.apexinvest.app.api.models.AuthRequest
import com.apexinvest.app.api.models.AuthResponse
import com.apexinvest.app.api.models.ChangePasswordRequest
import com.apexinvest.app.api.models.ForgotPasswordRequest
import com.apexinvest.app.api.models.GoogleAuthRequest
import com.apexinvest.app.api.models.ResetPasswordRequest
import com.apexinvest.app.api.models.SyncResponse
import com.apexinvest.app.api.models.TransactionItem
import com.apexinvest.app.api.models.VerifyOtpRequest
import com.apexinvest.app.api.models.WatchlistItem
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ApexAuthApiService {

    // --- 1. PUBLIC AUTHENTICATION ROUTES ---

    @POST("/auth/register")
    suspend fun registerUser(@Body request: AuthRequest): Response<AuthResponse>

    @POST("/auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<AuthResponse>

    @POST("/auth/login")
    suspend fun loginUser(@Body request: AuthRequest): Response<AuthResponse>

    @POST("/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<AuthResponse>

    @POST("/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<AuthResponse>

    // 🌟 NEW: Google Sign-In Endpoint
    @POST("/auth/google")
    suspend fun googleLogin(@Body request: GoogleAuthRequest): Response<AuthResponse>

    // --- 2. PROTECTED USER ROUTES (Requires JWT Token) ---

    // 🌟 NEW: Change Password (Old Password -> New Password)
    @POST("/user/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String, // Pass as "Bearer $token"
        @Body request: ChangePasswordRequest
    ): Response<AuthResponse>

    @GET("/user/sync")
    suspend fun syncUserData(
        @Header("Authorization") token: String
    ): Response<SyncResponse>

    @POST("/user/trade")
    suspend fun recordCloudTrade(
        @Header("Authorization") token: String,
        @Body trade: TransactionItem
    ): Response<AuthResponse>

    // Add Watchlist Item
    @POST("/user/watchlist")
    suspend fun updateCloudWatchlist(
        @Header("Authorization") token: String,
        @Body watchlistItem: WatchlistItem
    ): Response<Unit>

    // 🌟 NEW: Remove Watchlist Item
    @POST("/user/watchlist/remove")
    suspend fun deleteFromCloudWatchlist(
        @Header("Authorization") token: String,
        @Body watchlistItem: WatchlistItem
    ): Response<Unit>

    // 🌟 NEW: Delete User Account (Full Wipe)
    @DELETE("/user/account")
    suspend fun deleteUserAccount(
        @Header("Authorization") token: String
    ): Response<Unit>

    // 🌟 UPDATED: Changed transactionId from Int to String to match backend UUIDs
    @DELETE("/user/transactions/{id}")
    suspend fun deleteCloudTransaction(
        @Header("Authorization") token: String,
        @Path("id") transactionId: String
    ): Response<Unit>
}