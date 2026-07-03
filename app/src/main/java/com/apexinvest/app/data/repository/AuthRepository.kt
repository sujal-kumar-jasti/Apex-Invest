package com.apexinvest.app.data.repository

import android.util.Base64
import android.util.Log
import com.apexinvest.app.api.ApexAuthApiService
import com.apexinvest.app.api.models.AuthRequest
import com.apexinvest.app.api.models.ChangePasswordRequest
import com.apexinvest.app.api.models.ForgotPasswordRequest
import com.apexinvest.app.api.models.GoogleAuthRequest
import com.apexinvest.app.api.models.ResetPasswordRequest
import com.apexinvest.app.api.models.VerifyOtpRequest
import com.apexinvest.app.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class AuthRepository(
    private val apiService: ApexAuthApiService,
    private val sessionManager: SessionManager
    // Removed DAO dependencies here since PortfolioRepository handles DB operations
) {

    // --- JWT DECODER ---
    private fun extractEmailFromToken(jwtToken: String): String {
        try {
            val split = jwtToken.split(".")
            if (split.size > 1) {
                val decodedBytes = Base64.decode(split[1], Base64.URL_SAFE)
                val jsonObject = JSONObject(String(decodedBytes, Charsets.UTF_8))
                return jsonObject.optString("email", "google_user")
            }
        } catch (e: Exception) {
            Log.e("AuthRepo", "Failed to decode JWT: ${e.message}")
        }
        return "google_user"
    }

    // --- 1. GOOGLE AUTH ---

    suspend fun loginWithGoogle(idToken: String): Result<String> = withContext(Dispatchers.IO) {
        Log.d("AuthRepo", "Attempting Google Login...")
        try {
            val response = apiService.googleLogin(GoogleAuthRequest(idToken))
            if (response.isSuccessful && response.body()?.token != null) {
                val token = response.body()!!.token!!
                val realEmail = extractEmailFromToken(token)
                sessionManager.saveAuthToken(token, realEmail, isGoogle = true)

                // FIX: Removed performFullCloudSync(). PortfolioViewModel handles this now.
                Result.success("Google login successful")
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Google login failed"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- 2. CHANGE PASSWORD ---

    suspend fun changePassword(oldPw: String, newPw: String): Result<String> = withContext(Dispatchers.IO) {
        val token = sessionManager.getAuthToken() ?: return@withContext Result.failure(Exception("Not logged in"))
        val email = sessionManager.getUserEmail() ?: ""

        try {
            val response = apiService.changePassword(
                token = "Bearer $token",
                request = ChangePasswordRequest(email, oldPw, newPw)
            )
            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "Password updated")
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Update failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- REGISTER & OTP ---

    suspend fun register(email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.registerUser(AuthRequest(email, password))
            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "OTP sent to your email")
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyOtp(email: String, password: String, otp: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.verifyOtp(VerifyOtpRequest(email, password, otp))
            if (response.isSuccessful && response.body()?.token != null) {
                val token = response.body()!!.token!!
                sessionManager.saveAuthToken(token, email, isGoogle = false)

                // FIX: Removed performFullCloudSync(). PortfolioViewModel handles this now.
                Result.success("Verification successful")
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Invalid OTP"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- LOGIN ---

    suspend fun login(email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.loginUser(AuthRequest(email, password))
            if (response.isSuccessful && response.body()?.token != null) {
                val token = response.body()!!.token!!
                sessionManager.saveAuthToken(token, email, isGoogle = false)

                // FIX: Removed performFullCloudSync(). PortfolioViewModel handles this now.
                Result.success("Login successful")
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Login failed"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- PASSWORD RESET FLOW ---

    suspend fun requestPasswordResetOtp(email: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.forgotPassword(ForgotPasswordRequest(email))
            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "OTP sent to your email")
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Request failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun finalizePasswordReset(email: String, otp: String, newPw: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.resetPassword(ResetPasswordRequest(email, otp, newPw))
            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "Password updated successfully")
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Reset failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- SESSION & CLEANUP ---

    suspend fun logout() = withContext(Dispatchers.IO) {
        Log.d("AuthRepo", "Logging out and clearing session...")
        try {
            // FIX: Removed DAO clears. AuthViewModel already calls portfolioViewModel.clearAllData() before this.
            sessionManager.clearSession()
        } catch (e: Exception) {
            Log.e("AuthRepo", "Error during logout cleanup: ${e.message}")
        }
    }

    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()

    fun getEmail(): String? = sessionManager.getUserEmail()

    fun isGoogleUserAccount(): Boolean {
        return sessionManager.isGoogleUser()
    }
}