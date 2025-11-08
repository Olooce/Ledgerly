package ke.ac.ku.ledgerly.data.repository

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import ke.ac.ku.ledgerly.BuildConfig
import ke.ac.ku.ledgerly.WorkManagerSetup
import ke.ac.ku.ledgerly.data.LedgerlyDatabase
import ke.ac.ku.ledgerly.domain.AuthStateProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val oneTapClient: SignInClient,
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val workManagerSetup: WorkManagerSetup,
    private val authStateProvider: AuthStateProvider
) {
    suspend fun signInWithEmail(email: String, password: String): Result<Unit> = try {
        auth.signInWithEmailAndPassword(email, password).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun signUpWithEmail(email: String, password: String): Result<Unit> = try {
        auth.createUserWithEmailAndPassword(email, password).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun signInWithGoogle(): Result<BeginSignInRequest> = try {
        val signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                    .setFilterByAuthorizedAccounts(true)
                    .build()
            )
            .build()
        Result.success(signInRequest)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun signInWithGoogleCredential(credential: AuthCredential): Result<Unit> = try {
        auth.signInWithCredential(credential).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
                BiometricManager.BIOMETRIC_SUCCESS
    }

    suspend fun signOut(): Result<Unit> = try {

        workManagerSetup.cancelAllWork()
        workManagerSetup.pruneWork()

        withContext(Dispatchers.IO) {
            LedgerlyDatabase.Companion.getInstance(context).clearAllTables()
        }


        userPreferencesRepository.clearAll()


        auth.signOut()
        oneTapClient.signOut().await()

        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("AuthRepository", "Sign out failed", e)
        Result.failure(e)
    }

    fun getCurrentUser() = auth.currentUser

    private val _authState = MutableStateFlow(auth.currentUser != null)
    val authState = _authState.asStateFlow()

    init {
        // Listen for auth state changes
        auth.addAuthStateListener { firebaseAuth ->
            _authState.value = firebaseAuth.currentUser != null
        }
    }

    suspend fun getAuthToken(): String? {
        return auth.currentUser?.getIdToken(false)?.await()?.token
    }
}