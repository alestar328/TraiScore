package com.develop.traiscore.presentation.screens


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.develop.traiscore.data.local.entity.ExerciseEntity
import com.develop.traiscore.domain.model.WorkoutModel
import com.develop.traiscore.data.local.entity.WorkoutType
import com.develop.traiscore.data.local.entity.WorkoutWithExercise
import com.develop.traiscore.presentation.components.CircleDot
import com.develop.traiscore.presentation.components.WorkoutCard
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.viewmodels.ExercisesScreenViewModel
import java.util.Date


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesScreen(
    exeScreenViewModel: ExercisesScreenViewModel = hiltViewModel(),
   ) {
    val exercises = exeScreenViewModel.exercises

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Logo en lugar de texto
                    Image(
                        painter = painterResource(id = R.drawable.trailogoup),
                        contentDescription = "Logo cabecera",
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                actions = {
                    // Ícono de búsqueda
                    IconButton(onClick = { println("Search clicked") }) {
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
                    containerColor = Color.DarkGray, // Fondo de la barra
                    titleContentColor = MaterialTheme.colorScheme.onSurface // Color del texto
                )
            )
        },
        content = { paddingValues ->
            // Contenido principal
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .background(Color.Black)
                    .fillMaxSize()
                    .padding(TraiScoreTheme.dimens.paddingMedium),
                     // Fondo negro
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                items(
                    items = exercises,
                    key = { exercise -> exercise.workoutModel.id } // Usa workoutModel.id como clave
                ) { exercise ->
                    WorkoutCard(
                        workoutWithExercise = exercise,
                        onEditClick = { println("Edit ${exercise.exerciseEntity.name}") },
                        onDeleteClick = { println("Delete ${exercise.exerciseEntity.name}") }
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
    val workoutType = WorkoutType(
        id = 1,
        exerciseId = 1,
        title = "Sentadillas",
        weight = 100.0,
        reps = 10,
        rir = 2
    )

    val workoutModel = WorkoutModel(
        id = 1,
        exerciseId = 1,
        title = workoutType.title,
        reps = workoutType.reps,
        weight = workoutType.weight,
        timestamp = Date(),
        workoutTypeId = workoutType.id // Ahora es válido
    )
    val exerciseEntity = ExerciseEntity(
        id = 1,
        idIntern = "sentadillas",
        name = "Sentadillas",
        isDefault = true
    )
    val workoutWithExercise = WorkoutWithExercise(
        workoutModel = workoutModel,
        workoutType = workoutType,
        exerciseEntity = exerciseEntity
    )


    TraiScoreTheme {
        LazyColumn {
            items(
                items = listOf(workoutWithExercise),
                key = { it.workoutModel.id }
            ) { exercise ->
                WorkoutCard(
                    workoutWithExercise = exercise,
                    onEditClick = { println("Edit ${exercise.exerciseEntity.name}") },
                    onDeleteClick = { println("Delete ${exercise.exerciseEntity.name}") }
                )
            }
        }
    }
}