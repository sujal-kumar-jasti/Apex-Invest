package com.apexinvest.app.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.core.content.edit
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("apex_secure_session", Context.MODE_PRIVATE)
    @Volatile
    private var aead: Aead? = null
    private val aeadLock = Any()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AeadConfig.register()
                
                val keysetHandle = AndroidKeysetManager.Builder()
                    .withSharedPref(context, "tink_keyset", "apex_crypto_prefs")
                    .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
                    .withMasterKeyUri("android-keystore://apex_master_key")
                    .build()
                    .keysetHandle

                val primitive = keysetHandle.getPrimitive(RegistryConfiguration.get(), Aead::class.java)
                synchronized(aeadLock) {
                    aead = primitive
                }
                Log.d("SessionManager", "Secure Aead primitive initialized successfully.")
            } catch (e: Exception) {
                Log.e("SessionManager", "Failed to initialize Keystore/Aead", e)
            }
        }
    }

    companion object {
        private const val USER_TOKEN = "user_token"
        private const val USER_EMAIL = "user_email"
        private const val USER_ID = "user_id"
        private const val IS_GOOGLE_USER = "is_google_user"
        private const val DEFAULT_BUY_QTY = "default_buy_qty"
        private const val DEFAULT_SELL_QTY = "default_sell_qty"
    }

    /**
     * Helper to encrypt data using Tink.
     */
    private fun encrypt(value: String?): String? {
        if (value.isNullOrBlank() || aead == null) return value
        return try {
            val encrypted = aead!!.encrypt(value.toByteArray(), null)
            Base64.encodeToString(encrypted, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Helper to decrypt data using Tink.
     * 🛡️ BLOCKING: Since AEAD is initialized on a background thread, this will block
     * if called before initialization is complete. This is why it MUST be called from IO context.
     */
    private fun decrypt(encryptedValue: String?): String? {
        if (encryptedValue.isNullOrBlank()) return encryptedValue
        
        var currentAead = aead
        if (currentAead == null) {
            // Wait for initialization if necessary (max 5 seconds)
            val startTime = System.currentTimeMillis()
            while (currentAead == null && (System.currentTimeMillis() - startTime < 5000)) {
                Thread.sleep(50)
                currentAead = aead
            }
        }

        if (currentAead == null) {
            Log.e("SessionManager", "AEAD not initialized after timeout, returning null for decryption")
            return null
        }

        return try {
            val decoded = Base64.decode(encryptedValue, Base64.NO_WRAP)
            val decrypted = currentAead.decrypt(decoded, null)
            String(decrypted)
        } catch (e: Exception) {
            Log.e("SessionManager", "Decryption failed", e)
            null
        }
    }

    /**
     * Saves the session data after login or registration.
     */
    fun saveAuthToken(token: String?, email: String, userId: String? = null, isGoogle: Boolean = false) {
        prefs.edit {
            putString(USER_TOKEN, encrypt(token))
            putString(USER_EMAIL, encrypt(email))
            putString(USER_ID, encrypt(userId))
            putString(IS_GOOGLE_USER, encrypt(isGoogle.toString()))
        }
    }

    fun getAuthToken(): String? = decrypt(prefs.getString(USER_TOKEN, null))

    fun fetchAuthToken(): String? = getAuthToken()

    fun getUserEmail(): String? = decrypt(prefs.getString(USER_EMAIL, null))

    fun getUserId(): String? = decrypt(prefs.getString(USER_ID, null))

    fun isGoogleUser(): Boolean {
        val decryptedVal = decrypt(prefs.getString(IS_GOOGLE_USER, null))
        return decryptedVal?.toBooleanStrictOrNull() ?: false
    }

    fun clearSession() {
        prefs.edit { clear() }
    }

    fun isLoggedIn(): Boolean = !getAuthToken().isNullOrBlank()

    fun getAuthHeader(): String? {
        val token = getAuthToken()
        return if (!token.isNullOrBlank()) "Bearer $token" else null
    }

    // 🆕 Trading Presets
    fun saveTradingPresets(buyQty: Double, sellQty: Double) {
        prefs.edit {
            putFloat(DEFAULT_BUY_QTY, buyQty.toFloat())
            putFloat(DEFAULT_SELL_QTY, sellQty.toFloat())
        }
    }

    fun getDefaultBuyQty(): Double = prefs.getFloat(DEFAULT_BUY_QTY, 1.0f).toDouble()
    fun getDefaultSellQty(): Double = prefs.getFloat(DEFAULT_SELL_QTY, 1.0f).toDouble()
}