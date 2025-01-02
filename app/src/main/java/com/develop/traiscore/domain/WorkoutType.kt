package com.develop.traiscore.domain

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "workout_type")
data class WorkoutType(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Clave primaria
    val title: String,
    val weight: Double,
    val reps: Int,
    val rir: Int? = 0 // RIR es opcional y por defecto 0
) {
    // Propiedades derivadas similares al código de Swift
    val rirString: String
        get() = rir?.toString() ?: "0"

    val rirFloat: Float
        get() = rir?.toFloat() ?: 0f
}
