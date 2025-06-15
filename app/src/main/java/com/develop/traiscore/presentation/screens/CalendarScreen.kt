package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.develop.traiscore.data.local.entity.WorkoutEntry
import com.develop.traiscore.presentation.components.WorkoutCardList
import com.develop.traiscore.presentation.theme.tsColors
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

@Composable
fun CalendarScreen(
    groupedEntries: Map<String, List<WorkoutEntry>>,
    onEditClick: (WorkoutEntry) -> Unit,
    onDeleteClick: (WorkoutEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedDate = remember { mutableStateOf("") }
    val selectedDayWorkouts = remember(selectedDate.value, groupedEntries) {
        if (selectedDate.value.isNotEmpty()) {
            // Convertir LocalDate seleccionado a formato de fecha de groupedEntries
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Horizontal scroll de días
        HorizontalDaysScroll(
            selectedDate =selectedDate,
            groupedEntries = groupedEntries,
            onDateSelected = { date ->
                selectedDate.value = date
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        // Lista de ejercicios del día seleccionado
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 105.dp)
        ) {
            if (selectedDayWorkouts.isNotEmpty()) {
                // **NUEVO**: Mostrar ejercicios del día seleccionado
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
                // **NUEVO**: Mensaje cuando no hay ejercicios
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

@Composable
fun HorizontalDaysScroll(
    selectedDate: MutableState<String>,
    groupedEntries: Map<String, List<WorkoutEntry>>, // **NUEVO**: Parámetro agregado
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentDate = remember { LocalDate.now() }
    val currentMonth = remember { YearMonth.now() }

    val workoutDates = remember(groupedEntries) {
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        groupedEntries.keys.mapNotNull { dateString ->
            try {
                val date = formatter.parse(dateString)
                date?.let {
                    val calendar = java.util.Calendar.getInstance()
                    calendar.time = it
                    LocalDate.of(
                        calendar.get(java.util.Calendar.YEAR),
                        calendar.get(java.util.Calendar.MONTH) + 1,
                        calendar.get(java.util.Calendar.DAY_OF_MONTH)
                    )
                }
            } catch (e: Exception) {
                null
            }
        }.toSet()
    }

    // Generar todos los días del mes actual
    val daysInMonth = remember(currentMonth, workoutDates) { // **CAMBIO**: Agregar workoutDates
        (1..currentMonth.lengthOfMonth()).map { day ->
            val date = currentMonth.atDay(day)
            DayInfo(
                dayNumber = day,
                dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                date = date,
                isToday = date == currentDate,
                hasWorkouts = workoutDates.contains(date) // **NUEVO**: Verificar entrenamientos
            )
        }
    }

    // Inicializar con el día actual seleccionado
    LaunchedEffect(Unit) {
        if (selectedDate.value.isEmpty()) {
            selectedDate.value = currentDate.toString()
        }
    }

    val listState = rememberLazyListState()

    // Centrar en el día actual al inicio
    LaunchedEffect(daysInMonth) {
        val todayIndex = daysInMonth.indexOfFirst { it.isToday }
        if (todayIndex != -1) {
            listState.animateScrollToItem(
                index = maxOf(0, todayIndex - 3), // Centrar aproximadamente
                scrollOffset = 0
            )
        }
    }

    LazyRow(
        state = listState,
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(0.dp), // Sin espaciado para franja uniforme
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
    val hasWorkouts: Boolean = false
)

@Composable
fun DayCard(
    dayInfo: DayInfo,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.tsColors.ledCyan
        else -> Color.Transparent
    }

    val textColor = when {
        isSelected -> Color.Black
        dayInfo.isToday || dayInfo.hasWorkouts -> MaterialTheme.tsColors.ledCyan // **CAMBIO**: Agregar hasWorkouts
        else -> MaterialTheme.colorScheme.onSurface
    }


    // Franja uniforme sin Card
    Box(
        modifier = modifier
            .clickable { onClick() }
            .width(50.dp)
            .height(70.dp)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Día de la semana en MAYÚSCULA
            Text(
                text = dayInfo.dayOfWeek.take(1).uppercase(), // **MAYÚSCULA**
                style = MaterialTheme.typography.bodySmall,
                color = textColor.copy(alpha = 0.7f),
                fontWeight = FontWeight.Normal
            )

            // Número del día
            Text(
                text = dayInfo.dayNumber.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = textColor,
                fontWeight = if (isSelected || dayInfo.isToday || dayInfo.hasWorkouts) FontWeight.Bold else FontWeight.Normal // **CAMBIO**: Agregar hasWorkouts
            )
        }
    }
}