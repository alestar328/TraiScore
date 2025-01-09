package com.develop.traiscore.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.develop.traiscore.data.local.entity.WorkoutType
import java.util.Date


@Entity(tableName = "workout_table")
data class WorkoutModel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Clave primaria autogenerada
    val exerciseId: Int, // ID del ejercicio
    val title: String, // Nombre del ejercicio
    val weight: Double, // Peso usado
    val reps: Int, // Repeticiones
    val rir: Int? = 0, // RIR opcional
    val timestamp: Date, // Fecha del entrenamiento
    val workoutTypeId: Int // Relación con WorkoutType

)



fun WorkoutModel.toWorkoutType(): WorkoutType {
    return WorkoutType(
        id = this.id,
        exerciseId = this.exerciseId,
        title = this.title,
        weight = this.weight,
        reps = this.reps,
        rir = this.rir,

    )
}

