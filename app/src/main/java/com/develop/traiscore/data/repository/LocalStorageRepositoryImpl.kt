package com.develop.traiscore.data.repository

import com.develop.traiscore.data.local.dao.ExerciseDao
import com.develop.traiscore.data.local.dao.WorkoutDao
import com.develop.traiscore.data.local.entity.WorkoutType
import com.develop.traiscore.data.local.entity.WorkoutWithExercise
import com.develop.traiscore.domain.model.WorkoutModel
import com.develop.traiscore.domain.model.toWorkoutType
import com.develop.traiscore.domain.repository.LocalStorageRepository
import javax.inject.Inject

class LocalStorageRepositoryImpl @Inject constructor(
    private val workoutDao: WorkoutDao,
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

    override suspend fun getWorkout(startDate: Long, endDate: Long): WorkoutModel? {
        return try {
            val workoutType = workoutDao.getWorkoutByDate(startDate, endDate)
            workoutType?.toWorkoutModel()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun WorkoutType.toWorkoutModel(): WorkoutModel {
        return WorkoutModel(
            id = this.id,
            exerciseId = this.exerciseId,
            title = this.title,
            weight = this.weight,
            reps = this.reps,
            rir = this.rir,
            timestamp = this.timestamp
        )
    }
    override suspend fun getAllWorkouts(): List<WorkoutWithExercise> {
        return try {
            workoutDao.getAllWorkoutsWithExercise()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
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
            val workoutType = workoutModel.toWorkoutType() // Transformación
            workoutDao.updateWorkout(workoutType) // Llama al DAO con la entidad
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}