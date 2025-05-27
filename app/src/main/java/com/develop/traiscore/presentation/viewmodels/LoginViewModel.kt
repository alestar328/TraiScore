package com.develop.traiscore.presentation.viewmodels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.develop.traiscore.core.Gender
import com.develop.traiscore.core.UserRole
import com.develop.traiscore.data.Authentication.AuthResponse
import com.develop.traiscore.data.Authentication.AuthenticationManager
import com.develop.traiscore.data.local.entity.UserEntity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    // Campos de estado para email y password
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var isNewUser by mutableStateOf(false)

    // Mensaje de error
    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg = _errorMsg.asStateFlow()

    private val _requireRegistration = MutableSharedFlow<Unit>()
    val requireRegistration = _requireRegistration.asSharedFlow()

    // Evento de éxito en login
    private val _loginSuccess = MutableSharedFlow<Unit>()
    val loginSuccess: SharedFlow<Unit> = _loginSuccess.asSharedFlow()
    private val _registrationSuccess = MutableSharedFlow<Unit>()
    val registrationSuccess: SharedFlow<Unit> = _registrationSuccess.asSharedFlow()
    // Actualizar campos
    fun onEmailChange(newEmail: String) {
        email = newEmail
        _errorMsg.value = null
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
        _errorMsg.value = null
    }

    // Lógica de login con email


    // Lógica de login con Google
    fun signInWithGoogle(authManager: AuthenticationManager) {
        viewModelScope.launch {
            authManager.signInWithGoogle()
                .collect { resp ->
                    Log.d("AuthDebug", "ViewModel received AuthResponse: $resp") // <-- Add this
                    when (resp) {
                        AuthResponse.Success -> {
                            Log.d("AuthDebug", "ViewModel emitting _loginSuccess") // <-- Add this
                            _loginSuccess.emit(Unit)
                        }

                        AuthResponse.NewUser -> {
                            isNewUser = true
                            Log.d(
                                "AuthDebug",
                                "ViewModel emitting _requireRegistration"
                            ) // <-- Add this
                            _requireRegistration.emit(Unit)
                        }

                        is AuthResponse.Error -> {
                            Log.d(
                                "AuthDebug",
                                "ViewModel setting errorMsg: ${resp.message}"
                            ) // <-- Add this
                            _errorMsg.value = resp.message
                        }
                    }
                }
        }
    }




    fun completeRegistration(
        firstName: String,
        lastName: String,
        birthDate: LocalDate,
        gender: Gender,
        userRole: UserRole
    ) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    _errorMsg.value = "Error: Usuario no autenticado"
                    return@launch
                }

                // Pre-llenar con datos de Google si están vacíos
                val googleDisplayName = currentUser.displayName ?: ""
                val nameParts = googleDisplayName.split(" ")

                val finalFirstName = if (firstName.isBlank()) {
                    nameParts.getOrNull(0) ?: ""
                } else {
                    firstName
                }
                val finalLastName = if (lastName.isBlank()) {
                    nameParts.drop(1).joinToString(" ")
                } else {
                    lastName
                }

                // Validar datos
                if (finalFirstName.isBlank() || finalLastName.isBlank()) {
                    _errorMsg.value = "Nombre y apellido son obligatorios"
                    return@launch
                }

                // Crear UserEntity
                val userEntity = UserEntity(
                    uid = currentUser.uid,
                    firstName = finalFirstName.trim(),
                    lastName = finalLastName.trim(),
                    email = currentUser.email ?: "",
                    birthYear = birthDate.year,  // ← Extraer año de LocalDate
                    gender = gender,
                    userRole = userRole,
                    photoURL = currentUser.photoUrl?.toString()
                )

                // Guardar en Firestore
                db.collection("users")
                    .document(currentUser.uid)
                    .set(userEntity)
                    .addOnSuccessListener {
                        viewModelScope.launch {
                            Log.d("Registration", "Usuario registrado exitosamente: ${userEntity.uid}")
                            isNewUser = false
                            _registrationSuccess.emit(Unit)
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("Registration", "Error guardando usuario", exception)
                        _errorMsg.value = "Error al guardar el perfil: ${exception.message}"
                    }

            } catch (exception: Exception) {
                Log.e("Registration", "Error en completeRegistration", exception)
                _errorMsg.value = "Error inesperado: ${exception.message}"
            }
        }
    }
    fun getGoogleUserData(): Pair<String, String> {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val displayName = currentUser.displayName ?: ""
            val nameParts = displayName.split(" ")
            val firstName = nameParts.getOrNull(0) ?: ""
            val lastName = nameParts.drop(1).joinToString(" ")
            return Pair(firstName, lastName)
        }
        return Pair("", "")
    }
    // Verificar si el usuario ya existe en Firestore
    private suspend fun checkIfUserExists(uid: String): Boolean {
        return try {
            val document = db.collection("users").document(uid).get()
            document.result.exists()
        } catch (exception: Exception) {
            Log.e("AuthDebug", "Error checking user existence", exception)
            false
        }
    }

    // Resetear estado cuando sea necesario
    fun resetState() {
        isNewUser = false
        _errorMsg.value = null
        email = ""
        password = ""
    }

    // Limpiar mensaje de error
    fun clearError() {
        _errorMsg.value = null
    }

}
