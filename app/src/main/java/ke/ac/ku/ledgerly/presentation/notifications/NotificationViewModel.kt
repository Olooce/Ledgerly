package ke.ac.ku.ledgerly.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.ac.ku.ledgerly.base.NavigationEvent
import ke.ac.ku.ledgerly.data.model.NotificationEntity
import ke.ac.ku.ledgerly.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationState(
    val notifications: List<NotificationEntity> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class NotificationUiEvent {
    object OnBackClicked : NotificationUiEvent()
    object OnMarkAllAsReadClicked : NotificationUiEvent()
    data class OnMarkAsReadClicked(val notificationId: Long) : NotificationUiEvent()
    data class OnDeleteNotificationClicked(val notificationId: Long) : NotificationUiEvent()
    data class OnNotificationClicked(val notification: NotificationEntity) : NotificationUiEvent()
}

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _notificationState = MutableStateFlow(NotificationState())
    val notificationState: StateFlow<NotificationState> = _notificationState.asStateFlow()

    private val _unreadCount = notificationRepository.getUnreadNotificationCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val unreadCount: StateFlow<Int> = _unreadCount

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            _notificationState.update { it.copy(isLoading = true) }
            try {
                notificationRepository.getAllNotifications().collect { notifications ->
                    _notificationState.update {
                        it.copy(
                            notifications = notifications,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _notificationState.update {
                    it.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onEvent(event: NotificationUiEvent) {
        when (event) {
            NotificationUiEvent.OnBackClicked -> {
                viewModelScope.launch {
                    _navigationEvent.emit(NavigationEvent.NavigateBack)
                }
            }

            NotificationUiEvent.OnMarkAllAsReadClicked -> {
                markAllAsRead()
            }

            is NotificationUiEvent.OnMarkAsReadClicked -> {
                markAsRead(event.notificationId)
            }

            is NotificationUiEvent.OnDeleteNotificationClicked -> {
                deleteNotification(event.notificationId)
            }

            is NotificationUiEvent.OnNotificationClicked -> {
                handleNotificationClick(event.notification)
            }
        }
    }

    private fun markAsRead(notificationId: Long) {
        viewModelScope.launch {
            try {
                notificationRepository.markAsRead(notificationId)
            } catch (e: Exception) {
                _notificationState.update {
                    it.copy(error = "Failed to mark notification as read")
                }
            }
        }
    }

    private fun markAllAsRead() {
        viewModelScope.launch {
            try {
                notificationRepository.markAllAsRead()
            } catch (e: Exception) {
                _notificationState.update {
                    it.copy(error = "Failed to mark all notifications as read")
                }
            }
        }
    }

    private fun deleteNotification(notificationId: Long) {
        viewModelScope.launch {
            try {
                notificationRepository.softDeleteNotification(notificationId)
            } catch (e: Exception) {
                _notificationState.update {
                    it.copy(error = "Failed to delete notification")
                }
            }
        }
    }

    private fun handleNotificationClick(notification: NotificationEntity) {
        // Handle navigation based on notification type and actionUrl
        viewModelScope.launch {
            // Mark as read when clicked
            markAsRead(notification.id ?: return@launch)

            // Navigate based on notification type if needed
            notification.actionUrl?.let {
                // Parse and navigate to the related entity
            }
        }
    }
}
