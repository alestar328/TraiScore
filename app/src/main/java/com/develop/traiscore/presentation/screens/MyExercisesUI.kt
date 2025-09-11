package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.develop.traiscore.R
import com.develop.traiscore.presentation.components.TraiScoreTopBar
import com.develop.traiscore.presentation.theme.tsColors
import com.develop.traiscore.presentation.viewmodels.AddExerciseViewModel

@Composable
fun MyExercisesUI(
    navController: NavHostController,
    addExerciseViewModel: AddExerciseViewModel = hiltViewModel()
) {
    val exercisesWithCategory by remember { derivedStateOf { addExerciseViewModel.exercisesWithCategory } }

    // Cargar ejercicios cuando se inicia la pantalla
    LaunchedEffect(Unit) {
        addExerciseViewModel.loadAllExercisesWithCategory()
    }

    Scaffold(
        topBar = {
            TraiScoreTopBar(
                leftIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.tsColors.ledCyan
                        )
                    }
                },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                exercisesWithCategory.isEmpty() -> {
                    // Estado vacío
                    EmptyExercisesState()
                }
                else -> {
                    // Lista de ejercicios
                    ExercisesList(exercises = exercisesWithCategory)
                }
            }
        }
    }
}

@Composable
private fun EmptyExercisesState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painterResource(id = R.drawable.exercises_icon),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No hay ejercicios disponibles",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Los ejercicios aparecerán aquí una vez que estén disponibles",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ExercisesList(exercises: List<Pair<String, String>>) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header simple sin Card
        Text(
            text = "${exercises.size} ejercicios disponibles",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.tsColors.ledCyan
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            items(exercises) { exercise  ->
                ExerciseItem(
                    exerciseName = exercise.first,
                    category = exercise.second
                )
                Divider(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    thickness = 1.dp
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ExerciseItem(exerciseName: String, category: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        // Nombre del ejercicio
        Text(
            text = "$exerciseName ($category)",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
    }
}