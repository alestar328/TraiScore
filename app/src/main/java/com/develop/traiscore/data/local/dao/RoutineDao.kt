package com.develop.traiscore.data.local.dao


import androidx.room.*
import com.develop.traiscore.data.local.entity.*

@Dao
interface RoutineDao {

    // Crear rutina
    @Insert
    suspend fun insertRoutine(routine: RoutineEntity): Long

    @Insert
    suspend fun insertSections(sections: List<RoutineSectionEntity>): List<Long>

    @Insert
    suspend fun insertExercises(exercises: List<RoutineExerciseEntity>)

    // Obtener todas las rutinas del usuario
    @Query("SELECT * FROM routines WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getRoutines(userId: String): List<RoutineEntity>

    // Obtener secciones de una rutina
    @Query("SELECT * FROM routine_sections WHERE routineLocalId = :routineId")
    suspend fun getSections(routineId: Int): List<RoutineSectionEntity>

    // Obtener ejercicios de una sección
    @Query("SELECT * FROM routine_exercises WHERE sectionId = :sectionId")
    suspend fun getExercises(sectionId: Int): List<RoutineExerciseEntity>

    // Borrados
    @Query("DELETE FROM routines WHERE id = :id")
    suspend fun deleteRoutine(id: Int)

    @Query("DELETE FROM routine_sections WHERE routineLocalId = :routineId")
    suspend fun deleteSections(routineId: Int)

    @Query("DELETE FROM routine_exercises WHERE sectionId IN (:sectionIds)")
    suspend fun deleteExercises(sectionIds: List<Int>)
}