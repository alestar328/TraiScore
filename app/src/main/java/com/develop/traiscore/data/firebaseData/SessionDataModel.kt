package com.develop.traiscore.data.firebaseData

import java.util.Date

data class SessionDocument(
    val sessionId: String = "",
    val name: String = "",
    val color: String = "",              // Color en formato hex (#FF5722)
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val isActive: Boolean = false,       // Solo una sesión puede estar activa por usuario
    val userId: String = "",             // ID del usuario propietario
    val workoutCount: Int = 0            // Contador de ejercicios en la sesión
)

/**
 * Modelo simplificado para crear nuevas sesiones
 */
data class CreateSessionRequest(
    val name: String,
    val color: String,
    val userId: String
)

/**
 * Modelo para respuestas del repositorio
 */
data class SessionResponse(
    val success: Boolean,
    val sessionId: String? = null,
    val session: SessionDocument? = null,
    val error: String? = null
)