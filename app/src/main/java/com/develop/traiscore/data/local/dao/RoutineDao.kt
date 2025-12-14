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

    @Query("UPDATE routines SET routineIdFirebase = :firebaseId WHERE id = :localId")
    suspend fun updateRoutineFirebaseId(localId: Int, firebaseId: String)

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

    // Guardar snapshot de rutina
    @Insert
    suspend fun insertRoutineHistory(history: RoutineHistoryEntity): Long

    // Obtener todas las fechas con rutinas guardadas (para marcar en calendario)
    @Query("""
    SELECT DISTINCT savedDate 
    FROM routine_history 
    WHERE userId = :userId 
    ORDER BY savedDate DESC
""")
    suspend fun getDatesWithRoutines(userId: String): List<String>

    // Obtener rutinas guardadas en una fecha específica
    @Query("""
    SELECT * FROM routine_history 
    WHERE userId = :userId AND savedDate = :date 
    ORDER BY savedTimestamp DESC
""")
    suspend fun getRoutinesByDate(userId: String, date: String): List<RoutineHistoryEntity>

    // Obtener rutinas de un mes específico (para CalendarScreen)
    @Query("""
    SELECT * FROM routine_history 
    WHERE userId = :userId 
    AND savedDate LIKE :monthPattern 
    ORDER BY savedDate DESC, savedTimestamp DESC
""")
    suspend fun getRoutinesByMonth(userId: String, monthPattern: String): List<RoutineHistoryEntity>

    // Obtener rutinas de un año específico (para YearViewScreen)
    @Query("""
    SELECT * FROM routine_history 
    WHERE userId = :userId 
    AND savedDate LIKE :yearPattern 
    ORDER BY savedDate DESC
""")
    suspend fun getRoutinesByYear(userId: String, yearPattern: String): List<RoutineHistoryEntity>

    // Borrar historial de una rutina específica
    @Query("DELETE FROM routine_history WHERE routineLocalId = :routineId")
    suspend fun deleteRoutineHistory(routineId: Int)

    // Obtener último snapshot de una rutina
    @Query("""
    SELECT * FROM routine_history 
    WHERE routineLocalId = :routineId 
    ORDER BY savedTimestamp DESC 
    LIMIT 1
""")

    suspend fun getLastRoutineSnapshot(routineId: Int): RoutineHistoryEntity?
}