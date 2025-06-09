package com.develop.traiscore.presentation.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.develop.traiscore.domain.model.ExerciseAchievement
import com.develop.traiscore.domain.model.OverallAchievement
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.theme.traiOrange
import com.develop.traiscore.presentation.viewmodels.AchievementsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchivementsUI(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AchievementsViewModel = hiltViewModel(),
    clientId: String? = null // Para mostrar logros de un cliente específico
) {
    // Configurar ViewModel para el usuario/cliente correcto
    LaunchedEffect(clientId) {
        if (clientId != null) {
            viewModel.setTargetUser(clientId)
        }
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            modifier = modifier,
            containerColor = Color.DarkGray,
            contentColor = Color.White,
            dragHandle = {
                // Handle personalizado
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(
                                Color.Gray,
                                RoundedCornerShape(2.dp)
                            )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        ) {
            AchievementsContent(
                onDismiss = onDismiss,
                viewModel = viewModel,
                modifier = Modifier.fillMaxHeight(0.9f)
            )
        }
    }
}

@Composable
private fun AchievementsContent(
    onDismiss: () -> Unit,
    viewModel: AchievementsViewModel,
    modifier: Modifier = Modifier
) {
    val achievementsData by viewModel.achievementsData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🏆 Logros",
                style = MaterialTheme.typography.headlineMedium,
                color = traiBlue,
                fontWeight = FontWeight.Bold
            )

            Row {
                IconButton(
                    onClick = { viewModel.loadAchievements() }
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Actualizar",
                        tint = traiBlue
                    )
                }

                IconButton(
                    onClick = onDismiss
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Contenido principal
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = traiBlue)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Calculando logros...", color = Color.White)
                    }
                }
            }

            errorMessage != null -> {
                ErrorContent(
                    message = errorMessage ?: "Error desconocido",
                    onRetry = {
                        viewModel.clearError()
                        viewModel.loadAchievements()
                    }
                )
            }

            achievementsData != null -> {
                AchievementsSuccessContent(achievementsData!!)
            }

            else -> {
                EmptyContent()
            }
        }
    }
}

@Composable
private fun AchievementsSuccessContent(
    data: com.develop.traiscore.domain.model.AchievementsData
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Logro General
        item {
            OverallAchievementCard(data.overallAchievement)
        }

        // Título Top Ejercicios
        item {
            Text(
                text = "🥇 Top 10 Ejercicios",
                style = MaterialTheme.typography.titleLarge,
                color = traiBlue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Lista de ejercicios
        if (data.topExercises.isEmpty()) {
            item {
                EmptyExercisesCard()
            }
        } else {
            itemsIndexed(data.topExercises) { index, achievement ->
                ExerciseAchievementCard(
                    achievement = achievement,
                    rank = index + 1
                )
            }
        }

        // Espacio final para scroll
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun OverallAchievementCard(
    achievement: OverallAchievement
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Equivalencia principal
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = achievement.equivalence.emoji,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(end = 12.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = achievement.equivalence.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = traiOrange,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = achievement.getFormattedTotalWeight(),
                        style = MaterialTheme.typography.titleMedium,
                        color = traiBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = achievement.equivalence.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Estadísticas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Entrenamientos",
                    value = achievement.totalWorkouts.toString()
                )
                StatItem(
                    label = "Ejercicios",
                    value = achievement.differentExercises.toString()
                )
            }

            // Progreso hacia siguiente meta
            achievement.nextGoal?.let { nextGoal ->
                Spacer(modifier = Modifier.height(16.dp))

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Próxima meta:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Text(
                            text = "${achievement.getProgressPercentage()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = traiBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val animatedProgress by animateFloatAsState(
                        targetValue = achievement.progressToNext,
                        animationSpec = tween(durationMillis = 1000),
                        label = "progress_animation"
                    )

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = traiBlue,
                        trackColor = Color.Gray.copy(alpha = 0.3f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${nextGoal.emoji} ${nextGoal.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Text(
                            text = "Faltan ${achievement.getFormattedRemainingWeight()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseAchievementCard(
    achievement: ExerciseAchievement,
    rank: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(
            containerColor = when (rank) {
                1 -> Color(0xFF1A1A1A) // Más destacado para el #1
                2, 3 -> Color(0xFF2A2A2A) // Destacado para top 3
                else -> Color.DarkGray.copy(alpha = 0.6f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ranking
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = when (rank) {
                            1 -> Color(0xFFFFD700) // Oro
                            2 -> Color(0xFFC0C0C0) // Plata
                            3 -> Color(0xFFCD7F32) // Bronce
                            else -> traiBlue
                        },
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#$rank",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información del ejercicio
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.exerciseName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = achievement.getFormattedWeight(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = traiBlue,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = achievement.getWorkoutCountText(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                achievement.lastWorkoutDate?.let { date ->
                    Text(
                        text = "Último: $date",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Equivalencia
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = achievement.equivalence.emoji,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = achievement.equivalence.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = traiOrange,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(80.dp)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = traiBlue,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
private fun EmptyExercisesCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.DarkGray.copy(alpha = 0.6f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "💪",
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "¡Empieza a entrenar!",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Tus logros aparecerán aquí cuando comiences a registrar entrenamientos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun EmptyContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🏆",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No hay datos disponibles",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Text(
                text = "Comienza a entrenar para ver tus logros",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.Red.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⚠️",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Error al cargar logros",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = traiBlue
                )
            ) {
                Text("Reintentar")
            }
        }
    }
}