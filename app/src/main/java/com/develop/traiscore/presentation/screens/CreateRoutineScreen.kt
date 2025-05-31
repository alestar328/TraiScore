package com.develop.traiscore.presentation.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.develop.traiscore.core.ColumnType
import com.develop.traiscore.core.DefaultCategoryExer
import com.develop.traiscore.core.UserRole
import com.develop.traiscore.data.Authentication.UserRoleManager
import com.develop.traiscore.data.firebaseData.SimpleExercise
import com.develop.traiscore.exports.RoutineExportManager
import com.develop.traiscore.presentation.components.AddExeRoutineDialog
import com.develop.traiscore.presentation.components.AddRestButton
import com.develop.traiscore.presentation.components.CategoryCard
import com.develop.traiscore.presentation.theme.TraiScoreTheme
import com.develop.traiscore.presentation.viewmodels.AddExerciseViewModel
import com.develop.traiscore.presentation.viewmodels.RoutineViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoutineScreen(
    onBack: () -> Unit,
    navController: NavHostController,
    currentUserRole: UserRole,
    viewModel: AddExerciseViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val routineVM: RoutineViewModel = hiltViewModel()

    var exercises by remember {
        mutableStateOf(emptyList<SimpleExercise>())
    }
    var workoutName by remember { mutableStateOf("") }
    val exerciseVM: AddExerciseViewModel = viewModel()
    var selectedCategory by remember { mutableStateOf<DefaultCategoryExer?>(null) }
    var exerciseCategory by remember { mutableStateOf<DefaultCategoryExer?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    val categories = DefaultCategoryExer.entries

    fun updateExerciseField(index: Int, columnType: ColumnType, newValue: String) {
        exercises = exercises.toMutableList().apply {
            this[index] = when(columnType) {
                ColumnType.SERIES -> this[index].copy(series = newValue.toIntOrNull() ?: 0)
                ColumnType.WEIGHT -> this[index].copy(weight = newValue)
                ColumnType.REPS   -> this[index].copy(reps = newValue)
                ColumnType.RIR    -> this[index].copy(rir = newValue.toIntOrNull() ?: 0)
            }
        }
    }
    fun validateRoutineName(name: String): String? {
        return when {
            name.isBlank() -> "El nombre no puede estar vacío"
            name.length > 13 -> "El nombre no puede tener más de 13 caracteres"
            !name.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) -> "Solo se permiten letras y espacios"
            else -> null
        }
    }
    fun onNameChange(newName: String) {
        workoutName = newName
        nameError = validateRoutineName(newName)
    }
    LaunchedEffect(currentUserRole) {
        Log.d("CreateRoutineScreen", "Current user role: $currentUserRole")
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Rutina", color = Color.Yellow) },
                navigationIcon = {                            // ②
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.Yellow
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.DarkGray
                )
            )
        },
        containerColor = Color.DarkGray,
        floatingActionButton = {
            if (selectedCategory != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    if (currentUserRole == UserRole.TRAINER) {
                        Log.d("CreateRoutineScreen", "Showing TRAINER button")

                        FloatingActionButton(
                            onClick = {
                                Log.d("CreateRoutineScreen", "TRAINER export button clicked")

                                // Primero verificar si hay datos para exportar
                                if (selectedCategory == null || exercises.isEmpty() || workoutName.isBlank()) {
                                    // Si no hay datos completos, mostrar debug
                                    Log.d("CreateRoutineScreen", "=== MANUAL DEBUG ===")
                                    Log.d("CreateRoutineScreen", "Current role in composable: $currentUserRole")
                                    Log.d("CreateRoutineScreen", "Selected category: $selectedCategory")
                                    Log.d("CreateRoutineScreen", "Exercises count: ${exercises.size}")
                                    Log.d("CreateRoutineScreen", "Workout name: '$workoutName'")

                                    UserRoleManager.debugUserDocument()
                                    UserRoleManager.getCurrentUserRole { role ->
                                        Log.d("CreateRoutineScreen", "Role from callback: $role")
                                    }

                                    Toast.makeText(
                                        context,
                                        "Completa la rutina antes de exportar (nombre, categoría y ejercicios)",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return@FloatingActionButton
                                }

                                // Si hay datos completos, exportar
                                try {
                                    val routineToExport = com.develop.traiscore.data.firebaseData.RoutineDocument(
                                        userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "",
                                        trainerId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid,
                                        documentId = "",
                                        type = selectedCategory!!.name,
                                        createdAt = com.google.firebase.Timestamp.now(),
                                        clientName = workoutName,
                                        routineName = workoutName,
                                        sections = listOf(
                                            com.develop.traiscore.data.firebaseData.RoutineSection(
                                                type = selectedCategory!!.name,
                                                exercises = exercises
                                            )
                                        )
                                    )

                                    // Aquí irá la función de exportación cuando esté implementada
                                    Toast.makeText(
                                        context,
                                        "🚀 Exportando rutina: $workoutName (${exercises.size} ejercicios)",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    Log.d("CreateRoutineScreen", "Routine ready for export: ${routineToExport.routineName}")


                                    RoutineExportManager.exportRoutine(
                                        context = context,
                                        routine = routineToExport,
                                        onSuccess = { fileUri ->
                                            RoutineExportManager.shareRoutineFile(
                                                context = context,
                                                fileUri = fileUri,
                                                routineName = workoutName
                                            )
                                            Toast.makeText(
                                                context,
                                                "Rutina exportada exitosamente",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        onError = { error ->
                                            Toast.makeText(
                                                context,
                                                error,
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    )


                                } catch (e: Exception) {
                                    Log.e("CreateRoutineScreen", "Error preparing export", e)
                                    Toast.makeText(
                                        context,
                                        "Error al preparar la exportación: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            backgroundColor = Color.Yellow,
                            contentColor   = Color.Black,
                            modifier       = Modifier.size(56.dp)  // tamaño estándar
                        ) {
                            Icon(
                                imageVector   = Icons.Default.Email,
                                contentDescription = "Archivo",
                                tint = Color.Black
                            )
                        }
                    }

                    // Botón de "Guardar rutina"
                    ExtendedFloatingActionButton(
                        onClick = {
                            val validationError = validateRoutineName(workoutName)
                            if (validationError != null) {
                                Toast.makeText(
                                    context,
                                    validationError,
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@ExtendedFloatingActionButton
                            }

                            if (exercises.isEmpty()) {
                                Toast.makeText(
                                    context,
                                    "Agrega al menos un ejercicio",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@ExtendedFloatingActionButton
                            }

                            viewModel.createRoutine(
                                clientName = workoutName,
                                trainerId = null // o tu lógica de trainerId
                            ) { newRoutineId, createError ->
                                if (createError != null || newRoutineId == null) {
                                    Toast.makeText(
                                        context,
                                        "Error creando rutina: $createError",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@createRoutine
                                }
                                viewModel.saveSectionToRoutine(
                                    routineId = newRoutineId,
                                    sectionType = selectedCategory!!,
                                    exercises = exercises
                                ) { success, errorMsg ->
                                    if (success) {
                                        Toast.makeText(
                                            navController.context,
                                            "Rutina guardada con éxito",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        navController.popBackStack()
                                        println("Sección guardada correctamente")
                                    } else {
                                        Toast.makeText(
                                            navController.context,
                                            "Error al guardar: $errorMsg",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        println("Error al guardar: $errorMsg")
                                    }
                                }
                            }
                        },
                        icon = {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Guardar rutina"
                            )
                        },
                        text = { Text("Guardar rutina") },
                        containerColor = Color.Green,
                        contentColor = Color.Black
                    )

                    // Botón de agregar/restar ejercicio
                    AddRestButton(
                        onAdd = { showDialog = true },
                        onRemove = {
                            if (exercises.isNotEmpty()) {
                                exercises = exercises.dropLast(1)
                            }
                        }
                    )
                }
            }

            if (showDialog) {
                LaunchedEffect(Unit) { exerciseCategory = null }

                AddExeRoutineDialog(
                    onDismiss = { showDialog = false },
                    onSave = { name, category ->
                        exercises = exercises + SimpleExercise(name, 0, "", "", 0)

                        showDialog = false
                    },
                    exerciseNames = exerciseVM.exerciseNames,
                    selectedCategory = exerciseCategory, // 👈 correctamente pasado
                    onExerciseSelected = { name ->
                        // pide al VM la categoría y la actualiza
                        exerciseVM.fetchCategoryFor(name) { cat ->
                            exerciseCategory = cat
                        }
                    }

                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Nombre de la rutina",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                )

                OutlinedTextField(
                    value = workoutName,
                    modifier = Modifier
                        .fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy( // ✅ AÑADIR: Configuración del teclado
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    onValueChange = { onNameChange(it) },
                    isError = nameError != null,

                    placeholder = { Text("Nombra tu rutina") },
                    supportingText = {
                        nameError?.let { error ->
                            Text(
                                text = error,
                                color = Color.Red,
                                style = MaterialTheme.typography.bodySmall
                            )
                        } ?: run {
                            Text(
                                text = "${workoutName.length}/13 caracteres",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = com.develop.traiscore.presentation.theme.traiBlue,
                        unfocusedBorderColor = com.develop.traiscore.presentation.theme.traiBlue,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        cursorColor = Color.Black
                    ),
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Categorías",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }
            item {
                if (selectedCategory != null) {
                    val cat = selectedCategory!!

                    CategoryCard(
                        category = cat,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                selectedCategory = null
                            }
                    )

                    RoutineTable(
                        exercises = exercises,
                        onDeleteExercise = { index ->
                            exercises = exercises.toMutableList().apply {
                                removeAt(index)
                            }
                        },
                        onDuplicateExercise = { index -> // ✅ AÑADIR: Callback para duplicar
                            val exerciseToDuplicate = exercises[index]
                            exercises = exercises.toMutableList().apply {
                                add(index + 1, exerciseToDuplicate.copy()) // Duplica justo después del original
                            }
                        },
                        onSeriesChanged = { index, newSeries ->
                            updateExerciseField(index, ColumnType.SERIES, newSeries)
                        },
                        onWeightChanged = { index, newWeight ->
                            updateExerciseField(index, ColumnType.WEIGHT, newWeight)
                        },
                        onRepsChanged = { index, newReps ->
                            updateExerciseField(index, ColumnType.REPS, newReps)
                        },
                        onRirChanged = { index, newRir ->
                            updateExerciseField(index, ColumnType.RIR, newRir)
                        },
                        onFieldChanged = { exerciseIndex, columnType, newValue ->
                            updateExerciseField(exerciseIndex, columnType, newValue)
                        },
                        enableSwipe = true,
                        validateInput = routineVM::validateInput,
                        bottomPadding = 120.dp
                    )


                } else {
                    // Mostramos el grid usando directamente DefaultCategoryExer
                    categories.chunked(2).forEach { rowItems ->

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            rowItems.forEach { cat ->
                                CategoryCard(
                                    category = cat,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            // Al tocar guardamos la categoría seleccionada
                                            selectedCategory = cat
                                        }
                                )
                            }
                            // Si hay un hueco, compensamos
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateRoutineScreenPreview() {
    TraiScoreTheme {
        CreateRoutineScreen(
            currentUserRole =  UserRole.TRAINER,
            onBack = { /* Aquí iría popBackStack() en tu app real */ },
            navController = rememberNavController()
        )
    }
}