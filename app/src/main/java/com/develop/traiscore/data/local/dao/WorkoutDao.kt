package com.develop.traiscore.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.develop.traiscore.data.local.entity.WorkoutType
import com.develop.traiscore.data.local.entity.WorkoutWithExercise
import kotlinx.coroutines.flow.Flow

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
    @Query("SELECT * FROM workout_type WHERE timestamp BETWEEN :startDate AND :endDate LIMIT 1")
    suspend fun getWorkoutByDate(startDate: Long, endDate: Long): WorkoutType?


    @Transaction
    @Query("SELECT * FROM workout_type WHERE id = :workoutId")
    suspend fun getWorkoutWithExercise(workoutId: Int): WorkoutWithExercise?

    @Query("SELECT * FROM workout_type")
    fun getAllWorkoutsFlow(): Flow<List<WorkoutType>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkouts(workouts: List<WorkoutType>)

    @Query("SELECT * FROM workout_type WHERE exerciseId = :exerciseId")
    suspend fun getWorkoutTypesByExercise(exerciseId: Int): List<WorkoutType>

    @Query("SELECT * FROM workout_type WHERE id IN (:workoutTypeIds)")
    suspend fun getWorkoutTypesByIds(workoutTypeIds: List<Int>): List<WorkoutType>


    @Transaction
    @Query("SELECT * FROM workout_type")
    suspend fun getAllWorkoutsWithExercise(): List<WorkoutWithExercise>

    @Query("SELECT * FROM workout_type WHERE exerciseId = :exerciseId AND timestamp BETWEEN :startDate AND :endDate")
    suspend fun getWorkoutTypesForExerciseAndDateRange(
        exerciseId: Int,
        startDate: Long,
        endDate: Long
    ): List<WorkoutType>
}