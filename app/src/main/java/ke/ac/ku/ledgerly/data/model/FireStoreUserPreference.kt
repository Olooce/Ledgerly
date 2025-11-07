package ke.ac.ku.ledgerly.data.model

import com.google.firebase.firestore.PropertyName

data class FirestoreUserPreferences(
    @PropertyName("userId")
    val userId: String = "",
    @PropertyName("userName")
    val userName: String = "",
    @PropertyName("currency")
    val currency: String = "KES",
    @PropertyName("monthlyBudget")
    val monthlyBudget: Double = 0.0,
    @PropertyName("notificationEnabled")
    val notificationEnabled: Boolean = true,
    @PropertyName("onboardingCompleted")
    val onboardingCompleted: Boolean = false,
    @PropertyName("darkMode")
    val darkMode: Boolean = false,
    @PropertyName("syncEnabled")
    val syncEnabled: Boolean = false,
    @PropertyName("syncWifiOnly")
    val syncWifiOnly: Boolean = true,
    @PropertyName("syncChargingOnly")
    val syncChargingOnly: Boolean = false,
    @PropertyName("syncInterval")
    val syncInterval: Long = 6L,
    @PropertyName("lastUpdated")
    val lastUpdated: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromLocal(
            userId: String,
            userName: String,
            darkMode: Boolean,
            syncEnabled: Boolean,
            syncWifiOnly: Boolean = true,
            syncChargingOnly: Boolean = false,
            syncInterval: Long = 6L,
            currency: String = "KES",
            monthlyBudget: Double = 0.0,
            notificationEnabled: Boolean = true,
            onboardingCompleted: Boolean = false
        ): FirestoreUserPreferences {
            return FirestoreUserPreferences(
                userId = userId,
                userName = userName,
                currency = currency,
                monthlyBudget = monthlyBudget,
                notificationEnabled = notificationEnabled,
                onboardingCompleted = onboardingCompleted,
                darkMode = darkMode,
                syncEnabled = syncEnabled,
                syncWifiOnly = syncWifiOnly,
                syncChargingOnly = syncChargingOnly,
                syncInterval = syncInterval,
                lastUpdated = System.currentTimeMillis()
            )
        }
    }
}