package com.develop.traiscore.domain.model


import com.develop.traiscore.data.local.entity.WorkoutType
import java.util.Date


data class WorkoutModel(
    val id: Int = 0, // Clave primaria autogenerada
    val exerciseId: Int, // ID del ejercicio
    val title: String, // Nombre del ejercicio
    val weight: Double, // Peso usado
    val reps: Int, // Repeticiones
    val rir: Int? = 0, // RIR opcional
    val timestamp: Date, // Fecha del entrenamiento

)

fun WorkoutModel.toWorkoutType(): WorkoutType {
    return WorkoutType(
        id = this.id,
        exerciseId = this.exerciseId,
        title = this.title,
        weight = this.weight,
        reps = this.reps,
        rir = this.rir,
        timestamp = this.timestamp

    )
}

