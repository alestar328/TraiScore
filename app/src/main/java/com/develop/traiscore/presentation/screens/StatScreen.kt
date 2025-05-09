package com.develop.traiscore.presentation.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.develop.traiscore.R
import com.develop.traiscore.presentation.components.CircleDot
import com.develop.traiscore.presentation.components.CircularProgressView
import com.develop.traiscore.presentation.components.FilterableDropdown
import com.develop.traiscore.presentation.components.LineChartView
import com.develop.traiscore.presentation.theme.TraiScoreTheme
import com.develop.traiscore.presentation.theme.traiBackgroundDay
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.theme.traiOrange
import com.develop.traiscore.presentation.viewmodels.StatScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatScreen(
    modifier: Modifier = Modifier,
    viewModel: StatScreenViewModel = hiltViewModel()
) {
    val exerOptions by viewModel.exerciseOptions.collectAsState()
    val weightData by viewModel.weightProgress.collectAsState()
    val repsData by viewModel.repsProgress.collectAsState()
    val circularData by viewModel.circularData.collectAsState()
    val selected by viewModel.selectedExercise.collectAsState()

    val totalKg by viewModel.totalWeightSum.collectAsState()

    val (oneRepMax, maxReps, averageRIR) = circularData
    val lastTwelve = weightData.takeLast(12)
    val weightByReps = remember(lastTwelve) {
        lastTwelve.map { (_, peso) ->
            // etiqueta X = peso redondeado a entero
            peso.toInt().toString() to peso
        }
    }
    LaunchedEffect(selected, weightData, repsData) {
        Log.d("StatScreen", "Ejercicio=$selected  pesos=$weightData  reps=$repsData")
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
                    .navigationBarsPadding()
                    .background(traiBackgroundDay)
                    .fillMaxSize()
                    .padding(TraiScoreTheme.dimens.paddingMedium),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Filtros

                item {

                    Text(
                        text = "Filtrar por:",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    FilterableDropdown(
                        items        = exerOptions,
                        selectedValue= selected ?: "",
                        placeholder  = "Ejercicio",
                        onItemSelected = { viewModel.onExerciseSelected(it) },
                        modifier     = Modifier
                            .fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))


                }

                // Detalles del ejercicio
                item {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.DarkGray, RoundedCornerShape(8.dp)) // fondo + esquinas
                            .padding(10.dp)
                    ) {
                        // Peso
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(
                                text = "1RM: ${"%.1f".format(oneRepMax)}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = traiBlue
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            CircularProgressView(
                                progress = (oneRepMax / 150).toFloat().coerceIn(0f, 1f),
                                maxLabel = "${"%.1f".format(oneRepMax)} Kg",
                                modifier = Modifier.size(80.dp),
                                strokeColor = Color.Cyan,
                                backgroundColor = traiOrange
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(
                                text = "MR: $maxReps",
                                style = MaterialTheme.typography.bodyLarge,
                                color = traiBlue
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            CircularProgressView(
                                progress = (maxReps / 15f).coerceIn(0f, 1f),
                                maxLabel = "$maxReps reps",
                                modifier = Modifier.size(80.dp),
                                strokeColor = Color.Cyan,
                                backgroundColor = traiOrange
                            )
                        }
                        // RIR
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(
                                text = "Esfuerzo",
                                style = MaterialTheme.typography.bodyLarge,
                                color = traiBlue
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            CircularProgressView(
                                progress = (averageRIR / 10f).coerceIn(0f, 1f),
                                maxLabel = "$averageRIR RIR",
                                modifier = Modifier.size(80.dp),
                                strokeColor = Color.Cyan,
                                backgroundColor = traiOrange
                            )
                        }
                    }
                }

                // Gráficas
                item {
                    Text(
                        text = "Por peso:",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LineChartView(
                        dataPoints = weightByReps,
                        lineColor = Color.Cyan, // Puedes personalizar los colores según sea necesario
                        backgroundChartColor = Color.DarkGray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(horizontal = 0.dp)
                    )
                }
                item {
                    Text(
                        text = "Por repeticiones:",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LineChartView(
                        dataPoints = repsData,
                        lineColor = Color.Cyan, // Puedes personalizar los colores según sea necesario
                        backgroundChartColor = Color.DarkGray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(horizontal = 0.dp)
                    )
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.DarkGray, RoundedCornerShape(8.dp))
                            .padding(16.dp)           // el mismo padding que tus otras secciones
                    ) {
                        Text(
                            text = "Haz levantado: $totalKg kg",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = "Haz levantado: Un elefante!",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = "Energía consumida: 1000 kCal",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                }
            }

        }
    )
}

@Preview(
    name = "StatScreenPreview",
    showBackground = true
)
@Composable
fun StatScreenPreview() {
    TraiScoreTheme {
        StatScreen()
    }
}