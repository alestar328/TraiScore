package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.develop.traiscore.R
import com.develop.traiscore.data.local.entity.WorkoutEntry
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.theme.tsColors
import com.develop.traiscore.presentation.viewmodels.CalendarMode
import com.develop.traiscore.presentation.viewmodels.RoutineViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun YearViewScreen(
    mode: CalendarMode = CalendarMode.SESSIONS, // ✅ NUEVO
    groupedEntries: Map<String, List<WorkoutEntry>>,
    routineDates: Set<String> = emptySet(), // ✅ NUEVO
    onMonthSelected: (MonthYear) -> Unit,
    modifier: Modifier = Modifier,
    routineViewModel: RoutineViewModel = hiltViewModel()
) {
    val currentYear = remember {
        Calendar.getInstance().get(Calendar.YEAR)
    }

    val months = remember {
        (1..12).map { month ->
            MonthYear(year = currentYear, month = month)
        }
    }

    // ✅ Procesar fechas según modo
    val workoutDates = remember(groupedEntries, mode) {
        if (mode == CalendarMode.SESSIONS) {
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            groupedEntries.keys.mapNotNull { dateString ->
                try {
                    val date = formatter.parse(dateString)
                    date?.let {
                        val calendar = Calendar.getInstance()
                        calendar.time = it
                        String.format(
                            "%04d-%02d-%02d",
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH) + 1,
                            calendar.get(Calendar.DAY_OF_MONTH)
                        )
                    }
                } catch (e: Exception) {
                    null
                }
            }.toSet()
        } else {
            emptySet()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.calendar_year) + " $currentYear",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(months) { monthYear ->
                MonthCardCompat(
                    monthYear = monthYear,
                    workoutDates = workoutDates,
                    routineDates = routineDates,
                    mode = mode, // ✅ NUEVO
                    onClick = { onMonthSelected(monthYear) }
                )
            }
        }
    }
}

@Composable
private fun MonthCardCompat(
    monthYear: MonthYear,
    workoutDates: Set<String>,
    routineDates: Set<String>,
    mode: CalendarMode, // ✅ NUEVO
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val monthName = remember(monthYear) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, monthYear.month - 1)
        SimpleDateFormat("MMMM", Locale.getDefault())
            .format(calendar.time)
            .replaceFirstChar { it.uppercase() }
    }

    val daysInMonth = remember(monthYear) {
        val maxDay = monthYear.lengthOfMonth()
        (1..maxDay).map { day ->
            monthYear.atDay(day)
        }
    }

    // ✅ Filtrar según modo
    val workoutDaysInMonth = remember(daysInMonth, workoutDates, mode) {
        if (mode == CalendarMode.SESSIONS) {
            daysInMonth.filter { workoutDates.contains(it) }
        } else {
            emptyList()
        }
    }

    val routineDaysInMonth = remember(daysInMonth, routineDates, mode) {
        if (mode == CalendarMode.ROUTINES) {
            daysInMonth.filter { routineDates.contains(it) }
        } else {
            emptyList()
        }
    }

    val totalWorkouts = workoutDaysInMonth.size
    val totalRoutines = routineDaysInMonth.size
    val hasData = totalWorkouts > 0 || totalRoutines > 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = monthName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )

            if (hasData) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Grid de puntos
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(minOf(28, daysInMonth.size)) { index ->
                            val dayString = daysInMonth[index]
                            val hasWorkout = mode == CalendarMode.SESSIONS && workoutDates.contains(dayString)
                            val hasRoutine = mode == CalendarMode.ROUTINES && routineDates.contains(dayString)

                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .background(
                                        color = when {
                                            hasWorkout -> MaterialTheme.tsColors.ledCyan
                                            hasRoutine -> traiBlue
                                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                        },
                                        shape = CircleShape
                                    )
                            )
                        }
                    }

                    // ✅ Contador según modo
                    if (mode == CalendarMode.SESSIONS && totalWorkouts > 0) {
                        Text(
                            text = "$totalWorkouts ${stringResource(id = R.string.calendar_year_days_with_data)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.tsColors.ledCyan,
                            fontWeight = FontWeight.Medium
                        )
                    } else if (mode == CalendarMode.ROUTINES && totalRoutines > 0) {
                        Text(
                            text = "$totalRoutines días con rutinas",
                            style = MaterialTheme.typography.bodySmall,
                            color = traiBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                Text(
                    text = if (mode == CalendarMode.SESSIONS) {
                        stringResource(id = R.string.calendar_year_no_data)
                    } else {
                        "Sin rutinas guardadas"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}