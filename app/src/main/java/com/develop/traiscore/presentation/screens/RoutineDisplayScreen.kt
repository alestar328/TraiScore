package com.develop.traiscore.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.viewmodels.RoutineViewModel
import androidx.compose.ui.platform.LocalContext
import com.develop.traiscore.data.firebaseData.RoutineDocument
import com.develop.traiscore.data.firebaseData.SimpleExercise
import com.google.firebase.auth.FirebaseAuth
import java.util.Date


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

    // Inicializa el estado del ViewModel con los datos iniciales solo una vez
    LaunchedEffect(documentId) {
        routineViewModel.loadRoutine(documentId, userId)
    }


    // Si el ViewModel no tiene datos aún, muestra un mensaje de carga
    val currentRoutineData = routineViewModel.routineDocument
    if (currentRoutineData == null) {
        Text("Cargando datos...")
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
    val filteredExercises = currentRoutineData.routineExer[selectedType] ?: emptyList()


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Rutina: $selectedType",
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
                    containerColor = traiBlue
                )
            )
        }
    )
    { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.DarkGray)
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 60.dp)
        ) {
            // Encabezado general de la rutina
            item {
                Text(
                    text = "Rutina para: ${currentRoutineData.clientName}",
                    color = Color.Red,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    modifier = Modifier.padding(
                        top = 16.dp,
                        start = 16.dp,
                        end = 16.dp
                    )
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 3.dp,
                    color = traiBlue
                )
            }
            // Por cada tipo de entrenamiento, se muestra una tabla
            item {
                RoutineTable(
                    exercises = filteredExercises,
                    onRepsChanged = { exerciseIndex, newRep ->
                        routineViewModel.updateReps(exerciseIndex, selectedType, newRep)
                    },
                    onDeleteExercise = {
                    },
                    enableSwipe      = false
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
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
                            routineViewModel.saveRoutine(documentId) { isSuccess ->
                                val message =
                                    if (isSuccess) "Rutina guardada con éxito" else "Error al guardar la rutina"
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

@Preview(showBackground = true)
@Composable
fun RoutineScreenPreview() {
    val now = com.google.firebase.Timestamp(Date())

    val routineDocument = RoutineDocument(
        clientName = "Daniel",
        routineName = "Empujes",
        createdAt = now,
        type = "Pecho",
        userId = "545454",
        documentId = "sadasdasd",
//        createdAt = "2025-04-08T19:40:04.423Z",
        routineExer = mapOf(
            "Empuje" to listOf(
                SimpleExercise("Press banca", 3, "10", "100", 4),
                SimpleExercise("Triceps X", 4, "", "15", 7)
            ),
            "Pierna" to listOf(
                SimpleExercise("Sentadilla", 3, "5", "30", 3),
                SimpleExercise("Prensa", 2, "12", "100", 4)
            ),
            "Tirón" to listOf(
                SimpleExercise("Dominadas", 5, "8", "16", 1),
                SimpleExercise("Curl biceps", 3, "15", "20", 2)
            )
        )
    )

    RoutineScreen(
        documentId = "dummyDocumentId",
        selectedType = "Pierna",
        onBack = {}
    )
}
