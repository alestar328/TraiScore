package com.develop.traiscore.data

import com.develop.traiscore.data.local.dao.WorkoutDao
import com.develop.traiscore.data.local.entity.WorkoutEntry
import kotlinx.coroutines.flow.Flow

class WorkoutRepository(private val workoutDao: WorkoutDao) {

    val workouts: Flow<List<WorkoutEntry>> = workoutDao.getAllWorkoutsFlow()
    // Agregar un nuevo entrenamiento
    suspend fun addWorkout(workout: WorkoutEntry) {
        workoutDao.insertWorkout(workout)
    }

    // Actualizar un entrenamiento existente
    suspend fun updateWorkout(workout: WorkoutEntry) {
        workoutDao.updateWorkout(workout)
    }

    // Eliminar un entrenamiento
    suspend fun removeWorkout(workout: WorkoutEntry) {
        workoutDao.deleteWorkout(workout)
    }

    // Obtener un entrenamiento por ID
    suspend fun getWorkoutById(id: Int): WorkoutEntry? {
        return workoutDao.getWorkoutById(id)
    }

    // Obtener entrenamientos por ejercicio
    suspend fun getWorkoutsByExercise(exerciseId: Int): List<WorkoutEntry> {
        return workoutDao.getWorkoutTypesByExercise(exerciseId)
    }
}