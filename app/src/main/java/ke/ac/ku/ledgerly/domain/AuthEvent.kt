package ke.ac.ku.ledgerly.domain

sealed class AuthEvent {
    data class EmailSignIn(val email: String, val password: String) : AuthEvent()
    data class EmailSignUp(val email: String, val password: String) : AuthEvent()
    object GoogleSignIn : AuthEvent()
    object BiometricSignIn : AuthEvent()
    object SignOut : AuthEvent()
    object DismissError : AuthEvent()
}