package ke.ac.ku.ledgerly

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.google.android.gms.auth.api.identity.SignInClient
import dagger.hilt.android.AndroidEntryPoint
import ke.ac.ku.ledgerly.ui.theme.LedgerlyTheme
import ke.ac.ku.ledgerly.ui.theme.ThemeViewModel
import ke.ac.ku.ledgerly.feature.settings.SettingsViewModel
import ke.ac.ku.ledgerly.auth.presentation.AuthViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()



    @Inject
    lateinit var oneTapClient: SignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkMode by themeViewModel.isDarkMode.collectAsState()
            LedgerlyTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHostScreen(oneTapClient, themeViewModel = themeViewModel, settingsViewModel = settingsViewModel, authViewModel = authViewModel)
                }
            }
        }
    }
}