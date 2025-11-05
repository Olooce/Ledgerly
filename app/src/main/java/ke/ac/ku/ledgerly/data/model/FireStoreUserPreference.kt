package ke.ac.ku.ledgerly.data.model

data class FirestoreUserPreferences(
    val userId: String = "",
    val darkMode: Boolean = false,
    val syncEnabled: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromLocal(userId: String, darkMode: Boolean, syncEnabled: Boolean): FirestoreUserPreferences {
            return FirestoreUserPreferences(
                userId = userId,
                darkMode = darkMode,
                syncEnabled = syncEnabled,
                lastUpdated = System.currentTimeMillis()
            )
        }
    }
}
