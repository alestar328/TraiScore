package com.develop.traiscore.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.develop.traiscore.domain.model.WorkoutModel

data class WorkoutWithExercise(
    @Embedded val workoutModel: WorkoutModel,
    @Relation(
        parentColumn = "workoutTypeId", // Columna en WorkoutModel
        entityColumn = "id" // Columna en WorkoutType
    )
    val workoutType: WorkoutType,
    @Relation(
        parentColumn = "exerciseId", // Columna en WorkoutType
        entityColumn = "id" // Columna en ExerciseEntity
    )
    val exerciseEntity: ExerciseEntity
)
