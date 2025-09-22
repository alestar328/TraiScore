package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.develop.traiscore.R
import com.develop.traiscore.presentation.components.AddExerciseDialogToDB
import com.develop.traiscore.presentation.components.TraiScoreTopBar
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.theme.tsColors
import com.develop.traiscore.presentation.viewmodels.AddExerciseViewModel
import com.develop.traiscore.presentation.viewmodels.ExerciseWithSource
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyExercisesUI(
    navController: NavHostController,
    onBack: () -> Unit,
    addExerciseViewModel: AddExerciseViewModel = hiltViewModel()
) {
    val exercisesWithSource by remember { derivedStateOf { addExerciseViewModel.exercisesWithSource } }
    var showDialog by remember { mutableStateOf(false) }
    var selectedExercise by remember { mutableStateOf<ExerciseWithSource?>(null) }



    // Cargar ejercicios cuando se inicia la pantalla
    LaunchedEffect(Unit) {
        addExerciseViewModel.loadAllExercisesWithSource()
        // También cargar la lista original para compatibilidad
        addExerciseViewModel.loadAllExercisesWithCategory()
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Texto "TraiScore"
                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    style = SpanStyle(
                                        color = traiBlue // 👈 Color para "Trai"
                                    )
                                ) {
                                    append("Trai")
                                }
                                withStyle(
                                    style = SpanStyle(
                                        color = Color.White // 👈 Color para "Score"
                                    )
                                ) {
                                    append("Score")
                                }
                            },
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {  onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedExercise = null
                    showDialog = true
                },
                containerColor = MaterialTheme.tsColors.ledCyan,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar ejercicio")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                exercisesWithSource.isEmpty() -> {
                    EmptyExercisesState()
                }
                else -> {
                    ExercisesListWithSource(
                        exercises = exercisesWithSource,
                        onExerciseClick = { exercise ->
                            if (exercise.isUserCreated) {
                                selectedExercise = exercise
                                showDialog = true
                            }
                        }
                    )
                }
            }
        }

        // Diálogo para crear/editar ejercicio
        if (showDialog) {
            val isEditing = selectedExercise != null
            val exercise = selectedExercise

            AddExerciseDialogToDB(
                isEditing = isEditing,
                initialName = exercise?.name ?: "",
                initialCategory = exercise?.category ?: "",
                documentId = exercise?.documentId ?: "",
                onDismiss = {
                    showDialog = false
                    selectedExercise = null
                },
                onSave = { name, category ->
                    // Crear nuevo ejercicio
                    addExerciseViewModel.viewModelScope.launch {
                        addExerciseViewModel.saveExerciseToDatabase(name, category)
                    }
                },
                onUpdate = if (isEditing) { documentId, name, category ->
                    // Actualizar ejercicio existente
                    addExerciseViewModel.updateUserExercise(
                        documentId = documentId,
                        newName = name,
                        newCategory = category
                    ) { success, error ->
                        if (!success) {
                            println("❌ Error al actualizar: $error")
                        }
                    }
                } else null,
                onDelete = if (isEditing) { documentId ->
                    // Eliminar ejercicio
                    addExerciseViewModel.deleteUserExercise(documentId) { success, error ->
                        if (!success) {
                            println("❌ Error al eliminar: $error")
                        }
                    }
                } else null
            )
        }
    }
}
@Composable
private fun ExercisesListWithSource(
    exercises: List<ExerciseWithSource>,
    onExerciseClick: (ExerciseWithSource) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header con contador
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
            items(exercises) { exercise ->
                ExerciseItemWithSource(
                    exercise = exercise,
                    onClick = { onExerciseClick(exercise) }
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
private fun ExerciseItemWithSource(
    exercise: ExerciseWithSource,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = exercise.isUserCreated) { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = exercise.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Text(
                text = exercise.category,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Indicador de tipo de ejercicio
            if (exercise.isUserCreated) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Ejercicio personal",
                    tint = MaterialTheme.tsColors.ledCyan,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Personalizado",
                    fontSize = 12.sp,
                    color = MaterialTheme.tsColors.ledCyan
                )
            } else {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Ejercicio del sistema",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Sistema",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Indicador visual de que es clickeable
            if (exercise.isUserCreated) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = MaterialTheme.tsColors.ledCyan,
                    modifier = Modifier.size(16.dp)
                )
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
            items(exercises) { exercise ->
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