package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.develop.traiscore.data.firebaseData.FirestoreExercise
import com.develop.traiscore.presentation.components.FilterableDropdown

@Composable
fun CreateRoutineScreen() {
    var exercises by remember {
        mutableStateOf(
            listOf(
                SimpleExercise("Press de banca", 3, "12", "50", 2)
            )
        )
    }
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = "Crear Rutina",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .padding(vertical = 16.dp)

                )
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                var workoutName by remember { mutableStateOf("") }

                Text(
                    text = "Nombre de la rutina",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                )

                OutlinedTextField(
                    value = workoutName,
                    onValueChange = { workoutName = it },
                    placeholder = { Text("PushDay") },
                    modifier = Modifier.fillMaxWidth()

                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                var selectedCategory by remember { mutableStateOf("Selecciona categoria") }

                Text(
                    text = "Categoria",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                )
                FilterableDropdown(
                    items = listOf("Empuje", "Piernas", "Brazos", "Espalda"),
                    onItemSelected = { selectedCategory = it },
                    selectedValue = selectedCategory,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Ejercicios",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                )

                RoutineTable(
                    exercises = exercises,
                    onRepsChanged = { index, newRep ->
                        exercises = exercises.toMutableList().apply {
                            val current = get(index)
                            set(index, current.copy(reps = newRep))
                        }
                    }
                )

            }

        }


        FloatingActionButton(
            onClick = {
                exercises = exercises + SimpleExercise("", 0, "", "", 0)
            },
            containerColor = com.develop.traiscore.presentation.theme.traiBlue,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Agregar ejercicio")
        }
    }
}

@Composable
fun ExerciseRow(
    exercise: FirestoreExercise,
    onUpdate: (FirestoreExercise) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8F8F8))
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Exercise",
                modifier = Modifier
                    .weight(2f)
                    .padding(start = 8.dp),
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = "Sets",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = "Reps",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall
            )
        }

        // Inputs row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = exercise.name,
                onValueChange = { onUpdate(exercise.copy(name = it)) },
                placeholder = { Text("e.g. Bench Press") },
                modifier = Modifier.weight(2f)
            )
            OutlinedTextField(
                value = exercise.series,
                onValueChange = { onUpdate(exercise.copy(series = it)) },
                placeholder = { Text("e.g. 3") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = exercise.reps,
                onValueChange = { onUpdate(exercise.copy(reps = it)) },
                placeholder = { Text("e.g. 10") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateRoutineScreenPreview() {

    CreateRoutineScreen()

}