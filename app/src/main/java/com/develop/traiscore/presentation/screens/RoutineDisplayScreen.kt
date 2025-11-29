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
import com.google.firebase.auth.FirebaseAuth


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineScreen(
    routineViewModel: RoutineViewModel = viewModel(),
    documentId: String,
    selectedType: String, // <- nuevo parámetro
    onBack: () -> Unit
) {

    val context = LocalContext.current
    var showEmptyDialog by remember { mutableStateOf(false) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid
        ?: run { onBack(); return }
    val isTrainerVersion = BuildConfig.FLAVOR == "trainer"

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
        return   // 👈 MUY IMPORTANTE
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


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentRoutineData.routineName.ifBlank { "Rutina" },
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onBack() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                modifier = Modifier.height(56.dp)
            )
        },
        floatingActionButton = {
            // Solo para TRAINER mostramos el FAB de “Enviar rutina”
            if (isTrainerVersion) {
                FloatingActionButton(
                    onClick = {
                        // ✅ NUEVA FUNCIONALIDAD DE EXPORTACIÓN
                        val currentRoutine = routineViewModel.routineDocument

                        if (currentRoutine == null) {
                            Toast.makeText(
                                context,
                                "Error: No se pudo cargar la rutina",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@FloatingActionButton
                        }

                        // Verificar que hay datos para exportar
                        if (currentRoutine.sections.isEmpty()) {
                            Toast.makeText(
                                context,
                                "La rutina no tiene secciones para exportar",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@FloatingActionButton
                        }

                        try {
                            // Exportar usando RoutineExportManager
                            com.develop.traiscore.exports.RoutineExportManager.exportRoutine(
                                context = context,
                                routine = currentRoutine,
                                onSuccess = { fileUri ->
                                    // Compartir el archivo después de exportar
                                    com.develop.traiscore.exports.RoutineExportManager.shareRoutineFile(
                                        context = context,
                                        fileUri = fileUri,
                                        routineName = currentRoutine.routineName
                                    )
                                    Toast.makeText(
                                        context,
                                        "✅ Rutina '${currentRoutine.routineName}' exportada y compartida",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onError = { error ->
                                    Toast.makeText(
                                        context,
                                        "❌ Error al exportar: $error",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            )
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Error inesperado: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    containerColor = Color.Yellow,
                    contentColor = Color.Black,
                    modifier = Modifier
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Enviar rutina"
                    )
                }
            }
        }
    )
    { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Encabezado general de la rutina

            // Por cada tipo de entrenamiento, se muestra una tabla
            item {
                val exerciseCount = filteredExercises.size

                RoutineTable(
                    exercises = filteredExercises,
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
                        Text("Limpiar")
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
                        Text("Guardar")
                    }
                }
            }
        }
    }
}
