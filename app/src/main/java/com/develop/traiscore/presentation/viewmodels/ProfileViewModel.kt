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
                android.util.Log.d("ProfileVM", "🚀 Iniciando subida de foto: $imageUri")
                _uiState.value = _uiState.value.copy(isUploadingPhoto = true, error = null)

                val photoUrl = profileRepository.uploadProfilePhoto(imageUri)

                android.util.Log.d("ProfileVM", "✅ Foto subida exitosamente: $photoUrl")

                _uiState.value = _uiState.value.copy(
                    isUploadingPhoto = false,
                    photoUrl = photoUrl
                )
            } catch (e: Exception) {
                android.util.Log.e("ProfileVM", "❌ Error subiendo foto", e)
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
                android.util.Log.d("ProfileVM", "📸 Cargando foto actual del usuario...")

                val photoUrl = profileRepository.getCurrentUserPhotoUrl()

                android.util.Log.d("ProfileVM", "📸 Foto cargada: $photoUrl")

                _uiState.value = _uiState.value.copy(photoUrl = photoUrl)
            } catch (e: Exception) {
                android.util.Log.e("ProfileVM", "❌ Error cargando foto actual", e)
                _uiState.value = _uiState.value.copy(error = "Error cargando foto: ${e.message}")
            }
        }
    }


}