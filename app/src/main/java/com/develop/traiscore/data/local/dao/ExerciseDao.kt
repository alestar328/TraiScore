package com.develop.traiscore.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.develop.traiscore.data.local.entity.ExerciseEntity

@Dao
interface ExerciseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exerciseEntity: ExerciseEntity): Long

    @Update
    suspend fun updateExercise(exerciseEntity: ExerciseEntity)

    @Delete
    suspend fun deleteExercise(exerciseEntity: ExerciseEntity)

    @Query("SELECT * FROM exercise_table WHERE id = :exerciseId")
    suspend fun getExerciseById(exerciseId: Int): ExerciseEntity?

    @Query("SELECT * FROM exercise_table WHERE isDefault = 1")
    suspend fun getDefaultExercises(): List<ExerciseEntity>

    @Query("SELECT * FROM exercise_table WHERE isDefault = 0")
    suspend fun getCustomExercises(): List<ExerciseEntity>

    @Query("SELECT * FROM exercise_table WHERE name LIKE '%' || :search || '%'")
    suspend fun searchExercises(search: String): List<ExerciseEntity>
}