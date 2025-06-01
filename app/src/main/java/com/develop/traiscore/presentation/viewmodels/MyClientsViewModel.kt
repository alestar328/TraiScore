package com.develop.traiscore.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.develop.traiscore.data.local.entity.UserEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

    init {
        loadClients()
    }

    fun loadClients() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val currentTrainerId = auth.currentUser?.uid
                if (currentTrainerId == null) {
                    _error.value = "No se pudo identificar al entrenador"
                    _isLoading.value = false
                    return@launch
                }

                android.util.Log.d("MyClientsVM", "Cargando clientes para trainer: $currentTrainerId")

                val snapshot = firestore.collection("users")
                    .whereEqualTo("linkedTrainerUid", currentTrainerId)
                    .whereEqualTo("userRole", "CLIENT")
                   /* .whereEqualTo("isActive", true)*/
                    .get()
                    .await()

                android.util.Log.d("MyClientsVM", "Documentos encontrados: ${snapshot.size()}")

                val clientList = snapshot.documents.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        android.util.Log.d("MyClientsVM", "Cliente: ${doc.id} - ${data}")
                        UserEntity.fromFirestore(data, doc.id)
                    } catch (e: Exception) {
                        android.util.Log.e("MyClientsVM", "Error parseando cliente ${doc.id}", e)
                        null
                    }
                }.sortedBy { it.getFullName() }

                android.util.Log.d("MyClientsVM", "Clientes cargados: ${clientList.size}")
                _clients.value = clientList
                _isLoading.value = false

            } catch (e: Exception) {
                android.util.Log.e("MyClientsVM", "Error cargando clientes", e)
                _error.value = "Error al cargar clientes: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun refreshClients() {
        loadClients()
    }
}