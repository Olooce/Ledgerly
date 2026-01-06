package ke.ac.ku.ledgerly.base
sealed class BillReminderNavigationEvent : NavigationEvent {
    object NavigateBack : BillReminderNavigationEvent()
    data class NavigateToAddEditBill(val billId: Long?) : BillReminderNavigationEvent()
    data class NavigateToBillDetail(val billId: Long) : BillReminderNavigationEvent()
}
