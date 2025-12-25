package com.develop.traiscore.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.theme.traiOrange
import com.develop.traiscore.presentation.theme.tsColors
import com.develop.traiscore.presentation.viewmodels.ChronoState
import com.develop.traiscore.presentation.viewmodels.ChronoViewModel
import com.develop.traiscore.R

@Composable
fun ChronoScreen(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChronoViewModel = hiltViewModel()
) {
    val chronoState by viewModel.chronoState.collectAsState()
    val isCountDown by viewModel.isCountDown.collectAsState()
    val currentTime = viewModel.getCurrentDisplayTime()
    val displayTime by viewModel.run {
        if (isCountDown) remainingTime else elapsedTime
    }.collectAsState()

    // Fondo semi-transparente que cubre toda la pantalla
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { -it }, // Comienza desde arriba (valor negativo)
            animationSpec = tween(300)
        ),
        exit = slideOutVertically(
            targetOffsetY = { -it }, // Se va hacia arriba (valor negativo)
            animationSpec = tween(300)
        ),
        modifier = modifier.zIndex(10f) // Asegurar que esté por encima de todo
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)) // Fondo semi-transparente
                .clickable { onDismiss() } // Cerrar al tocar fuera
        ) {
            // Contenido del modal que se desliza desde arriba
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding() // Respetar la barra de estado
                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                    .background(MaterialTheme.tsColors.primaryBackgroundColor)
                    .clickable { } // Evitar que se cierre al tocar el contenido
                    .padding(20.dp)
            ) {
                // Header con título y botón de cerrar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isCountDown) "Cronómetro - Cuenta Atrás" else "Cronómetro",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar cronómetro",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onDismiss() }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Display del tiempo principal
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Tiempo principal en formato HH:MM:SS
                    Text(
                        text = viewModel.formatTime(displayTime),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = when {
                            isCountDown && displayTime <= 10000L -> Color.Red // Últimos 10 segundos en rojo
                            isCountDown -> traiOrange
                            else -> MaterialTheme.tsColors.ledCyan
                        },
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Estado actual
                    Text(
                        text = when (chronoState) {
                            ChronoState.STOPPED -> if (isCountDown) "Listo para cuenta atrás" else "Listo para comenzar"
                            ChronoState.RUNNING -> if (isCountDown) "Tiempo de descanso" else "Cronómetro activo"
                            ChronoState.PAUSED -> "En pausa"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Controles principales
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón de Reset
                    FloatingActionButton(
                        onClick = { viewModel.resetChrono() },
                        modifier = Modifier.size(56.dp),
                        containerColor = Color.Red.copy(alpha = 0.8f),
                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Botón principal Play/Pause
                    FloatingActionButton(
                        onClick = {
                            when (chronoState) {
                                ChronoState.STOPPED -> viewModel.startChrono()
                                ChronoState.RUNNING -> viewModel.pauseChrono()
                                ChronoState.PAUSED -> viewModel.resumeChrono()
                            }
                        },
                        modifier = Modifier.size(72.dp),
                        containerColor = when (chronoState) {
                            ChronoState.RUNNING -> traiOrange
                            else -> traiBlue
                        },
                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                    ) {
                        //                                Icons.Default.PlayArrow
                        Icon(
                            painter = if (chronoState == ChronoState.RUNNING) {
                                painterResource(R.drawable.pause_icon)
                            } else {
                                rememberVectorPainter(Icons.Default.PlayArrow)
                            },
                            contentDescription = if (chronoState == ChronoState.RUNNING) "Pausar" else "Iniciar",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Espacio para mantener simetría
                    Spacer(modifier = Modifier.size(56.dp))
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Botones de tiempo rápido
                Text(
                    text = "Tiempos de descanso rápidos:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuickTimeButton(
                        text = "1m",
                        onClick = { viewModel.setRestTime(1) }
                    )
                    QuickTimeButton(
                        text = "2m",
                        onClick = { viewModel.setRestTime(2) }
                    )
                    QuickTimeButton(
                        text = "3m",
                        onClick = { viewModel.setRestTime(3) }
                    )
                    QuickTimeButton(
                        text = "5m",
                        onClick = { viewModel.setRestTime(5) }
                    )
                }

                // Espaciado para evitar que se vea cortado
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun QuickTimeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.DarkGray,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}