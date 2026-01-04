package com.develop.traiscore.data.local.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface MigrationDao {
    @Query("UPDATE sessions SET userId = :newUid WHERE userId = :oldUid")
    suspend fun migrateSessions(oldUid: String, newUid: String)

    @Query("UPDATE routines SET userId = :newUid WHERE userId = :oldUid")
    suspend fun migrateRoutines(oldUid: String, newUid: String)

    // Repetir para exercises, bodyStats y medicalStats si tienen userId
    @Query("UPDATE exercise_table SET createdBy = :newUid WHERE createdBy = :oldUid")
    suspend fun migrateExercises(oldUid: String, newUid: String)

    @Query("UPDATE body_stats SET userId = :newUid WHERE userId = :oldUid")
    suspend fun migrateBodyStats(oldUid: String, newUid: String)
}