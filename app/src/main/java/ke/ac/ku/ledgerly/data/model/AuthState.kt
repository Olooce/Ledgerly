package ke.ac.ku.ledgerly.data.model

data class AuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: String? = null,
    val isBiometricAvailable: Boolean = false
)