package com.develop.traiscore.presentation.screens


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.develop.traiscore.data.local.entity.ExerciseEntity
import com.develop.traiscore.domain.model.WorkoutModel
import com.develop.traiscore.data.local.entity.WorkoutEntry
import com.develop.traiscore.data.local.entity.WorkoutWithExercise
import com.develop.traiscore.presentation.components.CircleDot
import com.develop.traiscore.presentation.components.FilterableDropdown
import com.develop.traiscore.presentation.components.WorkoutCard
import com.develop.traiscore.presentation.components.WorkoutCardList
import com.develop.traiscore.presentation.theme.traiBackgroundDay
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.viewmodels.WorkoutEntryViewModel
import java.util.Date


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesScreen(
    viewModel: WorkoutEntryViewModel = hiltViewModel()
) {
    val entries = viewModel.entries.value
    val showDialog = remember { mutableStateOf(false) }
    val selectedEntry = remember { mutableStateOf<WorkoutEntry?>(null) }
    val groupedEntries = viewModel.groupWorkoutsByDate(entries)
    val showSearchBar = remember { mutableStateOf(false) }
    val selectedSearch = remember { mutableStateOf("") }

    val filteredGrouped = if (selectedSearch.value.isNotBlank()) {
        viewModel.groupWorkoutsByDateFiltered(entries, selectedSearch.value)
    } else {
        groupedEntries
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.trailogoup),
                        contentDescription = "Logo cabecera",
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                        println("🔍 Icono de busqueda clicado")
                            showSearchBar.value = !showSearchBar.value
                            if (!showSearchBar.value) {
                                selectedSearch.value = "" // 👈 Esto reinicia el filtro al cerrar
                            }

                    }) {
                        CircleDot(color = traiBlue) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.White,
                                modifier = Modifier.size(TraiScoreTheme.dimens.iconSizeSmall)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.DarkGray,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },

        content = {
            paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(top = paddingValues.calculateTopPadding()) // Solo top
            ) {
                if (showSearchBar.value) {
                    FilterableDropdown(
                        items = viewModel.entries.value.map { it.title }.distinct(),
                        selectedValue = selectedSearch.value,
                        onItemSelected = { selectedSearch.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                LazyColumn(
                    modifier = Modifier
                        .background(traiBackgroundDay)
                        .fillMaxSize()
                        .padding(TraiScoreTheme.dimens.paddingMedium),
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
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                            )
                        }


                        item {
                            WorkoutCardList(
                                workouts = dailyWorkouts,
                                onEditClick = { workout ->
                                    selectedEntry.value = workout
                                    showDialog.value = true
                                },
                                onDeleteClick = { workout ->
                                    workout.uid?.let { viewModel.deleteWorkoutEntry(it) }
                                }
                            )
                        }
                    }
                }
            }

            // ⬇️ Este bloque debe ir AQUÍ justo después del Scaffold:
            if (showDialog.value && selectedEntry.value != null) {
                Dialog(
                    onDismissRequest = { showDialog.value = false }
                ) {
                    AddExerciseDialogContent(
                        workoutToEdit = selectedEntry.value,
                        onDismiss = { showDialog.value = false },
                        onSave = { updatedData ->
                            selectedEntry.value?.uid?.let { id ->
                                viewModel.editWorkoutEntry(id, updatedData)
                                showDialog.value = false
                            }
                        }
                    )
                }
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
            weight = 100.0,
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