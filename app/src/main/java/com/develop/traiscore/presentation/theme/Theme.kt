package com.develop.traiscore.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.develop.traiscore.presentation.theme.ColorUtils.fromHex
import com.develop.traiscore.presentation.theme.ColorUtils.fromRGB

// Esquema de colores OSCURO usando TSStyle
private val DarkColorScheme = darkColorScheme(
    primary = TSStyle.primary,                    // Bright Cyan
    onPrimary = TSStyle.primaryBackgroundColor,   // Dark background for text on primary
    secondary = TSStyle.secondary,                // Dark Cyan
    onSecondary = TSStyle.primaryText,            // White text on secondary
    tertiary = TSStyle.callToAction,              // Orange for tertiary actions
    onTertiary = TSStyle.primaryBackgroundColor,  // Dark text on tertiary

    background = TSStyle.primaryBackgroundColor,  // Main dark background
    onBackground = TSStyle.primaryText,           // White text on background
    surface = TSStyle.secondaryBackgroundColor,   // Secondary dark surface
    onSurface = TSStyle.primaryText,              // White text on surface

    surfaceVariant = TSStyle.lightGray,           // Light gray for variants
    onSurfaceVariant = TSStyle.lightGrayText,     // Light gray text

    outline = TSStyle.secondaryText,              // Gray for outlines
    outlineVariant = TSStyle.lightGray,           // Light gray for outline variants

    error = Color.Red,                            // Keep default error color
    onError = Color.White,
    errorContainer = Color.Red.copy(alpha = 0.1f),
    onErrorContainer = Color.Red,

    inverseSurface = TSStyle.accent,              // White for inverse
    inverseOnSurface = TSStyle.primaryBackgroundColor,
    inversePrimary = TSStyle.primaryBackgroundColor
)

// Esquema de colores CLARO (para cuando no esté en modo oscuro)
private val LightColorScheme = lightColorScheme(
    primary = TSStyle.primary,                    // Bright Cyan
    onPrimary = TSStyle.primaryBackgroundColor,   // Dark text on primary
    secondary = TSStyle.secondary,                // Dark Cyan
    onSecondary = TSStyle.accent,                 // White text on secondary
    tertiary = TSStyle.callToAction,              // Orange for tertiary
    onTertiary = TSStyle.primaryBackgroundColor,  // Dark text on tertiary

    background = TSStyle.background,              // Light background
    onBackground = TSStyle.primaryBackgroundColor,// Dark text on light background
    surface = TSStyle.accent,                     // White surface
    onSurface = TSStyle.primaryBackgroundColor,   // Dark text on surface

    surfaceVariant = TSStyle.background,          // Light gray surface variant
    onSurfaceVariant = TSStyle.secondaryText,     // Gray text on light surface

    outline = TSStyle.lightGray,                  // Gray outlines
    outlineVariant = TSStyle.lightGrayText,       // Light gray outline variants

    error = Color.Red,
    onError = Color.White,
    errorContainer = Color.Red.copy(alpha = 0.1f),
    onErrorContainer = Color.Red,

    inverseSurface = TSStyle.primaryBackgroundColor,
    inverseOnSurface = TSStyle.primaryText,
    inversePrimary = TSStyle.primary
)

private val LocalDimens = staticCompositionLocalOf { DefaultDimens }

object TSStyle {
    val tacaOrange = Color.fromRGB(0xf29724)
    val primaryBackgroundColor = Color.fromRGB(0x1C1C1D)
    val secondaryBackgroundColor = Color.fromRGB(0x39383B)

    val primaryText = Color.White
    val secondaryText = Color.fromRGB(0x96979F)

    val backgroundTopColor = Color.fromRGB(0x3b3c3f)
    val backgroundBottomColor = Color.fromRGB(0x10131b)

    val lightGray = Color.fromRGB(0x4e4f53)
    val lightGrayText = Color.fromRGB(0xb6bac0)

    val progressColor = Color.fromRGB(0x43f4ff)

    // Colores LED
    val ledCyan = Color.fromRGB(0x43f4ff)
    val ledPurple = Color.fromRGB(0xc6b2ff)
    val ledOrange = Color.fromRGB(0xffd3ba)
    val ledGreen = Color.fromRGB(0x69f390)
    val ledGray = Color.fromRGB(0xc4c4c4)

    val emptyStateColor = Color.fromRGB(0xd9c0a7)
    val badgeNewColor = Color.fromRGB(0xd9c0a7)
    val modalBackgroundColor = Color.fromHex("#101010")

    // Colores del tema principal
    val primary = Color.fromHex("#43f4ff")        // Bright Cyan
    val secondary = Color.fromHex("#009fb7")      // Dark Cyan
    val background = Color.fromHex("#f7f7f7")     // Soft Gray
    val accent = Color.fromHex("#ffffff")         // White
    val callToAction = Color.fromHex("#FFA726")   // Vibrant Orange
    val headerFooter = Color.fromHex("#0D47A1")   // Calm Navy Blue
}

//Proveedor local que establece las dimensiones del proyecto
@Composable
fun ProvideDimens(
    dimens: Dimens,
    content: @Composable () -> Unit
){
    val dimensionSet = remember { dimens }
    CompositionLocalProvider(LocalDimens provides dimensionSet, content = content)
}

@Composable
fun TraiScoreTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Cambiado a false para usar nuestros colores personalizados
    windowSize: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Configuración de dimensiones basada en el tamaño de ventana
    val dimensions = if(windowSize > WindowWidthSizeClass.Compact)
        TabletDimens
    else
        DefaultDimens

    ProvideDimens(dimens = dimensions){
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

object TraiScoreTheme{
    val dimens: Dimens
        @Composable
        @ReadOnlyComposable
        get() = LocalDimens.current
}

// Extensión para acceder fácilmente a los colores personalizados de TSStyle
val MaterialTheme.tsColors: TSStyleColors
    @Composable
    @ReadOnlyComposable
    get() = TSStyleColors

object TSStyleColors {
    val tacaOrange @Composable get() = TSStyle.tacaOrange
    val primaryBackgroundColor @Composable get() = TSStyle.primaryBackgroundColor
    val secondaryBackgroundColor @Composable get() = TSStyle.secondaryBackgroundColor
    val primaryText @Composable get() = TSStyle.primaryText
    val secondaryText @Composable get() = TSStyle.secondaryText
    val backgroundTopColor @Composable get() = TSStyle.backgroundTopColor
    val backgroundBottomColor @Composable get() = TSStyle.backgroundBottomColor
    val lightGray @Composable get() = TSStyle.lightGray
    val lightGrayText @Composable get() = TSStyle.lightGrayText
    val progressColor @Composable get() = TSStyle.progressColor
    val ledCyan @Composable get() = TSStyle.ledCyan
    val ledPurple @Composable get() = TSStyle.ledPurple
    val ledOrange @Composable get() = TSStyle.ledOrange
    val ledGreen @Composable get() = TSStyle.ledGreen
    val ledGray @Composable get() = TSStyle.ledGray
    val emptyStateColor @Composable get() = TSStyle.emptyStateColor
    val badgeNewColor @Composable get() = TSStyle.badgeNewColor
    val modalBackgroundColor @Composable get() = TSStyle.modalBackgroundColor
    val callToAction @Composable get() = TSStyle.callToAction
    val headerFooter @Composable get() = TSStyle.headerFooter
}