package com.develop.traiscore.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.develop.traiscore.data.local.entity.WorkoutEntry
import com.develop.traiscore.presentation.theme.traiBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun ResumeWorkoutCard(
workoutEntry: WorkoutEntry,
onEditClick: () -> Unit,
onDeleteClick: () -> Unit,
modifier: Modifier = Modifier
) {

    val workout = workoutEntry

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.DarkGray)
            .padding(5.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Título del ejercicio y botones
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
                    .height(30.dp)
            ) {
                Text(
                    text = workout.title.uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 13.sp
                )
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally // Centrado opcional

                ) {
                    Text(
                        text = "${workout.weight} Series",
                        color = Color.White,
                        fontWeight = FontWeight.Bold, // Destacar el número
                        fontSize = 12.sp // Tamaño más grande para el dato
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally // Centrado opcional
                ) {
                    Text(
                        text = "${workout.reps} Reps",
                        color = Color.White,
                        fontWeight = FontWeight.Bold, // Destacar el número
                        fontSize = 11.sp // Tamaño más grande para el dato
                    )
                }
                Row {

                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "More", tint = traiBlue,
                            modifier = Modifier.size(50.dp))
                    }
                }
            }


        }
    }
}
@Preview(showBackground = true)
@Composable
fun ResumeWorkoutCard() {
    val workoutEntry = WorkoutEntry(
        id = 1,
        exerciseId = 101,
        title = "Press Banca",
        weight = 85.0,
        reps = 10,
        rir = 2,
        series = 4,
        timestamp = Date()
    )

    ResumeWorkoutCard(
        workoutEntry = workoutEntry,
        onEditClick = { /* Acción de editar */ },
        onDeleteClick = { /* Acción de eliminar */ }
    )
}