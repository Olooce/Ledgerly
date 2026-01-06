package ke.ac.ku.ledgerly.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.ac.ku.ledgerly.data.repository.NotificationRepository
import ke.ac.ku.ledgerly.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DrawerViewModel @Inject constructor(
    userPreferencesRepository: UserPreferencesRepository,
    notificationRepository: NotificationRepository
) : ViewModel() {

    val userName: StateFlow<String> = userPreferencesRepository.userName
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            "User"
        )

    val unreadCount: StateFlow<Int> =
        notificationRepository.getUnreadNotificationCount()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                0
            )
}
