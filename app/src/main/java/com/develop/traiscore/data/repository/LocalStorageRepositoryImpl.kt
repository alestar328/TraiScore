package com.develop.traiscore.data.repository

import com.develop.traiscore.data.local.dao.ExerciseDao
import com.develop.traiscore.data.local.dao.WorkoutDao
import com.develop.traiscore.data.local.dao.WorkoutTypeDao
import com.develop.traiscore.data.local.entity.WorkoutWithType
import com.develop.traiscore.domain.model.WorkoutModel
import com.develop.traiscore.domain.model.toWorkoutType
import com.develop.traiscore.domain.repository.LocalStorageRepository
import javax.inject.Inject

class LocalStorageRepositoryImpl @Inject constructor(
    private val exerciseDao: ExerciseDao,
    private val workoutDao: WorkoutDao,
    private val workoutTypeDao: WorkoutTypeDao
) : LocalStorageRepository {

    override suspend fun saveWorkout(workoutModel: WorkoutModel): Boolean {
        return try {
            workoutDao.insertWorkout(workoutModel)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun getWorkout(date: String): WorkoutModel? {
        return try {
            // Obtenemos el WorkoutModel directamente desde la base de datos por la fecha
            workoutDao.getWorkoutByDate(date)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}