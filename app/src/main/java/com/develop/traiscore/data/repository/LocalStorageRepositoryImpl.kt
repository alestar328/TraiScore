package com.develop.traiscore.data.repository

import com.develop.traiscore.data.local.dao.ExerciseDao
import com.develop.traiscore.data.local.dao.WorkoutDao
import com.develop.traiscore.data.local.dao.WorkoutTypeDao
import com.develop.traiscore.data.local.entity.WorkoutWithExercise
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
            // Convierte WorkoutModel a WorkoutType antes de insertar
            val workoutType = workoutModel.toWorkoutType()
            workoutDao.insertWorkout(workoutType)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun getWorkout(date: String): WorkoutModel? {
        return try {
            workoutDao.getWorkoutByDate(date)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun getAllWorkouts(): List<WorkoutWithExercise> {
        return workoutDao.getAllWorkoutsWithExercise()
    }
    override suspend fun deleteWorkout(workoutId: Int): Boolean {
        return try {
            workoutDao.deleteWorkoutById(workoutId)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    override suspend fun updateWorkout(workoutModel: WorkoutModel): Boolean {
        return try {
            workoutDao.updateWorkout(workoutModel)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}