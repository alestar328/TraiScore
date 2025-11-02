package com.develop.traiscore.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.develop.traiscore.data.firebaseData.CreateSessionRequest
import com.develop.traiscore.data.firebaseData.SessionDocument
import com.develop.traiscore.data.firebaseData.SessionResponse
import com.develop.traiscore.data.local.dao.SessionDao
import com.develop.traiscore.data.local.entity.SessionEntity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository @Inject constructor(
    private val sessionDao: SessionDao,
    @ApplicationContext private val context: Context
) {

    private val firestore = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()


    /**
     * Crear una nueva sesión
     */
    suspend fun createSession(request: CreateSessionRequest): SessionResponse {
        return try {
            val sessionId = "session_${System.currentTimeMillis()}_${(1000..9999).random()}"
            val currentTime = Date()
            val userId = auth.currentUser?.uid ?: return SessionResponse(
                success = false,
                error = "Usuario no autenticado"
            )

            // 1. Guardar localmente PRIMERO
            val localSession = SessionEntity(
                sessionId = sessionId,
                name = request.name,
                color = request.color,
                userId = userId,
                createdAt = currentTime,
                isActive = true,
                isSynced = false,
                pendingAction = "CREATE"
            )

            // Desactivar otras sesiones localmente
            sessionDao.deactivateAllSessions(userId)
            sessionDao.insertSession(localSession)

            // 2. Intentar sincronizar con Firebase
            if (isNetworkAvailable()) {
                try {
                    // Desactivar todas las sesiones en Firebase
                    deactivateAllSessions(userId)

                    val sessionData = hashMapOf(
                        "sessionId" to sessionId,
                        "name" to request.name,
                        "color" to request.color,
                        "userId" to userId,
                        "createdAt" to currentTime,
                        "updatedAt" to currentTime,
                        "isActive" to true,
                        "workoutCount" to 0
                    )

                    firestore.collection("users")
                        .document(userId)
                        .collection("sessions")
                        .document(sessionId)
                        .set(sessionData)
                        .await()

                    sessionDao.markAsSynced(sessionId)

                } catch (e: Exception) {
                    println("Se guardará localmente y sincronizará después: ${e.message}")
                }
            }

            // Retornar éxito con SessionDocument
            SessionResponse(
                success = true,
                sessionId = sessionId,
                session = SessionDocument(
                    sessionId = sessionId,
                    name = request.name,
                    color = request.color,
                    createdAt = currentTime,
                    updatedAt = currentTime,
                    isActive = true,
                    userId = userId,
                    workoutCount = 0
                )
            )

        } catch (e: Exception) {
            SessionResponse(success = false, error = e.message)
        }
    }


    suspend fun getUserAvailableSessions(): List<Map<String, Any>> {
        return try {
            val userId = auth.currentUser?.uid ?: return emptyList()

            // Primero obtener de la base de datos local
            val localSessions = sessionDao.getAvailableSessionsList(userId)

            // Si no hay datos locales y hay red, sincronizar desde Firebase
            if (localSessions.isEmpty() && isNetworkAvailable()) {
                syncFromFirebase()
                // Volver a obtener después de sincronizar
                return sessionDao.getAvailableSessionsList(userId).map { session ->
                    mapOf(
                        "sessionId" to session.sessionId,
                        "name" to session.name,
                        "color" to session.color,
                        "createdAt" to session.createdAt,
                        "isActive" to session.isActive
                    )
                }
            }

            // Sincronizar en background si hay red (no bloquear)
            if (isNetworkAvailable()) {
                syncPendingSessions()
            }

            // Convertir a formato esperado
            localSessions.map { session ->
                mapOf(
                    "sessionId" to session.sessionId,
                    "name" to session.name,
                    "color" to session.color,
                    "createdAt" to session.createdAt,
                    "isActive" to session.isActive
                )
            }

        } catch (e: Exception) {
            println("❌ Error obteniendo sesiones disponibles: ${e.message}")
            emptyList()
        }
    }

    suspend fun createInactiveSession(request: CreateSessionRequest): SessionResponse {
        return try {
            val sessionId = "session_${System.currentTimeMillis()}_${(1000..9999).random()}"
            val currentTime = Date()
            val userId = auth.currentUser?.uid ?: return SessionResponse(
                success = false,
                error = "Usuario no autenticado"
            )

            // 1. Guardar localmente
            val localSession = SessionEntity(
                sessionId = sessionId,
                name = request.name,
                color = request.color,
                userId = userId,
                createdAt = currentTime,
                isActive = false,
                isSynced = false,
                pendingAction = "CREATE"
            )

            sessionDao.insertSession(localSession)

            // 2. Sincronizar con Firebase si hay red
            if (isNetworkAvailable()) {
                try {
                    val sessionData = hashMapOf(
                        "sessionId" to sessionId,
                        "name" to request.name,
                        "color" to request.color,
                        "userId" to userId,
                        "createdAt" to currentTime,
                        "updatedAt" to currentTime,
                        "isActive" to false,
                        "workoutCount" to 0
                    )

                    firestore.collection("users")
                        .document(userId)
                        .collection("sessions")
                        .document(sessionId)
                        .set(sessionData)
                        .await()

                    sessionDao.markAsSynced(sessionId)
                } catch (e: Exception) {
                    println("Error sincronizando: ${e.message}")
                }
            }

            SessionResponse(
                success = true,
                sessionId = sessionId,
                session = SessionDocument(
                    sessionId = sessionId,
                    name = request.name,
                    color = request.color,
                    createdAt = currentTime,
                    updatedAt = currentTime,
                    isActive = false,
                    userId = userId,
                    workoutCount = 0
                )
            )

        } catch (e: Exception) {
            SessionResponse(success = false, error = e.message)
        }
    }


    suspend fun activateSession(sessionId: String): SessionResponse {
        return try {
            val userId = auth.currentUser?.uid ?: return SessionResponse(
                success = false,
                error = "Usuario no autenticado"
            )

            // 1. Actualizar localmente
            sessionDao.deactivateAllSessions(userId)
            sessionDao.activateSession(sessionId, System.currentTimeMillis())

            val session = sessionDao.getSessionById(sessionId)

            // 2. Sincronizar con Firebase si hay red
            if (isNetworkAvailable()) {
                try {
                    // Desactivar todas las sesiones en Firebase
                    deactivateAllSessions(userId)

                    // Activar la sesión seleccionada
                    firestore.collection("users")
                        .document(userId)
                        .collection("sessions")
                        .document(sessionId)
                        .update(mapOf("isActive" to true, "updatedAt" to Date()))
                        .await()

                    sessionDao.markAsSynced(sessionId)
                } catch (e: Exception) {
                    // Marcar para sincronización posterior
                    session?.let {
                        sessionDao.updateSession(
                            it.copy(pendingAction = "UPDATE", isSynced = false)
                        )
                    }
                }
            }

            session?.let {
                SessionResponse(
                    success = true,
                    session = SessionDocument(
                        sessionId = it.sessionId,
                        name = it.name,
                        color = it.color,
                        createdAt = it.createdAt,
                        updatedAt = Date(),
                        isActive = true,
                        userId = it.userId,
                        workoutCount = 0
                    )
                )
            } ?: SessionResponse(success = false, error = "Sesión no encontrada")

        } catch (e: Exception) {
            SessionResponse(success = false, error = e.message)
        }
    }


    suspend fun getUserFinishedSessions(): Result<List<Map<String, Any>>> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(
                Exception("Usuario no autenticado")
            )

            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("sessions")  // ✅ CAMBIO: usar subcolección del usuario
                .whereEqualTo("isActive", false)
                .get()
                .await()

            val sessions = snapshot.documents.mapNotNull { doc ->
                doc.data
            }

            Result.success(sessions)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    /**
     * Obtener la sesión activa del usuario
     */
    suspend fun getActiveSession(): SessionResponse {
        return try {
            val userId = auth.currentUser?.uid ?: return SessionResponse(
                success = false,
                error = "Usuario no autenticado"
            )

            // Obtener de base de datos local primero
            val localSession = sessionDao.getActiveSession(userId)

            if (localSession != null) {
                SessionResponse(
                    success = true,
                    session = SessionDocument(
                        sessionId = localSession.sessionId,
                        name = localSession.name,
                        color = localSession.color,
                        createdAt = localSession.createdAt,
                        updatedAt = localSession.createdAt, // Usar createdAt si no tienes updatedAt
                        isActive = localSession.isActive,
                        userId = localSession.userId,
                        workoutCount = 0 // Tendrías que agregar este campo a SessionEntity si lo necesitas
                    )
                )
            } else {
                // Si no hay sesión local y hay red, intentar obtener de Firebase
                if (isNetworkAvailable()) {
                    val snapshot = firestore.collection("users")
                        .document(userId)
                        .collection("sessions")
                        .whereEqualTo("isActive", true)
                        .limit(1)
                        .get()
                        .await()

                    if (snapshot.documents.isNotEmpty()) {
                        val doc = snapshot.documents.first()
                        val session = SessionDocument(
                            sessionId = doc.getString("sessionId") ?: "",
                            name = doc.getString("name") ?: "",
                            color = doc.getString("color") ?: "#43f4ff",
                            createdAt = doc.getDate("createdAt") ?: Date(),
                            updatedAt = doc.getDate("updatedAt") ?: Date(),
                            isActive = doc.getBoolean("isActive") ?: false,
                            userId = doc.getString("userId") ?: "",
                            workoutCount = doc.getLong("workoutCount")?.toInt() ?: 0
                        )

                        // Guardar en local para futuro acceso offline
                        sessionDao.insertSession(
                            SessionEntity(
                                sessionId = session.sessionId,
                                name = session.name,
                                color = session.color,
                                userId = session.userId,
                                createdAt = session.createdAt,
                                isActive = session.isActive,
                                isSynced = true
                            )
                        )

                        SessionResponse(success = true, session = session)
                    } else {
                        SessionResponse(success = false, error = "No hay sesión activa")
                    }
                } else {
                    SessionResponse(success = false, error = "No hay sesión activa")
                }
            }

        } catch (e: Exception) {
            SessionResponse(success = false, error = "Error al obtener sesión activa: ${e.message}")
        }
    }

    /**
     * Terminar la sesión activa
     */
    suspend fun endActiveSession(): SessionResponse {
        return try {
            val userId = auth.currentUser?.uid ?: return SessionResponse(
                success = false,
                error = "Usuario no autenticado"
            )

            val activeSession = sessionDao.getActiveSession(userId)

            activeSession?.let { session ->
                // 1. Actualizar localmente
                sessionDao.endSession(session.sessionId, Date())

                // 2. Sincronizar con Firebase si hay red
                if (isNetworkAvailable()) {
                    try {
                        firestore.collection("users")
                            .document(userId)
                            .collection("sessions")
                            .document(session.sessionId)
                            .update(mapOf(
                                "isActive" to false,
                                "updatedAt" to Date()
                            ))
                            .await()

                        sessionDao.markAsSynced(session.sessionId)
                    } catch (e: Exception) {
                        sessionDao.updateSession(
                            session.copy(pendingAction = "UPDATE", isSynced = false)
                        )
                    }
                }

                SessionResponse(
                    success = true,
                    session = SessionDocument(
                        sessionId = session.sessionId,
                        name = session.name,
                        color = session.color,
                        createdAt = session.createdAt,
                        updatedAt = Date(),
                        isActive = false,
                        userId = session.userId,
                        workoutCount = 0
                    )
                )
            } ?: SessionResponse(success = false, error = "No hay sesión activa para terminar")

        } catch (e: Exception) {
            SessionResponse(success = false, error = "Error al terminar sesión: ${e.message}")
        }
    }

    /**
     * Incrementar contador de workouts de una sesión
     */
    suspend fun incrementWorkoutCount(sessionId: String): SessionResponse {
        return try {
            val userId = auth.currentUser?.uid ?: return SessionResponse(
                success = false,
                error = "Usuario no autenticado"
            )

            firestore.collection("users")
                .document(userId)
                .collection("sessions")
                .document(sessionId)
                .update(
                    mapOf(
                        "workoutCount" to com.google.firebase.firestore.FieldValue.increment(1),
                        "updatedAt" to Date()
                    )
                )
                .await()

            println("✅ Contador de workouts incrementado para sesión: $sessionId")

            SessionResponse(success = true)

        } catch (e: Exception) {
            println("❌ Error incrementando contador: ${e.message}")
            SessionResponse(
                success = false,
                error = "Error al incrementar contador: ${e.message}"
            )
        }
    }



    /**
     * Desactivar todas las sesiones activas del usuario
     */
    private suspend fun deactivateAllSessions(userId: String) {
        try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("sessions")
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val batch = firestore.batch()
            snapshot.documents.forEach { doc ->
                batch.update(doc.reference, mapOf("isActive" to false, "updatedAt" to Date()))
            }

            if (snapshot.documents.isNotEmpty()) {
                batch.commit().await()
                println("✅ ${snapshot.documents.size} sesiones desactivadas")
            }

        } catch (e: Exception) {
            println("❌ Error desactivando sesiones: ${e.message}")
        }
    }
    suspend fun syncPendingSessions() {
        if (!isNetworkAvailable()) return

        try {
            val userId = auth.currentUser?.uid ?: return
            val unsyncedSessions = sessionDao.getUnsyncedSessions()

            unsyncedSessions.forEach { session ->
                try {
                    when (session.pendingAction) {
                        "CREATE" -> {
                            val sessionData = hashMapOf(
                                "sessionId" to session.sessionId,
                                "name" to session.name,
                                "color" to session.color,
                                "userId" to session.userId,
                                "createdAt" to session.createdAt,
                                "updatedAt" to session.createdAt,
                                "isActive" to session.isActive,
                                "workoutCount" to 0
                            )

                            firestore.collection("users")
                                .document(userId)
                                .collection("sessions")
                                .document(session.sessionId)
                                .set(sessionData)
                                .await()
                        }
                        "UPDATE" -> {
                            val updates = hashMapOf<String, Any>(
                                "isActive" to session.isActive,
                                "updatedAt" to Date()
                            )

                            session.endedAt?.let {
                                updates["endedAt"] = it
                            }

                            firestore.collection("users")
                                .document(userId)
                                .collection("sessions")
                                .document(session.sessionId)
                                .update(updates)
                                .await()
                        }
                        "DELETE" -> {
                            firestore.collection("users")
                                .document(userId)
                                .collection("sessions")
                                .document(session.sessionId)
                                .delete()
                                .await()

                            // Eliminar localmente después de sincronizar
                            sessionDao.deleteSession(session)
                        }
                    }

                    sessionDao.markAsSynced(session.sessionId)

                } catch (e: Exception) {
                    println("Error sincronizando sesión ${session.sessionId}: ${e.message}")
                }
            }
        } catch (e: Exception) {
            println("Error en sincronización general: ${e.message}")
        }
    }

    /**
     * Sincronizar desde Firebase a local (para recuperar datos)
     */
    private suspend fun syncFromFirebase() {
        try {
            val userId = auth.currentUser?.uid ?: return

            val firebaseSessions = firestore.collection("users")
                .document(userId)
                .collection("sessions")
                .get()
                .await()

            firebaseSessions.documents.forEach { doc ->
                val sessionEntity = SessionEntity(
                    sessionId = doc.getString("sessionId") ?: doc.id,
                    name = doc.getString("name") ?: "",
                    color = doc.getString("color") ?: "#355E58",
                    userId = userId,
                    createdAt = doc.getDate("createdAt") ?: Date(),
                    isActive = doc.getBoolean("isActive") ?: false,
                    isFinished = false, // Ajusta según tu lógica
                    endedAt = doc.getDate("endedAt"),
                    isSynced = true,
                    lastModified = System.currentTimeMillis()
                )

                sessionDao.insertSession(sessionEntity)
            }
        } catch (e: Exception) {
            println("Error sincronizando desde Firebase: ${e.message}")
        }
    }

    /**
     * Verificar conexión a internet
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun deleteSession(sessionId: String): SessionResponse {
        return try {
            val session = sessionDao.getSessionById(sessionId)

            session?.let {
                if (isNetworkAvailable()) {
                    // Si hay red, eliminar de ambos lados
                    sessionDao.deleteSession(it)

                    try {
                        firestore.collection("users")
                            .document(it.userId)
                            .collection("sessions")
                            .document(sessionId)
                            .delete()
                            .await()
                    } catch (e: Exception) {
                        // Si falla Firebase, restaurar localmente con marca de eliminación
                        sessionDao.insertSession(
                            it.copy(pendingAction = "DELETE", isSynced = false)
                        )
                    }
                } else {
                    // Sin red, marcar para eliminar después
                    sessionDao.updateSession(
                        it.copy(pendingAction = "DELETE", isSynced = false)
                    )
                }

                SessionResponse(success = true)
            } ?: SessionResponse(success = false, error = "Sesión no encontrada")

        } catch (e: Exception) {
            SessionResponse(success = false, error = "Error al eliminar sesión: ${e.message}")
        }
    }
}