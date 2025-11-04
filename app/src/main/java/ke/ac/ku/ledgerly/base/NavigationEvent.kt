package ke.ac.ku.ledgerly.base

sealed class NavigationEvent {
    object NavigateBack : NavigationEvent()
}

sealed class AddTransactionNavigationEvent : NavigationEvent() {
    object MenuOpenedClicked : AddTransactionNavigationEvent()
}

sealed class HomeNavigationEvent : NavigationEvent() {
    object NavigateToAddExpense : HomeNavigationEvent()
    object NavigateToAddIncome : HomeNavigationEvent()
    object NavigateToSeeAll : HomeNavigationEvent()
}