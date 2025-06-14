package com.develop.traiscore.presentation.components.trainers


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.develop.traiscore.R
import com.develop.traiscore.presentation.theme.navbarDay
import com.develop.traiscore.presentation.viewmodels.ThemeViewModel

enum class TopBarType {
    CLIENTS,    // Para "Mis Clientes" - con Person icon, contador y refresh
    ROUTINES,   // Para "Rutinas" - con Share icon
    SETTINGS    // Para "Settings" - con Settings icon
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarTrainers(
    title: String,
    topBarType: TopBarType,
    modifier: Modifier = Modifier,
    // Parámetros opcionales según el tipo
    clientCount: Int = 0,
    onRefreshClick: (() -> Unit)? = null,
    onRightIconClick: (() -> Unit)? = null,
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Logo a la izquierda (siempre presente)
                Image(
                    painter = painterResource(id = R.drawable.tslogo),
                    contentDescription = "TraiScore Logo",
                    modifier = Modifier.size(36.dp)
                )

                // Título en el centro
                Text(
                    text = title,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )

                // Sección derecha - varía según el tipo
                when (topBarType) {
                    TopBarType.CLIENTS -> {
                        // Para "Mis Clientes": contador + refresh + person icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            // Botón de actualizar
                            if (onRefreshClick != null) {
                                IconButton(onClick = onRefreshClick) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = "Actualizar",
                                        tint = Color.White
                                    )
                                }
                            }

                            // Icono de persona
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Clientes",
                                tint = Color.White
                            )
                        }
                    }

                    TopBarType.ROUTINES -> {
                        // Para "Rutinas": solo share icon
                        IconButton(
                            onClick = onRightIconClick ?: {}
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Compartir",
                                tint = Color.White
                            )
                        }
                    }

                    TopBarType.SETTINGS -> {
                        // Para "Settings": solo settings icon
                        IconButton(
                            onClick = onRightIconClick ?: {}
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Configuración",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        modifier = modifier
    )
}

// Composables de conveniencia para cada tipo específico

@Composable
fun TopBarTrainersClients(
    title: String = "Mis Clientes",
    clientCount: Int,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopBarTrainers(
        title = title,
        topBarType = TopBarType.CLIENTS,
        clientCount = clientCount,
        onRefreshClick = onRefreshClick,
        modifier = modifier
    )
}

@Composable
fun TopBarTrainersRoutines(
    title: String = "Rutinas",
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopBarTrainers(
        title = title,
        topBarType = TopBarType.ROUTINES,
        onRightIconClick = onShareClick,
        modifier = modifier
    )
}

@Composable
fun TopBarTrainersSettings(
    title: String = "Configuración",
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopBarTrainers(
        title = title,
        topBarType = TopBarType.SETTINGS,
        onRightIconClick = onSettingsClick,
        modifier = modifier
    )
}