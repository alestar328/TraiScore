package com.develop.traiscore.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.develop.traiscore.domain.model.WorkoutModel
import java.util.Date


@Entity(tableName = "workout_type")
data class WorkoutType(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Clave primaria
    val exerciseId: Int,
    val title: String,
    val weight: Double,
    val reps: Int,
    @ColumnInfo(defaultValue = "0") val rir: Int? = 0, // RIR es opcional y por defecto 0
    val timestamp: Date // Fecha del entrenamiento
)