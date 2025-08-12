package com.develop.traiscore.presentation.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class ActiveSession(
    val id: String = "",
    val name: String = "",
    val color: Color = Color.Cyan,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

@HiltViewModel
class NewSessionViewModel @Inject constructor(
    // Aquí agregaremos dependencias como repositorio cuando implementemos persistencia
) : ViewModel() {

    // Estado de la sesión activa
    private val _activeSession = MutableStateFlow<ActiveSession?>(null)
    val activeSession: StateFlow<ActiveSession?> = _activeSession.asStateFlow()

    // Estado para controlar si hay una sesión en progreso
    private val _hasActiveSession = MutableStateFlow(false)
    val hasActiveSession: StateFlow<Boolean> = _hasActiveSession.asStateFlow()

    // Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Estado de error
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Crear una nueva sesión
     */
    fun createSession(name: String, color: Color) {
        try {
            _isLoading.value = true
            _error.value = null

            // Validaciones
            if (name.isBlank()) {
                _error.value = "El nombre de la sesión no puede estar vacío"
                return
            }

            // Si ya hay una sesión activa, terminarla primero
            if (_hasActiveSession.value) {
                endCurrentSession()
            }

            // Crear nueva sesión
            val newSession = ActiveSession(
                id = generateSessionId(),
                name = name.trim(),
                color = color,
                createdAt = System.currentTimeMillis(),
                isActive = true
            )

            // Actualizar estados
            _activeSession.value = newSession
            _hasActiveSession.value = true

            println("✅ Sesión creada: ${newSession.name} con color $color")

        } catch (e: Exception) {
            _error.value = "Error al crear la sesión: ${e.message}"
            println("❌ Error creando sesión: ${e.message}")
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Terminar la sesión actual
     */
    fun endCurrentSession() {
        try {
            val currentSession = _activeSession.value
            if (currentSession != null) {
                println("🔚 Terminando sesión: ${currentSession.name}")

                // Aquí se implementará la lógica de guardado/persistencia
                // saveSessionToDatabase(currentSession.copy(isActive = false))
            }

            // Limpiar estados
            _activeSession.value = null
            _hasActiveSession.value = false
            _error.value = null

        } catch (e: Exception) {
            _error.value = "Error al terminar la sesión: ${e.message}"
            println("❌ Error terminando sesión: ${e.message}")
        }
    }

    /**
     * Limpiar errores
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Verificar si hay una sesión activa al iniciar
     */
    fun checkForActiveSession() {
        // Aquí implementaremos la verificación desde la base de datos
        // Por ahora, simulamos que no hay sesión activa
        _hasActiveSession.value = false
        _activeSession.value = null
    }

    /**
     * Generar ID único para la sesión
     */
    private fun generateSessionId(): String {
        return "session_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }

    /**
     * Obtener información de la sesión actual
     */
    fun getCurrentSessionInfo(): Triple<String, Color, Boolean> {
        val session = _activeSession.value
        return if (session != null) {
            Triple(session.name, session.color, session.isActive)
        } else {
            Triple("", Color.Cyan, false)
        }
    }
}