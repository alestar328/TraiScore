package com.develop.traiscore.data

import com.develop.traiscore.data.local.dao.WorkoutDao
import com.develop.traiscore.data.local.entity.WorkoutType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class WorkoutRepository(private val workoutDao: WorkoutDao) {

    val workouts: Flow<List<WorkoutType>> = workoutDao.getAllWorkoutsFlow()
    // Agregar un nuevo entrenamiento
    suspend fun addWorkout(workout: WorkoutType) {
        workoutDao.insertWorkout(workout)
    }

    // Actualizar un entrenamiento existente
    suspend fun updateWorkout(workout: WorkoutType) {
        workoutDao.updateWorkout(workout)
    }

    // Eliminar un entrenamiento
    suspend fun removeWorkout(workout: WorkoutType) {
        workoutDao.deleteWorkout(workout)
    }

    // Obtener un entrenamiento por ID
    suspend fun getWorkoutById(id: Int): WorkoutType? {
        return workoutDao.getWorkoutById(id)
    }

    // Obtener entrenamientos por ejercicio
    suspend fun getWorkoutsByExercise(exerciseId: Int): List<WorkoutType> {
        return workoutDao.getWorkoutTypesByExercise(exerciseId)
    }
}