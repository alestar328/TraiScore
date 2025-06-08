package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.develop.traiscore.R
import com.develop.traiscore.presentation.components.ThemeModeCard
import com.develop.traiscore.presentation.theme.TraiScoreTheme
import com.develop.traiscore.presentation.theme.TSStyle
import com.develop.traiscore.presentation.viewmodels.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenModeUI(
    onBack: () -> Unit = {},
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Logo en lugar de texto
                    Image(
                        painter = painterResource(id = R.drawable.trailogoup),
                        contentDescription = "Logo cabecera",
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(TraiScoreTheme.dimens.paddingMedium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título
            Text(
                text = "Esquema de colores",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Contenedor de las opciones de tema
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Modo Diurno
                ThemeModeCard(
                    title = "Modo diurno",
                    isSelected = !isDarkTheme,
                    onClick = { themeViewModel.setDarkTheme(false) },
                    modifier = Modifier.weight(1f)
                ) {
                     Image(
                        painter = painterResource(id = R.drawable.day_mode_pic),
                        contentDescription = "Modo diurno",
                       modifier = Modifier
                           .fillMaxWidth()
                           .height(200.dp),
                       contentScale = ContentScale.Crop
                    )

                    // Placeholder temporal con gradiente claro
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        TSStyle.background,
                                        TSStyle.accent
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Modo Diurno",
                            color = TSStyle.primaryBackgroundColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Modo Nocturno
                ThemeModeCard(
                    title = "Modo oscuro",
                    isSelected = isDarkTheme,
                    onClick = { themeViewModel.setDarkTheme(true) },
                    modifier = Modifier.weight(1f)
                ) {
                     Image(
                        painter = painterResource(id = R.drawable.dark_mode_pic),
                        contentDescription = "Modo nocturno",
                        modifier = Modifier
                          .fillMaxWidth()
                            .height(200.dp),
                      contentScale = ContentScale.Crop
                     )

                    // Placeholder temporal con gradiente oscuro
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        TSStyle.backgroundTopColor,
                                        TSStyle.backgroundBottomColor
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Modo Oscuro",
                            color = TSStyle.primaryText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Información adicional
            Text(
                text = "Selecciona el modo de visualización que prefieras. El cambio se aplicará inmediatamente.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Preview
@Composable
fun ScreenModeUIPreview() {
    TraiScoreTheme {
        ScreenModeUI()
    }
}
