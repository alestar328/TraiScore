package com.develop.traiscore.domain.repository

import com.develop.traiscore.domain.model.WorkoutModel

interface LocalStorageRepository {

    suspend fun saveWorkout(workoutModel: WorkoutModel) : Boolean
    suspend fun getWorkout(date: String) : WorkoutModel?
}