package com.develop.traiscore.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.develop.traiscore.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    // ✅ OPERACIONES EXISTENTES - NO TOCAR
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

    // 🆕 NUEVAS QUERIES - Para reactividad y sincronización

    // Flow para UI reactiva (como workouts)
    @Query("SELECT * FROM exercise_table ORDER BY name ASC")
    fun getAllExercisesFlow(): Flow<List<ExerciseEntity>>

    // Obtener todos (una sola vez)
    @Query("SELECT * FROM exercise_table ORDER BY name ASC")
    suspend fun getAllExercises(): List<ExerciseEntity>

    // Insertar múltiples (para importación inicial)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    // Query para sincronización
    @Query("SELECT * FROM exercise_table WHERE isSynced = 0")
    suspend fun getUnsyncedExercises(): List<ExerciseEntity>

    // Verificar si ya se importaron ejercicios globales
    @Query("SELECT COUNT(*) FROM exercise_table WHERE isDefault = 1")
    suspend fun getGlobalExerciseCount(): Int

    // Buscar por Firebase ID
    @Query("SELECT * FROM exercise_table WHERE idIntern = :firebaseId LIMIT 1")
    suspend fun getExerciseByFirebaseId(firebaseId: String): ExerciseEntity?
}