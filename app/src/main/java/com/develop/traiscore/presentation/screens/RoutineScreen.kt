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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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



data class RoutineData(
    val clientName: String,
    val createdAt: String,  // O usa Date, según necesites.
    val routine: Map<String, List<SimpleExercise>>
)

@Composable
fun RoutineScreen(routineData: RoutineData, documentId: String) {
    val routineViewModel: RoutineViewModel = viewModel()
    val context = LocalContext.current

    // Inicializa el estado del ViewModel con los datos iniciales solo una vez
    LaunchedEffect(routineData) {
        if (routineViewModel.routineData == null) {
            routineViewModel.routineData = routineData
        }
    }

    // Si el ViewModel no tiene datos aún, muestra un mensaje de carga
    val currentRoutineData  = routineViewModel.routineData
    if (currentRoutineData  == null) {
        Text("Cargando datos...")
        return
    }
    Scaffold {
        innerPadding ->

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
            currentRoutineData.routine.forEach { (trainingType, exercises) ->
                item {
                    RoutineTable(
                        routineType = trainingType,
                        exercises = exercises,
                        onRepsChanged = { exerciseIndex, newRep ->
                            routineViewModel.updateReps(exerciseIndex, trainingType, newRep)
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Button(
                        onClick = {
                            routineViewModel.cleanRoutine()
                            val message = "Datos eliminados"
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        )

                    ){
                        Text("Limpiar")
                    }
                    Button(
                        onClick = {
                            routineViewModel.saveRoutine(documentId) { isSuccess ->
                                val message = if (isSuccess) {
                                    "Rutina guardada con éxito"
                                } else {
                                    "Error al guardar la rutina"
                                }
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Green
                        )
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
    // Datos dummy de ejemplo replicando la estructura del JSON:
    val routineData = RoutineData(
        clientName = "Daniel",
        createdAt = "2025-04-08T19:40:04.423Z",
        routine = mapOf(
            "Empuje" to listOf(
                SimpleExercise("Press banca", series = 3, reps = "10", weight = "100", rir = 4),
                SimpleExercise("Triceps X", series = 4, reps = "", weight = "15", rir = 7)
            ),
            "Pierna" to listOf(
                SimpleExercise("Sentadilla", series = 3, reps = "5", weight = "30", rir = 3),
                SimpleExercise("Prensa", series = 2, reps = "12", weight = "100", rir = 4)
            ),
            "Tirón" to listOf(
                SimpleExercise("Dominadas", series = 5, reps = "8", weight = "16", rir = 1),
                SimpleExercise("Curl biceps", series = 3, reps = "15", weight = "20", rir = 2)
            )
        )
    )

    // Muestra la pantalla completa con scroll vertical
    RoutineScreen(routineData = routineData, documentId = "dummyDocumentId")
}