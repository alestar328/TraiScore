package com.develop.traiscore.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.develop.traiscore.domain.model.WorkoutModel

data class WorkoutWithType(
    @Embedded val workoutModel: WorkoutModel,
    @Relation(
        parentColumn = "workoutTypeId",
        entityColumn = "id"
    )
    val workoutType: WorkoutType
)