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

    @PropertyName("lastUpdated")
    val lastUpdated: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromLocal(
            userId: String,
            userName: String,
            darkMode: Boolean,
            syncEnabled: Boolean,
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
                lastUpdated = System.currentTimeMillis()
            )
        }
    }
}
