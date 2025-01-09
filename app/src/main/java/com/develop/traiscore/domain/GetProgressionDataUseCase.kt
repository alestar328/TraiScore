package com.develop.traiscore.domain
import com.develop.traiscore.data.local.entity.WorkoutWithExercise
import com.develop.traiscore.domain.model.WorkoutModel

class GetProgressionDataUseCase {
    operator fun invoke(workouts: List<WorkoutWithExercise>, exercise: String): List<Pair<String, Float>> {
        // Filtrar por ejercicio
        val filteredWorkouts = workouts.filter { it.exerciseEntity.name == exercise }

        // Ordenar por fecha
        val sortedWorkouts = filteredWorkouts.sortedBy { it.workoutModel.timestamp }

        // Convertir a datos para el gráfico (fecha -> peso)
        return sortedWorkouts.map { workout ->
            val weight = workout.workoutType.weight.toFloat()
            Pair(
                workout.workoutModel.timestamp.toString(), // Fecha como cadena
                weight // Peso como valor flotante
            )
        }
    }
}