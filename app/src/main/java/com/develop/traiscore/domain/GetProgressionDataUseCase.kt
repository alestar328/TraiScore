package com.develop.traiscore.domain
import com.develop.traiscore.domain.model.WorkoutModel

class GetProgressionDataUseCase {
    operator fun invoke(workouts: List<WorkoutModel>, exercise: String): List<Pair<String, Float>> {
        // Filtrar por ejercicio
        val filteredWorkouts = workouts.filter { it.type.title == exercise }

        // Ordenar por fecha
        val sortedWorkouts = filteredWorkouts.sortedBy { it.timestamp }

        // Convertir a datos para el gráfico (fecha -> peso)
        return sortedWorkouts.map { workout ->
            Pair(
                workout.timestamp.toString(), // Fecha como cadena
                workout.type.weight.toFloat() // Peso como valor flotante
            )
        }
    }
}