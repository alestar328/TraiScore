package com.develop.traiscore.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.develop.traiscore.domain.model.WorkoutModel

data class WorkoutWithExercise(
    @Embedded val workout: WorkoutEntry, // ✅ el Workout local de Room
    @Relation(
        parentColumn = "exerciseId",
        entityColumn = "id"
    )
    val exercise: ExerciseEntity? = null // opcional si puede no existir
)
