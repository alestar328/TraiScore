package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.develop.traiscore.data.pressBancaWorkouts
import com.develop.traiscore.presentation.components.CircleDot
import com.develop.traiscore.presentation.components.DropdownMenuComponent
import com.develop.traiscore.presentation.components.LineChart
import com.develop.traiscore.presentation.theme.TraiScoreTheme
import com.develop.traiscore.presentation.theme.traiBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatScreen(modifier: Modifier = Modifier) {
    val timeOptions = listOf("Hoy", "Esta semana", "Este mes", "Este año") // Opciones de tiempo
    val exerOptions =
        listOf("Press banca", "Sentadilla", "Dominadas", "Curl biceps") // Opciones de tiempo


    var selectedTime by remember { mutableStateOf("") }
    // Normalizar los datos para el eje Y
    val maxWeight = pressBancaWorkouts.maxOfOrNull { it.type.weight } ?: 1.0
    val minWeight = pressBancaWorkouts.minOfOrNull { it.type.weight } ?: 0.0



    val pressBancaData = pressBancaWorkouts.mapIndexed { index, workout ->
        index.toFloat() to ((workout.type.weight - minWeight) / (maxWeight - minWeight)).toFloat()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Logo en lugar de texto
                    Text(
                        text = "Estadisticas",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = MaterialTheme.typography.titleLarge.fontSize * 2
                        ), // Estilo del texto
                        color = traiBlue, // Color del texto
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    /*     // Logo en lugar de texto
                    Image(
                        painter = painterResource(id = R.drawable.trailogoup),
                        contentDescription = "Logo cabecera",
                        modifier = Modifier.fillMaxWidth(),
                    )*/
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
                    .background(Color.DarkGray)
                    .fillMaxSize()
                    .padding(TraiScoreTheme.dimens.paddingMedium),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Filtrar por:",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    DropdownMenuComponent(
                        items = timeOptions,
                        onItemSelected = { selected ->
                            selectedTime = selected
                            println("Tiempo seleccionado: $selected")
                        },
                        placeholder = "Tiempo",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(TraiScoreTheme.dimens.spacerNormal))
                    DropdownMenuComponent(
                        items = exerOptions,
                        onItemSelected = { selected ->
                            selectedTime = selected
                            println("Tiempo seleccionado: $selected")
                        },
                        placeholder = "Ejercicio",
                        modifier = Modifier.fillMaxWidth()
                    )

                }
                item {
                    Text(
                        text = "Progreso del ejercicio:",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LineChart(
                        data = pressBancaData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .padding(horizontal = 16.dp)
                    )
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