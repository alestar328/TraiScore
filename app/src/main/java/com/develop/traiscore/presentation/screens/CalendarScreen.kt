package com.develop.traiscore.presentation.screens

import android.graphics.drawable.Icon
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.develop.traiscore.R
import com.develop.traiscore.data.local.entity.RoutineHistoryEntity
import com.develop.traiscore.data.local.entity.WorkoutEntry
import com.develop.traiscore.presentation.components.QuickStats
import com.develop.traiscore.presentation.components.WorkoutCardList
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.theme.tsColors
import com.develop.traiscore.presentation.viewmodels.AddExerciseViewModel
import com.develop.traiscore.presentation.viewmodels.CalendarMode
import com.develop.traiscore.presentation.viewmodels.RoutineViewModel
import com.develop.traiscore.presentation.viewmodels.SessionWithWorkouts
import com.develop.traiscore.presentation.viewmodels.WorkoutEntryViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

data class MonthYear(
    val year: Int,
    val month: Int // 1-12 (Enero = 1, Diciembre = 12)
) {
    companion object {
        fun now(): MonthYear {
            val calendar = Calendar.getInstance()
            return MonthYear(
                year = calendar.get(Calendar.YEAR),
                month = calendar.get(Calendar.MONTH) + 1 // Calendar usa 0-11
            )
        }
    }

    fun atDay(day: Int): String {
        return String.format("%04d-%02d-%02d", year, month, day)
    }

    fun lengthOfMonth(): Int {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
}

@Composable
fun CalendarScreen(
    mode: CalendarMode = CalendarMode.SESSIONS, // ✅ NUEVO: Por defecto sesiones
    groupedEntries: Map<String, List<WorkoutEntry>>,
    routineDates: Set<String> = emptySet(), // ✅ NUEVO: Solo para modo ROUTINES
    selectedMonth: MonthYear?,
    onEditClick: (WorkoutEntry) -> Unit,
    onDeleteClick: (WorkoutEntry) -> Unit,
    modifier: Modifier = Modifier,
    workoutEntryViewModel: WorkoutEntryViewModel = hiltViewModel(),
    routineViewModel: RoutineViewModel = hiltViewModel()
) {
    val selectedDate = remember { mutableStateOf("") }
    val exerciseVM: AddExerciseViewModel = hiltViewModel()
    val exerciseNames by exerciseVM.exerciseNames.collectAsState()
    // ✅ CONDICIONAL: Solo cargar sesiones si estamos en modo SESSIONS
    val sessionWorkouts = if (mode == CalendarMode.SESSIONS) {
        workoutEntryViewModel.sessionWorkouts.value
    } else {
        emptyMap()
    }

    // ✅ Calcular workout del día seleccionado (solo modo SESSIONS)
    val selectedDayWorkouts = remember(selectedDate.value, groupedEntries, mode) {
        if (mode == CalendarMode.SESSIONS && selectedDate.value.isNotEmpty()) {
            try {
                val parts = selectedDate.value.split("-")
                if (parts.size == 3) {
                    val year = parts[0].toInt()
                    val month = parts[1].toInt() - 1
                    val day = parts[2].toInt()
                    val calendar = Calendar.getInstance()
                    calendar.set(year, month, day, 0, 0, 0)
                    calendar.set(Calendar.MILLISECOND, 0)

                    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val formattedDate = formatter.format(calendar.time)
                    groupedEntries[formattedDate] ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    // ✅ Sesiones del día (solo modo SESSIONS)
    val selectedDaySessions = remember(selectedDate.value, sessionWorkouts, mode) {
        if (mode == CalendarMode.SESSIONS && selectedDate.value.isNotEmpty()) {
            try {
                val parts = selectedDate.value.split("-")
                if (parts.size == 3) {
                    val year = parts[0].toInt()
                    val month = parts[1].toInt() - 1
                    val day = parts[2].toInt()
                    val calendar = Calendar.getInstance()
                    calendar.set(year, month, day, 0, 0, 0)
                    calendar.set(Calendar.MILLISECOND, 0)

                    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val formattedDate = formatter.format(calendar.time)
                    sessionWorkouts[formattedDate] ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    // ✅ NUEVO: Rutinas del día (solo modo ROUTINES)
    var selectedDayRoutines by remember { mutableStateOf<List<RoutineHistoryEntity>>(emptyList()) }

    LaunchedEffect(selectedDate.value, mode) {
        if (mode == CalendarMode.ROUTINES && selectedDate.value.isNotEmpty()) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            userId?.let { id ->
                routineViewModel.getRoutinesByDate(id, selectedDate.value) { routines ->
                    selectedDayRoutines = routines
                }
            }
        }
    }

    // Estadísticas (solo modo SESSIONS)
    val totalExercises = remember(selectedDayWorkouts) {
        if (selectedDayWorkouts.isNotEmpty()) {
            selectedDayWorkouts
                .groupBy { workout ->
                    if (workout.exerciseId > 0) workout.exerciseId else workout.title
                }
                .keys
                .size
        } else {
            0
        }
    }
    val totalSeries = remember(selectedDayWorkouts) { selectedDayWorkouts.size }
    val totalReps = remember(selectedDayWorkouts) { selectedDayWorkouts.sumOf { it.reps } }
    val totalWeight = remember(selectedDayWorkouts) { selectedDayWorkouts.sumOf { it.weight.toDouble() } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HorizontalDaysScrollCompat(
            selectedDate = selectedDate,
            selectedMonth = selectedMonth,
            sessionWorkouts = sessionWorkouts,
            routineDates = routineDates,
            mode = mode, // ✅ NUEVO
            onDateSelected = { date ->
                selectedDate.value = date
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        // ✅ QuickStats solo en modo SESSIONS
        if (mode == CalendarMode.SESSIONS && selectedDayWorkouts.isNotEmpty()) {
            QuickStats(
                totalExercises = totalExercises,
                totalSeries = totalSeries,
                totalReps = totalReps,
                totalWeight = totalWeight
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ✅ MODO SESSIONS
            if (mode == CalendarMode.SESSIONS) {
                if (selectedDaySessions.isNotEmpty()) {
                    items(
                        items = selectedDaySessions,
                        key = { session -> session.sessionId }
                    ) { session ->
                        SessionDayCard(
                            session = session,
                            onEditClick = onEditClick,
                            onDeleteClick = onDeleteClick
                        )
                    }
                } else if (selectedDayWorkouts.isNotEmpty()) {
                    item {
                        val parts = selectedDate.value.split("-")
                        val calendar = Calendar.getInstance()
                        if (parts.size == 3) {
                            calendar.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                        }
                        val dayFormatter = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
                        Text(
                            text = "Ejercicios de ${dayFormatter.format(calendar.time)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                        )
                    }
                    item {
                        WorkoutCardList(
                            workouts = selectedDayWorkouts,
                            onEditClick = onEditClick,
                            onDeleteClick = onDeleteClick
                        )
                    }
                } else {
                    item {
                        val isToday = selectedDate.value == getTodayDateString()
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isToday) {
                                        stringResource(id = R.string.calendar_month_no_data)
                                    } else {
                                        stringResource(id = R.string.calendar_month_no_data_that_day)
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
            // ✅ MODO ROUTINES
            else if (mode == CalendarMode.ROUTINES) {
                if (selectedDayRoutines.isNotEmpty()) {
                    item {
                        val parts = selectedDate.value.split("-")
                        val calendar = Calendar.getInstance()
                        if (parts.size == 3) {
                            calendar.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                        }
                        val dayFormatter = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
                        Text(
                            text = "Rutinas guardadas - ${dayFormatter.format(calendar.time)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = traiBlue,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                        )
                    }

                    items(
                        items = selectedDayRoutines,
                        key = { it.id }
                    ) { routine ->
                        RoutineHistoryCard(
                            routine = routine,
                            routineViewModel = routineViewModel,
                            exerciseNames = exerciseNames
                        )
                    }
                } else {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay rutinas guardadas en esta fecha",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ✅ NUEVO: Card para mostrar rutinas guardadas
@Composable
private fun RoutineHistoryCard(
    routine: RoutineHistoryEntity,
    routineViewModel: RoutineViewModel,
    exerciseNames: List<String>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    // ✅ Parseo 1 vez por rutina (fluido)
    val sections = remember(routine.sectionsSnapshot) {
        routineViewModel.deserializeSectionsSnapshot(routine.sectionsSnapshot)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = routine.routineName,
                        style = MaterialTheme.typography.titleMedium,
                        color = traiBlue,
                        fontWeight = FontWeight.Bold
                    )

                    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                    Text(
                        text = timeFormatter.format(Date(routine.savedTimestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expandir",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {

                    if (sections.isEmpty()) {
                        Text(
                            text = "Snapshot vacío",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        return@Column
                    }

                    // ✅ Renderizar tablas por sección
                    sections.forEach { sec ->
                        Text(
                            text = sec.type,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 6.dp, top = 6.dp)
                        )

                        RoutineTable(
                            exercises = sec.exercises,
                            exerciseNames = exerciseNames,
                            onDeleteExercise = {},
                            onFieldChanged = { _, _, _ -> }, // read-only “lógico” (no guardamos cambios)
                            onSeriesChanged = { _, _ -> },
                            onWeightChanged = { _, _ -> },
                            onRepsChanged = { _, _ -> },
                            onRirChanged = { _, _ -> },
                            enableSwipe = false,
                            validateInput = routineViewModel::validateInput,
                            bottomPadding = 8.dp
                        )
                    }
                }
            }
        }
    }
}

// ✅ Función helper para obtener fecha de hoy en formato yyyy-MM-dd
private fun getTodayDateString(): String {
    val calendar = Calendar.getInstance()
    return String.format(
        "%04d-%02d-%02d",
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH)
    )
}

@Composable
private fun SessionDayCard(
    session: SessionWithWorkouts,
    onEditClick: (WorkoutEntry) -> Unit,
    onDeleteClick: (WorkoutEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val defaultCyanColor = MaterialTheme.tsColors.ledCyan

    val sessionColor = try {
        Color(android.graphics.Color.parseColor(session.sessionColor))
    } catch (e: Exception) {
        defaultCyanColor
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Header compacto de la sesión
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(sessionColor, CircleShape)
            )

            Text(
                text = session.sessionName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
        }

        WorkoutCardList(
            workouts = session.workouts,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick
        )
    }
}

@Composable
fun HorizontalDaysScrollCompat(
    selectedDate: MutableState<String>,
    selectedMonth: MonthYear?,
    sessionWorkouts: Map<String, List<SessionWithWorkouts>>,
    routineDates: Set<String>,
    mode: CalendarMode, // ✅ NUEVO
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentCalendar = remember { Calendar.getInstance() }
    val displayCalendar = remember(selectedMonth) {
        Calendar.getInstance().apply {
            if (selectedMonth != null) {
                set(Calendar.YEAR, selectedMonth.year)
                set(Calendar.MONTH, selectedMonth.month - 1)
                set(Calendar.DAY_OF_MONTH, 1)
            }
        }
    }

    val defaultCyanColor = MaterialTheme.tsColors.ledCyan
    val traiBlueColor = traiBlue

    // ✅ Obtener días con sesiones (solo modo SESSIONS)
    val sessionDates = remember(sessionWorkouts, defaultCyanColor, mode) {
        if (mode == CalendarMode.SESSIONS) {
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sessionWorkouts.mapNotNull { (dateString, sessions) ->
                try {
                    val date = formatter.parse(dateString)
                    date?.let {
                        val calendar = Calendar.getInstance()
                        calendar.time = it
                        val dateKey = String.format(
                            "%04d-%02d-%02d",
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH) + 1,
                            calendar.get(Calendar.DAY_OF_MONTH)
                        )
                        val sessionColor = if (sessions.isNotEmpty()) {
                            try {
                                Color(android.graphics.Color.parseColor(sessions.first().sessionColor))
                            } catch (e: Exception) {
                                defaultCyanColor
                            }
                        } else {
                            defaultCyanColor
                        }
                        dateKey to sessionColor
                    }
                } catch (e: Exception) {
                    null
                }
            }.toMap()
        } else {
            emptyMap()
        }
    }

    // Generar días del mes
    val daysInMonth = remember(displayCalendar, sessionDates, routineDates, mode) {
        val year = displayCalendar.get(Calendar.YEAR)
        val month = displayCalendar.get(Calendar.MONTH)
        val maxDay = displayCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        (1..maxDay).map { day ->
            val dayCalendar = Calendar.getInstance()
            dayCalendar.set(year, month, day, 0, 0, 0)
            dayCalendar.set(Calendar.MILLISECOND, 0)

            val dateKey = String.format("%04d-%02d-%02d", year, month + 1, day)
            val dayOfWeek = SimpleDateFormat("EEE", Locale.getDefault()).format(dayCalendar.time)

            val isToday = currentCalendar.get(Calendar.YEAR) == year &&
                    currentCalendar.get(Calendar.MONTH) == month &&
                    currentCalendar.get(Calendar.DAY_OF_MONTH) == day

            // ✅ Datos según modo
            val hasSession = mode == CalendarMode.SESSIONS && sessionDates.containsKey(dateKey)
            val hasRoutine = mode == CalendarMode.ROUTINES && routineDates.contains(dateKey)

            DayInfoCompat(
                dayNumber = day,
                dayOfWeek = dayOfWeek,
                dateKey = dateKey,
                isToday = isToday,
                hasWorkouts = hasSession,
                sessionColor = if (mode == CalendarMode.SESSIONS) sessionDates[dateKey] else null,
                hasRoutine = hasRoutine,
                routineColor = if (hasRoutine) traiBlueColor else null
            )
        }
    }

    LaunchedEffect(Unit) {
        if (selectedDate.value.isEmpty()) {
            selectedDate.value = getTodayDateString()
        }
    }

    val listState = rememberLazyListState()

    LaunchedEffect(daysInMonth) {
        val todayIndex = daysInMonth.indexOfFirst { it.isToday }
        if (todayIndex != -1) {
            listState.animateScrollToItem(
                index = maxOf(0, todayIndex - 3),
                scrollOffset = 0
            )
        }
    }

    LazyRow(
        state = listState,
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) {
        items(
            items = daysInMonth,
            key = { dayInfo -> dayInfo.dateKey }
        ) { dayInfo ->
            DayCardCompat(
                dayInfo = dayInfo,
                isSelected = selectedDate.value == dayInfo.dateKey,
                onClick = {
                    selectedDate.value = dayInfo.dateKey
                    onDateSelected(dayInfo.dateKey)
                }
            )
        }
    }
}
data class DayInfoCompat(
    val dayNumber: Int,
    val dayOfWeek: String,
    val dateKey: String, // Formato: yyyy-MM-dd
    val isToday: Boolean,
    val hasWorkouts: Boolean = false,
    val sessionColor: Color? = null,
    val hasRoutine: Boolean = false, // ✅ NUEVO
    val routineColor: Color? = null  // ✅ NUEVO
)

@Composable
fun DayCardCompat(
    dayInfo: DayInfoCompat,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cyanColor = MaterialTheme.tsColors.ledCyan
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    val backgroundColor = when {
        isSelected && dayInfo.sessionColor != null -> dayInfo.sessionColor!!
        isSelected && dayInfo.routineColor != null -> dayInfo.routineColor!!
        isSelected -> cyanColor
        else -> Color.Transparent
    }

    val textColor = when {
        isSelected -> Color.Black
        dayInfo.isToday || dayInfo.hasWorkouts -> dayInfo.sessionColor ?: cyanColor
        dayInfo.hasRoutine -> dayInfo.routineColor ?: traiBlue
        else -> onSurfaceColor
    }
    val borderColor = when {
        dayInfo.hasWorkouts && dayInfo.sessionColor != null && !isSelected -> dayInfo.sessionColor
        dayInfo.hasRoutine && !isSelected -> dayInfo.routineColor ?: traiBlue
        else -> null
    }
    Box(
        modifier = modifier
            .clickable { onClick() }
            .width(50.dp)
            .height(70.dp)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .let { modifierBox ->
                if (borderColor != null) {
                    modifierBox.border(
                        width = 2.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    modifierBox
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dayInfo.dayOfWeek.take(1).uppercase(),
                style = MaterialTheme.typography.bodySmall,
                color = textColor.copy(alpha = 0.7f),
                fontWeight = FontWeight.Normal
            )
            Text(
                text = dayInfo.dayNumber.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = textColor,
                fontWeight = if (isSelected || dayInfo.isToday || dayInfo.hasWorkouts || dayInfo.hasRoutine)
                    FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}