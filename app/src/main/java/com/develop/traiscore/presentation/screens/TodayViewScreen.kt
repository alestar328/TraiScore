package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.develop.traiscore.R
import com.develop.traiscore.data.local.entity.WorkoutEntry
import com.develop.traiscore.presentation.components.QuickStats
import com.develop.traiscore.presentation.components.SessionCard
import com.develop.traiscore.presentation.components.SessionWorkoutCard
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

    // Estados del ViewModel
    val hasActiveSession by viewModel.hasActiveSession.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val finishedSessions by viewModel.finishedSessions.collectAsState()
    val availableSessions by viewModel.availableSessions.collectAsState() // NUEVO

    var showEndSessionDialog by remember { mutableStateOf(false) }

    val todayWorkouts = groupedEntries[todayFormatted] ?: emptyList()

    LaunchedEffect(Unit) {
        viewModel.checkForActiveSession()
        viewModel.loadAvailableSessions()
    }

    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            println("Error: $errorMessage")
            viewModel.clearError()
        }
    }

    fun hexToColor(hex: String): Color {
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: Exception) {
            Color(0xFF355E58)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        TodayHeaderSection(
            hasActiveSession = hasActiveSession,
            activeSession = activeSession,
            onEndSession = { showEndSessionDialog = true }
        )

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.tsColors.ledCyan
            )
        }

        // QuickStats solo si hay sesión activa y workouts de hoy
        if (hasActiveSession && todayWorkouts.isNotEmpty()) {
            val activeSessionWorkouts = todayWorkouts.filter {
                it.sessionId == activeSession?.id
            }

            if (activeSessionWorkouts.isNotEmpty()) {
                val totalSeries = activeSessionWorkouts.size
                val totalReps = activeSessionWorkouts.sumOf { it.reps }
                val totalWeight = activeSessionWorkouts.sumOf { it.weight.toDouble() }
                val totalExercises = activeSessionWorkouts
                    .groupBy { if (it.exerciseId > 0) it.exerciseId else it.title }
                    .keys.size

                QuickStats(
                    totalExercises = totalExercises,
                    totalSeries = totalSeries,
                    totalReps = totalReps,
                    totalWeight = totalWeight
                )
            }
        }

        // Lista principal
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Sesiones terminadas (solo mostrar si no hay sesión activa)
            if (availableSessions.isNotEmpty() && !hasActiveSession) {
                item {
                    Text(
                        text = stringResource(id = R.string.exer_screen_my_sessions),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(availableSessions) { session ->
                    val sessionName = session["name"] as? String ?: "Sesión"
                    val sessionColor = session["color"] as? String ?: "#355E58"
                    val sessionId = session["sessionId"] as? String ?: ""

                    SessionCard(
                        title = sessionName,
                        accent = hexToColor(sessionColor),
                        sessionId = sessionId, // ✅ Pasar sessionId
                        onClick = {
                            viewModel.activateSession(sessionId)
                        },
                        onDelete = { id -> // ✅ Añadir callback de borrado
                            viewModel.deleteSession(id)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Workouts agrupados por sesión (solo del día actual)
            if (hasActiveSession && todayWorkouts.isNotEmpty()) {
                val workoutsBySession = todayWorkouts.groupBy { workout ->
                    workout.sessionId ?: "legacy_session"
                }


                val activeSessionWorkouts = workoutsBySession.filter { (sessionId, _) ->
                    sessionId == activeSession?.id
                }

                if (activeSessionWorkouts.isNotEmpty()) {
                    items(activeSessionWorkouts.entries.toList()) { (sessionId, sessionWorkouts) ->
                        val firstWorkout = sessionWorkouts.first()
                        val sessionName = firstWorkout.sessionName ?: "Entrenamiento"
                        val sessionColor = firstWorkout.sessionColor ?: "#43f4ff"

                        SessionWorkoutCard(
                            sessionName = sessionName,
                            sessionColor = sessionColor,
                            workouts = sessionWorkouts,
                            isActive = true,
                            onEditClick = onEditClick,
                            onDeleteClick = onDeleteClick,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                    }
                }
            } else if (!hasActiveSession && availableSessions.isEmpty()) {
                    item {
                        EmptyTodayState(
                            viewModel = viewModel,
                            availableSessionsCount = availableSessions.size

                        )
                    }

            }
        }


    }

    // Dialog de confirmación
    if (showEndSessionDialog) {
        AlertDialog(
            onDismissRequest = { showEndSessionDialog = false },
            title = { Text(stringResource(id = R.string.today_end_session)) },
            text = {
                Text(
                    stringResource(id = R.string.today_closing_message) + "${activeSession?.name ?: ""}\"?\n\n" +
                            stringResource(id = R.string.today_closing_message2)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.endCurrentSession()
                        showEndSessionDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text(stringResource(id = R.string.today_end))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndSessionDialog = false }) {
                    Text(stringResource(id = R.string.today_cancel))
                }
            }
        )
    }
}
// ⭐ COMPONENTE SEPARADO para el header inteligente
@Composable
private fun TodayHeaderSection(
    hasActiveSession: Boolean,
    activeSession: com.develop.traiscore.presentation.viewmodels.ActiveSession?,
    onEndSession: () -> Unit = {}
) {
    if (hasActiveSession && activeSession != null) {
        // ✅ Mostrar sesión activa con color personalizado
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = activeSession.color.copy(alpha = 1f)
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
                // Icono de pesa en negro
                Icon(
                    painter = painterResource(id = R.drawable.pesa_icon),
                    contentDescription = stringResource(id = R.string.today_session_active),
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    // Nombre de la sesión
                    Text(
                        text = activeSession.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )

                    // Fecha actual
                    Text(
                        text = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
                            .format(Calendar.getInstance().time),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                }

                // ⭐ NUEVO: Botón para terminar sesión
                IconButton(
                    onClick = onEndSession,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(id = R.string.today_close_session),
                        tint = Color.Red.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
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
                    contentDescription = stringResource(id = R.string.today_today),
                    tint = MaterialTheme.tsColors.ledCyan,
                    modifier = Modifier.size(32.dp)
                )

                Column {
                    Text(
                        text = stringResource(id = R.string.exer_screen_welcome_phrase),
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
    viewModel: NewSessionViewModel,
    availableSessionsCount: Int
) {
    var showNewSessionDialog by remember { mutableStateOf(false) }
    val canAddMoreSessions = availableSessionsCount < 6


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
                contentDescription = stringResource(id = R.string.exer_no_trainings),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = stringResource(id = R.string.exer_screen_welcome_phrase),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (canAddMoreSessions) {
                    stringResource(id = R.string.today_no_data_start)
                } else {
                    stringResource(id = R.string.today_limit_data)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            if (canAddMoreSessions) {
                FilledTonalButton(
                    onClick = { showNewSessionDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.tsColors.ledCyan)
                ) {
                    Text(stringResource(id = R.string.session_new_session))
                }
            }
        }
    }

    if (showNewSessionDialog) {
        NewSessionUX(
            onDismiss = { showNewSessionDialog = false },
            onSessionCreated = {
                showNewSessionDialog = false
            },
            viewModel = viewModel
        )
    }
}