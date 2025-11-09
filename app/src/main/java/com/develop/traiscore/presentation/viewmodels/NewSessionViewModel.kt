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

    private val _hasSessionsLoaded = mutableStateOf(false)
    val hasSessionsLoaded: State<Boolean> = _hasSessionsLoaded

    // NUEVO: Estado de sincronización
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    init {
        // Verificar sesión activa al inicializar
        checkForActiveSession()
        // NUEVO: Cargar sesiones disponibles al iniciar
        loadAvailableSessions()
        // NUEVO: Sincronizar en background al iniciar
        syncInBackground()
    }

    // NUEVO: Función para sincronizar en background
    private fun syncInBackground() {
        viewModelScope.launch {
            try {
                _isSyncing.value = true
                sessionRepository.syncPendingSessions()
                // Recargar sesiones después de sincronizar
                loadAvailableSessions()
            } catch (e: Exception) {
                println("Error sincronizando en background: ${e.message}")
            } finally {
                _isSyncing.value = false
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
                    // Recargar lista inmediatamente (ahora es desde local, más rápido)
                    loadAvailableSessions()
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
                _isLoading.value = true // NUEVO: Mostrar loading

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

                    // NUEVO: Recargar sesiones para actualizar UI
                    loadAvailableSessions()

                    println("✅ Sesión activada: ${session.name}")
                } else {
                    _error.value = response.error ?: "Error al activar sesión"
                }
            } catch (e: Exception) {
                _error.value = "Error al activar sesión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val response = sessionRepository.deleteSession(sessionId)

                if (response.success) {
                    // Recargar la lista de sesiones disponibles (ahora desde local)
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
     * MODIFICADO: Ahora carga desde local y sincroniza en background
     */
    fun loadAvailableSessions() {
        viewModelScope.launch {
            try {
                println("🔍 Sincronizando y cargando sesiones disponibles...")
                sessionRepository.syncPendingSessions() // 🔁 fuerza sincronización desde Firebase

                val sessions = sessionRepository.getUserAvailableSessions()
                _availableSessions.value = sessions
                _hasSessionsLoaded.value = true

                println("✅ ${sessions.size} sesiones sincronizadas y cargadas")
            } catch (e: Exception) {
                println("❌ Error al cargar sesiones: ${e.message}")
                _error.value = "Error al cargar sesiones: ${e.message}"
            }
        }
    }

    /**
     * Verificar si hay una sesión activa al iniciar
     * Sin cambios necesarios - funciona igual
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
     * Terminar la sesión actual
     * MODIFICADO: Actualiza estado local inmediatamente
     */
    fun endCurrentSession() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val response = sessionRepository.endActiveSession()

                if (response.success) {
                    _activeSession.value = null
                    _hasActiveSession.value = false
                    _error.value = null

                    // ✅ Recargar de Firebase, no solo local
                    sessionRepository.syncPendingSessions()
                    loadAvailableSessions()

                    println("✅ Sesión terminada exitosamente y sincronizada con Firebase")
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


    private fun hexToColor(hex: String): Color {
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: Exception) {
            Color.Cyan
        }
    }

    private fun colorToHex(color: Color): String {
        val red = (color.red * 255).toInt()
        val green = (color.green * 255).toInt()
        val blue = (color.blue * 255).toInt()
        return String.format("#%02X%02X%02X", red, green, blue)
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


    fun clearError() {
        _error.value = null
    }

    private fun generateSessionId(): String {
        return "session_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}