package com.develop.traiscore.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.develop.traiscore.domain.model.WorkoutModel

data class WorkoutWithExercise(
    @Embedded val workoutModel: WorkoutModel,
    @Relation(
        parentColumn = "id", // Columna en WorkoutModel
        entityColumn = "id" // Columna en WorkoutEntry
    )
    val workoutEntry: WorkoutEntry,
    @Relation(
        parentColumn = "exerciseId", // Columna en WorkoutEntry
        entityColumn = "id" // Columna en ExerciseEntity
    )
    val exerciseEntity: ExerciseEntity
)
