package com.develop.traiscore.data.repository

import android.util.Log
import com.develop.traiscore.data.local.dao.BodyStatsDao
import com.develop.traiscore.data.local.entity.BodyStatsEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BodyStatsRepository @Inject constructor(
    private val bodyStatsDao: BodyStatsDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    companion object {
        private const val TAG = "BodyStatsRepo"
    }

    /**
     * Flow principal para observar cambios en las medidas del usuario
     */
    fun getBodyStatsFlow(userId: String): Flow<List<BodyStatsEntity>> {
        return bodyStatsDao.getBodyStatsForUserFlow(userId)
    }

    /**
     * Flow para observar la medida más reciente
     */
    fun getLatestBodyStatsFlow(userId: String): Flow<BodyStatsEntity?> {
        return bodyStatsDao.getLatestBodyStatsFlow(userId)
    }

    /**
     * Obtener todas las medidas del usuario (una sola vez)
     */
    suspend fun getBodyStatsForUser(userId: String): List<BodyStatsEntity> {
        return bodyStatsDao.getBodyStatsForUser(userId)
    }

    /**
     * Obtener medida más reciente
     */
    suspend fun getLatestBodyStats(userId: String): BodyStatsEntity? {
        return bodyStatsDao.getLatestBodyStats(userId)
    }

    /**
     * Obtener medida por ID
     */
    suspend fun getBodyStatsById(id: Long): BodyStatsEntity? {
        return bodyStatsDao.getBodyStatsById(id)
    }

    /**
     * Contar registros para verificar límites de suscripción
     */
    suspend fun getBodyStatsCount(userId: String): Int {
        return bodyStatsDao.getBodyStatsCountForUser(userId)
    }

    /**
     * Crear nueva medida (local-first)
     */
    suspend fun createBodyStats(
        gender: String,
        measurements: Map<String, String>
    ): Long {
        val userId = auth.currentUser?.uid ?: return -1

        val entity = BodyStatsEntity.fromMeasurementsMap(
            userId = userId,
            gender = gender,
            measurements = measurements,
            isSynced = false,
            pendingAction = "CREATE"
        )

        val localId = bodyStatsDao.insertBodyStats(entity)

        // Sincronizar en background
        syncPendingBodyStats()

        return localId
    }

    /**
     * Actualizar medida existente
     */
    suspend fun updateBodyStats(
        bodyStatsId: Long,
        gender: String,
        measurements: Map<String, String>
    ): Boolean {
        val existing = bodyStatsDao.getBodyStatsById(bodyStatsId) ?: return false

        val updated = BodyStatsEntity.fromMeasurementsMap(
            userId = existing.userId,
            gender = gender,
            measurements = measurements,
            firebaseId = existing.firebaseId,
            isSynced = false,
            pendingAction = "UPDATE"
        ).copy(
            id = bodyStatsId,
            createdAt = existing.createdAt, // Mantener fecha original
            updatedAt = System.currentTimeMillis()
        )

        bodyStatsDao.updateBodyStats(updated)

        // Sincronizar en background
        syncPendingBodyStats()

        return true
    }

    /**
     * Eliminar medida
     */
    suspend fun deleteBodyStats(bodyStatsId: Long): Boolean {
        val existing = bodyStatsDao.getBodyStatsById(bodyStatsId) ?: return false

        if (existing.firebaseId.isEmpty()) {
            // Si nunca se sincronizó, eliminar directamente
            bodyStatsDao.deleteBodyStatsById(bodyStatsId)
        } else {
            // Marcar para eliminación
            val marked = existing.copy(
                isSynced = false,
                pendingAction = "DELETE",
                updatedAt = System.currentTimeMillis()
            )
            bodyStatsDao.updateBodyStats(marked)

            // Sincronizar
            syncPendingBodyStats()
        }

        return true
    }

    /**
     * Importar medidas desde Firebase (al login)
     */
    suspend fun importBodyStatsFromFirebase() {
        val userId = auth.currentUser?.uid ?: return

        try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("bodyStats")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val firebaseStats = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                val measurements = data["measurements"] as? Map<String, Any> ?: return@mapNotNull null

                // Verificar si ya existe localmente
                val existingLocal = bodyStatsDao.getBodyStatsByFirebaseId(doc.id)
                if (existingLocal != null) {
                    return@mapNotNull null
                }

                BodyStatsEntity(
                    firebaseId = doc.id,
                    userId = userId,
                    gender = data["gender"] as? String ?: "Male",
                    height = (measurements["Height"] as? String)?.toDoubleOrNull() ?: 0.0,
                    weight = (measurements["Weight"] as? String)?.toDoubleOrNull() ?: 0.0,
                    neck = (measurements["Neck"] as? String)?.toDoubleOrNull() ?: 0.0,
                    chest = (measurements["Chest"] as? String)?.toDoubleOrNull() ?: 0.0,
                    arms = (measurements["Arms"] as? String)?.toDoubleOrNull() ?: 0.0,
                    waist = (measurements["Waist"] as? String)?.toDoubleOrNull() ?: 0.0,
                    thigh = (measurements["Thigh"] as? String)?.toDoubleOrNull() ?: 0.0,
                    calf = (measurements["Calf"] as? String)?.toDoubleOrNull() ?: 0.0,
                    isSynced = true,
                    pendingAction = null,
                    createdAt = (data["createdAt"] as? com.google.firebase.Timestamp)?.toDate()?.time
                        ?: System.currentTimeMillis(),
                    updatedAt = (data["updatedAt"] as? com.google.firebase.Timestamp)?.toDate()?.time
                        ?: System.currentTimeMillis()
                )
            }

            if (firebaseStats.isNotEmpty()) {
                bodyStatsDao.insertMultipleBodyStats(firebaseStats)
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error importando medidas desde Firebase", e)
        }
    }

    /**
     * Sincronizar medidas pendientes con Firebase
     */
    suspend fun syncPendingBodyStats() {
        val userId = auth.currentUser?.uid ?: return
        val unsynced = bodyStatsDao.getUnsyncedBodyStatsForUser(userId)

        if (unsynced.isEmpty()) return

        unsynced.forEach { bodyStats ->
            try {
                when (bodyStats.pendingAction) {
                    "CREATE" -> {
                        val documentId = generateUniqueId()
                        val data = mapOf(
                            "id" to documentId,
                            "userId" to userId,
                            "gender" to bodyStats.gender,
                            "measurements" to bodyStats.toMeasurementsMap(),
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )

                        firestore.collection("users")
                            .document(userId)
                            .collection("bodyStats")
                            .document(documentId)
                            .set(data)
                            .await()

                        // Actualizar con Firebase ID
                        bodyStatsDao.updateBodyStats(
                            bodyStats.copy(
                                firebaseId = documentId,
                                isSynced = true,
                                pendingAction = null
                            )
                        )
                    }

                    "UPDATE" -> {
                        if (bodyStats.firebaseId.isNotEmpty()) {
                            val updateData = mapOf(
                                "gender" to bodyStats.gender,
                                "measurements" to bodyStats.toMeasurementsMap(),
                                "updatedAt" to FieldValue.serverTimestamp()
                            )

                            firestore.collection("users")
                                .document(userId)
                                .collection("bodyStats")
                                .document(bodyStats.firebaseId)
                                .update(updateData)
                                .await()

                            bodyStatsDao.updateBodyStats(
                                bodyStats.copy(
                                    isSynced = true,
                                    pendingAction = null
                                )
                            )
                        }
                    }

                    "DELETE" -> {
                        if (bodyStats.firebaseId.isNotEmpty()) {
                            firestore.collection("users")
                                .document(userId)
                                .collection("bodyStats")
                                .document(bodyStats.firebaseId)
                                .delete()
                                .await()

                            // Eliminar localmente
                            bodyStatsDao.deleteBodyStatsById(bodyStats.id)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error sincronizando medida ${bodyStats.id}", e)
                // Quedará pendiente para próximo intento
            }
        }

        // Limpiar registros eliminados ya sincronizados
        bodyStatsDao.cleanupDeletedRecords()
    }

    private fun generateUniqueId(): String {
        val timestamp = System.currentTimeMillis()
        val random = (1000..9999).random()
        return "measurement_${timestamp}_$random"
    }
}