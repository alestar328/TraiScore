package com.develop.traiscore.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.develop.traiscore.data.local.entity.InvitationEntity
import com.develop.traiscore.data.repository.InvitationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class InvitationViewModel @Inject constructor(
    private val invitationRepository: InvitationRepository
) : ViewModel() {

    private val _invitations = MutableStateFlow<List<InvitationEntity>>(emptyList())
    val invitations: StateFlow<List<InvitationEntity>> = _invitations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadInvitations() {
        viewModelScope.launch {
            _isLoading.value = true
            invitationRepository.getTrainerInvitations().fold(
                onSuccess = { invitationList ->
                    _invitations.value = invitationList
                    _error.value = null
                },
                onFailure = { exception ->
                    _error.value = exception.message
                }
            )
            _isLoading.value = false
        }
    }
    fun deleteInvitation(invitationId: String) {
        viewModelScope.launch {
            invitationRepository.deleteInvitation(invitationId).fold(
                onSuccess = {
                    loadInvitations() // Recargar la lista para reflejar los cambios
                },
                onFailure = { exception ->
                    _error.value = "Error al eliminar invitación: ${exception.message}"
                }
            )
        }
    }
    fun createInvitation(
        trainerName: String,
        trainerEmail: String,
        expirationDays: Int? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            invitationRepository.createInvitation(
                trainerName = trainerName,
                trainerEmail = trainerEmail,
                expirationDays = expirationDays
            ).fold(
                onSuccess = { invitation ->
                    _error.value = null
                    loadInvitations() // Recargar la lista
                },
                onFailure = { exception ->
                    _error.value = "Error al crear invitación: ${exception.message}"
                    _isLoading.value = false
                }
            )
        }
    }

    fun cancelInvitation(invitationId: String) {
        viewModelScope.launch {
            invitationRepository.cancelInvitation(invitationId).fold(
                onSuccess = {
                    loadInvitations() // Recargar la lista
                },
                onFailure = { exception ->
                    _error.value = "Error al cancelar invitación: ${exception.message}"
                }
            )
        }
    }

    fun acceptInvitation(code: String, clientId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            // Primero buscar la invitación
            invitationRepository.findInvitationByCode(code).fold(
                onSuccess = { invitation ->
                    if (invitation == null) {
                        _error.value = "Código de invitación no válido"
                    } else if (!invitation.isAvailable()) {
                        _error.value = "Esta invitación ya no está disponible"
                    } else {
                        // Aceptar la invitación
                        invitationRepository.acceptInvitation(invitation.id, clientId).fold(
                            onSuccess = {
                                _error.value = null
                                // Aquí podrías emitir un evento de éxito
                            },
                            onFailure = { exception ->
                                _error.value = "Error al aceptar invitación: ${exception.message}"
                            }
                        )
                    }
                },
                onFailure = { exception ->
                    _error.value = "Error al buscar invitación: ${exception.message}"
                }
            )

            _isLoading.value = false
        }
    }
}