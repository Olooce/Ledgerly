package ke.ac.ku.ledgerly.data.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Manages biometric authentication (fingerprint/face ID) for session unlock and re-authentication.
 * Handles BiometricPrompt integration and state management.
 */
class BiometricAuthenticationManager(private val context: Context) {

    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun getAvailableBiometricType(): BiometricType {
        val biometricManager = BiometricManager.from(context)
        return when (BiometricManager.BIOMETRIC_SUCCESS) {
            biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) -> BiometricType.STRONG
            biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) -> BiometricType.WEAK
            else -> BiometricType.NONE
        }
    }

    suspend fun authenticate(
        activity: FragmentActivity,
        title: String = "Unlock Ledgerly",
        subtitle: String = "Use biometric to unlock your session",
        negativeButtonText: String = "Use Password"
    ): BiometricAuthResult = suspendCancellableCoroutine { continuation ->
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {

                    super.onAuthenticationError(errorCode, errString)
                    if (!continuation.isActive) return
                    if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        // User chose fallback password entry
                        continuation.resume(BiometricAuthResult.Fallback)
                    } else {
                        continuation.resume(
                            BiometricAuthResult.Error(
                                errorCode,
                                errString.toString()
                            )
                        )
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    if (continuation.isActive) continuation.resume(BiometricAuthResult.Success)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    if (continuation.isActive) continuation.resume(BiometricAuthResult.Failed)
                }
            }
        )

        continuation.invokeOnCancellation { biometricPrompt.cancelAuthentication() }
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    enum class BiometricType {
        STRONG,
        WEAK,
        NONE
    }

    sealed class BiometricAuthResult {
        object Success : BiometricAuthResult()
        object Failed : BiometricAuthResult()
        object Fallback : BiometricAuthResult()
        data class Error(val errorCode: Int, val errorMessage: String) : BiometricAuthResult()
    }
}
