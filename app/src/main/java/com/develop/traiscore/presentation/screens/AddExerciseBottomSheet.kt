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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import com.develop.traiscore.R
import com.develop.traiscore.data.local.entity.WorkoutEntry
import com.develop.traiscore.presentation.components.FilterableDropdown
import com.develop.traiscore.presentation.components.RIRSlider
import com.develop.traiscore.presentation.theme.primaryWhite
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.viewmodels.AddExerciseViewModel
import com.develop.traiscore.presentation.viewmodels.NewSessionViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExerciseBottomSheet(
    modifier: Modifier = Modifier,
    viewModel: AddExerciseViewModel = hiltViewModel(),
    workoutToEdit: WorkoutEntry? = null,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onSave: (Map<String, Any>) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true // ✅ AÑADIR: Salta el estado parcial
    )
) {
    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = Color.Gray,
            contentColor = Color.White,
            modifier = modifier

        ) {
            AddExerciseBottomSheetContent(
                viewModel = viewModel,
                workoutToEdit = workoutToEdit,
                onDismiss = onDismiss,
                onSave = onSave
            )
        }
    }
}

@Composable
private fun AddExerciseBottomSheetContent(
    viewModel: AddExerciseViewModel,
    workoutToEdit: WorkoutEntry?,
    onDismiss: () -> Unit,
    onSave: (Map<String, Any>) -> Unit
) {
    val exerciseNames = viewModel.exerciseNames
    var selectedExercise by remember {
        mutableStateOf(
            workoutToEdit?.title ?: viewModel.lastUsedExerciseName.orEmpty()
        )
    }
    var repsText by remember { mutableStateOf(workoutToEdit?.reps?.toString() ?: "") }
    var exerciseId by remember { mutableStateOf(workoutToEdit?.exerciseId ?: 0) }
    var weightText by remember { mutableStateOf(workoutToEdit?.weight?.toString() ?: "") }
    var rirValue by remember { mutableStateOf(workoutToEdit?.rir ?: 2) }
    val newSessionViewModel: NewSessionViewModel = hiltViewModel()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // ✅ OBTENER strings fuera del scope de corrutina
    val savedMessage = stringResource(id = R.string.add_exer_saved)

    // ✅ USAR Box con Modifier correcto
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Título
            Text(
                text = stringResource(id = R.string.add_activity_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = traiBlue
            )

            // Selección de ejercicio
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.add_exer_select_exer),
                    color = traiBlue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FilterableDropdown(
                    items = exerciseNames,
                    selectedValue = selectedExercise,
                    onItemSelected = { selectedExercise = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Campo de peso
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.add_exer_weight),
                    color = traiBlue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = traiBlue,
                            shape = RoundedCornerShape(9.dp)
                        )
                ) {
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { newValue ->
                            if (newValue.length <= 6 && !newValue.startsWith("-") && !newValue.startsWith(
                                    "."
                                )
                                && !newValue.contains("-") && !newValue.contains(" ") && !newValue.contains(
                                    ".."
                                )
                                && !(newValue.startsWith("0") && newValue.length > 1)
                            ) {
                                weightText = newValue
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = traiBlue,
                            unfocusedBorderColor = traiBlue,
                            cursorColor = Color.Black,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }
            }

            // Campo de repeticiones
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.add_exer_reps),
                    color = traiBlue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = traiBlue,
                            shape = RoundedCornerShape(9.dp)
                        )
                ) {
                    OutlinedTextField(
                        value = repsText,
                        onValueChange = { newValue ->
                            if (newValue.length <= 2 && !newValue.startsWith("-") && !newValue.startsWith(
                                    "0"
                                )
                                && !newValue.contains("-") && !newValue.contains(".") && !newValue.contains(
                                    " "
                                )
                                && !(newValue.startsWith("0") && newValue.length > 1)
                            ) {
                                repsText = newValue
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = traiBlue,
                            unfocusedBorderColor = traiBlue,
                            cursorColor = Color.Black,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }
            }

            // RIR Slider
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.add_exer_rir) + " $rirValue",
                    color = traiBlue,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                RIRSlider(
                    value = rirValue,
                    onValueChange = { rirValue = it }
                )
            }

            // Botones de acción
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        if (workoutToEdit == null) {
                            val sessionData = newSessionViewModel.getSessionDataForWorkout()
                            // Guardar nuevo
                            viewModel.addExerciseToActiveSession(
                                title = selectedExercise,
                                reps = repsText.toIntOrNull() ?: 0,
                                weight = weightText.toFloatOrNull()
                                    ?: 0.0f, // Float en lugar de Double
                                rir = rirValue
                            )
                        } else {
                            // Editar existente
                            val updated: Map<String, Any> = mapOf(
                                "title" to selectedExercise as Any,
                                "exerciseId" to exerciseId as Any,
                                "reps" to (repsText.toIntOrNull() ?: 0) as Any,
                                "weight" to (weightText.toDoubleOrNull() ?: 0.0) as Any,
                                "rir" to rirValue as Any
                            )
                            onSave(updated)
                        }
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = savedMessage, // ✅ USAR string obtenido fuera de la corrutina
                                duration = SnackbarDuration.Short
                            )
                        }
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = traiBlue
                    )
                ) {
                    Text(text = stringResource(id = R.string.add_exer_save), color = primaryWhite)
                }

                Button(
                    onClick = { onDismiss() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text(text = stringResource(id = R.string.add_exer_cancel), color = primaryWhite)
                }
            }

            // Espaciado inferior para evitar que se corte con la navegación
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ✅ SNACKBARHOST fuera de la Column, usando Alignment.BottomCenter del Box
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
        )
    }
}