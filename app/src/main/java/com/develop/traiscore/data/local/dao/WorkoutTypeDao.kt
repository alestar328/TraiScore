package com.develop.traiscore.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.develop.traiscore.data.local.entity.WorkoutType

@Dao
interface WorkoutTypeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutType(workoutType: WorkoutType): Long

    @Update
    suspend fun updateWorkoutType(workoutType: WorkoutType)

    @Delete
    suspend fun deleteWorkoutType(workoutType: WorkoutType)

    @Query("SELECT * FROM workout_type WHERE id = :workoutTypeId")
    suspend fun getWorkoutTypeById(workoutTypeId: Int): WorkoutType?

    @Query("SELECT * FROM workout_type WHERE exerciseId = :exerciseId")
    suspend fun getWorkoutTypesByExercise(exerciseId: Int): List<WorkoutType>
}