package com.develop.traiscore.domain

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID


@Entity(tableName = "workout_table")
data class WorkoutModel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Clave primaria autogenerada
    val timestamp: Date,
    val type: WorkoutType // Relación con WorkoutType
)
