package com.develop.traiscore.presentation.screens

import  android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.develop.traiscore.presentation.viewmodels.RoutineViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.develop.traiscore.BuildConfig
import com.develop.traiscore.R
import com.develop.traiscore.data.local.entity.RoutineHistoryEntity
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.theme.tsColors
import com.develop.traiscore.presentation.viewmodels.AddExerciseViewModel
import com.develop.traiscore.presentation.viewmodels.CalendarMode
import com.develop.traiscore.presentation.viewmodels.ViewMode
import com.develop.traiscore.presentation.viewmodels.ViewModeSelector
import com.google.firebase.auth.FirebaseAuth


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineScreen(
    routineViewModel: RoutineViewModel = viewModel(),
    documentId: String,
    selectedType: String,
    onBack: () -> Unit,
    onConfigureTopBar: (
        @Composable () -> Unit,
        @Composable () -> Unit,
        (@Composable () -> Unit)?
    ) -> Unit,
    onConfigureFAB: (fab: (@Composable () -> Unit)?) -> Unit
) {
    val exerciseVM: AddExerciseViewModel = viewModel()
    val exerciseNames by exerciseVM.exerciseNames.collectAsState()
    val context = LocalContext.current

    var showEmptyDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var exerciseCategory by remember { mutableStateOf<com.develop.traiscore.core.DefaultCategoryExer?>(null) }

    var showViewModeSelector by remember { mutableStateOf(false) }
    var currentViewMode by remember { mutableStateOf(ViewMode.TODAY) }
    var selectedMonth by remember { mutableStateOf<MonthYear?>(null) }

    // ✅ NUEVO: Estados para cargar rutinas guardadas
    var routineDates by remember { mutableStateOf<Set<String>>(emptySet()) }
    var routinesByDate by remember { mutableStateOf<Map<String, List<RoutineHistoryEntity>>>(emptyMap()) }

    val userId = FirebaseAuth.getInstance().currentUser?.uid
        ?: run { onBack(); return }

    val isTrainerVersion = BuildConfig.FLAVOR == "trainer"
    val canEditExercises = true

    // ✅ Cargar fechas con rutinas cuando cambia el modo de vista
    LaunchedEffect(currentViewMode) {
        if (currentViewMode != ViewMode.TODAY) {
            routineViewModel.getDatesWithRoutines(userId) { dates ->
                routineDates = dates.toSet()
            }
        }
    }

    LaunchedEffect(documentId) {
        routineViewModel.loadRoutine(documentId)
    }

    val currentRoutineData = routineViewModel.routineDocument

    if (currentRoutineData == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (showEmptyDialog) {
        AlertDialog(
            onDismissRequest = { onBack() },
            title = { Text("Sin rutinas") },
            text = { Text("No tienes rutinas guardadas") },
            confirmButton = {
                TextButton(onClick = { onBack() }) {
                    Text("Aceptar")
                }
            }
        )
        return
    }

    val filteredExercises = routineViewModel.getExercisesByType(selectedType)
    val routineTitle = currentRoutineData.routineName.ifBlank { "Rutina" }

    LaunchedEffect(routineTitle) {
        onConfigureTopBar(
            {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = traiBlue
                    )
                }
            },
            {
                IconButton(
                    onClick = {
                        showViewModeSelector = !showViewModeSelector
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Selector de vista",
                        tint = MaterialTheme.tsColors.ledCyan
                    )
                }
            },
            {
                Text(
                    text = routineTitle,
                    color = traiBlue
                )
            }
        )
    }

    LaunchedEffect(isTrainerVersion) {
        if (isTrainerVersion) {
            onConfigureFAB {
                FloatingActionButton(
                    onClick = {
                        val routine = routineViewModel.routineDocument ?: return@FloatingActionButton
                        if (routine.sections.isEmpty()) {
                            Toast.makeText(context, "La rutina no tiene secciones", Toast.LENGTH_SHORT).show()
                            return@FloatingActionButton
                        }
                        com.develop.traiscore.exports.RoutineExportManager.exportRoutine(
                            context = context,
                            routine = routine,
                            onSuccess = { fileUri ->
                                com.develop.traiscore.exports.RoutineExportManager.shareRoutineFile(
                                    context,
                                    fileUri,
                                    routine.routineName
                                )
                            },
                            onError = { err ->
                                Toast.makeText(context, "❌ Error: $err", Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    containerColor = Color.Yellow,
                    contentColor = Color.Black
                ) {
                    Icon(Icons.Default.Email, contentDescription = "Enviar Rutina")
                }
            }
        } else {
            onConfigureFAB(null)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedVisibility(
            visible = showViewModeSelector,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300)),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(200)
            ) + fadeOut(animationSpec = tween(200))
        ) {
            ViewModeSelector(
                selectedMode = ViewMode.TODAY,
                onModeSelected = { mode ->
                    currentViewMode = mode
                    showViewModeSelector = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        when (currentViewMode) {
            ViewMode.TODAY -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    item {
                        RoutineTable(
                            exercises = filteredExercises,
                            exerciseNames = exerciseNames,
                            onSeriesChanged = { exerciseIndex, newSeries ->
                                routineViewModel.updateExerciseFieldInMemory(
                                    exerciseIndex, selectedType,
                                    com.develop.traiscore.core.ColumnType.SERIES, newSeries
                                )
                            },
                            onWeightChanged = { exerciseIndex, newWeight ->
                                routineViewModel.updateExerciseFieldInMemory(
                                    exerciseIndex, selectedType,
                                    com.develop.traiscore.core.ColumnType.WEIGHT, newWeight
                                )
                            },
                            onRepsChanged = { exerciseIndex, newRep ->
                                routineViewModel.updateExerciseFieldInMemory(
                                    exerciseIndex, selectedType,
                                    com.develop.traiscore.core.ColumnType.REPS, newRep
                                )
                            },
                            onRirChanged = { exerciseIndex, newRir ->
                                routineViewModel.updateExerciseFieldInMemory(
                                    exerciseIndex, selectedType,
                                    com.develop.traiscore.core.ColumnType.RIR, newRir
                                )
                            },
                            onFieldChanged = { idx, columnType, newValue ->
                                routineViewModel.updateExerciseFieldInMemory(
                                    exerciseIndex = idx,
                                    trainingType = selectedType,
                                    columnType = columnType,
                                    newValue = newValue
                                )
                            },
                            onDeleteExercise = {},
                            enableSwipe = false,
                            validateInput = routineViewModel::validateInput,
                            bottomPadding = if (isTrainerVersion) 80.dp else 10.dp
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    routineViewModel.cleanRoutine()
                                    Toast.makeText(context, "Datos eliminados", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Limpiar rutina",
                                    tint = Color.White
                                )
                            }

                            Button(
                                onClick = {
                                    routineViewModel.saveRoutineToFirebase(documentId) { isSuccess ->
                                        val message =
                                            if (isSuccess) "Rutina guardada con éxito"
                                            else "Error al guardar la rutina"
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.save_icon),
                                    contentDescription = "Guardar rutina",
                                    tint = Color.White
                                )
                            }

                            Button(
                                onClick = { showAddDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = com.develop.traiscore.presentation.theme.traiBlue)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Añadir ejercicio",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }

            ViewMode.MONTH -> {
                // ✅ MODO RUTINAS: Pasar solo datos de rutinas
                CalendarScreen(
                    mode = CalendarMode.ROUTINES, // ✅ NUEVO
                    groupedEntries = emptyMap(), // ✅ No pasar sesiones
                    routineDates = routineDates, // ✅ Solo rutinas
                    selectedMonth = selectedMonth,
                    onEditClick = {}, // Sin edición en modo rutinas
                    onDeleteClick = {}, // Sin borrado en modo rutinas
                    routineViewModel = routineViewModel
                )
            }

            ViewMode.YEAR -> {
                // ✅ MODO RUTINAS: Pasar solo datos de rutinas
                YearViewScreen(
                    mode = CalendarMode.ROUTINES, // ✅ NUEVO
                    groupedEntries = emptyMap(), // ✅ No pasar sesiones
                    routineDates = routineDates, // ✅ Solo rutinas
                    onMonthSelected = { month ->
                        selectedMonth = month
                        currentViewMode = ViewMode.MONTH
                    },
                    routineViewModel = routineViewModel
                )
            }
        }

        if (showAddDialog) {
            LaunchedEffect(Unit) { exerciseCategory = null }
            com.develop.traiscore.presentation.components.AddExeRoutineDialog(
                onDismiss = { showAddDialog = false },
                onSave = { exerciseName, category ->
                    routineViewModel.addExerciseToSection(
                        trainingType = selectedType,
                        newExercise = com.develop.traiscore.data.firebaseData.SimpleExercise(
                            name = exerciseName,
                            series = 0,
                            reps = "",
                            weight = "",
                            rir = 0
                        )
                    )
                    Toast.makeText(
                        context,
                        "Ejercicio '$exerciseName' añadido",
                        Toast.LENGTH_SHORT
                    ).show()
                    showAddDialog = false
                },
                exerciseNames = exerciseNames,
                selectedCategory = exerciseCategory,
                onExerciseSelected = { name ->
                    exerciseVM.fetchCategoryFor(name) { cat ->
                        exerciseCategory = cat
                    }
                }
            )
        }
    }
}