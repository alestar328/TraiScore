package com.develop.traiscore.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val primaryBlack = Color(0XFF000000);
val primaryWhite = Color(0xFFFFFFFF);
val gray = Color(0XFF888888);
val lightGray = Color(0XFFCCCCCC);

val traiBlue = Color(0xFF3AF2FF)
val traiBlueLabel = Color(0xFF29A7B0)
val traiBackgroundDark = Color(0xFF1C1C1C)
val traiBackgroundDay = Color(0xFFAFB6C0)

val navbarDay = Color(0xFF9EADA8)
val navbarDark = Color(0xFFAFB6C0)

val Black = Color(0xFF000113)
val LightBlueWhite = Color(0xFFF1F5F9) //Social media background
val BlueGray = Color(0xFF334155)

val traiOrange = Color(0xFFFFC36F)
val traiYellow = Color(0xFFFFE082)
object ColorUtils {
    /**
     * Crea un Color desde valores RGB individuales (0-255)
     */
    fun Color.Companion.fromRGB(red: Int, green: Int, blue: Int): Color {
        require(red in 0..255) { "Invalid red component" }
        require(green in 0..255) { "Invalid green component" }
        require(blue in 0..255) { "Invalid blue component" }
        return Color(red = red / 255f, green = green / 255f, blue = blue / 255f)
    }

    /**
     * Crea un Color desde un valor RGB hexadecimal
     */
    fun Color.Companion.fromRGB(rgb: Int): Color {
        val red = (rgb shr 16) and 0xFF
        val green = (rgb shr 8) and 0xFF
        val blue = rgb and 0xFF
        return Color(red = red / 255f, green = green / 255f, blue = blue / 255f)
    }

    /**
     * Crea un Color desde una cadena hexadecimal
     */
    fun Color.Companion.fromHex(hex: String): Color {
        val hexSanitized = hex.trim().removePrefix("#")
        val rgb = hexSanitized.toLong(16)
        val red = ((rgb and 0xFF0000) shr 16) / 255f
        val green = ((rgb and 0x00FF00) shr 8) / 255f
        val blue = (rgb and 0x0000FF) / 255f
        return Color(red = red, green = green, blue = blue)
    }
}
val ColorScheme.focusedTextFieldText
    @Composable
    get() = if(isSystemInDarkTheme()) Color.White else Color.Black

val ColorScheme.unfocusedTextFieldText
    @Composable
    get() = if(isSystemInDarkTheme()) Color(0xFF94A3B8) else Color(0xFF475569)

val ColorScheme.textFieldContainer
    @Composable
    get() = if(isSystemInDarkTheme()) BlueGray.copy(alpha = 0.6f) else LightBlueWhite