package com.develop.traiscore.presentation.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.develop.traiscore.core.DefaultCategoryExer
import com.develop.traiscore.data.firebaseData.SimpleExercise
import com.develop.traiscore.presentation.components.AddExeRoutineDialog
import com.develop.traiscore.presentation.components.AddRestButton
import com.develop.traiscore.presentation.components.CategoryCard
import com.develop.traiscore.presentation.theme.TraiScoreTheme
import com.develop.traiscore.presentation.viewmodels.AddExerciseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoutineScreen(
    onBack: () -> Unit,
    navController: NavHostController,
    viewModel: AddExerciseViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    var exercises by remember {
        mutableStateOf(emptyList<SimpleExercise>())
    }
    var workoutName by remember { mutableStateOf("") }
    val exerciseVM: AddExerciseViewModel = viewModel()
    var selectedCategory by remember { mutableStateOf<DefaultCategoryExer?>(null) }

    var showDialog by remember { mutableStateOf(false) }
    val categories = DefaultCategoryExer.entries


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
                    // Botón de "Guardar rutina"
                    ExtendedFloatingActionButton(
                        onClick = {
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
                AddExeRoutineDialog(
                    onDismiss = { showDialog = false },
                    onSave = { name, category ->
                            exercises = exercises + SimpleExercise(name, 0, "", "", 0)

                        showDialog = false
                    },
                    exerciseNames = exerciseVM.exerciseNames,
                    selectedCategory = selectedCategory, // 👈 correctamente pasado
                    onExerciseSelected = { exerciseName ->
                        exerciseVM.fetchCategoryFor(exerciseName) { category ->
                            selectedCategory = category
                        }
                    },
                    onCategorySelected = { category ->
                        selectedCategory = category
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
                    onValueChange = { workoutName = it },
                    placeholder = { Text("Nombra tu rutina") },
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
                        onRepsChanged = { index, newRep ->
                            exercises = exercises.toMutableList().apply {
                                this[index] = this[index].copy(reps = newRep)
                            }
                        }
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
            onBack = { /* Aquí iría popBackStack() en tu app real */ },
            navController = rememberNavController()
        )
    }
}