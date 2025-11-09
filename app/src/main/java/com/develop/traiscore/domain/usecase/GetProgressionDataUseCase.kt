package com.develop.traiscore.domain.usecase
import com.develop.traiscore.data.local.entity.WorkoutWithExercise
import java.text.SimpleDateFormat
import java.util.Locale

class GetProgressionDataUseCase {

    operator fun invoke(
        workouts: List<WorkoutWithExercise>,
        exerciseName: String
    ): List<Pair<String, Float>> {
        if (workouts.isEmpty()) return emptyList()

        // Filtrar solo los workouts que corresponden al ejercicio solicitado
        val filteredWorkouts = workouts.filter {
            it.exercise?.name.equals(exerciseName, ignoreCase = true)
        }

        // Ordenar por fecha
        val sortedWorkouts = filteredWorkouts.sortedBy { it.workout.timestamp }

        // Formateador para mostrar fecha legible
        val dateFormatter = SimpleDateFormat("dd/MM", Locale.getDefault())

        // Generar pares (fecha → peso)
        return sortedWorkouts.map { item ->
            val dateLabel = dateFormatter.format(item.workout.timestamp)
            val weight = item.workout.weight
            dateLabel to weight
        }
    }
}