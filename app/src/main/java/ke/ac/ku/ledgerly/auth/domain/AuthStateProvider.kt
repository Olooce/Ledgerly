package ke.ac.ku.ledgerly.auth.domain

import com.google.firebase.auth.FirebaseAuth
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class AuthStateProvider @Inject constructor(
    private val auth: FirebaseAuth
) {
    fun getCurrentUserId(): String? = auth.currentUser?.uid
    fun isUserAuthenticated(): Boolean = auth.currentUser != null

    private val _authState = MutableStateFlow(auth.currentUser != null)
    val authState = _authState.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _authState.value = firebaseAuth.currentUser != null
        }
    }
}