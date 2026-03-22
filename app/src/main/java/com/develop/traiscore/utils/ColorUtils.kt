package com.develop.traiscore.utils

import androidx.compose.ui.graphics.Color

private val DEFAULT_COLOR = Color(0xFF355E58)

fun hexToColor(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (_: Exception) {
    DEFAULT_COLOR
}

fun colorToHex(color: Color): String {
    val red = (color.red * 255).toInt()
    val green = (color.green * 255).toInt()
    val blue = (color.blue * 255).toInt()
    return String.format("#%02X%02X%02X", red, green, blue)
}
