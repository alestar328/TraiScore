package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.develop.traiscore.core.CategoryVisualMapper
import com.develop.traiscore.core.DefaultCategoryExer
import com.develop.traiscore.core.VisualCategory
import com.develop.traiscore.data.firebaseData.SimpleExercise
import com.develop.traiscore.presentation.components.AddExeRoutineDialog
import com.develop.traiscore.presentation.components.AddRestButton
import com.develop.traiscore.presentation.components.CategoryCard
import com.develop.traiscore.presentation.theme.TraiScoreTheme
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.viewmodels.AddExerciseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoutineScreen(
    onBack: () -> Unit,
    viewModel: AddExerciseViewModel = hiltViewModel()
) {
    var exercises by remember {
        mutableStateOf(emptyList<SimpleExercise>())
    }
    var workoutName by remember { mutableStateOf("") }
    val context = LocalContext.current
    val exerciseVM: AddExerciseViewModel = viewModel()
    var selectedCategory by remember { mutableStateOf<DefaultCategoryExer?>(null) }

    var selectedVisualCategory by remember { mutableStateOf<VisualCategory?>(null) }
    var showDialog by remember { mutableStateOf(false) }

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
            if (selectedVisualCategory != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Botón de "Guardar rutina"
                    ExtendedFloatingActionButton(
                        onClick = {
                            println("✅ Guardando rutina: $workoutName")
                        },
                        icon = { Icon(Icons.Default.Check, contentDescription = "Guardar rutina") },
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
                        val alreadyExists = exercises.any { it.name.equals(name, ignoreCase = true) }
                        if (!alreadyExists) {
                            exercises = exercises + SimpleExercise(name, 0, "", "", 0)
                        }
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
                if (selectedVisualCategory != null) {
                    val selected = selectedVisualCategory!!
                    val title = stringResource(id = selected.nameResId)
                    CategoryCard(
                        imageRes = selected.imageResId,
                        title = title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                selectedVisualCategory = null // <- esto restablece la vista
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
                    val chunked = CategoryVisualMapper.visualCategories.chunked(2)
                    chunked.forEach { rowItems ->
                        androidx.compose.foundation.layout.Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(
                                16.dp
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            rowItems.forEach { visualCategory ->
                                val title = stringResource(id = visualCategory.nameResId)
                                CategoryCard(
                                    imageRes = visualCategory.imageResId,
                                    title = title,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            selectedVisualCategory =
                                                if (selectedVisualCategory == visualCategory) null else visualCategory

                                        }
                                )
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp)) // Extra space for FAB
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateRoutineScreenPreview() {
    // Le pasamos un callback vacío para la flecha
    TraiScoreTheme {
        CreateRoutineScreen(
            onBack = { /* Aquí iría popBackStack() en tu app real */ }
        )
    }
}