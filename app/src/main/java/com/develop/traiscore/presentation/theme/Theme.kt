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
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = primaryBlack,
    secondary = gray,
    tertiary = lightGray,
    surface = primaryBlack,
    background = primaryBlack
)

private val LightColorScheme = lightColorScheme(
    primary = primaryWhite,
    secondary = gray,
    tertiary = lightGray,
    surface = primaryWhite,
    background = primaryWhite

)
private val LocalDimens = staticCompositionLocalOf { DefaultDimens }

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
    dynamicColor: Boolean = true,
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
    //Esto es añadido:
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