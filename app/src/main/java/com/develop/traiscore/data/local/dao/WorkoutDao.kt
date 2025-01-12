package com.develop.traiscore.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.develop.traiscore.data.local.entity.WorkoutType
import com.develop.traiscore.domain.model.WorkoutModel
import com.develop.traiscore.data.local.entity.WorkoutWithExercise
@Dao
interface WorkoutDao  {

    // Inserta un nuevo entrenamiento
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutType): Long

    // Actualiza un entrenamiento existente
    @Update
    suspend fun updateWorkout(workout: WorkoutType)

    // Elimina un entrenamiento específico
    @Delete
    suspend fun deleteWorkout(workout: WorkoutType)

    // Elimina un entrenamiento por ID
    @Query("DELETE FROM workout_type WHERE id = :workoutId")
    suspend fun deleteWorkoutById(workoutId: Int)

    // Obtiene un entrenamiento por ID
    @Query("SELECT * FROM workout_type WHERE id = :workoutId")
    suspend fun getWorkoutById(workoutId: Int): WorkoutType?

    // Obtiene un entrenamiento por fecha
    @Query("SELECT * FROM workout_type WHERE timestamp = :date LIMIT 1")
    suspend fun getWorkoutByDate(date: String): WorkoutType?

    // Obtiene todos los entrenamientos
    @Query("SELECT * FROM workout_type")
    suspend fun getAllWorkouts(): List<WorkoutType>
}