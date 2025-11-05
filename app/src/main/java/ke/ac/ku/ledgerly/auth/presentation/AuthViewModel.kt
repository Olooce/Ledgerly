package ke.ac.ku.ledgerly.auth.presentation

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
    val state: StateFlow<AuthState> = _state

    init {
        _state.update {
            it.copy(
                isAuthenticated = repository.getCurrentUser() != null,
                isBiometricAvailable = repository.isBiometricAvailable()
            )
        }
    }

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.EmailSignIn -> signInWithEmail(event.email, event.password)
            is AuthEvent.EmailSignUp -> signUpWithEmail(event.email, event.password)
            is AuthEvent.GoogleSignIn -> signInWithGoogle()
            is AuthEvent.BiometricSignIn -> {} // Handled in UI
            is AuthEvent.SignOut -> signOut()
            is AuthEvent.DismissError -> dismissError()
        }
    }

    private fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.signInWithEmail(email, password)
                .onSuccess {
                    _state.update { it.copy(isAuthenticated = true, error = null) }
                    syncManager.syncAllData()

                }
                .onFailure { e ->
                    _state.update { it.copy(error = e.message) }
                }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun signUpWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.signUpWithEmail(email, password)
                .onSuccess {
                    _state.update { it.copy(isAuthenticated = true, error = null) }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = e.message) }
                }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun signInWithGoogle() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            repository.signInWithGoogle()
                .onSuccess { signInRequest ->
                    try {
                        val result = oneTapClient.beginSignIn(signInRequest).await()
                        // TODO: Handle the result
                    } catch (e: Exception) {
                        _state.update { it.copy(error = e.message) }
                    } finally {
                        syncManager.syncAllData()
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = e.message) }
                }
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun handleGoogleSignInResult(idToken: String) {
        viewModelScope.launch {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            repository.signInWithGoogleCredential(credential)
                .onSuccess {
                    _state.update { it.copy(isAuthenticated = true, error = null) }
                    syncManager.syncAllData()
                }
                .onFailure { e ->
                    _state.update { it.copy(error = e.message) }
                }
        }
    }

    private fun signOut() {
        repository.signOut()
        _state.update { it.copy(isAuthenticated = false) }
    }

    private fun dismissError() {
        _state.update { it.copy(error = null) }
    }
}