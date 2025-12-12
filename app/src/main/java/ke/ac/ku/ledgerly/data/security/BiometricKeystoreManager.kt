package ke.ac.ku.ledgerly.data.security

import android.content.Context
import android.util.Base64
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec

/**
 * Manages secure storage of biometric-related credentials and preferences using
 * Android Keystore and EncryptedSharedPreferences.
 */
class BiometricKeystoreManager(
    context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFERENCES_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val PREFERENCES_NAME = "ledgerly_biometric_secure"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_BIOMETRIC_TOKEN = "biometric_token"
        private const val KEY_BIOMETRIC_IV = "biometric_iv"
        private const val KEY_LINKED_GOOGLE_ACCOUNT = "linked_google_account"
        private const val KEY_LAST_BIOMETRIC_AUTH = "last_biometric_auth"
    }

    fun enableBiometricUnlock() {
        encryptedPreferences.edit { putBoolean(KEY_BIOMETRIC_ENABLED, true) }
    }

    fun disableBiometricUnlock() {
        encryptedPreferences.edit().apply {
            putBoolean(KEY_BIOMETRIC_ENABLED, false)
            remove(KEY_BIOMETRIC_TOKEN)
            remove(KEY_BIOMETRIC_IV)
            remove(KEY_LAST_BIOMETRIC_AUTH)
            apply()
        }
    }

    fun isBiometricUnlockEnabled(): Boolean {
        return encryptedPreferences.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    fun storeEncryptedSessionToken(token: String) {
        try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val keyGen = KeyGenerator.getInstance("AES")
            keyGen.init(256)
            val secretKey = keyGen.generateKey()

            // Generate random IV
            val iv = ByteArray(12)
            SecureRandom().nextBytes(iv)
            val ivSpec = IvParameterSpec(iv)

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
            val encryptedToken = cipher.doFinal(token.toByteArray())

            // Store both encrypted token and IV (encoded as Base64)
            encryptedPreferences.edit().apply {
                putString(
                    KEY_BIOMETRIC_TOKEN,
                    Base64.encodeToString(encryptedToken, Base64.DEFAULT)
                )
                putString(
                    KEY_BIOMETRIC_IV,
                    Base64.encodeToString(iv, Base64.DEFAULT)
                )
                apply()
            }
        } catch (e: Exception) {
            // Fallback: if encryption fails, just remove the token
            encryptedPreferences.edit { remove(KEY_BIOMETRIC_TOKEN) }
        }
    }

    fun getEncryptedSessionToken(): String? {
        return encryptedPreferences.getString(KEY_BIOMETRIC_TOKEN, null)
    }

    fun setLinkedGoogleAccount(email: String) {
        encryptedPreferences.edit { putString(KEY_LINKED_GOOGLE_ACCOUNT, email) }
    }


    fun getLinkedGoogleAccount(): String? {
        return encryptedPreferences.getString(KEY_LINKED_GOOGLE_ACCOUNT, null)
    }


    fun clearLinkedGoogleAccount() {
        encryptedPreferences.edit { remove(KEY_LINKED_GOOGLE_ACCOUNT) }
    }

    fun updateLastBiometricAuthTime() {
        encryptedPreferences.edit {
            putLong(
                KEY_LAST_BIOMETRIC_AUTH,
                System.currentTimeMillis()
            )
        }
    }

    fun getLastBiometricAuthTime(): Long {
        return encryptedPreferences.getLong(KEY_LAST_BIOMETRIC_AUTH, 0L)
    }

    fun clearAll() {
        encryptedPreferences.edit { clear() }
    }
}
