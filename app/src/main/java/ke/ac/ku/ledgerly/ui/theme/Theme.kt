package ke.ac.ku.ledgerly.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
private val DarkColorScheme = darkColorScheme(
    primary = LedgerlyGreen,
    onPrimary = OnPrimaryDark,
    secondary = LedgerlyBlue,
    onSecondary = OnPrimaryDark,
    tertiary = LedgerlyGreenLight,
    background = Color(0xFF0D1B2A),
    surface = Color(0xFF1B263B),
    onSurface = OnSurfaceDark,
    error = ErrorRed,
    outline = LedgerlyAccent
)
private val LightColorScheme = lightColorScheme(
    primary = LedgerlyGreen,
    onPrimary = OnPrimaryLight,
    secondary = LedgerlyBlue,
    onSecondary = OnPrimaryLight,
    tertiary = LedgerlyGreenLight,
    background = Color(0xFFF5F7FA),
    surface = Color(0xFFFFFFFF),
    onSurface = OnSurfaceLight,
    error = ErrorRed,
    outline = LedgerlyAccent
)


@Composable
fun LedgerlyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = LedgerlyShapes,
        content = content
    )
}
