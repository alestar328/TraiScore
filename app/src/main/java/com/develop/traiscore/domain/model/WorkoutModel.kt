package com.develop.traiscore.domain.model


import com.develop.traiscore.data.local.entity.WorkoutEntry
import java.util.Date


data class WorkoutModel(
    val id: Int = 0, // Clave primaria autogenerada
    val exerciseId: Int, // ID del ejercicio
    val title: String, // Nombre del ejercicio
    val weight: Double, // Peso usado
    val series:Int,
    val reps: Int, // Repeticiones
    val rir: Int? = 0, // RIR opcional
    val timestamp: Date, // Fecha del entrenamiento

)

fun WorkoutModel.toWorkoutType(): WorkoutEntry {
    return WorkoutEntry(
        id = this.id,
        exerciseId = this.exerciseId,
        title = this.title,
        weight = this.weight,
        series = this.series,
        reps = this.reps,
        rir = this.rir,
        timestamp = this.timestamp

    )
}

fun WorkoutEntry.toWorkoutModel(): WorkoutModel {
    return WorkoutModel(
        id = this.id,
        exerciseId = this.exerciseId,
        title = this.title,
        weight = this.weight,
        series = this.series,
        reps = this.reps,
        rir = this.rir,
        timestamp = this.timestamp
    )
}
