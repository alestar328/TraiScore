package com.develop.traiscore.data.local.dao

import androidx.room.*
import com.develop.traiscore.data.local.entity.BodyStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyStatsDao {

    // Insertar
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBodyStats(bodyStats: BodyStatsEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultipleBodyStats(bodyStatsList: List<BodyStatsEntity>)

    // Actualizar
    @Update
    suspend fun updateBodyStats(bodyStats: BodyStatsEntity)

    // Eliminar
    @Delete
    suspend fun deleteBodyStats(bodyStats: BodyStatsEntity)

    @Query("DELETE FROM body_stats WHERE id = :id")
    suspend fun deleteBodyStatsById(id: Long)

    // Queries de lectura - Flow para UI reactiva
    @Query("SELECT * FROM body_stats WHERE userId = :userId ORDER BY createdAt DESC")
    fun getBodyStatsForUserFlow(userId: String): Flow<List<BodyStatsEntity>>

    @Query("SELECT * FROM body_stats WHERE userId = :userId ORDER BY createdAt DESC LIMIT 1")
    fun getLatestBodyStatsFlow(userId: String): Flow<BodyStatsEntity?>

    // Queries de lectura - Una sola vez
    @Query("SELECT * FROM body_stats WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getBodyStatsForUser(userId: String): List<BodyStatsEntity>

    @Query("SELECT * FROM body_stats WHERE userId = :userId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestBodyStats(userId: String): BodyStatsEntity?

    @Query("SELECT * FROM body_stats WHERE id = :id")
    suspend fun getBodyStatsById(id: Long): BodyStatsEntity?

    @Query("SELECT * FROM body_stats WHERE firebaseId = :firebaseId LIMIT 1")
    suspend fun getBodyStatsByFirebaseId(firebaseId: String): BodyStatsEntity?

    // Para sincronización
    @Query("SELECT * FROM body_stats WHERE isSynced = 0")
    suspend fun getUnsyncedBodyStats(): List<BodyStatsEntity>

    @Query("SELECT * FROM body_stats WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedBodyStatsForUser(userId: String): List<BodyStatsEntity>

    // Contador para límites de suscripción
    @Query("SELECT COUNT(*) FROM body_stats WHERE userId = :userId AND pendingAction != 'DELETE'")
    suspend fun getBodyStatsCountForUser(userId: String): Int

    // Limpiar registros marcados para eliminar
    @Query("DELETE FROM body_stats WHERE pendingAction = 'DELETE' AND isSynced = 1")
    suspend fun cleanupDeletedRecords()
}