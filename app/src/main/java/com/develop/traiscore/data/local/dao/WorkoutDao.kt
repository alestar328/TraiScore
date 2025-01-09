package com.develop.traiscore.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.develop.traiscore.domain.model.WorkoutModel
import com.develop.traiscore.data.local.entity.WorkoutWithExercise
@Dao
interface WorkoutDao  {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutModel): Long

    @Query("SELECT * FROM workout_table WHERE timestamp = :date LIMIT 1")
    suspend fun getWorkoutByDate(date: String): WorkoutModel

    @Update
    suspend fun updateWorkout(workout: WorkoutModel)

    @Delete
    suspend fun deleteWorkout(workout: WorkoutModel)

    @Query("SELECT * FROM workout_table WHERE id = :workoutId")
    suspend fun getWorkoutById(workoutId: Int): WorkoutModel?

    @Transaction
    @Query("SELECT * FROM workout_table")
    suspend fun getAllWorkoutsWithExercise(): List<WorkoutWithExercise>

    @Transaction
    @Query("SELECT * FROM workout_table WHERE id = :workoutId")
    suspend fun getWorkoutWithExerciseById(workoutId: Int): WorkoutWithExercise?
}