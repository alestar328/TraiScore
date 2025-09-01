package com.develop.traiscore.presentation.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import java.time.format.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.develop.traiscore.data.local.entity.WorkoutEntry
import com.develop.traiscore.presentation.components.QuickStats
import com.develop.traiscore.presentation.components.WorkoutCardList
import com.develop.traiscore.presentation.theme.tsColors
import com.develop.traiscore.presentation.viewmodels.SessionWithWorkouts
import com.develop.traiscore.presentation.viewmodels.WorkoutEntryViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

@Composable
fun CalendarScreen(
    groupedEntries: Map<String, List<WorkoutEntry>>,
    selectedMonth: YearMonth?,
    onEditClick: (WorkoutEntry) -> Unit,
    onDeleteClick: (WorkoutEntry) -> Unit,
    modifier: Modifier = Modifier,
    workoutEntryViewModel: WorkoutEntryViewModel = hiltViewModel()
) {
    val selectedDate = remember { mutableStateOf("") }

    // ⭐ Obtener sesiones agrupadas del ViewModel
    val sessionWorkouts = workoutEntryViewModel.sessionWorkouts.value

    val selectedDayWorkouts = remember(selectedDate.value, groupedEntries) {
        if (selectedDate.value.isNotEmpty()) {
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            try {
                val selectedLocalDate = LocalDate.parse(selectedDate.value)
                val calendar = java.util.Calendar.getInstance()
                calendar.set(
                    selectedLocalDate.year,
                    selectedLocalDate.monthValue - 1,
                    selectedLocalDate.dayOfMonth
                )
                val formattedDate = formatter.format(calendar.time)
                groupedEntries[formattedDate] ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    // ⭐ Obtener sesiones del día seleccionado
    val selectedDaySessions = remember(selectedDate.value, sessionWorkouts) {
        if (selectedDate.value.isNotEmpty()) {
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            try {
                val selectedLocalDate = LocalDate.parse(selectedDate.value)
                val calendar = java.util.Calendar.getInstance()
                calendar.set(
                    selectedLocalDate.year,
                    selectedLocalDate.monthValue - 1,
                    selectedLocalDate.dayOfMonth
                )
                val formattedDate = formatter.format(calendar.time)
                sessionWorkouts[formattedDate] ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

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
        HorizontalDaysScroll(
            selectedDate = selectedDate,
            selectedMonth = selectedMonth,
            sessionWorkouts = sessionWorkouts, // ⭐ Pasar sesiones en lugar de entries
            onDateSelected = { date ->
                selectedDate.value = date
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        if (selectedDayWorkouts.isNotEmpty()) {
            QuickStats(
                totalExercises = totalExercises,
                totalSeries = totalSeries,
                totalReps = totalReps,
                totalWeight = totalWeight
            )
        }

        // Lista de ejercicios del día seleccionado
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 105.dp)
        ) {
            if (selectedDaySessions.isNotEmpty()) {
                // ⭐ MOSTRAR POR SESIONES
                items(selectedDaySessions) { session ->
                    SessionDayCard(
                        session = session,
                        onEditClick = onEditClick,
                        onDeleteClick = onDeleteClick
                    )
                }
            } else if (selectedDayWorkouts.isNotEmpty()) {
                // ⭐ DATOS LEGACY (sin sesiones)
                item {
                    val selectedLocalDate = try {
                        LocalDate.parse(selectedDate.value)
                    } catch (e: Exception) {
                        LocalDate.now()
                    }
                    val dayFormatter = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
                    val calendar = java.util.Calendar.getInstance()
                    calendar.set(selectedLocalDate.year, selectedLocalDate.monthValue - 1, selectedLocalDate.dayOfMonth)

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
                // SIN EJERCICIOS
                item {
                    val selectedLocalDate = try {
                        LocalDate.parse(selectedDate.value)
                    } catch (e: Exception) {
                        LocalDate.now()
                    }

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
                                text = if (selectedLocalDate == LocalDate.now()) {
                                    "No hay ejercicios registrados hoy"
                                } else {
                                    "No hay ejercicios registrados este día"
                                },
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

// ⭐ NUEVO: Card compacta para mostrar sesiones en calendario
@Composable
private fun SessionDayCard(
    session: SessionWithWorkouts,
    onEditClick: (WorkoutEntry) -> Unit,
    onDeleteClick: (WorkoutEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    // ⭐ SOLUCIÓN: Obtener color por defecto fuera del try-catch
    val defaultCyanColor = MaterialTheme.tsColors.ledCyan

    val sessionColor = try {
        Color(android.graphics.Color.parseColor(session.sessionColor))
    } catch (e: Exception) {
        defaultCyanColor  // ⭐ Usar variable en lugar de función @Composable
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        // Header compacto de la sesión
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Indicador de color
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(sessionColor, CircleShape)
            )

            Text(
                text = "${session.sessionName} (${session.workouts.size} series)",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Lista de workouts
        WorkoutCardList(
            workouts = session.workouts,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick
        )
    }
}
@Composable
fun HorizontalDaysScroll(
    selectedDate: MutableState<String>,
    selectedMonth: YearMonth?,
    sessionWorkouts: Map<String, List<SessionWithWorkouts>>,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentDate = remember { LocalDate.now() }
    val currentMonth = remember(selectedMonth) { selectedMonth ?: YearMonth.now() }

    // ⭐ SOLUCIÓN: Obtener el color por defecto FUERA del remember
    val defaultCyanColor = MaterialTheme.tsColors.ledCyan

    // ⭐ Obtener días con sesiones y sus colores
    val sessionDates = remember(sessionWorkouts, defaultCyanColor) {
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        sessionWorkouts.mapNotNull { (dateString, sessions) ->
            try {
                val date = formatter.parse(dateString)
                date?.let {
                    val calendar = java.util.Calendar.getInstance()
                    calendar.time = it
                    val localDate = LocalDate.of(
                        calendar.get(java.util.Calendar.YEAR),
                        calendar.get(java.util.Calendar.MONTH) + 1,
                        calendar.get(java.util.Calendar.DAY_OF_MONTH)
                    )
                    // Si hay múltiples sesiones, usar el color de la primera
                    val sessionColor = if (sessions.isNotEmpty()) {
                        try {
                            Color(android.graphics.Color.parseColor(sessions.first().sessionColor))
                        } catch (e: Exception) {
                            defaultCyanColor  // ⭐ Usar variable en lugar de función @Composable
                        }
                    } else {
                        defaultCyanColor  // ⭐ Usar variable en lugar de función @Composable
                    }
                    localDate to sessionColor
                }
            } catch (e: Exception) {
                null
            }
        }.toMap()
    }

    // Generar todos los días del mes actual
    val daysInMonth = remember(currentMonth, sessionDates) {
        (1..currentMonth.lengthOfMonth()).map { day ->
            val date = currentMonth.atDay(day)
            DayInfo(
                dayNumber = day,
                dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                date = date,
                isToday = date == currentDate,
                hasWorkouts = sessionDates.containsKey(date),
                sessionColor = sessionDates[date] // ⭐ Color de la sesión
            )
        }
    }

    LaunchedEffect(currentMonth) {
        if (selectedDate.value.isEmpty() || selectedMonth != null) {
            val initialDate = if (selectedMonth != null) {
                selectedMonth.atDay(1)
            } else {
                currentDate
            }
            selectedDate.value = initialDate.toString()
        }
    }

    val listState = rememberLazyListState()

    LaunchedEffect(daysInMonth, currentMonth) {
        if (currentMonth == YearMonth.now()) {
            val todayIndex = daysInMonth.indexOfFirst { it.isToday }
            if (todayIndex != -1) {
                listState.animateScrollToItem(
                    index = maxOf(0, todayIndex - 3),
                    scrollOffset = 0
                )
            }
        } else {
            listState.animateScrollToItem(0)
        }
    }

    LazyRow(
        state = listState,
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) {
        items(daysInMonth) { dayInfo ->
            DayCard(
                dayInfo = dayInfo,
                isSelected = selectedDate.value == dayInfo.date.toString(),
                onClick = {
                    selectedDate.value = dayInfo.date.toString()
                    onDateSelected(dayInfo.date.toString())
                }
            )
        }
    }
}

data class DayInfo(
    val dayNumber: Int,
    val dayOfWeek: String,
    val date: LocalDate,
    val isToday: Boolean,
    val hasWorkouts: Boolean = false,
    val sessionColor: Color? = null // ⭐ Color de la sesión del día
)

@Composable
fun DayCard(
    dayInfo: DayInfo,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // ⭐ SOLUCIÓN: Obtener colores FUERA de las condiciones
    val cyanColor = MaterialTheme.tsColors.ledCyan
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    val backgroundColor = when {
        isSelected -> cyanColor
        else -> Color.Transparent
    }

    val textColor = when {
        isSelected -> Color.Black
        dayInfo.isToday || dayInfo.hasWorkouts -> dayInfo.sessionColor ?: cyanColor
        else -> onSurfaceColor
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
            // ⭐ Borde con color de sesión si tiene workouts
            .let { modifierBox ->
                if (dayInfo.hasWorkouts && dayInfo.sessionColor != null && !isSelected) {
                    modifierBox.border(
                        width = 2.dp,
                        color = dayInfo.sessionColor,
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
                fontWeight = if (isSelected || dayInfo.isToday || dayInfo.hasWorkouts) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}