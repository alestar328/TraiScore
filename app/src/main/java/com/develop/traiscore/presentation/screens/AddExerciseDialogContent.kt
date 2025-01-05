package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.develop.traiscore.domain.defaultExercises
import com.develop.traiscore.presentation.components.FilterableDropdown
import com.develop.traiscore.presentation.theme.primaryWhite
import com.develop.traiscore.presentation.theme.traiBlue

@Composable
fun AddExerciseDialogContent(
    modifier: Modifier = Modifier,
    onSave: (String, Int, Double, Int) -> Unit,
    onCancel: () -> Unit
) {
    var exerciseName by remember { mutableStateOf("") }
    var exerciseReps by remember { mutableStateOf("") }
    var exerciseWeight by remember { mutableStateOf("") }
    var sliderValue by remember { mutableStateOf(5f) } // Slider for RIR

    Column(
        modifier = modifier
            .fillMaxWidth(0.95f)
            .padding(16.dp)
            .background(
                color = Color.Gray,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(24.dp), // Espaciado interno
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Nueva actividad",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = traiBlue
        )
        Spacer(modifier = Modifier.size(24.dp))

        //SELECCIONA del ejericicio
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Selecciona ejercicio",
                color = traiBlue,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            FilterableDropdown(
                items = com.develop.traiscore.domain.defaultExercises.map { it.name },
                onItemSelected = { exerciseName = it },
                selectedValue = exerciseName
            )
        }


        Spacer(modifier = Modifier.size(16.dp))

        //Repeticiones ejercicio
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Repeticiones",
                color = traiBlue,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = traiBlue, // Color del borde
                        shape = RoundedCornerShape(12.dp) // Bordes redondeados
                    )
                    .padding(3.dp) // Grosor del borde
            ) {
                OutlinedTextField(
                    value = exerciseReps.toString(),
                    onValueChange = { exerciseReps = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = traiBlue,
                        unfocusedBorderColor = traiBlue
                    )
                )
            }
        }
        Spacer(modifier = Modifier.size(16.dp))

        //Peso ejercicio
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Peso",
                color = traiBlue,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = traiBlue, // Color del borde
                        shape = RoundedCornerShape(12.dp) // Bordes redondeados
                    )
                    .padding(3.dp) // Grosor del borde
            ) {
                OutlinedTextField(
                    value = exerciseWeight.toString(),
                    onValueChange = { exerciseWeight = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = traiBlue,
                        unfocusedBorderColor = traiBlue
                    )
                )
            }
        }
        Spacer(modifier = Modifier.size(16.dp))

        //RIR SLIDER
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "RIR",
                color = traiBlue,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.size(8.dp))
            var sliderValue by remember { mutableStateOf(5f) } // Valor inicial del slider

            Text(
                text = "@${sliderValue.toInt()}",
                color = traiBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                valueRange = 1f..10f, // Rango de valores del slider
                steps = 8, // Pasos intermedios (10 - 1 = 9, por lo tanto, 8 pasos intermedios)
                colors = SliderDefaults.colors(
                    thumbColor = traiBlue, // Color del deslizador circular
                    activeTrackColor = traiBlue, // Color de la barra activa
                    inactiveTrackColor = Color.Gray // Color de la barra inactiva
                )
            )
        }
        Spacer(modifier = Modifier.size(24.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    val reps = exerciseReps.toIntOrNull() ?: 0
                    val weight = exerciseWeight.toDoubleOrNull() ?: 0.0
                    val rir = sliderValue.toInt() ?: 0

                    onSave(exerciseName, reps, weight, rir)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = traiBlue // Botón guardar en azul
                )
            ) {
                Text(text = "Guardar", color = primaryWhite)
            }

            Button(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red // Color rojo para el botón
                )
            ) {
                Text(text = "Cancelar", color = primaryWhite)
            }
        }
    }
}