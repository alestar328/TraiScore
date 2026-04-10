package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.develop.traiscore.data.local.entity.WorkoutEntry
import com.develop.traiscore.presentation.theme.traiBackgroundDay
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.theme.tsColors
import com.develop.traiscore.presentation.viewmodels.CalendarMode
import com.develop.traiscore.presentation.viewmodels.ClientWorkoutViewModel
import com.develop.traiscore.presentation.viewmodels.ViewMode
import com.develop.traiscore.presentation.viewmodels.ViewModeSelector
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientSessionsScreen(
    clientId: String,
    clientName: String,
    onBack: () -> Unit,
    viewModel: ClientWorkoutViewModel = hiltViewModel()
) {
    val groupedByDate by viewModel.groupedByDate.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var currentViewMode by remember { mutableStateOf(ViewMode.TODAY) }
    var selectedMonth by remember { mutableStateOf<MonthYear?>(null) }

    LaunchedEffect(clientId) {
        viewModel.loadWorkouts(clientId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entrenamientos de $clientName") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = traiBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Selector de vista
            ViewModeSelector(
                selectedMode = currentViewMode,
                onModeSelected = { currentViewMode = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = traiBlue)
                    }
                }

                error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(error!!, color = Color.Red, textAlign = TextAlign.Center)
                    }
                }

                else -> when (currentViewMode) {
                    ViewMode.TODAY -> ClientTodayView(groupedByDate)
                    ViewMode.MONTH -> CalendarScreen(
                        mode = CalendarMode.SESSIONS,
                        groupedEntries = groupedByDate,
                        routineDates = emptySet(),
                        selectedMonth = selectedMonth,
                        onEditClick = {}, // read-only
                        onDeleteClick = {} // read-only
                    )
                    ViewMode.YEAR -> YearViewScreen(
                        mode = CalendarMode.SESSIONS,
                        groupedEntries = groupedByDate,
                        routineDates = emptySet(),
                        onMonthSelected = { month ->
                            selectedMonth = month
                            currentViewMode = ViewMode.MONTH
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ClientTodayView(groupedByDate: Map<String, List<WorkoutEntry>>) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val today = remember { dateFormat.format(Date()) }
    val todayWorkouts = groupedByDate[today] ?: emptyList()

    if (todayWorkouts.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = "Sin entrenamientos hoy",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Cambia a Vista Mes o Año para ver el historial completo",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f),
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "Hoy · ${todayWorkouts.size} ejercicios",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            items(todayWorkouts) { entry ->
                ReadOnlyWorkoutCard(entry)
            }
        }
    }
}

@Composable
private fun ReadOnlyWorkoutCard(entry: WorkoutEntry) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!entry.sessionName.isNullOrBlank()) {
                    Text(
                        text = entry.sessionName,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatChip("${entry.reps} rep")
                StatChip("${entry.weight} kg")
                entry.rir?.takeIf { it > 0 }?.let { StatChip("RIR $it") }
            }
        }
    }
}

@Composable
private fun StatChip(text: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = traiBlue.copy(alpha = 0.12f)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = traiBlue,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
