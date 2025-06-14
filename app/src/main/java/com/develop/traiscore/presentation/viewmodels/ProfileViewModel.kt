package com.develop.traiscore.presentation.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.develop.traiscore.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


data class ProfileUiState(
    val isUploadingPhoto: Boolean = false,
    val photoUrl: String? = null,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun uploadProfilePhoto(imageUri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isUploadingPhoto = true, error = null)

                val photoUrl = profileRepository.uploadProfilePhoto(imageUri)

                _uiState.value = _uiState.value.copy(
                    isUploadingPhoto = false,
                    photoUrl = photoUrl
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUploadingPhoto = false,
                    error = e.message ?: "Error desconocido al subir la foto"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun loadCurrentUserPhoto() {
        viewModelScope.launch {
            try {
                val photoUrl = profileRepository.getCurrentUserPhotoUrl()
                _uiState.value = _uiState.value.copy(photoUrl = photoUrl)
            } catch (e: Exception) {
                // Silenciosamente manejar el error de carga inicial
            }
        }
    }


}