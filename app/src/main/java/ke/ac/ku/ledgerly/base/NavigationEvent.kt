package ke.ac.ku.ledgerly.base

sealed interface NavigationEvent {
    object NavigateBack : NavigationEvent
}


