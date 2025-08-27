package com.develop.traiscore.presentation.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import com.develop.traiscore.presentation.theme.TraiScoreTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.develop.traiscore.R
import com.develop.traiscore.data.firebaseData.calculateTodayDataAndNavigate
import com.develop.traiscore.data.local.entity.ExerciseEntity
import com.develop.traiscore.domain.model.WorkoutModel
import com.develop.traiscore.data.local.entity.WorkoutEntry
import com.develop.traiscore.data.local.entity.WorkoutWithExercise
import com.develop.traiscore.presentation.components.FilterableDropdown
import com.develop.traiscore.presentation.components.TraiScoreTopBar
import com.develop.traiscore.presentation.components.WorkoutCard
import com.develop.traiscore.presentation.components.general.NewSessionUX
import com.develop.traiscore.presentation.theme.tsColors
import com.develop.traiscore.presentation.viewmodels.AddExerciseViewModel
import com.develop.traiscore.presentation.viewmodels.NewSessionViewModel
import com.develop.traiscore.presentation.viewmodels.StatScreenViewModel
import com.develop.traiscore.presentation.viewmodels.WorkoutEntryViewModel
import java.util.Date

import com.develop.traiscore.presentation.viewmodels.ViewMode
import com.develop.traiscore.presentation.viewmodels.ViewModeSelector
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ExercisesScreen(
    viewModel: WorkoutEntryViewModel = hiltViewModel(),
    statViewModel: StatScreenViewModel = hiltViewModel(), // ✅ AGREGAR esta línea
    navController: NavController
) {
    val entries = viewModel.entries.value
    val showBottomSheet = remember { mutableStateOf(false) }
    val selectedEntry = remember { mutableStateOf<WorkoutEntry?>(null) }
    val groupedEntries = viewModel.groupWorkoutsByDate(entries)
    val showSearchBar = remember { mutableStateOf(false) }
    val selectedSearch = remember { mutableStateOf("") }
    val context = LocalContext.current
    // Estados para el nuevo sistema de vistas
    val showViewModeSelector = remember { mutableStateOf(false) }
    val currentViewMode = remember { mutableStateOf(ViewMode.TODAY) }
    val selectedMonth = remember { mutableStateOf<YearMonth?>(null) }
    val circularData by statViewModel.circularData.collectAsState()
    val (oneRepMax, maxReps, _) = circularData
    val filteredGrouped = if (selectedSearch.value.isNotBlank()) {
        viewModel.groupWorkoutsByDateFiltered(entries, selectedSearch.value)
    } else {
        groupedEntries
    }
    val sessionViewModel: NewSessionViewModel = hiltViewModel()
    val availableSessions by sessionViewModel.availableSessions.collectAsState()
    val hasActiveSession by sessionViewModel.hasActiveSession.collectAsState()
    var showNewSessionDialog by remember { mutableStateOf(false) }

    val newSessionViewModel: NewSessionViewModel = hiltViewModel()
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
                                    if (showSearchBar.value) {
                                        showSearchBar.value = false
                                        selectedSearch.value = ""
                                    }
                                    showViewModeSelector.value = !showViewModeSelector.value
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Selector de vista",
                                tint = MaterialTheme.tsColors.ledCyan,
                            )
                        }
                    },
                    rightIcon = {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clickable {
                                    calculateTodayDataAndNavigate(
                                        context = context,
                                        navController = navController,
                                        viewModel = statViewModel,
                                        oneRepMax = oneRepMax,
                                        maxReps = maxReps
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_camara),
                                contentDescription = "Compartir sesión",
                                tint = MaterialTheme.tsColors.ledCyan,
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                // FAB condicional solo en vista TODAY
                if (currentViewMode.value == ViewMode.TODAY &&
                    !hasActiveSession &&
                    availableSessions.size < 6) {
                    FloatingActionButton(
                        onClick = { showNewSessionDialog = true },
                        containerColor = MaterialTheme.tsColors.ledCyan,
                        contentColor = Color.Black,
                        modifier = Modifier
                            .padding(bottom = 100.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Nueva Sesión"
                        )
                    }
                }
            },
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(top = paddingValues.calculateTopPadding())
                ) {
                    // Barra de búsqueda
                    AnimatedVisibility(
                        visible = showViewModeSelector.value,
                        enter = slideInVertically(
                            initialOffsetY = { -it },
                            animationSpec = tween(300)
                        ) + fadeIn(animationSpec = tween(300)),
                        exit = slideOutVertically(
                            targetOffsetY = { -it },
                            animationSpec = tween(200)
                        ) + fadeOut(animationSpec = tween(200))
                    ) {
                        ViewModeSelector(
                            selectedMode = currentViewMode.value,
                            onModeSelected = { mode ->
                                currentViewMode.value = mode
                                showViewModeSelector.value = false // ✅ Se cierra automáticamente al seleccionar
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(
                                    horizontal = TraiScoreTheme.dimens.paddingMedium,
                                    vertical = 8.dp
                                )
                        )
                    }
                    AnimatedVisibility(
                        visible = showSearchBar.value,
                        enter = slideInVertically(
                            initialOffsetY = { -it },
                            animationSpec = tween(300)
                        ) + fadeIn(animationSpec = tween(300)),
                        exit = slideOutVertically(
                            targetOffsetY = { -it },
                            animationSpec = tween(200)
                        ) + fadeOut(animationSpec = tween(200))
                    ) {
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

                    // Contenido principal según el modo de vista seleccionado
                    AnimatedContent(
                        targetState = currentViewMode.value,
                        transitionSpec = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            ) with slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            )
                        },
                        label = "ViewModeTransition"
                    ) { viewMode ->
                        when (viewMode) {
                            ViewMode.YEAR -> {
                                YearViewScreen(
                                    groupedEntries = filteredGrouped,
                                    onMonthSelected = { month ->
                                        selectedMonth.value = month
                                        currentViewMode.value = ViewMode.MONTH
                                    }
                                )
                            }

                            ViewMode.MONTH -> {
                                CalendarScreen(
                                    groupedEntries = filteredGrouped,
                                    selectedMonth = selectedMonth.value,
                                    onEditClick = { workout ->
                                        selectedEntry.value = workout
                                        showBottomSheet.value = true
                                    },
                                    onDeleteClick = { workout ->
                                        workout.uid?.let { viewModel.deleteWorkoutEntry(it) }
                                    }
                                )
                            }

                            ViewMode.TODAY -> {
                                TodayViewScreen(
                                    groupedEntries = filteredGrouped,
                                    onEditClick = { workout ->
                                        selectedEntry.value = workout
                                        showBottomSheet.value = true
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

    // Bottom Sheet para editar ejercicios
    AddExerciseBottomSheet(
        workoutToEdit = selectedEntry.value,
        isVisible = showBottomSheet.value,
        onDismiss = {
            showBottomSheet.value = false
            selectedEntry.value = null
        },
        onSave = { updatedData ->
            selectedEntry.value?.uid?.let { id ->
                viewModel.editWorkoutEntry(id, updatedData)
                showBottomSheet.value = false
                selectedEntry.value = null
            }
        }
    )
    if (showNewSessionDialog) {
        NewSessionUX(
            onDismiss = { showNewSessionDialog = false },
            onSessionCreated = {
                showNewSessionDialog = false
            },
            viewModel = newSessionViewModel
        )
    }
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