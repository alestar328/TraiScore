package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.develop.traiscore.R
import com.develop.traiscore.data.local.entity.WorkoutEntry
import com.develop.traiscore.presentation.components.ActiveSessionCard
import com.develop.traiscore.presentation.components.QuickStats
import com.develop.traiscore.presentation.components.WorkoutCardList
import com.develop.traiscore.presentation.components.general.NewSessionUX
import com.develop.traiscore.presentation.theme.tsColors
import com.develop.traiscore.presentation.viewmodels.NewSessionViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

@Composable
fun TodayViewScreen(
    groupedEntries: Map<String, List<WorkoutEntry>>,
    onEditClick: (WorkoutEntry) -> Unit,
    onDeleteClick: (WorkoutEntry) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NewSessionViewModel = hiltViewModel()
) {
    val today = LocalDate.now()
    val todayFormatted = remember {
        val calendar = Calendar.getInstance()
        calendar.set(today.year, today.monthValue - 1, today.dayOfMonth)
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(calendar.time)
    }

    // ⭐ Estados del ViewModel
    val hasActiveSession by viewModel.hasActiveSession.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()

    val todayWorkouts = groupedEntries[todayFormatted] ?: emptyList()

    // ⭐ Verificar sesión activa al iniciar
    LaunchedEffect(Unit) {
        viewModel.checkForActiveSession()
    }

    // Estadísticas del día
    val totalExercises = todayWorkouts
        .groupBy { workout ->
            if (workout.exerciseId > 0) workout.exerciseId else workout.title
        }
        .keys
        .size

    val totalSeries = todayWorkouts.size
    val totalReps = todayWorkouts.sumOf { it.reps }
    val totalWeight = todayWorkouts.sumOf { it.weight.toDouble() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ⭐ HEADER INTELIGENTE - Muestra sesión activa o estado normal
        TodayHeaderSection(
            hasActiveSession = hasActiveSession,
            activeSession = activeSession
        )

        // Estadísticas rápidas
        if (todayWorkouts.isNotEmpty()) {
            QuickStats(
                totalExercises = totalExercises,
                totalSeries = totalSeries,
                totalReps = totalReps,
                totalWeight = totalWeight
            )
        }

        // Lista de ejercicios
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (todayWorkouts.isNotEmpty()) {
                item {
                    Text(
                        text = "Ejercicios Realizados",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                item {
                    WorkoutCardList(
                        workouts = todayWorkouts,
                        onEditClick = onEditClick,
                        onDeleteClick = onDeleteClick
                    )
                }
            } else {
                // ⭐ SOLO mostrar EmptyState si NO hay sesión activa
                if (!hasActiveSession) {
                    item {
                        EmptyTodayState(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

// ⭐ COMPONENTE SEPARADO para el header inteligente
@Composable
private fun TodayHeaderSection(
    hasActiveSession: Boolean,
    activeSession: com.develop.traiscore.presentation.viewmodels.ActiveSession?
) {
    if (hasActiveSession && activeSession != null) {
        // ✅ Mostrar sesión activa con color personalizado
        ActiveSessionCard(
            sessionName = activeSession.name,
            sessionColor = activeSession.color
        )
    } else {
        // ❌ Mostrar header normal
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.tsColors.ledCyan.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.today_icon),
                    contentDescription = "Hoy",
                    tint = MaterialTheme.tsColors.ledCyan,
                    modifier = Modifier.size(32.dp)
                )

                Column {
                    Text(
                        text = "Entrenamientos de Hoy",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
                            .format(Calendar.getInstance().time),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

// ⭐ EmptyState actualizado para usar el ViewModel
@Composable
private fun EmptyTodayState(
    viewModel: NewSessionViewModel
) {
    var showNewSessionDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Sin entrenamientos",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = "¡Es hora de entrenar!",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "No tienes entrenamientos registrados para hoy. ¡Agrega tu primer ejercicio!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            FilledTonalButton(
                onClick = { showNewSessionDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.tsColors.ledCyan)
            ) {
                Text("Nueva Sesión")
            }
        }
    }

    if (showNewSessionDialog) {
        NewSessionUX(
            onDismiss = { showNewSessionDialog = false },
            onSessionCreated = {
                // ⭐ La UI se actualiza automáticamente por el StateFlow
                showNewSessionDialog = false
            },
            viewModel = viewModel  // ⭐ Usar el mismo ViewModel
        )
    }
}