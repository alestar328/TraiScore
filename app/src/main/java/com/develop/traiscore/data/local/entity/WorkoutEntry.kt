package com.develop.traiscore.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date


@Entity(tableName = "workout_entry")
data class WorkoutEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Clave primaria
    val uid: String? = null, // <-- Este será el ID real de Firebase
    val exerciseId: Int,
    val title: String,
    val weight: Float,
    val series: Int,
    val reps: Int,
    val rir: Int? = 0,
    val type: String = "", // "Empuje", "Pierna", etc. — para agrupar en UI
    val timestamp: Date // Fecha del entrenamiento
)