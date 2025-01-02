package com.develop.traiscore.domain

import androidx.room.Entity
import java.util.Date
import java.util.UUID


@Entity(tableName = "workout_table")
data class WorkoutModel(
    val id: String = UUID.randomUUID().toString(), // Generar un identificador único
    val timestamp: Date,
    val type: WorkoutType // Relación con WorkoutType
)
