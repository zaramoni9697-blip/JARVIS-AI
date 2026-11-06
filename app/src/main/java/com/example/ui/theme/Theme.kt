package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val HologramDarkColorScheme = darkColorScheme(
  primary = HologramCyan,
  onPrimary = HologramDeepNavy,
  primaryContainer = Color(0xFF0E2A47),
  onPrimaryContainer = HologramCyanBright,
  secondary = HologramBlue,
  onSecondary = Color(0xFF031024),
  secondaryContainer = Color(0xFF132845),
  onSecondaryContainer = Color(0xFFBAE6FD),
  tertiary = HologramPurple,
  onTertiary = Color.White,
  tertiaryContainer = Color(0xFF2E1065),
  onTertiaryContainer = Color(0xFFE9D5FF),
  background = HologramDeepNavy,
  onBackground = HologramTextPrimary,
  surface = HologramSurfaceNavy,
  onSurface = HologramTextPrimary,
  surfaceVariant = HologramCardBg,
  onSurfaceVariant = HologramTextSecondary,
  outline = HologramCardBorder,
  outlineVariant = Color(0x3300E5FF),
  error = HologramRed,
  onError = Color.White
)

val HologramMatrixColorScheme = darkColorScheme(
  primary = HologramEmerald,
  onPrimary = Color(0xFF022C22),
  primaryContainer = Color(0xFF064E3B),
  onPrimaryContainer = Color(0xFFA7F3D0),
  secondary = HologramCyan,
  onSecondary = HologramDeepNavy,
  secondaryContainer = Color(0xFF062E3B),
  onSecondaryContainer = Color(0xFFBAE6FD),
  tertiary = HologramAmber,
  background = Color(0xFF040D09),
  onBackground = HologramTextPrimary,
  surface = Color(0xFF081A12),
  onSurface = HologramTextPrimary,
  surfaceVariant = Color(0xDD0D261B),
  onSurfaceVariant = HologramTextSecondary,
  outline = Color(0x6610B981)
)

val HologramAmethystColorScheme = darkColorScheme(
  primary = HologramPurple,
  onPrimary = Color.White,
  primaryContainer = Color(0xFF3B0764),
  onPrimaryContainer = Color(0xFFF3E8FF),
  secondary = HologramCyan,
  onSecondary = HologramDeepNavy,
  tertiary = HologramBlue,
  background = Color(0xFF0A0517),
  onBackground = HologramTextPrimary,
  surface = Color(0xFF13092A),
  onSurface = HologramTextPrimary,
  surfaceVariant = Color(0xDD1C0E3D),
  onSurfaceVariant = HologramTextSecondary,
  outline = Color(0x66A855F7)
)

val HologramLightColorScheme = lightColorScheme(
  primary = Color(0xFF0284C7),
  onPrimary = Color.White,
  primaryContainer = Color(0xFFE0F2FE),
  onPrimaryContainer = Color(0xFF0369A1),
  secondary = Color(0xFF0D9488),
  onSecondary = Color.White,
  tertiary = Color(0xFF7C3AED),
  background = HologramLightBg,
  onBackground = HologramLightText,
  surface = HologramLightSurface,
  onSurface = HologramLightText,
  surfaceVariant = HologramLightCard,
  onSurfaceVariant = Color(0xFF475569),
  outline = HologramLightBorder
)

@Composable
fun JarvisAITheme(
  themeMode: String = "CYAN_DEFAULT",
  isDarkTheme: Boolean = true,
  content: @Composable () -> Unit
) {
  val colorScheme = when {
    !isDarkTheme -> HologramLightColorScheme
    themeMode == "EMERALD_MATRIX" -> HologramMatrixColorScheme
    themeMode == "PURPLE_NEON" -> HologramAmethystColorScheme
    else -> HologramDarkColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
