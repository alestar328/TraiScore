package com.develop.traiscore.domain.repository

import com.develop.traiscore.data.local.entity.WorkoutWithExercise
import com.develop.traiscore.domain.model.WorkoutModel

interface LocalStorageRepository {

    suspend fun saveWorkout(workoutModel: WorkoutModel) : Boolean
    suspend fun getWorkout(date: String) : WorkoutModel?
    suspend fun getAllWorkouts() : List<WorkoutWithExercise>
    suspend fun updateWorkout(workoutModel: WorkoutModel) : Boolean
    suspend fun deleteWorkout(workoutId: Int): Boolean
}