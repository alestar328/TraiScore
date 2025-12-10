package com.develop.traiscore.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.develop.traiscore.BuildConfig
import com.develop.traiscore.presentation.viewmodels.AddExerciseViewModel
import com.google.firebase.auth.FirebaseAuth


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineScreen(
    routineViewModel: RoutineViewModel = viewModel(),
    documentId: String,
    selectedType: String,
    onBack: () -> Unit,
    onConfigureTopBar: (
        @Composable () -> Unit,      // left
        @Composable () -> Unit,      // right
        (@Composable () -> Unit)?
    ) -> Unit,
    onConfigureFAB: (fab: (@Composable () -> Unit)?) -> Unit
) {
    val exerciseVM: AddExerciseViewModel = viewModel()
    val exerciseNames by exerciseVM.exerciseNames.collectAsState()
    val context = LocalContext.current
    var showEmptyDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) } // ✅ NUEVO: Estado para diálogo
    var exerciseCategory by remember { mutableStateOf<com.develop.traiscore.core.DefaultCategoryExer?>(null) } // ✅ NUEVO

    val userId = FirebaseAuth.getInstance().currentUser?.uid
        ?: run { onBack(); return }
    val isTrainerVersion = BuildConfig.FLAVOR == "trainer"
    val canEditExercises = true
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
                        tint = Color.White
                    )
                }
            },
            { /* nada a la derecha */ },
            {
                Text(
                    text = routineTitle,
                    color = Color.White
                )
            }
        )
    }

    // ► Configuración del FAB SOLO trainer
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

    // ► Carga de datos
    LaunchedEffect(documentId) {
        routineViewModel.loadRoutine(documentId)
    }
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
                    // ✅ BOTÓN LIMPIAR
                    Button(
                        onClick = {
                            routineViewModel.cleanRoutine()
                            Toast.makeText(context, "Datos eliminados", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Limpiar")
                    }

                    // ✅ BOTÓN GUARDAR
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
                        Text("Guardar")
                    }

                    // ✅ NUEVO: BOTÓN AÑADIR EJERCICIO
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

        // ✅ DIÁLOGO PARA AÑADIR EJERCICIO (igual que CreateRoutineScreen)
        if (showAddDialog) {
            LaunchedEffect(Unit) { exerciseCategory = null }

            com.develop.traiscore.presentation.components.AddExeRoutineDialog(
                onDismiss = { showAddDialog = false },
                onSave = { exerciseName, category ->
                    // ✅ Añadir ejercicio al final de la sección actual
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