package ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


private val lightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    scrim = ScrimLight,
    inverseSurface = InverseSurfaceLight,
    inverseOnSurface = InverseOnSurfaceLight,
    inversePrimary = InversePrimaryLight,
    surfaceDim = SurfaceDimLight,
    surfaceBright = SurfaceBrightLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
)

private val darkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    scrim = ScrimDark,
    inverseSurface = InverseSurfaceDark,
    inverseOnSurface = InverseOnSurfaceDark,
    inversePrimary = InversePrimaryDark,
    surfaceDim = SurfaceDimDark,
    surfaceBright = SurfaceBrightDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
)

// Black background color scheme - a variant of dark theme with true black background
private val blackBackgroundColorScheme = darkColorScheme.copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceVariant = Color(0xFF121212),
    surfaceDim = Color.Black,
    surfaceBright = Color(0xFF121212),
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color(0xFF0A0A0A),
    surfaceContainer = Color(0xFF121212),
    surfaceContainerHigh = Color(0xFF1E1E1E),
    surfaceContainerHighest = Color(0xFF2A2A2A)
)

/**
 * Light Android gradient colors
 */
val LightAndroidGradientColors = GradientColors(container = PrimaryContainerLight)

/**
 * Dark Android gradient colors
 */
val DarkAndroidGradientColors = GradientColors(container = PrimaryContainerDark)

/**
 * Black Android gradient colors
 */
val BlackAndroidGradientColors = GradientColors(container = Color.Black)

/**
 * Light Android background theme
 */
val LightAndroidBackgroundTheme = BackgroundTheme(color = PrimaryContainerLight)

/**
 * Dark Android background theme
 */
val DarkAndroidBackgroundTheme = BackgroundTheme(color = PrimaryContainerDark)

/**
 * Black Android background theme
 */
val BlackAndroidBackgroundTheme = BackgroundTheme(color = Color.Black)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(32.dp)
)


val LocalThemeIsDark = compositionLocalOf { mutableStateOf(true) }
val LocalUseBlackBackground = compositionLocalOf { mutableStateOf(false) }


@Composable
fun AppTheme(
    isDarkTheme: Boolean? = null,
    useBlackBackground: Boolean = false,
    content: @Composable() () -> Unit
) {
    val systemIsDark = isSystemInDarkTheme()
    val isDarkState = remember { mutableStateOf(systemIsDark) }
    val useBlackBackgroundState = remember { mutableStateOf(useBlackBackground) }

    // Используем переданное значение или системное
    isDarkState.value = isDarkTheme ?: systemIsDark
    useBlackBackgroundState.value = useBlackBackground

    val isDark by isDarkState
    val useBlack by useBlackBackgroundState

    // Определяем, нужно ли использовать черный фон
    // Черный фон применяется только в темной теме
    val shouldUseBlackBackground = useBlack && isDark

    // Выбираем цветовую схему в зависимости от настроек
    val colorScheme = when {
        !isDark -> lightColorScheme
        shouldUseBlackBackground -> blackBackgroundColorScheme
        else -> darkColorScheme
    }.switch()

    // Gradient colors
    val gradientColors = when {
        !isDark -> LightAndroidGradientColors
        shouldUseBlackBackground -> BlackAndroidGradientColors
        else -> DarkAndroidGradientColors
    }

    // Background theme
    val backgroundTheme = when {
        !isDark -> LightAndroidBackgroundTheme
        shouldUseBlackBackground -> BlackAndroidBackgroundTheme
        else -> DarkAndroidBackgroundTheme
    }

    val tintTheme = TintTheme(colorScheme.primary)


    // Composition locals
    CompositionLocalProvider(
        LocalThemeIsDark provides isDarkState,
        LocalUseBlackBackground provides useBlackBackgroundState,
        LocalGradientColors provides gradientColors,
        LocalBackgroundTheme provides backgroundTheme,
        LocalTintTheme provides tintTheme,
    ) {
        SystemAppearance(isDark = !isDark)
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}


@Suppress("NO_ACTUAL_FOR_EXPECT")
@Composable
internal expect fun SystemAppearance(isDark: Boolean)
