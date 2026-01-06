package ke.ac.ku.ledgerly.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Returns a color based on progress percentage.
 * @param progress Progress value in range 0-100
 * @return Color representing progress urgency
 */
fun progressColor(progress: Double): Color {
    return when {
        progress >= 100.0 -> LedgerlyGreen
        progress >= 75.0 -> Color(0xFF4CAF50)
        progress >= 50.0 -> Color(0xFFFFC107)
        progress >= 25.0 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }
}