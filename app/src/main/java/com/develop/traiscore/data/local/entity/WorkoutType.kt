package com.develop.traiscore.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.develop.traiscore.domain.model.WorkoutModel


@Entity(tableName = "workout_type")
data class WorkoutType(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Clave primaria
    val exerciseId: Int,
    val title: String,
    val weight: Double,
    val reps: Int,
    @ColumnInfo(defaultValue = "0") val rir: Int? = 0 // RIR es opcional y por defecto 0
) {
    // Propiedades derivadas similares al código de Swift
    val rirString: String
        get() = rir?.toString() ?: "0"

    val rirFloat: Float
        get() = rir?.toFloat() ?: 0f
}
