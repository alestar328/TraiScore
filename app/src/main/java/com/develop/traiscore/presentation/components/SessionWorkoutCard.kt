package com.develop.traiscore.presentation.components


import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.develop.traiscore.data.local.entity.WorkoutEntry
import androidx.compose.ui.Modifier


@Composable
fun SessionWorkoutCard(
    sessionName: String,
    sessionColor: String,
    workouts: List<WorkoutEntry>,
    isActive: Boolean = false,
    onEditClick: (WorkoutEntry) -> Unit,
    onDeleteClick: (WorkoutEntry) -> Unit,
    onExpandClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    fun hexToColor(hex: String): Color {
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: Exception) {
            Color(0xFF355E58) // Color por defecto
        }
    }

    val hexColor = hexToColor(sessionColor)
    val backgroundColor = if (isActive) {
        hexColor.copy(alpha = 0.15f)
    } else {
        Color.DarkGray.copy(alpha = 0.8f)
    }

    val headerColor = if (isActive) hexColor else Color.Gray



        // Lista de ejercicios
        WorkoutCardList(
            workouts = workouts,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick
        )

}