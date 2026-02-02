package com.apexinvest.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// State to define the application's authentication status
sealed class AuthState {
    object Loading : AuthState()
    object LoggedIn : AuthState()
    object LoggedOut : AuthState()
    data class Error(val message: String) : AuthState()
    // State for mid-phone verification process
    data class PhoneVerification(val verificationId: String, val phoneNumber: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    // --- Dependencies ---
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // --- Auth State Management ---
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: MutableStateFlow<AuthState> = _authState

    // --- User ID Flow (CRITICAL FIX FOR EAGER DATA LOADING) ---
    private val _userIdFlow = MutableStateFlow<String?>(auth.currentUser?.uid)
    val userIdFlow: StateFlow<String?> = _userIdFlow.asStateFlow()

    // --- Initialization & Listener ---
    init {
        // Global listener that updates states when user signs in or out
        auth.addAuthStateListener { firebaseAuth ->
            val currentUser = firebaseAuth.currentUser
            _userIdFlow.value = currentUser?.uid // Update the observable UID

            if (currentUser != null) {
                // Only set LoggedIn state if we aren't interrupting a phone verification process
                if (_authState.value !is AuthState.PhoneVerification) {
                    _authState.value = AuthState.LoggedIn
                }
            } else {
                _authState.value = AuthState.LoggedOut
            }
        }
    }

    // --- Utility Functions ---

    private fun createFirestoreProfile() {
        val user = auth.currentUser ?: return
        val userData = hashMapOf(
            "email" to (user.email ?: "phone_user"),
            "uid" to user.uid,
            "createdAt" to com.google.firebase.Timestamp.now(),
            "lastLogin" to com.google.firebase.Timestamp.now()
        )
        firestore.collection("users").document(user.uid)
            .set(userData)
            .addOnFailureListener { e ->
                println("Error creating user profile in Firestore: $e")
            }
    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }

    fun setPhoneVerificationState(verificationId: String, phoneNumber: String) {
        _authState.value = AuthState.PhoneVerification(verificationId, phoneNumber)
    }

    // --- Authentication Logic ---

    fun signInWithEmail(email: String, password: String) = viewModelScope.launch {
        // CRITICAL FIX: Local Validation Check (Prevents crash on empty input)
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password fields cannot be empty.")
            return@launch
        }

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    _authState.value = AuthState.Error(task.exception?.localizedMessage ?: "Sign-in failed.")
                }
                // AuthStateListener handles success state change
            }
    }

    fun signUpWithEmail(email: String, password: String) = viewModelScope.launch {
        // CRITICAL FIX: Local Validation Check (Prevents crash on empty input)
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password fields cannot be empty.")
            return@launch
        }

        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    createFirestoreProfile()
                    // Listener handles state change
                } else {
                    _authState.value = AuthState.Error(task.exception?.localizedMessage ?: "Sign-up failed.")
                }
            }
    }

    fun handleGoogleSignInResult(idToken: String) = viewModelScope.launch {
        _authState.value = AuthState.Loading
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    createFirestoreProfile()
                } else {
                    _authState.value = AuthState.Error(task.exception?.localizedMessage ?: "Google Sign-in failed.")
                }
            }
    }

    fun signInWithPhoneCredential(credential: PhoneAuthCredential) = viewModelScope.launch {
        _authState.value = AuthState.Loading
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    createFirestoreProfile()
                } else {
                    _authState.value = AuthState.Error(task.exception?.localizedMessage ?: "Phone sign-in failed.")
                }
            }
    }
}