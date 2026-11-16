package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class TarotExtendedColors(
  val goldAccent: Color,
  val goldSoft: Color,
  val goldBorder: Color,
  val celestialGlow: Color,
  val cardSurface: Color,
  val sanctuaryMist: Color,
  val uprightBadge: Color,
  val reversedBadge: Color,
  val isDark: Boolean,
)

val LocalTarotColors = staticCompositionLocalOf {
  TarotExtendedColors(
    goldAccent = SacredGold,
    goldSoft = SacredGoldSoft,
    goldBorder = SacredGoldLight,
    celestialGlow = CelestialBlue,
    cardSurface = SanctuaryPearlCard,
    sanctuaryMist = CelestialMist,
    uprightBadge = SageBlessing,
    reversedBadge = RoseQuartz,
    isDark = false,
  )
}

private val LightSanctuaryColorScheme =
  lightColorScheme(
    primary = SacredGold,
    onPrimary = Color.White,
    primaryContainer = SacredGoldSoft,
    onPrimaryContainer = Color(0xFF4A350B),
    secondary = MysticAmethyst,
    onSecondary = Color.White,
    secondaryContainer = MysticAmethystSoft,
    onSecondaryContainer = Color(0xFF342254),
    tertiary = CelestialBlue,
    onTertiary = Color.White,
    tertiaryContainer = CelestialMist,
    onTertiaryContainer = Color(0xFF1D3252),
    background = SanctuaryAlabaster,
    onBackground = SanctuaryIndigo,
    surface = SanctuaryPearlCard,
    onSurface = SanctuaryIndigo,
    surfaceVariant = SanctuaryParchment,
    onSurfaceVariant = SanctuaryTwilight,
    outline = SanctuaryOutline,
    outlineVariant = Color(0xFFECE4D7),
    error = Color(0xFFB84A5E),
    onError = Color.White,
  )

private val DarkSanctuaryColorScheme =
  darkColorScheme(
    primary = LuminousGold,
    onPrimary = Color(0xFF281E06),
    primaryContainer = LuminousGoldSoft,
    onPrimaryContainer = Color(0xFFF6DF9F),
    secondary = MidnightAmethyst,
    onSecondary = Color(0xFF24153D),
    secondaryContainer = Color(0xFF372756),
    onSecondaryContainer = Color(0xFFE7DAFF),
    tertiary = MidnightBlueGlow,
    onTertiary = Color(0xFF0E223D),
    tertiaryContainer = Color(0xFF20324F),
    onTertiaryContainer = Color(0xFFD5E5FF),
    background = MidnightVelvet,
    onBackground = MoonlitIvory,
    surface = MidnightTemple,
    onSurface = MoonlitIvory,
    surfaceVariant = MidnightCard,
    onSurfaceVariant = MoonlitLavender,
    outline = MidnightOutline,
    outlineVariant = Color(0xFF2C2645),
    error = Color(0xFFE58A9B),
    onError = Color(0xFF3E0A17),
  )

val TarotShapes =
  Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
  )

enum class SanctuaryThemeMode {
  LIGHT,
  DARK,
  SYSTEM,
}

@Composable
fun AureliaTarotTheme(
  themeMode: SanctuaryThemeMode = SanctuaryThemeMode.LIGHT,
  content: @Composable () -> Unit,
) {
  val useDark =
    when (themeMode) {
      SanctuaryThemeMode.LIGHT -> false
      SanctuaryThemeMode.DARK -> true
      SanctuaryThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

  val colorScheme = if (useDark) DarkSanctuaryColorScheme else LightSanctuaryColorScheme
  val extendedColors =
    if (useDark) {
      TarotExtendedColors(
        goldAccent = LuminousGold,
        goldSoft = LuminousGoldSoft,
        goldBorder = Color(0xFF7B6532),
        celestialGlow = MidnightBlueGlow,
        cardSurface = MidnightCard,
        sanctuaryMist = Color(0xFF1F233D),
        uprightBadge = Color(0xFF78C2A4),
        reversedBadge = Color(0xFFE08C9D),
        isDark = true,
      )
    } else {
      TarotExtendedColors(
        goldAccent = SacredGold,
        goldSoft = SacredGoldSoft,
        goldBorder = SacredGoldLight,
        celestialGlow = CelestialBlue,
        cardSurface = SanctuaryPearlCard,
        sanctuaryMist = CelestialMist,
        uprightBadge = SageBlessing,
        reversedBadge = RoseQuartz,
        isDark = false,
      )
    }

  CompositionLocalProvider(LocalTarotColors provides extendedColors) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = AppTypography,
      shapes = TarotShapes,
      content = content,
    )
  }
}
