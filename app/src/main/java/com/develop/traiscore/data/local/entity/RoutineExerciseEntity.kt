package com.develop.traiscore.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routine_exercises")
data class RoutineExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val sectionId: Int,        // FK a RoutineSectionEntity.id
    val name: String,
    val series: Int,
    val reps: String,
    val weight: String,
    val rir: Int
)