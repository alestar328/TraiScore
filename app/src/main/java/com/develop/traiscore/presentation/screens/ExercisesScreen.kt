package com.develop.traiscore.presentation.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import com.develop.traiscore.presentation.theme.TraiScoreTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.develop.traiscore.data.local.entity.ExerciseEntity
import com.develop.traiscore.domain.model.WorkoutModel
import com.develop.traiscore.data.local.entity.WorkoutEntry
import com.develop.traiscore.data.local.entity.WorkoutWithExercise
import com.develop.traiscore.presentation.components.FilterableDropdown
import com.develop.traiscore.presentation.components.TraiScoreTopBar
import com.develop.traiscore.presentation.components.WorkoutCard
import com.develop.traiscore.presentation.components.WorkoutCardList
import com.develop.traiscore.presentation.theme.tsColors
import com.develop.traiscore.presentation.viewmodels.WorkoutEntryViewModel
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ExercisesScreen(
    viewModel: WorkoutEntryViewModel = hiltViewModel()
) {
    val entries = viewModel.entries.value
    val showBottomSheet =
        remember { mutableStateOf(false) }
    val selectedEntry = remember { mutableStateOf<WorkoutEntry?>(null) }
    val groupedEntries = viewModel.groupWorkoutsByDate(entries)
    val showSearchBar = remember { mutableStateOf(false) }
    val selectedSearch = remember { mutableStateOf("") }

    val showCalendarView = remember { mutableStateOf(false) }


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
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clickable {
                                    showCalendarView.value = !showCalendarView.value
                                },
                            contentAlignment = Alignment.Center
                        )  {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Calendario",
                                tint = MaterialTheme.tsColors.ledCyan,
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
                        .background(MaterialTheme.colorScheme.background)
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
                                .background(MaterialTheme.colorScheme.background)
                                .padding(
                                    horizontal = TraiScoreTheme.dimens.paddingMedium,
                                    vertical = 4.dp
                                )
                        )
                    }

                    AnimatedContent(
                        targetState = showCalendarView.value,
                        transitionSpec = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            ) with slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            )
                        },
                        label = "ViewTransition"
                    ) { isCalendarView ->
                        if (isCalendarView) {
                            CalendarScreen(
                                groupedEntries = filteredGrouped,
                                onEditClick = { workout ->
                                    selectedEntry.value = workout
                                    showBottomSheet.value = true
                                },
                                onDeleteClick = { workout ->
                                    workout.uid?.let { viewModel.deleteWorkoutEntry(it) }
                                }
                            )
                        } else {
                            // Lista de entrenamientos
                            LazyColumn(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.background)
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
                                            color = MaterialTheme.colorScheme.onBackground,
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
                                                    true
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