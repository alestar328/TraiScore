package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import com.develop.traiscore.R
import com.develop.traiscore.presentation.theme.TraiScoreTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.develop.traiscore.data.local.entity.ExerciseEntity
import com.develop.traiscore.domain.model.WorkoutModel
import com.develop.traiscore.data.local.entity.WorkoutEntry
import com.develop.traiscore.data.local.entity.WorkoutWithExercise
import com.develop.traiscore.presentation.components.CircleDot
import com.develop.traiscore.presentation.components.FilterableDropdown
import com.develop.traiscore.presentation.components.TraiScoreTopBar
import com.develop.traiscore.presentation.components.WorkoutCard
import com.develop.traiscore.presentation.components.WorkoutCardList
import com.develop.traiscore.presentation.theme.TSStyle
import com.develop.traiscore.presentation.theme.TSStyleColors.ledCyan
import com.develop.traiscore.presentation.theme.navbarDay
import com.develop.traiscore.presentation.theme.traiBackgroundDay
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.theme.tsColors
import com.develop.traiscore.presentation.viewmodels.WorkoutEntryViewModel
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesScreen(
    viewModel: WorkoutEntryViewModel = hiltViewModel()
) {
    val entries = viewModel.entries.value
    val showBottomSheet =
        remember { mutableStateOf(false) } // ✅ CAMBIO: showDialog → showBottomSheet
    val selectedEntry = remember { mutableStateOf<WorkoutEntry?>(null) }
    val groupedEntries = viewModel.groupWorkoutsByDate(entries)
    val showSearchBar = remember { mutableStateOf(false) }
    val selectedSearch = remember { mutableStateOf("") }

    val filteredGrouped = if (selectedSearch.value.isNotBlank()) {
        viewModel.groupWorkoutsByDateFiltered(entries, selectedSearch.value)
    } else {
        groupedEntries
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = TraiScoreTheme.dimens.paddingMedium)
    ) {
        Scaffold(
            topBar = {
                TraiScoreTopBar(
                    leftIcon = {
                        FloatingActionButton(
                            onClick = {
                                println("⏱️ Icono de cronometro")
                            },
                            modifier = Modifier.size(30.dp),
                            containerColor = MaterialTheme.tsColors.ledCyan,
                            contentColor = Color.White,
                            elevation = FloatingActionButtonDefaults.elevation(0.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.timer_icon),
                                contentDescription = "Temporizador",
                                tint = Color.Black
                            )
                        }

                    },
                    rightIcon = {
                        FloatingActionButton(
                            onClick = {
                                println("🔍 Icono de busqueda clicado")
                                showSearchBar.value = !showSearchBar.value
                                if (!showSearchBar.value) {
                                    selectedSearch.value = "" // Reinicia el filtro al cerrar
                                }
                            },
                            modifier = Modifier.size(30.dp),
                            containerColor = MaterialTheme.tsColors.ledCyan,
                            contentColor = Color.White,
                            elevation = FloatingActionButtonDefaults.elevation(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.Black
                            )
                        }
                    }
                )
            },
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.tsColors.primaryBackgroundColor)
                        .padding(top = paddingValues.calculateTopPadding())
                ) {
                    // Barra de búsqueda
                    if (showSearchBar.value) {
                        FilterableDropdown(
                            items = viewModel.entries.value.map { it.title }.distinct(),
                            selectedValue = selectedSearch.value,
                            onItemSelected = { selectedSearch.value = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.tsColors.primaryBackgroundColor)
                                .padding(
                                    horizontal = TraiScoreTheme.dimens.paddingMedium,
                                    vertical = 4.dp
                                )
                        )
                    }

                    // Lista de entrenamientos
                    LazyColumn(
                        modifier = Modifier
                            .background(TSStyle.primaryBackgroundColor)
                            .fillMaxSize(),
                        contentPadding = PaddingValues(
                            bottom = paddingValues.calculateBottomPadding() + 100.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filteredGrouped.forEach { (date, dailyWorkouts) ->
                            item {
                                Text(
                                    text = date,
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                                )
                            }

                            item {
                                WorkoutCardList(
                                    workouts = dailyWorkouts,
                                    onEditClick = { workout ->
                                        selectedEntry.value = workout
                                        showBottomSheet.value =
                                            true // ✅ CAMBIO: Mostrar bottom sheet
                                    },
                                    onDeleteClick = { workout ->
                                        workout.uid?.let { viewModel.deleteWorkoutEntry(it) }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        )
    }

    // ✅ CAMBIO: Reemplazar Dialog con AddExerciseBottomSheet
    AddExerciseBottomSheet(
        workoutToEdit = selectedEntry.value,
        isVisible = showBottomSheet.value,
        onDismiss = {
            showBottomSheet.value = false
            selectedEntry.value = null // Limpiar selección al cerrar
        },
        onSave = { updatedData ->
            selectedEntry.value?.uid?.let { id ->
                viewModel.editWorkoutEntry(id, updatedData)
                showBottomSheet.value = false
                selectedEntry.value = null
            }
        }
    )
}

@Preview(
    name = "ExercisesScreenPreview",
    showBackground = true
)
@Composable
fun ExercisesScreenPreview() {
    val workoutEntry = WorkoutEntry(
        id = 1,
        exerciseId = 1,
        title = "Sentadillas",
        weight = 100.0f,
        reps = 10,
        rir = 2,
        series = 0,
        timestamp = Date()
    )

    val workoutModel = WorkoutModel(
        id = 1,
        exerciseId = 1,
        title = workoutEntry.title,
        reps = workoutEntry.reps,
        weight = workoutEntry.weight,
        series = 0,
        timestamp = Date()
    )

    val exerciseEntity = ExerciseEntity(
        id = 1,
        idIntern = "sentadillas",
        name = "Sentadillas",
        isDefault = true
    )

    val workoutWithExercise = WorkoutWithExercise(
        workoutModel = workoutModel,
        workoutEntry = workoutEntry,
        exerciseEntity = exerciseEntity
    )

    TraiScoreTheme {
        LazyColumn {
            items(
                items = listOf(workoutWithExercise),
                key = { it.workoutModel.id }
            ) { exercise ->
                WorkoutCard(
                    workoutEntry = workoutEntry,
                    onEditClick = { println("Edit ${exercise.exerciseEntity.name}") },
                    onDeleteClick = { println("Delete ${exercise.exerciseEntity.name}") }
                )
            }
        }
    }
}