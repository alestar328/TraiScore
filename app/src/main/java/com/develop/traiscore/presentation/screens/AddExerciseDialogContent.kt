package com.develop.traiscore.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.develop.traiscore.data.local.entity.WorkoutEntry
import com.develop.traiscore.presentation.components.FilterableDropdown
import com.develop.traiscore.presentation.components.RIRSlider
import com.develop.traiscore.presentation.theme.primaryWhite
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.viewmodels.AddExerciseViewModel

@Composable
fun AddExerciseDialogContent(
    modifier: Modifier = Modifier,
    viewModel: AddExerciseViewModel = hiltViewModel(),
    workoutToEdit: WorkoutEntry? = null,
    onDismiss: () -> Unit,
    onSave: (Map<String, Any>) -> Unit
) {
    val exerciseNames = viewModel.exerciseNames
    var selectedExercise by remember {
        mutableStateOf(
            workoutToEdit?.title ?: viewModel.lastUsedExerciseName.orEmpty()
        ) }
    var repsText by remember { mutableStateOf(workoutToEdit?.reps?.toString() ?: "") }
    var weightText by remember { mutableStateOf(workoutToEdit?.weight?.toString() ?: "") }
    var rirValue by remember { mutableStateOf(workoutToEdit?.rir ?: 2) }
    var repsError by remember { mutableStateOf(false) }
    var weightError by remember { mutableStateOf(false) }


    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
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
        Spacer(modifier = Modifier.size(20.dp))

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
                items = exerciseNames,
                selectedValue = selectedExercise,
                onItemSelected = { selectedExercise = it },
                modifier = Modifier.fillMaxWidth()
            )
        }


        Spacer(modifier = Modifier.size(10.dp))
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
                        shape = RoundedCornerShape(9.dp) // Bordes redondeados
                    )
            ) {
                OutlinedTextField(
                    value = weightText,
                    onValueChange = {newValue ->
                        if(newValue.length <=6 && !newValue.startsWith("-") && !newValue.startsWith("0")  && !newValue.startsWith(".")
                            && !newValue.contains("-") && !newValue.contains(" ") && !newValue.contains("..")
                            && !(newValue.startsWith("0") && newValue.length>1)){
                            weightText = newValue
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = traiBlue,
                        unfocusedBorderColor = traiBlue,
                        cursorColor = Color.Black, // Asegura que el cursor sea visible
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )

                )
            }
        }
        Spacer(modifier = Modifier.size(10.dp))

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
                        shape = RoundedCornerShape(9.dp) // Bordes redondeados
                    )
            ) {
                OutlinedTextField(
                    value = repsText,
                    onValueChange = { newValue ->
                        if(newValue.length <=2 && !newValue.startsWith("-") && !newValue.startsWith("0")
                            && !newValue.contains("-") && !newValue.contains(".") && !newValue.contains(" ")
                            && !(newValue.startsWith("0") && newValue.length>1)){
                            repsText = newValue
                        }


                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = traiBlue,
                        unfocusedBorderColor = traiBlue,
                        cursorColor = Color.Black, // Asegura que el cursor sea visible
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

            }
        }


        Spacer(modifier = Modifier.size(10.dp))

        //RIR SLIDER
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally // 👈 Aquí está la clave

        ) {
            Text(
                text = "RIR: $rirValue",
                color = traiBlue,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            RIRSlider(
                value = rirValue,
                onValueChange = { rirValue = it }
            )


        }
        Spacer(modifier = Modifier.size(10.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    if (workoutToEdit == null) {
                        // Guardar nuevo
                        viewModel.saveWorkoutEntry(
                            title = selectedExercise,
                            reps = repsText.toIntOrNull() ?: 0,
                            weight = weightText.toDoubleOrNull() ?: 0.0,
                            rir = rirValue
                        )
                    } else {
                        // Editar existente
                        val updated: Map<String, Any> = mapOf(
                            "title" to selectedExercise as Any,
                            "reps" to (repsText.toIntOrNull() ?: 0) as Any,
                            "weight" to (weightText.toDoubleOrNull() ?: 0.0) as Any,
                            "rir" to rirValue as Any
                        )
                        onSave(updated)
                    }
                    Toast.makeText(context, "Guardado", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = traiBlue // Botón guardar en azul
                )
            ) {
                Text(text = "Guardar", color = primaryWhite)
            }

            Button(
                onClick = { onDismiss() },
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