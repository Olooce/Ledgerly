package ke.ac.ku.ledgerly.data.model

data class AuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: String? = null,
    val isBiometricAvailable: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val showBiometricUnlock: Boolean = false,
    val showBiometricOptIn: Boolean = false,
    val linkedGoogleAccount: String? = null,
    val infoMessage: String? = null
)