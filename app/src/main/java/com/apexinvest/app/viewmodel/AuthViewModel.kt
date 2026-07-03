package com.apexinvest.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apexinvest.app.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class AuthState {
    object Loading : AuthState()
    object LoggedIn : AuthState()
    object LoggedOut : AuthState()
    data class SuccessMessage(val message: String, val lastState: AuthState) : AuthState()
    data class Error(val message: String, val lastState: AuthState) : AuthState()
    data class OtpVerificationRequired(val email: String, val password: String) : AuthState()
    data class PasswordResetRequired(val email: String) : AuthState()
}

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val TAG = "AuthViewModel"
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState = _authState.asStateFlow()

    init { 
        viewModelScope.launch(Dispatchers.IO) {
            val loggedIn = authRepository.isLoggedIn()
            withContext(Dispatchers.Main) {
                _authState.value = if (loggedIn) AuthState.LoggedIn else AuthState.LoggedOut
            }
        }
    }
    
    fun checkSession() {
        viewModelScope.launch(Dispatchers.IO) {
            val loggedIn = authRepository.isLoggedIn()
            withContext(Dispatchers.Main) {
                _authState.value = if (loggedIn) AuthState.LoggedIn else AuthState.LoggedOut
            }
        }
    }

    fun signInWithGoogle(idToken: String) = viewModelScope.launch {
        _authState.value = AuthState.Loading
        authRepository.loginWithGoogle(idToken).fold(onSuccess = { _authState.value = AuthState.LoggedIn }, onFailure = { _authState.value = AuthState.Error(it.message ?: "Google Sign-In failed", AuthState.LoggedOut) })
    }

    fun updatePassword(old: String, new: String) = viewModelScope.launch {
        authRepository.changePassword(old, new).fold(onSuccess = { _authState.value = AuthState.SuccessMessage("Password updated!", _authState.value) }, onFailure = { _authState.value = AuthState.Error(it.message ?: "Update failed", _authState.value) })
    }

    fun signInWithEmail(e: String, p: String) = viewModelScope.launch {
        _authState.value = AuthState.Loading
        authRepository.login(e, p).fold(onSuccess = { _authState.value = AuthState.LoggedIn }, onFailure = { _authState.value = AuthState.Error(it.message ?: "Sign-In failed", AuthState.LoggedOut) })
    }

    fun signUpWithEmail(e: String, p: String) = viewModelScope.launch {
        _authState.value = AuthState.Loading
        authRepository.register(e, p).fold(onSuccess = { _authState.value = AuthState.OtpVerificationRequired(e, p) }, onFailure = { _authState.value = AuthState.Error(it.message ?: "Registration failed", AuthState.LoggedOut) })
    }

    fun requestPasswordReset(e: String) = viewModelScope.launch {
        _authState.value = AuthState.Loading
        authRepository.requestPasswordResetOtp(e).fold(onSuccess = { _authState.value = AuthState.PasswordResetRequired(e) }, onFailure = { _authState.value = AuthState.Error(it.message ?: "Request failed", AuthState.LoggedOut) })
    }

    fun submitOtp(otp: String, pass: String?) = viewModelScope.launch {
        val s = _authState.value; _authState.value = AuthState.Loading
        if (s is AuthState.OtpVerificationRequired) {
            authRepository.verifyOtp(s.email, s.password, otp).fold(onSuccess = { _authState.value = AuthState.LoggedIn }, onFailure = { _authState.value = AuthState.Error(it.message ?: "Verification failed", s) })
        } else if (s is AuthState.PasswordResetRequired) {
            authRepository.finalizePasswordReset(s.email, otp, pass ?: "").fold(onSuccess = { _authState.value = AuthState.LoggedOut }, onFailure = { _authState.value = AuthState.Error(it.message ?: "Reset failed", s) })
        }
    }

    fun performLogout(
        portfolioViewModel: PortfolioViewModel,
        predictionViewModel: PredictionViewModel,
        exploreViewModel: ExploreViewModel
    ) = viewModelScope.launch {
        _authState.value = AuthState.Loading
        try {
            withContext(Dispatchers.IO) {
                portfolioViewModel.clearAllData()
                predictionViewModel.clearAllData()
                exploreViewModel.clearAllData()
                authRepository.logout()
            }
        }
        catch (e: Exception) { Log.e(TAG, "Logout error: ${e.message}") } finally { _authState.value = AuthState.LoggedOut }
    }

    fun performDeleteAccount(
        portfolioViewModel: PortfolioViewModel,
        predictionViewModel: PredictionViewModel,
        exploreViewModel: ExploreViewModel
    ) = viewModelScope.launch {
        _authState.value = AuthState.Loading
        try {
            withContext(Dispatchers.IO) {
                portfolioViewModel.deleteUserAccount()
                predictionViewModel.clearAllData()
                exploreViewModel.clearAllData()
                authRepository.logout()
            }
        }
        catch (e: Exception) { Log.e(TAG, "Delete error: ${e.message}") } finally { _authState.value = AuthState.LoggedOut }
    }

    fun clearError() { val s = _authState.value; if (s is AuthState.Error) _authState.value = s.lastState; if (s is AuthState.SuccessMessage) _authState.value = s.lastState }
    fun showLocalValidationError(m: String) { _authState.value = AuthState.Error(m, _authState.value) }
    fun getUserEmail() = authRepository.getEmail()
    fun isGoogleUser() = authRepository.isGoogleUserAccount()

    fun resendRegistrationOtp(e: String) = viewModelScope.launch {
        authRepository.register(e, "").fold(onSuccess = { _authState.value = AuthState.SuccessMessage("New code sent!", _authState.value) }, onFailure = { _authState.value = AuthState.Error(it.message ?: "Send failed", _authState.value) })
    }
}
