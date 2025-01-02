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
import com.develop.traiscore.domain.WorkoutModel
import com.develop.traiscore.domain.WorkoutType
import com.develop.traiscore.presentation.components.CircleDot
import com.develop.traiscore.presentation.components.WorkoutCard
import com.develop.traiscore.presentation.theme.traiBlue
import java.util.Date


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesScreen(modifier: Modifier = Modifier) {
    val workouts = sampleWorkouts//EJERCICIOS EJEMPLO

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

                items(workouts) { workout ->
                    WorkoutCard(
                        workout = workout,
                        onEditClick = { println("Edit ${workout.type.title}") },
                        onDeleteClick = { println("Delete ${workout.type.title}") }
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
    TraiScoreTheme {
        ExercisesScreen()
    }
}

val sampleWorkouts = listOf(
    WorkoutModel(
        timestamp = Date(),
        type = WorkoutType(
            title = "Sentadillas",
            weight = 100.0,
            reps = 8,
            rir = 2
        )
    ),
    WorkoutModel(
        timestamp = Date(),
        type = WorkoutType(
            title = "Press de Banca",
            weight = 80.0,
            reps = 10,
            rir = 1
        )
    ),
    WorkoutModel(
        timestamp = Date(),
        type = WorkoutType(
            title = "Peso Muerto",
            weight = 120.0,
            reps = 6,
            rir = 3
        )
    ),
    WorkoutModel(
        timestamp = Date(),
        type = WorkoutType(
            title = "Press Militar",
            weight = 120.0,
            reps = 6,
            rir = 3
        )
    ),
    WorkoutModel(
        timestamp = Date(),
        type = WorkoutType(
            title = "Dominadas",
            weight = 120.0,
            reps = 6,
            rir = 3
        )
    ),
    WorkoutModel(
        timestamp = Date(),
        type = WorkoutType(
            title = "Prensa",
            weight = 120.0,
            reps = 6,
            rir = 3
        )
    ),
    WorkoutModel(
        timestamp = Date(),
        type = WorkoutType(
            title = "Curl biceps",
            weight = 120.0,
            reps = 6,
            rir = 3
        )
    ),
)