package com.develop.traiscore.data.local.dao

import androidx.room.*
import com.develop.traiscore.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface SessionDao {

    @Query("SELECT * FROM sessions WHERE userId = :userId AND isFinished = 0 ORDER BY createdAt DESC")
    fun getAvailableSessions(userId: String): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE userId = :userId AND isActive = 1 LIMIT 1")
    suspend fun getActiveSession(userId: String): SessionEntity?

    @Query("SELECT * FROM sessions WHERE userId = :userId AND isFinished = 1 ORDER BY createdAt DESC")
    fun getFinishedSessions(userId: String): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE sessionId = :sessionId")
    suspend fun getSessionById(sessionId: String): SessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Delete
    suspend fun deleteSession(session: SessionEntity)

    @Query("UPDATE sessions SET isActive = 0 WHERE userId = :userId")
    suspend fun deactivateAllSessions(userId: String)

    @Query("UPDATE sessions SET isActive = 1, lastModified = :timestamp WHERE sessionId = :sessionId")
    suspend fun activateSession(sessionId: String, timestamp: Long)

    @Query("UPDATE sessions SET isFinished = 1, isActive = 0, endedAt = :endTime WHERE sessionId = :sessionId")
    suspend fun endSession(sessionId: String, endTime: Date)

    // Para sincronización
    @Query("SELECT * FROM sessions WHERE isSynced = 0")
    suspend fun getUnsyncedSessions(): List<SessionEntity>

    @Query("UPDATE sessions SET isSynced = 1, pendingAction = NULL WHERE sessionId = :sessionId")
    suspend fun markAsSynced(sessionId: String)

    @Query("SELECT * FROM sessions WHERE userId = :userId AND isFinished = 0 ORDER BY createdAt DESC")
    suspend fun getAvailableSessionsList(userId: String): List<SessionEntity>
}