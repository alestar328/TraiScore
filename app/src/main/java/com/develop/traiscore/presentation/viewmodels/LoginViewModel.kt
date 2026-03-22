package com.develop.traiscore.presentation.viewmodels


import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.develop.traiscore.core.Gender
import com.develop.traiscore.data.Authentication.AuthResponse
import com.develop.traiscore.data.Authentication.AuthenticationManager
import com.develop.traiscore.data.local.entity.UserEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

sealed class AuthUiState {
    object Loading : AuthUiState()
    object Login : AuthUiState()
    object RegisterRequired : AuthUiState()
    object LoggedIn : AuthUiState()
}

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Login)

    // Estados para Google Sign-In
    var googleUserEmail by mutableStateOf("")
        private set
    var googleUserName by mutableStateOf("")
        private set
    var googleUserPhotoUrl by mutableStateOf<String?>(null)
        private set

    // Estados para el formulario de registro
    var firstName by mutableStateOf("")
        private set
    var lastName by mutableStateOf("")
        private set
    var birthDate by mutableStateOf<LocalDate?>(null)
    var gender by mutableStateOf<Gender?>(null)

    // Mensaje de error
    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg = _errorMsg.asStateFlow()

    fun updateFirstName(value: String) {
        firstName = value
        _errorMsg.value = null
    }

    fun updateLastName(value: String) {
        lastName = value
        _errorMsg.value = null
    }

    fun signInWithGoogle(authManager: AuthenticationManager) {
        viewModelScope.launch {
            authManager.signInWithGoogle()
                .collect { response ->
                    when (response) {
                        AuthResponse.Success -> {
                            authUiState.value = AuthUiState.LoggedIn
                        }

                        is AuthResponse.NewUser -> {
                            googleUserEmail = response.email
                            googleUserName = response.displayName
                            googleUserPhotoUrl = response.photoUrl
                            authUiState.value = AuthUiState.RegisterRequired
                        }

                        is AuthResponse.Error -> {
                            _errorMsg.value = response.message
                        }
                    }
                }
        }
    }

    fun completeRegistration(
        firstName: String,
        lastName: String,
        birthDate: LocalDate,
        gender: Gender
    ) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    _errorMsg.value = "Error: Usuario no autenticado"
                    return@launch
                }

                // Usar datos de Google si están disponibles
                val finalFirstName = firstName.ifBlank {
                    googleUserName.split(" ").getOrNull(0) ?: ""
                }
                val finalLastName = lastName.ifBlank {
                    googleUserName.split(" ").drop(1).joinToString(" ")
                }

                // Validar
                if (finalFirstName.isBlank() || finalLastName.isBlank()) {
                    _errorMsg.value = "Nombre y apellido son obligatorios"
                    return@launch
                }

                // ✅ VERIFICACIÓN: No sobrescribir si ya existe
                val existingDoc = db.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()

                if (existingDoc.exists()) {
                    Log.w("Registration", "⚠️ El documento ya existe, no se sobrescribe")
                    resetRegistrationState()
                    authUiState.value = AuthUiState.LoggedIn
                    return@launch
                }

                val userEntity = UserEntity(
                    uid = currentUser.uid,
                    firstName = finalFirstName.trim(),
                    lastName = finalLastName.trim(),
                    email = currentUser.email ?: googleUserEmail,
                    birthYear = birthDate.year,
                    gender = gender,
                    photoURL = currentUser.photoUrl?.toString() ?: googleUserPhotoUrl
                )

                db.collection("users")
                    .document(currentUser.uid)
                    .set(userEntity)
                    .await()

                resetRegistrationState()
                authUiState.value = AuthUiState.LoggedIn

            } catch (exception: Exception) {
                Log.e("Registration", "❌ Error en completeRegistration", exception)
                _errorMsg.value = "Error al guardar el perfil: ${exception.message}"
            }
        }
    }

    fun getPrefilledData(): Triple<String, String, String> {
        return Triple(
            googleUserEmail,
            firstName.ifBlank { googleUserName.split(" ").getOrNull(0) ?: "" },
            lastName.ifBlank { googleUserName.split(" ").drop(1).joinToString(" ") }
        )
    }

    private fun resetRegistrationState() {
        googleUserEmail = ""
        googleUserName = ""
        googleUserPhotoUrl = null
        firstName = ""
        lastName = ""
        birthDate = null
        gender = null
    }

    fun clearError() {
        _errorMsg.value = null
    }

    fun onBackToLogin() {
        resetRegistrationState()
        clearError()
        authUiState.value = AuthUiState.Login
    }
}