package ke.ac.ku.ledgerly.presentation.auth

import android.util.Log
import android.util.Patterns
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.identity.SignInClient
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.ac.ku.ledgerly.base.AuthEvent
import ke.ac.ku.ledgerly.data.model.AuthState
import ke.ac.ku.ledgerly.data.repository.AuthRepository
import ke.ac.ku.ledgerly.data.repository.UserPreferencesRepository
import ke.ac.ku.ledgerly.domain.SyncManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    val repository: AuthRepository,
    private val oneTapClient: SignInClient,
    private val syncManager: SyncManager,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    private val _oneTapIntent = MutableSharedFlow<IntentSenderRequest>()
    val oneTapIntent = _oneTapIntent.asSharedFlow()

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    val currentUserEmail: String?
        get() = repository.getCurrentUser()?.email

    val currentUserName: String?
        get() = repository.getCurrentUser()?.displayName

    init {
        checkAuthenticationStatus()
    }

    private fun checkAuthenticationStatus() {
        _state.update {
            it.copy(
                isAuthenticated = repository.getCurrentUser() != null,
                isBiometricAvailable = repository.isBiometricAvailable(),
                isBiometricEnabled = repository.isBiometricUnlockEnabled(),
                linkedGoogleAccount = repository.getLinkedGoogleAccount(),
                isLoading = false
            )
        }
    }

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.EmailSignIn -> signInWithEmail(event.email, event.password)
            is AuthEvent.EmailSignUp -> signUpWithEmail(event.email, event.password)
            is AuthEvent.SignInWithGoogle -> signInWithGoogle()
            is AuthEvent.GoogleSignInWithCredential -> handleGoogleSignInResult(event.idToken)
            is AuthEvent.LinkGoogleAccount -> linkGoogleAccount(event.idToken)
            is AuthEvent.BiometricSignIn -> handleBiometricSignIn()
            is AuthEvent.BiometricUnlockAttempt -> handleBiometricUnlockAttempt(event.password)
            is AuthEvent.EnableBiometricUnlock -> toggleBiometricUnlock(event.enable)
            is AuthEvent.SignOut -> signOut()
            is AuthEvent.DismissError -> dismissError()
            is AuthEvent.DismissInfoMessage -> dismissInfoMessage()
        }
    }

    private fun signInWithEmail(email: String, password: String) {
        if (!validateEmailAndPassword(email, password)) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            repository.signInWithEmail(email, password)
                .onSuccess { user ->
                    performInitialSyncAndAuthenticate(showBiometricOptIn = true)
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
                    performInitialSyncAndAuthenticate(showBiometricOptIn = true)
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
                        val intentReq = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()

                        _oneTapIntent.emit(intentReq)

                        _state.update { it.copy(isLoading = false) }
                    } catch (e: Exception) {
                        _state.update {
                            it.copy(
                                error = "Google sign in unavailable. Please try another method.",
                                isLoading = false
                            )
                        }
                    }
                }
                .onFailure {
                    _state.update {
                        it.copy(
                            error = "Google sign in failed. Please try again.",
                            isLoading = false
                        )
                    }
                }
        }
    }


    private fun handleGoogleSignInResult(idToken: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            repository.signInWithGoogleCredential(idToken)
                .onSuccess { user ->
                    // Extract email from ID token and store as linked account
                    val googleEmail = repository.getCurrentUser()?.providerData
                        ?.find { it.providerId == "google.com" }?.email
                    googleEmail?.let { repository.setLinkedGoogleAccount(it) }

                    performInitialSyncAndAuthenticate(showBiometricOptIn = true)
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

    private fun linkGoogleAccount(idToken: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            repository.linkGoogleAccount(idToken)
                .onSuccess {
                    // Extract and store linked Google account email
                    val googleEmail = repository.getCurrentUser()?.providerData
                        ?.find { it.providerId == "google.com" }?.email
                    googleEmail?.let { repository.setLinkedGoogleAccount(it) }

                    _state.update {
                        it.copy(
                            linkedGoogleAccount = googleEmail,
                            error = null,
                            isLoading = false
                        )
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            error = "Failed to link Google account: ${mapAuthErrorToMessage(e)}",
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun handleBiometricSignIn() {
               viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val currentUser = repository.getCurrentUser()
            if (currentUser == null) {
                _state.update {
                    it.copy(
                        error = "No user account found. Please sign in first to enable biometric.",
                        isLoading = false
                    )
                }
                return@launch
            }

            performInitialSyncAndAuthenticate(showBiometricOptIn = false)
        }
    }

    private fun handleBiometricUnlockAttempt(password: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            // If password provided, use it as fallback
            if (password.isNullOrBlank()) {
                _state.update { it.copy(error = "Password is required", isLoading = false) }
                return@launch
            }
            val currentUser = repository.getCurrentUser()
            val email = currentUser?.email
            if (currentUser == null || email.isNullOrBlank()) {
                _state.update { it.copy(error = "No user account found. Please sign in again.", isLoading = false) }
                return@launch
            }
            try {
                currentUser.reauthenticate(
                    com.google.firebase.auth.EmailAuthProvider.getCredential(email, password)
                ).await()
                handleBiometricSignIn()
            } catch (e: Exception) {
                _state.update { it.copy(error = "Password verification failed", isLoading = false) }
            }
        }
    }


    private fun toggleBiometricUnlock(enable: Boolean) {
        if (enable) {
            repository.enableBiometricUnlock()
        } else {
            repository.disableBiometricUnlock()
        }
        _state.update { it.copy(isBiometricEnabled = enable) }
    }

    private fun performInitialSyncAndAuthenticate(showBiometricOptIn: Boolean = false) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // Show info message for first-time users
            _state.update {
                it.copy(
                    infoMessage = "Welcome! Syncing your data from the cloud..."
                )
            }

            userPreferencesRepository.loadFromFirestore()
                .onSuccess {
                    viewModelScope.launch {
                        syncManager.syncAllData()
                    }
                }
                .onFailure { e ->
                    Log.w("AuthViewModel", "Failed to load preferences, continuing sync", e)
                    viewModelScope.launch {
                        syncManager.syncAllData()
                    }
                }

            _state.update {
                it.copy(
                    isAuthenticated = true,
                    error = null,
                    isLoading = false,
                    infoMessage = null,
                    showBiometricOptIn = showBiometricOptIn &&
                            repository.isBiometricAvailable() &&
                            !repository.isBiometricUnlockEnabled(),
                    isBiometricEnabled = repository.isBiometricUnlockEnabled(),
                    linkedGoogleAccount = repository.getLinkedGoogleAccount()
                )
            }
        }
    }

    fun dismissBiometricOptIn() {
        _state.update { it.copy(showBiometricOptIn = false) }
    }

    private fun signOut() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            syncManager.syncAllData(
                onSuccess = {
                    viewModelScope.launch {
                        repository.signOut()
                            .onSuccess {
                                _state.update {
                                    AuthState(
                                        isAuthenticated = false,
                                        isBiometricAvailable = repository.isBiometricAvailable(),
                                        isLoading = false
                                    )
                                }
                            }
                            .onFailure { e ->
                                _state.update {
                                    it.copy(
                                        error = "Logout failed: ${e.message}",
                                        isLoading = false
                                    )
                                }
                            }
                    }
                },
                onError = { error ->
                    Log.e("AuthViewModel", "Sync failed before logout: $error")
                    viewModelScope.launch {
                        repository.signOut()
                            .onSuccess {
                                _state.update {
                                    AuthState(
                                        isAuthenticated = false,
                                        isBiometricAvailable = repository.isBiometricAvailable(),
                                        isLoading = false
                                    )
                                }
                            }
                            .onFailure { e ->
                                _state.update {
                                    it.copy(
                                        error = "Logout failed: ${e.message}",
                                        isLoading = false
                                    )
                                }
                            }
                    }
                }
            )
        }
    }

    private fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    private fun dismissInfoMessage() {
        _state.update { it.copy(infoMessage = null) }
    }

    private fun validateEmailAndPassword(email: String, password: String): Boolean {
        return when {
            email.isBlank() -> {
                _state.update { it.copy(error = "Email cannot be empty") }
                false
            }

            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
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