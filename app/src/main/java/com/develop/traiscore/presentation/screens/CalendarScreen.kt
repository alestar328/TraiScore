package com.develop.traiscore.presentation.screens

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.develop.traiscore.R
import com.develop.traiscore.data.local.entity.WorkoutEntry
import com.develop.traiscore.presentation.components.QuickStats
import com.develop.traiscore.presentation.components.WorkoutCardList
import com.develop.traiscore.presentation.theme.tsColors
import com.develop.traiscore.presentation.viewmodels.SessionWithWorkouts
import com.develop.traiscore.presentation.viewmodels.WorkoutEntryViewModel
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
    groupedEntries: Map<String, List<WorkoutEntry>>,
    selectedMonth: MonthYear?, // ✅ CAMBIO: YearMonth → MonthYear
    onEditClick: (WorkoutEntry) -> Unit,
    onDeleteClick: (WorkoutEntry) -> Unit,
    modifier: Modifier = Modifier,
    workoutEntryViewModel: WorkoutEntryViewModel = hiltViewModel()
) {
    // ✅ Usar Calendar en lugar de LocalDate para compatibilidad API 24+
    val selectedDate = remember { mutableStateOf("") }
    val sessionWorkouts = workoutEntryViewModel.sessionWorkouts.value

    // ✅ CALCULADO usando Calendar y SimpleDateFormat (API 24+)
    val selectedDayWorkouts = remember(selectedDate.value, groupedEntries) {
        if (selectedDate.value.isNotEmpty()) {
            try {
                // Parsear yyyy-MM-dd
                val parts = selectedDate.value.split("-")
                if (parts.size == 3) {
                    val year = parts[0].toInt()
                    val month = parts[1].toInt() - 1
                    val day = parts[2].toInt()

                    val calendar = Calendar.getInstance()
                    calendar.set(year, month, day, 0, 0, 0)
                    calendar.set(Calendar.MILLISECOND, 0)

                    // ✅ Formatear como dd/MM/yyyy
                    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val formattedDate = formatter.format(calendar.time)

                    println("🔍 Buscando fecha: $formattedDate en keys: ${groupedEntries.keys}")
                    groupedEntries[formattedDate] ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                println("❌ Error parseando fecha: ${e.message}")
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    // ✅ Sesiones del día seleccionado
    val selectedDaySessions = remember(selectedDate.value, sessionWorkouts) {
        if (selectedDate.value.isNotEmpty()) {
            try {
                val parts = selectedDate.value.split("-")
                if (parts.size == 3) {
                    val year = parts[0].toInt()
                    val month = parts[1].toInt() - 1
                    val day = parts[2].toInt()

                    val calendar = Calendar.getInstance()
                    calendar.set(year, month, day, 0, 0, 0)
                    calendar.set(Calendar.MILLISECOND, 0)

                    // ✅ Formatear como dd/MM/yyyy
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

    // ✅ Estadísticas del día
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
            onDateSelected = { date ->
                selectedDate.value = date
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        // ✅ QuickStats (ahora debería mostrarse)
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (selectedDaySessions.isNotEmpty()) {
                // Mostrar por sesiones
                items(selectedDaySessions) { session ->
                    SessionDayCard(
                        session = session,
                        onEditClick = onEditClick,
                        onDeleteClick = onDeleteClick
                    )
                }
            } else if (selectedDayWorkouts.isNotEmpty()) {
                // Datos legacy (sin sesiones)
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
                // Sin ejercicios
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
    selectedMonth: MonthYear?, // ✅ CAMBIO
    sessionWorkouts: Map<String, List<SessionWithWorkouts>>,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentCalendar = remember { Calendar.getInstance() }

    // ✅ CORREGIDO - Sin usar .year ni .monthValue
    val displayCalendar = remember(selectedMonth) {
        Calendar.getInstance().apply {
            if (selectedMonth != null) {
                set(Calendar.YEAR, selectedMonth.year)
                set(Calendar.MONTH, selectedMonth.month - 1) // MonthYear usa 1-12
                set(Calendar.DAY_OF_MONTH, 1)
            }
        }
    }

    val defaultCyanColor = MaterialTheme.tsColors.ledCyan

    // Obtener días con sesiones
    val sessionDates = remember(sessionWorkouts, defaultCyanColor) {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // ✅ CAMBIO
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
    }

    // Generar días del mes
    val daysInMonth = remember(displayCalendar, sessionDates) {
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

            DayInfoCompat(
                dayNumber = day,
                dayOfWeek = dayOfWeek,
                dateKey = dateKey,
                isToday = isToday,
                hasWorkouts = sessionDates.containsKey(dateKey),
                sessionColor = sessionDates[dateKey]
            )
        }
    }

    // Inicializar fecha seleccionada
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
        items(daysInMonth) { dayInfo ->
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
    val sessionColor: Color? = null
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