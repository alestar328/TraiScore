package com.develop.traiscore.presentation.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.develop.traiscore.data.firebaseData.CreateSessionRequest
import com.develop.traiscore.data.repository.SessionRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
    private val sessionRepository: SessionRepository
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

    private val _finishedSessions = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val finishedSessions: StateFlow<List<Map<String, Any>>> = _finishedSessions.asStateFlow()

    private val _availableSessions = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val availableSessions: StateFlow<List<Map<String, Any>>> = _availableSessions.asStateFlow()
    private val _hasSessionsLoaded = mutableStateOf(false) // ⭐ NUEVO para tracking
    val hasSessionsLoaded: State<Boolean> = _hasSessionsLoaded


    init {
        // Verificar sesión activa al inicializar
        checkForActiveSession()
    }
    fun preloadSessions() {
        if (!_hasSessionsLoaded.value) {
            loadAvailableSessions()
        }
    }
    /**
     * Crear una nueva sesión
     */
    fun createSession(name: String, color: Color) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                // Validaciones
                if (name.isBlank()) {
                    _error.value = "El nombre de la sesión no puede estar vacío"
                    return@launch
                }

                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId == null) {
                    _error.value = "Usuario no autenticado"
                    return@launch
                }

                // Crear sesión en Firebase
                val createRequest = CreateSessionRequest(
                    name = name.trim(),
                    color = colorToHex(color),
                    userId = userId
                )

                val response = sessionRepository.createSession(createRequest)

                if (response.success && response.session != null) {
                    // Actualizar estado local
                    val session = response.session
                    _activeSession.value = ActiveSession(
                        id = session.sessionId,
                        name = session.name,
                        color = hexToColor(session.color),
                        createdAt = session.createdAt.time,
                        isActive = session.isActive
                    )
                    _hasActiveSession.value = true

                    println("✅ Sesión creada exitosamente: ${session.name}")

                } else {
                    _error.value = response.error ?: "Error desconocido al crear sesión"
                    println("❌ Error creando sesión: ${response.error}")
                }

            } catch (e: Exception) {
                _error.value = "Error al crear la sesión: ${e.message}"
                println("❌ Excepción creando sesión: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun createInactiveSession(name: String, color: Color) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val createRequest = CreateSessionRequest(
                    name = name.trim(),
                    color = colorToHex(color),
                    userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                )

                val response = sessionRepository.createInactiveSession(createRequest)

                if (response.success) {
                    loadAvailableSessions() // Recargar lista
                    println("✅ SessionCard creada: ${response.session?.name}")
                } else {
                    _error.value = response.error ?: "Error al crear sesión"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun activateSession(sessionId: String) {
        viewModelScope.launch {
            try {
                val response = sessionRepository.activateSession(sessionId)

                if (response.success && response.session != null) {
                    val session = response.session
                    _activeSession.value = ActiveSession(
                        id = session.sessionId,
                        name = session.name,
                        color = hexToColor(session.color),
                        createdAt = session.createdAt.time,
                        isActive = true
                    )
                    _hasActiveSession.value = true

                    println("✅ Sesión activada: ${session.name}")
                }
            } catch (e: Exception) {
                _error.value = "Error al activar sesión: ${e.message}"
            }
        }
    }
    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val response = sessionRepository.deleteSession(sessionId)

                if (response.success) {
                    // Recargar la lista de sesiones disponibles
                    loadAvailableSessions()
                    println("✅ Sesión eliminada correctamente")
                } else {
                    _error.value = response.error ?: "Error al eliminar sesión"
                }
            } catch (e: Exception) {
                _error.value = "Error al eliminar sesión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    /**
     * Cargar sesiones disponibles
     */
    fun loadAvailableSessions() {
        viewModelScope.launch {
            try {
                println("🔍 Iniciando carga de sesiones disponibles...")
                val sessions = sessionRepository.getUserAvailableSessions()
                println("🔍 Sesiones obtenidas: ${sessions.size}")
                sessions.forEach { session ->
                    println("🔍 Sesión: ${session["name"]} - ${session["sessionId"]} - Active: ${session["isActive"]}")
                }
                _availableSessions.value = sessions
                println("🔍 Estado actualizado con ${sessions.size} sesiones")
            } catch (e: Exception) {
                println("❌ Error al cargar sesiones viewmodel: ${e.message}")
                _error.value = "Error al cargar sesiones: ${e.message}"
            }
        }
    }

    private fun hexToColor(hex: String): Color {
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: Exception) {
            Color.Cyan // Color por defecto
        }
    }
    fun getSessionDataForWorkout(): Triple<String, String, String>? {
        val session = _activeSession.value
        return if (session != null && session.isActive) {
            Triple(
                session.id,
                session.name,
                colorToHex(session.color)
            )
        } else {
            null
        }
    }
    fun loadFinishedSessions() {
        viewModelScope.launch {
            try {
                val result = sessionRepository.getUserFinishedSessions()
                result.onSuccess { sessions ->
                    _finishedSessions.value = sessions
                }.onFailure { error ->
                    _error.value = error.message ?: "Error al cargar sesiones"
                }
            } catch (e: Exception) {
                _error.value = "Error al cargar sesiones: ${e.message}"
            }
        }
    }

    private fun colorToHex(color: Color): String {
        val red = (color.red * 255).toInt()
        val green = (color.green * 255).toInt()
        val blue = (color.blue * 255).toInt()
        return String.format("#%02X%02X%02X", red, green, blue)
    }
    /**
     * Terminar la sesión actual
     */
    fun endCurrentSession() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val response = sessionRepository.endActiveSession()

                if (response.success) {
                    // Limpiar estados locales
                    _activeSession.value = null
                    _hasActiveSession.value = false
                    _error.value = null

                    // Recargar sesiones disponibles (no terminadas)
                    loadAvailableSessions()

                    println("✅ Sesión terminada exitosamente")
                } else {
                    _error.value = response.error ?: "Error al terminar sesión"
                }
            } catch (e: Exception) {
                _error.value = "Error al terminar la sesión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
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
        viewModelScope.launch {
            try {
                val response = sessionRepository.getActiveSession()

                if (response.success && response.session != null) {
                    val session = response.session
                    _activeSession.value = ActiveSession(
                        id = session.sessionId,
                        name = session.name,
                        color = hexToColor(session.color),
                        createdAt = session.createdAt.time,
                        isActive = session.isActive
                    )
                    _hasActiveSession.value = true

                    println("✅ Sesión activa encontrada: ${session.name}")

                } else {
                    // No hay sesión activa
                    _activeSession.value = null
                    _hasActiveSession.value = false

                    println("ℹ️ No hay sesión activa")
                }

            } catch (e: Exception) {
                _error.value = "Error al verificar sesión activa: ${e.message}"
                println("❌ Error verificando sesión activa: ${e.message}")
            }
        }
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