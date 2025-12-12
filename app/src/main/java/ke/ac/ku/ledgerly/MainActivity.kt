package ke.ac.ku.ledgerly

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.auth.api.identity.SignInClient
import dagger.hilt.android.AndroidEntryPoint
import ke.ac.ku.ledgerly.data.repository.UserPreferencesRepository
import ke.ac.ku.ledgerly.domain.SessionTimeoutManager
import ke.ac.ku.ledgerly.presentation.auth.AuthViewModel
import ke.ac.ku.ledgerly.presentation.settings.SettingsViewModel
import ke.ac.ku.ledgerly.ui.theme.LedgerlyTheme
import ke.ac.ku.ledgerly.ui.theme.ThemeViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var oneTapClient: SignInClient

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    @Inject
    lateinit var sessionTimeoutManager: SessionTimeoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkMode by themeViewModel.isDarkMode.collectAsState()
            LedgerlyTheme(darkTheme = isDarkMode ?: false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHostScreen(
                        oneTapClient = oneTapClient,
                        themeViewModel = themeViewModel,
                        settingsViewModel = settingsViewModel,
                        authViewModel = authViewModel,
                        userPreferencesRepository = userPreferencesRepository,
                        sessionTimeoutManager = sessionTimeoutManager
                    )
                }
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        sessionTimeoutManager.recordUserActivity()
        return super.dispatchTouchEvent(ev)
    }
}
