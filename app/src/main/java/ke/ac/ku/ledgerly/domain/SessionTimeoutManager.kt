package ke.ac.ku.ledgerly.domain

import android.content.Context
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import ke.ac.ku.ledgerly.data.repository.UserPreferencesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionTimeoutManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authStateProvider: AuthStateProvider
) : LifecycleEventObserver {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var timeoutCheckJob: Job? = null

    private val _sessionTimeoutEvent = MutableSharedFlow<Unit>(
        replay = 1,
        extraBufferCapacity = 1
    )
    val sessionTimeoutEvent = _sessionTimeoutEvent.asSharedFlow()

    companion object {
        private const val TAG = "SessionTimeoutManager"
        private const val TIMEOUT_CHECK_INTERVAL_MS = 60_000L
    }

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStateChanged(source: androidx.lifecycle.LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                Log.d(TAG, "App resumed - checking session timeout")
                startTimeoutCheck()
            }

            Lifecycle.Event.ON_PAUSE -> {
                Log.d(TAG, "App paused - pausing session timeout check")
                pauseTimeoutCheck()
            }

            else -> {}
        }
    }

    fun recordUserActivity() {
        scope.launch {
            try {
                userPreferencesRepository.updateLastActivityTime(syncNow = false)
                Log.d(TAG, "Activity recorded at ${System.currentTimeMillis()}")
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(TAG, "Failed to record user activity", e)
            }
        }
    }

    private fun startTimeoutCheck() {
        timeoutCheckJob?.cancel()

        timeoutCheckJob = scope.launch {
            while (isActive) {
                try {
                    checkSessionTimeout()
                    delay(TIMEOUT_CHECK_INTERVAL_MS)
                } catch (ce: CancellationException) {
                    throw ce
                } catch (e: Exception) {
                    Log.e(TAG, "Error in timeout check", e)
                }
            }
        }
    }


    private fun pauseTimeoutCheck() {
        timeoutCheckJob?.cancel()
        timeoutCheckJob = null
    }

    private suspend fun checkSessionTimeout() {
        try {
            val prefs = userPreferencesRepository.getCurrentPreferences()

            if (!prefs.sessionTimeoutEnabled || !authStateProvider.isUserAuthenticated()) {
                return
            }

            val currentTime = System.currentTimeMillis()
            val lastActivityTime = prefs.lastActivityTime
            val timeoutDurationMs = prefs.sessionTimeoutMinutes * 60 * 1000L

            val timeSinceLastActivity = currentTime - lastActivityTime

            Log.d(
                TAG,
                "Session check: ${timeSinceLastActivity}ms since activity, " +
                        "timeout in ${timeoutDurationMs - timeSinceLastActivity}ms"
            )

            if (timeSinceLastActivity > timeoutDurationMs) {
                Log.w(TAG, "Session timeout detected!")
                _sessionTimeoutEvent.tryEmit(Unit)
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            Log.e(TAG, "Error checking session timeout", e)
        }
    }

    /**
     * Cleanup when manager is destroyed
     */
    fun cleanup() {

        pauseTimeoutCheck()
        scope.cancel()
    }
}
