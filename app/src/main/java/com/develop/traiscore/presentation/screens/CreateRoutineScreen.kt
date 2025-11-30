package com.develop.traiscore.presentation.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.develop.traiscore.BuildConfig
import com.develop.traiscore.core.ColumnType
import com.develop.traiscore.core.DefaultCategoryExer
import com.develop.traiscore.data.firebaseData.SimpleExercise
import com.develop.traiscore.exports.RoutineExportManager
import com.develop.traiscore.presentation.components.AddExeRoutineDialog
import com.develop.traiscore.presentation.components.MuscleGroupCarousel
import com.develop.traiscore.presentation.theme.TraiScoreTheme
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.viewmodels.AddExerciseViewModel
import com.develop.traiscore.presentation.viewmodels.RoutineViewModel
import com.google.firebase.auth.FirebaseAuth


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoutineScreen(
    onBack: () -> Unit,
    navController: NavHostController,
    targetClientId: String? = null,
    clientName: String? = null,
    viewModel: AddExerciseViewModel = hiltViewModel(),
    routineViewModel: RoutineViewModel = hiltViewModel()

) {
    val context = LocalContext.current
    val routineVM: RoutineViewModel = hiltViewModel()
    val isTrainerVersion = BuildConfig.FLAVOR == "trainer"

    var exercises by remember {
        mutableStateOf(emptyList<SimpleExercise>())
    }
    var workoutName by remember { mutableStateOf("") }
    val exerciseVM: AddExerciseViewModel = viewModel()

    var exerciseCategory by remember { mutableStateOf<DefaultCategoryExer?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    val exerciseNames by exerciseVM.exerciseNames.collectAsState()

    // ✅ Estado para la imagen seleccionada del carousel
    var selectedMuscleGroupImage by remember { mutableStateOf<Int?>(null) }
    var showSelectedImage by remember { mutableStateOf(false) }
    var selectedCategoryEnum by remember { mutableStateOf<DefaultCategoryExer?>(null) } // 👈 nuevo

    fun resToEnum(resId: Int): DefaultCategoryExer? =
        DefaultCategoryExer.values().firstOrNull { it.imageCat == resId }

    fun updateExerciseField(index: Int, columnType: ColumnType, newValue: String) {
        exercises = exercises.toMutableList().apply {
            this[index] = when (columnType) {

                ColumnType.EXERCISE_NAME ->
                    this[index].copy(name = newValue)

                ColumnType.SERIES ->
                    this[index].copy(series = newValue.toIntOrNull() ?: 0)

                ColumnType.WEIGHT ->
                    this[index].copy(weight = newValue)

                ColumnType.REPS ->
                    this[index].copy(reps = newValue)

                ColumnType.RIR ->
                    this[index].copy(rir = newValue.toIntOrNull() ?: 0)
            }
        }
    }


    fun validateRoutineName(name: String): String? {
        return when {
            name.isBlank() -> "El nombre no puede estar vacío"
            name.length > 50 -> "El nombre no puede tener más de 50 caracteres"
            else -> null
        }
    }

    fun onNameChange(newName: String) {
        workoutName = newName
        nameError = validateRoutineName(newName)
    }

    fun canSave(): Boolean {
        return workoutName.trim().isNotEmpty() &&
                nameError == null &&
                exercises.isNotEmpty()
    }

    val screenTitle = when {
        targetClientId != null && clientName != null -> "Nueva Rutina para $clientName"
        else -> "Nueva Rutina"
    }
    val effectiveUserId = targetClientId ?: FirebaseAuth.getInstance().currentUser?.uid

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = screenTitle,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = traiBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                modifier = Modifier.height(56.dp)
            )
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
            .exclude(WindowInsets.navigationBars),
        containerColor = MaterialTheme.colorScheme.background,

    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    // Campo de nombre de la rutina
                    Text(
                        text = "Nombre de la rutina",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    )

                    OutlinedTextField(
                        value = workoutName,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done
                        ),
                        onValueChange = { onNameChange(it) },
                        isError = nameError != null,
                        placeholder = { Text("Ej: Push Day, Rutina Piernas, Día 1...") },
                        supportingText = {
                            nameError?.let { error ->
                                Text(
                                    text = error,
                                    color = Color.Red,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            } ?: run {
                                Text(
                                    text = "${workoutName.length}/50 caracteres",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = traiBlue,
                            unfocusedBorderColor = traiBlue,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            cursorColor = Color.Black
                        ),
                    )

                }
                item {
                    // Sección de ejercicios
                    Text(
                        text = "Ejercicios",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

                    if (exercises.isNotEmpty()) {
                        // Mostrar tabla de ejercicios
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RoutineTable(
                                exercises = exercises,
                                exerciseNames = exerciseNames,
                                onDeleteExercise = { index ->
                                    exercises = exercises.toMutableList().apply {
                                        removeAt(index)
                                    }
                                },
                                onDuplicateExercise = { index ->
                                    val exerciseToDuplicate = exercises[index]
                                    exercises = exercises.toMutableList().apply {
                                        add(index + 1, exerciseToDuplicate.copy())
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
                                validateInput = routineVM::validateInput
                            )
                        }
                    } else {
                        // Mensaje cuando no hay ejercicios
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay ejercicios agregados.\nToca el botón + para agregar ejercicios.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                }
                item {
                    // ✅ Carousel de grupos musculares - SIEMPRE VISIBLE
                    Text(
                        text = "Selecciona el grupo muscular principal",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

                    MuscleGroupCarousel(
                        modifier = Modifier.fillMaxWidth(),
                        onImageSelected = { imageRes ->
                            selectedMuscleGroupImage = imageRes
                            selectedCategoryEnum = resToEnum(imageRes) // 👈 guarda el enum
                            showSelectedImage = true
                            Log.d(
                                "CreateRoutineScreen",
                                "Imagen seleccionada: $imageRes -> ${selectedCategoryEnum?.name}"
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(50.dp))
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter) // 👈 Clave: ahora sí están abajo
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(Color.Transparent),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 🔸 BOTÓN EXPORTAR (solo entrenador)
                if (isTrainerVersion && canSave()) {
                    FloatingActionButton(
                        onClick = {
                            try {
                                val routineToExport =
                                    com.develop.traiscore.data.firebaseData.RoutineDocument(
                                        userId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                                        trainerId = FirebaseAuth.getInstance().currentUser?.uid,
                                        documentId = "",
                                        type = selectedCategoryEnum?.name ?: "CUSTOM",
                                        createdAt = com.google.firebase.Timestamp.now(),
                                        clientName = workoutName,
                                        routineName = workoutName,
                                        sections = listOf(
                                            com.develop.traiscore.data.firebaseData.RoutineSection(
                                                type = workoutName,
                                                exercises = exercises
                                            )
                                        )
                                    )

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
                                Toast.makeText(
                                    context,
                                    "Error al preparar la exportación: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        backgroundColor = Color.Yellow,
                        contentColor = Color.Black,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Exportar",
                            tint = Color.Black
                        )
                    }
                }

                // 🔸 BOTÓN GUARDAR
                ExtendedFloatingActionButton(
                    onClick = {
                        if (!canSave()) {
                            when {
                                workoutName.trim().isEmpty() ->
                                    Toast.makeText(
                                        context,
                                        "Ingresa un nombre para la rutina",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                nameError != null ->
                                    Toast.makeText(context, nameError, Toast.LENGTH_SHORT).show()

                                exercises.isEmpty() ->
                                    Toast.makeText(
                                        context,
                                        "Agrega al menos un ejercicio",
                                        Toast.LENGTH_SHORT
                                    ).show()
                            }
                            return@ExtendedFloatingActionButton
                        }

                        if (effectiveUserId != null) {
                            routineViewModel.createRoutineForUser(
                                userId = effectiveUserId,
                                clientName = workoutName,
                                trainerId = if (targetClientId != null)
                                    FirebaseAuth.getInstance().currentUser?.uid else null,
                                routineType = selectedCategoryEnum?.name
                            ) { newRoutineId, createError ->
                                if (createError != null || newRoutineId == null) {
                                    Toast.makeText(
                                        context,
                                        "Error creando rutina: $createError",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@createRoutineForUser
                                }

                                routineViewModel.saveSectionToRoutineForUser(
                                    userId = effectiveUserId,
                                    routineId = newRoutineId,
                                    sectionName = selectedCategoryEnum?.name ?: "CUSTOM",
                                    exercises = exercises
                                ) { success, errorMsg ->
                                    if (success) {
                                        Toast.makeText(
                                            context,
                                            if (targetClientId != null)
                                                "Rutina creada para $clientName exitosamente"
                                            else
                                                "Rutina guardada con éxito",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        onBack()
                                        exercises = emptyList()
                                        workoutName = ""
                                        selectedCategoryEnum = null
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Error al guardar: $errorMsg",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Check, contentDescription = "Guardar rutina") },
                    text = {
                        Text(if (targetClientId != null) "Crear para cliente" else "Guardar rutina")
                    },
                    containerColor = if (canSave()) Color.Green else Color.Gray,
                    contentColor = Color.Black
                )

                // 🔸 BOTÓN + / -
                FloatingActionButton(
                    onClick = { showDialog = true },
                    backgroundColor = traiBlue,
                    contentColor = Color.White,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar ejercicio"
                    )
                }
            }

            // 🔹 DIALOGO DE NUEVO EJERCICIO
            if (showDialog) {
                LaunchedEffect(Unit) { exerciseCategory = null }

                AddExeRoutineDialog(
                    onDismiss = { showDialog = false },
                    onSave = { name, category ->
                        exercises = exercises + SimpleExercise(name, 0, "", "", 0)
                        showDialog = false
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
}

@Preview(showBackground = true)
@Composable
fun CreateRoutineScreenPreview() {
    TraiScoreTheme {
        CreateRoutineScreen(
            onBack = { /* Preview */ },
            navController = rememberNavController()
        )
    }
}