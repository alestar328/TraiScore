package com.develop.traiscore.presentation.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.develop.traiscore.data.repository.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
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

    // Flag para no repetir la petición de Firestore en cada visita a ProfileScreen
    private var _photoLoadedFromFirestore = false

    init {
        // Mostrar la foto de Firebase Auth inmediatamente (sin red), luego actualizar con Firestore
        val authPhotoUrl = FirebaseAuth.getInstance().currentUser?.photoUrl?.toString()
        if (authPhotoUrl != null) {
            _uiState.value = _uiState.value.copy(photoUrl = authPhotoUrl)
        }
        // Cargar URL definitiva de Firestore en background (solo si aún no hay URL)
        loadCurrentUserPhoto()
    }

    fun uploadProfilePhoto(imageUri: Uri) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ProfileVM", "🚀 Iniciando subida de foto: $imageUri")
                _uiState.value = _uiState.value.copy(isUploadingPhoto = true, error = null)

                val photoUrl = profileRepository.uploadProfilePhoto(imageUri)

                android.util.Log.d("ProfileVM", "✅ Foto subida exitosamente: $photoUrl")

                _photoLoadedFromFirestore = true
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
        // Si ya tenemos una URL cargada desde Firestore, no volvemos a hacer la petición de red.
        // La URL de Firebase Auth (set en init) no cuenta como "cargada desde Firestore".
        if (_photoLoadedFromFirestore) return
        viewModelScope.launch {
            try {
                android.util.Log.d("ProfileVM", "📸 Cargando foto desde Firestore...")
                val photoUrl = profileRepository.getCurrentUserPhotoUrl()
                android.util.Log.d("ProfileVM", "📸 Foto cargada: $photoUrl")
                _uiState.value = _uiState.value.copy(photoUrl = photoUrl)
                _photoLoadedFromFirestore = true
            } catch (e: Exception) {
                android.util.Log.e("ProfileVM", "❌ Error cargando foto actual", e)
                _uiState.value = _uiState.value.copy(error = "Error cargando foto: ${e.message}")
            }
        }
    }

    // Forzar recarga (usar tras subir una nueva foto)
    fun reloadPhoto() {
        _photoLoadedFromFirestore = false
        loadCurrentUserPhoto()
    }


}