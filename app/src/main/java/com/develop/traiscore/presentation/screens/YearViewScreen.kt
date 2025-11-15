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
import com.develop.traiscore.R
import com.develop.traiscore.data.local.entity.WorkoutEntry
import com.develop.traiscore.presentation.theme.tsColors
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun YearViewScreen(
    groupedEntries: Map<String, List<WorkoutEntry>>,
    onMonthSelected: (MonthYear) -> Unit,
    modifier: Modifier = Modifier
) {
    // ✅ Usar Calendar en lugar de LocalDate.now()
    val currentYear = remember {
        Calendar.getInstance().get(Calendar.YEAR)
    }

    // ✅ Generar lista de meses usando MonthYear
    val months = remember {
        (1..12).map { month ->
            MonthYear(year = currentYear, month = month)
        }
    }

    // ✅ Convertir fechas de groupedEntries a formato yyyy-MM-dd usando Calendar
    val workoutDates = remember(groupedEntries) {
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
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
                    onClick = { onMonthSelected(monthYear) }
                )
            }
        }
    }
}

@Composable
private fun MonthCardCompat(
    monthYear: MonthYear,
    workoutDates: Set<String>, // ✅ Ahora es Set<String> en formato yyyy-MM-dd
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // ✅ Obtener nombre del mes usando Calendar
    val monthName = remember(monthYear) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, monthYear.month - 1)
        SimpleDateFormat("MMMM", Locale.getDefault())
            .format(calendar.time)
            .replaceFirstChar { it.uppercase() }
    }

    // ✅ Calcular días del mes que tienen entrenamientos
    val daysInMonth = remember(monthYear) {
        val maxDay = monthYear.lengthOfMonth()
        (1..maxDay).map { day ->
            monthYear.atDay(day) // Devuelve String en formato yyyy-MM-dd
        }
    }

    val workoutDaysInMonth = remember(daysInMonth, workoutDates) {
        daysInMonth.filter { dateString ->
            workoutDates.contains(dateString)
        }
    }

    val totalWorkouts = workoutDaysInMonth.size

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
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
            // Nombre del mes
            Text(
                text = monthName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )

            // Indicador visual de días con entrenamientos
            if (totalWorkouts > 0) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Puntos indicadores (máximo 28 visibles para representar el mes)
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
                            val hasWorkout = workoutDates.contains(dayString)

                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .background(
                                        color = if (hasWorkout) {
                                            MaterialTheme.tsColors.ledCyan
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                        },
                                        shape = CircleShape
                                    )
                            )
                        }
                    }

                    // Contador
                    Text(
                        text = "$totalWorkouts " + stringResource(id = R.string.calendar_year_days_with_data),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.tsColors.ledCyan,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Text(
                    text = stringResource(id = R.string.calendar_year_no_data),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}