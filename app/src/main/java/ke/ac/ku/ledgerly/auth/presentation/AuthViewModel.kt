package ke.ac.ku.ledgerly.auth.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.ac.ku.ledgerly.auth.data.AuthRepository
import ke.ac.ku.ledgerly.auth.domain.AuthEvent
import ke.ac.ku.ledgerly.auth.domain.AuthState
import ke.ac.ku.ledgerly.domain.SyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val oneTapClient: SignInClient,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        checkAuthenticationStatus()
    }

    private fun checkAuthenticationStatus() {
        _state.update {
            it.copy(
                isAuthenticated = repository.getCurrentUser() != null,
                isBiometricAvailable = repository.isBiometricAvailable(),
                isLoading = false
            )
        }
    }

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.EmailSignIn -> signInWithEmail(event.email, event.password)
            is AuthEvent.EmailSignUp -> signUpWithEmail(event.email, event.password)
            is AuthEvent.GoogleSignIn -> signInWithGoogle()
            is AuthEvent.BiometricSignIn -> handleBiometricSignIn()
            is AuthEvent.SignOut -> signOut()
            is AuthEvent.DismissError -> dismissError()
        }
    }

    private fun signInWithEmail(email: String, password: String) {
        if (!validateEmailAndPassword(email, password)) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            repository.signInWithEmail(email, password)
                .onSuccess { user ->
                    _state.update {
                        it.copy(
                            isAuthenticated = true,
                            error = null,
                            isLoading = false
                        )
                    }
                    viewModelScope.launch {
                        syncManager.syncAllData()
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            error = mapAuthErrorToMessage(e),
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun signUpWithEmail(email: String, password: String) {
        if (!validateEmailAndPassword(email, password)) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            repository.signUpWithEmail(email, password)
                .onSuccess { user ->
                    _state.update {
                        it.copy(
                            isAuthenticated = true,
                            error = null,
                            isLoading = false
                        )
                    }

                    viewModelScope.launch {
                        syncManager.syncAllData()
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            error = mapAuthErrorToMessage(e),
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun signInWithGoogle() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            repository.signInWithGoogle()
                .onSuccess { signInRequest ->
                    try {
                        val result = oneTapClient.beginSignIn(signInRequest).await()
                        _state.update {
                            it.copy(
//                                googleSignInRequest = result,
                                isLoading = false
                            )
                        }
                    } catch (e: Exception) {
                        _state.update {
                            it.copy(
                                error = "Google sign in unavailable. Please try another method.",
                                isLoading = false
                            )
                        }
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            error = "Google sign in failed. Please try again.",
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun handleGoogleSignInResult(idToken: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val credential = GoogleAuthProvider.getCredential(idToken, null)
            repository.signInWithGoogleCredential(credential)
                .onSuccess { user ->
                    _state.update {
                        it.copy(
                            isAuthenticated = true,
                            error = null,
                            isLoading = false
                        )
                    }
                    viewModelScope.launch {
                        syncManager.syncAllData()
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            error = "Google sign in failed. Please try again.",
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun handleBiometricSignIn() {
    }

    fun onBiometricSuccess() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isAuthenticated = true,
                    error = null
                )
            }
            syncManager.syncAllData()
        }
    }

    fun onBiometricError(errorMessage: String) {
        _state.update {
            it.copy(error = "Biometric authentication failed: $errorMessage")
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            repository.signOut()
            _state.update {
                AuthState(
                    isAuthenticated = false,
                    isBiometricAvailable = repository.isBiometricAvailable()
                )
            }
        }
    }

    private fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    private fun validateEmailAndPassword(email: String, password: String): Boolean {
        return when {
            email.isBlank() -> {
                _state.update { it.copy(error = "Email cannot be empty") }
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _state.update { it.copy(error = "Please enter a valid email") }
                false
            }
            password.isBlank() -> {
                _state.update { it.copy(error = "Password cannot be empty") }
                false
            }
            password.length < 6 -> {
                _state.update { it.copy(error = "Password must be at least 6 characters") }
                false
            }
            else -> true
        }
    }

    private fun mapAuthErrorToMessage(exception: Throwable): String {
        return when {
            exception.message?.contains("network", ignoreCase = true) == true ->
                "Network error. Please check your connection."
            exception.message?.contains("password", ignoreCase = true) == true ->
                "Incorrect email or password."
            exception.message?.contains("user", ignoreCase = true) == true ->
                "No account found with this email."
            exception.message?.contains("email", ignoreCase = true) == true ->
                "This email is already registered."
            else -> exception.message ?: "Authentication failed. Please try again."
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}