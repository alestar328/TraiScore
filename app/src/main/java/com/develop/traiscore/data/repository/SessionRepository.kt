package com.develop.traiscore.data.repository

import com.develop.traiscore.data.firebaseData.CreateSessionRequest
import com.develop.traiscore.data.firebaseData.SessionDocument
import com.develop.traiscore.data.firebaseData.SessionResponse
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository @Inject constructor() {

    private val firestore = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    /**
     * Crear una nueva sesión
     */
    suspend fun createSession(request: CreateSessionRequest): SessionResponse {
        return try {
            val userId = auth.currentUser?.uid ?: return SessionResponse(
                success = false,
                error = "Usuario no autenticado"
            )

            // Primero, desactivar cualquier sesión activa existente
            deactivateAllSessions(userId)

            // Generar ID único para la sesión
            val sessionId = generateSessionId()
            val now = Date()

            val sessionData = mapOf(
                "sessionId" to sessionId,
                "name" to request.name,
                "color" to request.color,
                "createdAt" to now,
                "updatedAt" to now,
                "isActive" to true,  // Nueva sesión siempre activa
                "userId" to userId,
                "workoutCount" to 0
            )

            // Guardar en Firestore
            firestore.collection("users")
                .document(userId)
                .collection("sessions")
                .document(sessionId)
                .set(sessionData)
                .await()

            println("✅ Sesión creada: $sessionId")

            SessionResponse(
                success = true,
                sessionId = sessionId,
                session = SessionDocument(
                    sessionId = sessionId,
                    name = request.name,
                    color = request.color,
                    createdAt = now,
                    updatedAt = now,
                    isActive = true,
                    userId = userId,
                    workoutCount = 0
                )
            )

        } catch (e: Exception) {
            println("❌ Error creando sesión: ${e.message}")
            SessionResponse(
                success = false,
                error = "Error al crear sesión: ${e.message}"
            )
        }
    }
    suspend fun getUserAvailableSessions(): List<Map<String, Any>> {
        return try {
            val userId = auth.currentUser?.uid ?: return emptyList()

            println("🔍 Buscando sesiones para usuario: $userId")

            // CAMBIO: Quitar el orderBy que está causando problemas con índices
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("sessions")
                .whereEqualTo("isActive", false)
                // .orderBy("createdAt") // COMENTAR esta línea temporalmente
                .get()
                .await()

            println("🔍 Documentos encontrados: ${snapshot.documents.size}")

            val sessions = snapshot.documents.mapNotNull { doc ->
                println("🔍 Documento: ${doc.id} - ${doc.data}")
                doc.data
            }

            println("✅ Sesiones disponibles encontradas: ${sessions.size}")
            return sessions

        } catch (e: Exception) {
            println("❌ Error obteniendo sesiones disponibles repository: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun createInactiveSession(request: CreateSessionRequest): SessionResponse {
        return try {
            val userId = auth.currentUser?.uid ?: return SessionResponse(
                success = false,
                error = "Usuario no autenticado"
            )

            // Verificar límite de 6 sesiones
            val existingSessions = getUserAvailableSessions()
            if (existingSessions.size >= 6) {
                return SessionResponse(
                    success = false,
                    error = "Has alcanzado el límite de 6 sesiones"
                )
            }

            val sessionId = generateSessionId()
            val now = Date()

            val sessionData = mapOf(
                "sessionId" to sessionId,
                "name" to request.name,
                "color" to request.color,
                "createdAt" to now,
                "updatedAt" to now,
                "isActive" to false,  // INACTIVA por defecto
                "userId" to userId,
                "workoutCount" to 0
            )

            firestore.collection("users")
                .document(userId)
                .collection("sessions")
                .document(sessionId)
                .set(sessionData)
                .await()

            SessionResponse(
                success = true,
                sessionId = sessionId,
                session = SessionDocument(
                    sessionId = sessionId,
                    name = request.name,
                    color = request.color,
                    createdAt = now,
                    updatedAt = now,
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

            // Desactivar todas las sesiones primero
            deactivateAllSessions(userId)

            // Activar la sesión seleccionada
            firestore.collection("users")
                .document(userId)
                .collection("sessions")
                .document(sessionId)
                .update(mapOf("isActive" to true, "updatedAt" to Date()))
                .await()

            // Obtener la sesión actualizada
            val doc = firestore.collection("users")
                .document(userId)
                .collection("sessions")
                .document(sessionId)
                .get()
                .await()

            val session = SessionDocument(
                sessionId = doc.getString("sessionId") ?: "",
                name = doc.getString("name") ?: "",
                color = doc.getString("color") ?: "#43f4ff",
                createdAt = doc.getDate("createdAt") ?: Date(),
                updatedAt = doc.getDate("updatedAt") ?: Date(),
                isActive = true,
                userId = userId,
                workoutCount = doc.getLong("workoutCount")?.toInt() ?: 0
            )

            println("✅ Sesión activada: ${session.name}")
            SessionResponse(success = true, session = session)

        } catch (e: Exception) {
            println("❌ Error activando sesión: ${e.message}")
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

                SessionResponse(
                    success = true,
                    session = session
                )
            } else {
                SessionResponse(
                    success = false,
                    error = "No hay sesión activa"
                )
            }

        } catch (e: Exception) {
            println("❌ Error obteniendo sesión activa: ${e.message}")
            SessionResponse(
                success = false,
                error = "Error al obtener sesión activa: ${e.message}"
            )
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

            // Obtener sesión activa
            val activeSessionResponse = getActiveSession()
            if (!activeSessionResponse.success || activeSessionResponse.session == null) {
                return SessionResponse(
                    success = false,
                    error = "No hay sesión activa para terminar"
                )
            }

            val session = activeSessionResponse.session
            val updateData = mapOf(
                "isActive" to false,
                "updatedAt" to Date()
            )

            firestore.collection("users")
                .document(userId)
                .collection("sessions")
                .document(session.sessionId)
                .update(updateData)
                .await()

            println("✅ Sesión terminada: ${session.sessionId}")

            SessionResponse(
                success = true,
                session = session.copy(isActive = false, updatedAt = Date())
            )

        } catch (e: Exception) {
            println("❌ Error terminando sesión: ${e.message}")
            SessionResponse(
                success = false,
                error = "Error al terminar sesión: ${e.message}"
            )
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
     * Obtener todas las sesiones del usuario
     */
    suspend fun getAllSessions(): List<SessionDocument> {
        return try {
            val userId = auth.currentUser?.uid ?: return emptyList()

            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("sessions")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    SessionDocument(
                        sessionId = doc.getString("sessionId") ?: return@mapNotNull null,
                        name = doc.getString("name") ?: return@mapNotNull null,
                        color = doc.getString("color") ?: "#43f4ff",
                        createdAt = doc.getDate("createdAt") ?: Date(),
                        updatedAt = doc.getDate("updatedAt") ?: Date(),
                        isActive = doc.getBoolean("isActive") ?: false,
                        userId = doc.getString("userId") ?: "",
                        workoutCount = doc.getLong("workoutCount")?.toInt() ?: 0
                    )
                } catch (e: Exception) {
                    println("❌ Error parseando sesión: ${e.message}")
                    null
                }
            }

        } catch (e: Exception) {
            println("❌ Error obteniendo sesiones: ${e.message}")
            emptyList()
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

    /**
     * Generar ID único para sesión
     */
    private fun generateSessionId(): String {
        return "session_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}