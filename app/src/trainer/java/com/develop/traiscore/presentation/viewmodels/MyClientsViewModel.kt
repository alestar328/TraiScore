package com.develop.traiscore.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.develop.traiscore.data.local.entity.UserEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class MyClientsViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _clients = MutableStateFlow<List<UserEntity>>(emptyList())
    val clients: StateFlow<List<UserEntity>> = _clients.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Listener para actualizaciones en tiempo real
    private var clientsListener: ListenerRegistration? = null

    init {
        setupRealtimeListener()
    }

    /**
     * Configura un listener en tiempo real para detectar cambios en los clientes
     */
    private fun setupRealtimeListener() {
        val currentTrainerId = auth.currentUser?.uid
        if (currentTrainerId == null) {
            _error.value = "No se pudo identificar al entrenador"
            return
        }

        _isLoading.value = true
        _error.value = null

        android.util.Log.d("MyClientsVM", "Configurando listener para trainer: $currentTrainerId")

        // Limpiar listener anterior si existe
        clientsListener?.remove()

        // Configurar nuevo listener en tiempo real
        clientsListener = firestore.collection("users")
            .whereEqualTo("linkedTrainerUid", currentTrainerId)
            .whereEqualTo("userRole", "CLIENT")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("MyClientsVM", "Error en listener de clientes", error)
                    _error.value = "Error al cargar clientes: ${error.message}"
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    android.util.Log.d("MyClientsVM", "Listener - Documentos encontrados: ${snapshot.size()}")

                    val clientList = snapshot.documents.mapNotNull { doc ->
                        try {
                            val data = doc.data ?: return@mapNotNull null

                            // Verificar que el cliente esté realmente vinculado al trainer
                            val linkedTrainerUid = data["linkedTrainerUid"] as? String
                            if (linkedTrainerUid != currentTrainerId) {
                                android.util.Log.d("MyClientsVM", "Cliente ${doc.id} ya no está vinculado a este trainer")
                                return@mapNotNull null
                            }

                            android.util.Log.d("MyClientsVM", "Cliente activo: ${doc.id} - ${data}")
                            UserEntity.fromFirestore(data, doc.id)
                        } catch (e: Exception) {
                            android.util.Log.e("MyClientsVM", "Error parseando cliente ${doc.id}", e)
                            null
                        }
                    }.sortedBy { it.getFullName() }

                    android.util.Log.d("MyClientsVM", "Clientes actualizados en tiempo real: ${clientList.size}")
                    _clients.value = clientList
                    _isLoading.value = false
                } else {
                    _clients.value = emptyList()
                    _isLoading.value = false
                }
            }
    }

    /**
     * Función legacy para cargar clientes (ahora usa el listener en tiempo real)
     */
    fun loadClients() {
        // El listener en tiempo real ya se encarga de cargar los clientes
        // Pero podemos reiniciar el listener si es necesario
        setupRealtimeListener()
    }

    /**
     * Da de baja a un cliente eliminando su vinculación con el trainer
     */
    fun removeClient(
        clientId: String,
        onComplete: (success: Boolean, error: String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val currentTrainerId = auth.currentUser?.uid
                if (currentTrainerId == null) {
                    onComplete(false, "No se pudo identificar al entrenador")
                    return@launch
                }

                android.util.Log.d("MyClientsVM", "Dando de baja cliente: $clientId")

                // Eliminar la vinculación del cliente con el trainer
                firestore.collection("users")
                    .document(clientId)
                    .update(
                        mapOf(
                            "linkedTrainerUid" to null,
                            "isActive" to false // Opcional: marcar como inactivo
                        )
                    )
                    .await()

                android.util.Log.d("MyClientsVM", "Cliente dado de baja exitosamente: $clientId")

                // NO necesitamos actualizar manualmente _clients.value aquí
                // El listener en tiempo real se encargará automáticamente de actualizar la lista
                // cuando detecte que linkedTrainerUid cambió a null

                onComplete(true, null)

            } catch (e: Exception) {
                android.util.Log.e("MyClientsVM", "Error dando de baja cliente", e)
                onComplete(false, "Error al dar de baja cliente: ${e.message}")
            }
        }
    }

    /**
     * Actualiza la lista de clientes reiniciando el listener
     */
    fun refreshClients() {
        setupRealtimeListener()
    }

    /**
     * Limpia el listener cuando el ViewModel se destruye
     */
    override fun onCleared() {
        super.onCleared()
        clientsListener?.remove()
        android.util.Log.d("MyClientsVM", "Listener de clientes limpiado")
    }
}