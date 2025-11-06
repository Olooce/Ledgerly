package ke.ac.ku.ledgerly.ui.theme

import androidx.compose.ui.graphics.Color

val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
val Zinc = Color(0xFF2F7E79)
val LightGrey = Color(0xFF666666)
val Navy = Color(0xFF1B1B1F)
val Navy80 = Color(0xCCFFFFFF)
val Teal = Color(0xFF00AEAE)
val Teal80 = Color(0xCCFFFFFF)
val Indigo = Color(0xFF4B0082)
val Indigo80 = Color(0xCCFFFFFF)
val Amethyst = Color(0xFF9370DB)
val Amethyst80 = Color(0xCCFFFFFF)
val White = Color(0xFFFFFFFF)
val White80 = Color(0xCCFFFFFF)
val Red = Color(0xFFFF0000)
val Green = Color(0xFF00FF00)

val Yellow = Color(0xFFFFDD00)

val DeepNavy = Color(0xFF0A2540)
val OceanBlue = Color(0xFF155E75)
val EmeraldGreen = Color(0xFF2E7D32)
val SlateGrey = Color(0xFF607D8B)
val SoftGold = Color(0xFFF9A825)
val BackgroundDark = Color(0xFF0D1B2A)
val BackgroundLight = Color(0xFFF5F7FA)
val SurfaceDark = Color(0xFF1B263B)
val SurfaceLight = Color(0xFFFFFFFF)
val TextPrimaryDark = Color(0xFFECEFF1)
val TextPrimaryLight = Color(0xFF1C1C1C)

val Blue900 = Color(0xFF0A2540)
val Blue800 = Color(0xFF12355B)
val Blue700 = Color(0xFF155E75)
val Blue600 = Color(0xFF1E88E5)
val Blue500 = Color(0xFF42A5F5)
val Blue400 = Color(0xFF64B5F6)
val Blue300 = Color(0xFF90CAF9)
val Blue200 = Color(0xFFBBDEFB)
val Blue100 = Color(0xFFE3F2FD)

val Green900 = Color(0xFF1B5E20)
val Green700 = Color(0xFF2E7D32)
val Green500 = Color(0xFF4CAF50)
val Green300 = Color(0xFF81C784)
val Green100 = Color(0xFFC8E6C9)

val Gold900 = Color(0xFF8C6D1F)
val Gold700 = Color(0xFFF9A825)
val Gold500 = Color(0xFFFFC107)
val Gold300 = Color(0xFFFFD54F)
val Gold100 = Color(0xFFFFF8E1)

val Red900 = Color(0xFFB71C1C)
val Red700 = Color(0xFFD32F2F)
val Red500 = Color(0xFFF44336)
val Red300 = Color(0xFFE57373)
val Red100 = Color(0xFFFFCDD2)

val Yellow500 = Color(0xFFFFD600)
val Yellow300 = Color(0xFFFFF176)
val Yellow100 = Color(0xFFFFF9C4)

// Ledgerly Brand Colors
val LedgerlyGreen = Color(0xFF0B3D2E)
val LedgerlyGreenLight = Color(0xFF1A5C47)
val LedgerlyAccent = Color(0xFFE6F0EC)
val LedgerlyBlue = Color(0xFF4A90E2)
val LedgerlyBlueLight = Color(0xFFD6E4F0)

// Standard Palette Extensions
val OnPrimaryDark = Color(0xFFFFFFFF)
val OnPrimaryLight = Color(0xFFFFFFFF)
val OnSurfaceDark = Color(0xFFECEFF1)
val OnSurfaceLight = Color(0xFF1C1C1C)
val ErrorRed = Color(0xFFD32F2F)
val SuccessGreen = Color(0xFF4CAF50)
val WarningYellow = Color(0xFFFFD600)

sealed class ThemeColors(
    val background: Color,
    val surface: Color,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val text: Color
) {
    data object Night : ThemeColors(
        background = BackgroundDark,
        surface = SurfaceDark,
        primary = DeepNavy,
        secondary = OceanBlue,
        tertiary = EmeraldGreen,
        text = TextPrimaryDark
    )

    data object Day : ThemeColors(
        background = BackgroundLight,
        surface = SurfaceLight,
        primary = DeepNavy,
        secondary = SlateGrey,
        tertiary = SoftGold,
        text = TextPrimaryLight
    )
}