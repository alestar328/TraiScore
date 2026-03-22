package com.develop.traiscore.presentation.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.develop.traiscore.data.repository.ProfileRepository
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

data class TrainerInfo(
    val name: String,
    val email: String,
    val photoUrl: String? = null
)

data class ProfileUiState(
    val isUploadingPhoto: Boolean = false,
    val photoUrl: String? = null,
    val error: String? = null,
    val trainerInfo: TrainerInfo? = null,
    val isTrainerLoading: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var firestoreListener: ListenerRegistration? = null

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

    fun loadTrainerInfo() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        _uiState.value = _uiState.value.copy(isTrainerLoading = true, trainerInfo = null)
        firestoreListener?.remove()
        firestoreListener = FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.value = _uiState.value.copy(isTrainerLoading = false)
                    return@addSnapshotListener
                }
                val trainerId = snapshot?.getString("linkedTrainerUid")
                if (trainerId != null) {
                    viewModelScope.launch {
                        val info = fetchTrainerInfo(trainerId)
                        _uiState.value = _uiState.value.copy(trainerInfo = info, isTrainerLoading = false)
                    }
                } else {
                    _uiState.value = _uiState.value.copy(trainerInfo = null, isTrainerLoading = false)
                }
            }
    }

    private suspend fun fetchTrainerInfo(trainerId: String): TrainerInfo? {
        return try {
            val doc = FirebaseFirestore.getInstance()
                .collection("users")
                .document(trainerId)
                .get()
                .await()
            val name = "${doc.getString("firstName") ?: ""} ${doc.getString("lastName") ?: ""}".trim()
            val email = doc.getString("email") ?: ""
            val photo = doc.getString("photoURL")
            if (name.isNotEmpty()) TrainerInfo(name, email, photo) else null
        } catch (e: Exception) {
            android.util.Log.e("ProfileVM", "Error cargando trainer info", e)
            null
        }
    }

    override fun onCleared() {
        super.onCleared()
        firestoreListener?.remove()
    }

    fun loadCurrentUserPhoto() {
        viewModelScope.launch {
            try {
                val photoUrl = profileRepository.getCurrentUserPhotoUrl()

                _uiState.value = _uiState.value.copy(photoUrl = photoUrl)
            } catch (e: Exception) {
                android.util.Log.e("ProfileVM", "❌ Error cargando foto actual", e)
                _uiState.value = _uiState.value.copy(error = "Error cargando foto: ${e.message}")
            }
        }
    }


}