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

val Black = Color(0xFF000113)
val LightBlueWhite = Color(0xFFF1F5F9) //Social media background
val BlueGray = Color(0xFF334155)

val ColorScheme.focusedTextFieldText
    @Composable
    get() = if(isSystemInDarkTheme()) Color.White else Color.Black

val ColorScheme.unfocusedTextFieldText
    @Composable
    get() = if(isSystemInDarkTheme()) Color(0xFF94A3B8) else Color(0xFF475569)

val ColorScheme.textFieldContainer
    @Composable
    get() = if(isSystemInDarkTheme()) BlueGray.copy(alpha = 0.6f) else LightBlueWhite