package ke.ac.ku.ledgerly.base

sealed class HomeNavigationEvent : NavigationEvent {
    object NavigateToAddExpense : HomeNavigationEvent()
    object NavigateToAddIncome : HomeNavigationEvent()
    object NavigateToSeeAll : HomeNavigationEvent()
}
