package com.develop.traiscore.presentation.components

import androidx.compose.foundation.layout.height
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.viewmodels.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TraiScoreTopBar(
    leftIcon: @Composable () -> Unit = {},
    title: @Composable (() -> Unit)? = null,
    rightIcon: @Composable () -> Unit = {},
    showLogo: Boolean = true,
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

    CenterAlignedTopAppBar(
        title = {
            if (title != null) {
                // 👉 Mostrar título dinámico (ej: nombre de rutina)
                title()
            } else {
                // 👉 Mostrar logo TraiScore (comportamiento por defecto)
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = traiBlue)) {
                            append("Trai")
                        }
                        withStyle(
                            SpanStyle(
                                color = if (isDarkTheme) Color.White else Color.Black
                            )
                        ) {
                            append("Score")
                        }
                    },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        },
        navigationIcon = { leftIcon() },
        modifier = Modifier
            .height(70.dp),
        actions = { rightIcon() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}