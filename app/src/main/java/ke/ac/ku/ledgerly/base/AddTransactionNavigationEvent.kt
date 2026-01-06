package ke.ac.ku.ledgerly.base
sealed class AddTransactionNavigationEvent : NavigationEvent {
    object MenuOpenedClicked : AddTransactionNavigationEvent()
}
