package com.develop.traiscore.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.develop.traiscore.data.local.entity.WorkoutEntry
import java.util.Date

@Composable
fun WorkoutCardList(
    workouts: List<WorkoutEntry>,
    onEditClick: (WorkoutEntry) -> Unit,
    onDeleteClick: (WorkoutEntry) -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.DarkGray) // fondo general contenedor
            .padding(8.dp)
    ) {
        workouts.forEachIndexed  { index, workout ->

            WorkoutCard(
                workoutEntry = workout,
                onEditClick = { onEditClick(workout) },
                onDeleteClick = { onDeleteClick(workout) },
                modifier = Modifier
                    .fillMaxWidth()
            )
            if (index < workouts.lastIndex) {
                HorizontalDivider(
                    thickness = 1.5.dp,
                    color = Color.LightGray.copy(alpha = 0.4f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WorkoutCardListPreview() {
    val mockWorkouts = listOf(
        WorkoutEntry(
            id = 1,
            exerciseId = 101,
            title = "Press Banca",
            weight = 85.0,
            reps = 10,
            rir = 2,
            series = 4,
            timestamp = Date()
        ),
        WorkoutEntry(
            id = 2,
            exerciseId = 102,
            title = "Sentadillas",
            weight = 100.0,
            reps = 8,
            rir = 3,
            series = 4,
            timestamp = Date()
        ),
        WorkoutEntry(
            id = 3,
            exerciseId = 103,
            title = "Dominadas",
            weight = 0.0,
            reps = 12,
            rir = 1,
            series = 3,
            timestamp = Date()
        ),
        WorkoutEntry(
            id = 4,
            exerciseId = 104,
            title = "Fondos",
            weight = 10.0,
            reps = 15,
            rir = 2,
            series = 3,
            timestamp = Date()
        )
    )

    WorkoutCardList(
        workouts = mockWorkouts,
        onEditClick = { workout -> println("Edit: ${workout.title}") },
        onDeleteClick = { workout -> println("Delete: ${workout.title}") }
    )
}