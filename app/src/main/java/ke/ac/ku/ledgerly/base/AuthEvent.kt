package ke.ac.ku.ledgerly.base

sealed class AuthEvent {
    data class EmailSignIn(val email: String, val password: String) : AuthEvent()
    data class EmailSignUp(val email: String, val password: String) : AuthEvent()
    object GoogleSignIn : AuthEvent()
    data class GoogleSignInWithCredential(val idToken: String) : AuthEvent()
    data class LinkGoogleAccount(val idToken: String) : AuthEvent()
    object BiometricSignIn : AuthEvent()
    data class BiometricUnlockAttempt(val password: String? = null) : AuthEvent()
    data class EnableBiometricUnlock(val enable: Boolean) : AuthEvent()
    object SignOut : AuthEvent()
    object DismissError : AuthEvent()
    object DismissInfoMessage : AuthEvent()
}