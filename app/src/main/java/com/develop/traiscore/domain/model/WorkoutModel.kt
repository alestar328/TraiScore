package com.develop.traiscore.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date


@Entity(tableName = "workout_table")
data class WorkoutModel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Clave primaria autogenerada
    val timestamp: Date,
    val workoutTypeId: Int // Relación con WorkoutType
)
