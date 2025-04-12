package com.develop.traiscore.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.develop.traiscore.data.local.entity.WorkoutEntry
import com.develop.traiscore.data.local.entity.WorkoutWithExercise
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao  {

    // Inserta un nuevo entrenamiento
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntry): Long

    // Actualiza un entrenamiento existente
    @Update
    suspend fun updateWorkout(workout: WorkoutEntry)

    // Elimina un entrenamiento específico
    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntry)

    // Elimina un entrenamiento por ID
    @Query("DELETE FROM workout_entry WHERE id = :workoutId")
    suspend fun deleteWorkoutById(workoutId: Int)

    // Obtiene un entrenamiento por ID
    @Query("SELECT * FROM workout_entry WHERE id = :workoutId")
    suspend fun getWorkoutById(workoutId: Int): WorkoutEntry?

    // Obtiene un entrenamiento por fecha
    @Query("SELECT * FROM workout_entry WHERE timestamp BETWEEN :startDate AND :endDate LIMIT 1")
    suspend fun getWorkoutByDate(startDate: Long, endDate: Long): WorkoutEntry?


    @Transaction
    @Query("SELECT * FROM workout_entry WHERE id = :workoutId")
    suspend fun getWorkoutWithExercise(workoutId: Int): WorkoutWithExercise?

    @Query("SELECT * FROM workout_entry")
    fun getAllWorkoutsFlow(): Flow<List<WorkoutEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkouts(workouts: List<WorkoutEntry>)

    @Query("SELECT * FROM workout_entry WHERE exerciseId = :exerciseId")
    suspend fun getWorkoutTypesByExercise(exerciseId: Int): List<WorkoutEntry>

    @Query("SELECT * FROM workout_entry WHERE id IN (:workoutTypeIds)")
    suspend fun getWorkoutTypesByIds(workoutTypeIds: List<Int>): List<WorkoutEntry>


    @Transaction
    @Query("SELECT * FROM workout_entry")
    suspend fun getAllWorkoutsWithExercise(): List<WorkoutWithExercise>

    @Query("SELECT * FROM workout_entry WHERE exerciseId = :exerciseId AND timestamp BETWEEN :startDate AND :endDate")
    suspend fun getWorkoutTypesForExerciseAndDateRange(
        exerciseId: Int,
        startDate: Long,
        endDate: Long
    ): List<WorkoutEntry>
}