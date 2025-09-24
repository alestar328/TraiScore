package com.develop.traiscore.presentation.viewmodels

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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

    // Estados para email/password login
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set

    // Estados para Google Sign-In
    var isNewUser by mutableStateOf(false)
        private set
    var googleUserEmail by mutableStateOf("")
        private set
    var googleUserName by mutableStateOf("")
        private set
    var googleUserPhotoUrl by mutableStateOf<String?>(null)
        private set

    // Mensaje de error
    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg = _errorMsg.asStateFlow()

    // Eventos
    private val _requireRegistration = MutableSharedFlow<Unit>()
    val requireRegistration = _requireRegistration.asSharedFlow()

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

    fun sendPasswordResetEmail(email: String) {
        if (email.isBlank()) {
            _errorMsg.value = "Ingresa tu email para recuperar la contraseña"
            return
        }

        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        _errorMsg.value = "✅ Se ha enviado un enlace de recuperación a tu email"
                    }
                    .addOnFailureListener { exception ->
                        _errorMsg.value = when {
                            exception.message?.contains("user not found") == true ->
                                "No existe una cuenta con este email"
                            exception.message?.contains("invalid email") == true ->
                                "Email no válido"
                            else -> "Error al enviar email: ${exception.message}"
                        }
                    }
            } catch (exception: Exception) {
                _errorMsg.value = "Error inesperado: ${exception.message}"
            }
        }
    }

    // Email/Password Sign In
    fun signInWithEmail() {
        if (email.isBlank() || password.isBlank()) {
            _errorMsg.value = "Email y contraseña son obligatorios"
            return
        }

        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        viewModelScope.launch {
                            _loginSuccess.emit(Unit)
                        }
                    }
                    .addOnFailureListener { exception ->
                        _errorMsg.value = when (exception.message) {
                            "The email address is badly formatted." -> "Email mal formateado"
                            "There is no user record corresponding to this identifier." -> "Usuario no encontrado"
                            "The password is invalid or the user does not have a password." -> "Contraseña incorrecta"
                            else -> "Error de autenticación: ${exception.message}"
                        }
                    }
            } catch (exception: Exception) {
                _errorMsg.value = "Error inesperado: ${exception.message}"
            }
        }
    }

    // Email/Password Sign Up
    fun registerWithEmail() {
        if (email.isBlank() || password.isBlank()) {
            _errorMsg.value = "Email y contraseña son obligatorios"
            return
        }
        if (password.length < 6) {
            _errorMsg.value = "La contraseña debe tener al menos 6 caracteres"
            return
        }

        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        isNewUser = true
                        // Para registro con email, mantener los campos
                        googleUserEmail = email
                        viewModelScope.launch {
                            _requireRegistration.emit(Unit)
                        }
                    }
                    .addOnFailureListener { exception ->
                        _errorMsg.value = when {
                            exception.message?.contains("already in use") == true -> "Este email ya está registrado"
                            exception.message?.contains("weak password") == true -> "La contraseña es muy débil"
                            else -> "Error de registro: ${exception.message}"
                        }
                    }
            } catch (exception: Exception) {
                _errorMsg.value = "Error inesperado: ${exception.message}"
            }
        }
    }

    // Google Sign In
    fun signInWithGoogle(authManager: AuthenticationManager) {
        viewModelScope.launch {
            authManager.signInWithGoogle()
                .collect { response ->
                    Log.d("AuthDebug", "ViewModel received AuthResponse: $response")
                    when (response) {
                        AuthResponse.Success -> {
                            Log.d("AuthDebug", "ViewModel emitting _loginSuccess")
                            _loginSuccess.emit(Unit)
                        }

                        is AuthResponse.NewUser -> {
                            Log.d("AuthDebug", "ViewModel received NewUser with email: ${response.email}")
                            isNewUser = true
                            // ✅ Pre-llenar datos de Google
                            googleUserEmail = response.email
                            googleUserName = response.displayName
                            googleUserPhotoUrl = response.photoUrl

                            // Para Google Sign-In, limpiar password (no lo necesita)
                            password = ""

                            _requireRegistration.emit(Unit)
                        }

                        is AuthResponse.Error -> {
                            Log.d("AuthDebug", "ViewModel setting errorMsg: ${response.message}")
                            _errorMsg.value = response.message
                        }
                    }
                }
        }
    }

    // Completar registro (tanto para email como Google)
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

                // Usar datos de Google si están disponibles, sino usar los del formulario
                val finalFirstName = if (firstName.isBlank() && googleUserName.isNotBlank()) {
                    googleUserName.split(" ").getOrNull(0) ?: ""
                } else {
                    firstName
                }

                val finalLastName = if (lastName.isBlank() && googleUserName.isNotBlank()) {
                    googleUserName.split(" ").drop(1).joinToString(" ")
                } else {
                    lastName
                }

                // Validar datos requeridos
                if (finalFirstName.isBlank() || finalLastName.isBlank()) {
                    _errorMsg.value = "Nombre y apellido son obligatorios"
                    return@launch
                }

                // Crear UserEntity
                val userEntity = UserEntity(
                    uid = currentUser.uid,
                    firstName = finalFirstName.trim(),
                    lastName = finalLastName.trim(),
                    email = currentUser.email ?: googleUserEmail,
                    birthYear = birthDate.year,
                    gender = gender,
                    userRole = userRole,
                    photoURL = currentUser.photoUrl?.toString() ?: googleUserPhotoUrl
                )

                // Guardar en Firestore
                db.collection("users")
                    .document(currentUser.uid)
                    .set(userEntity)
                    .addOnSuccessListener {
                        Log.d("Registration", "Usuario registrado exitosamente: ${userEntity.uid}")
                        viewModelScope.launch {
                            resetRegistrationState()
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

    // Obtener datos prellenados para el formulario
    fun getPrefilledData(): Triple<String, String, String> {
        return Triple(
            googleUserEmail.ifBlank { email },
            if (googleUserName.isNotBlank()) googleUserName.split(" ").getOrNull(0) ?: "" else "",
            if (googleUserName.isNotBlank()) googleUserName.split(" ").drop(1).joinToString(" ") else ""
        )
    }

    // Reset estados de registro
    private fun resetRegistrationState() {
        isNewUser = false
        googleUserEmail = ""
        googleUserName = ""
        googleUserPhotoUrl = null
    }

    // Reset completo
    fun resetState() {
        resetRegistrationState()
        _errorMsg.value = null
        email = ""
        password = ""
    }

    fun clearError() {
        _errorMsg.value = null
    }

    // ✅ Navegación a registro manual (email)
    fun onNavigateToRegister() {
        isNewUser = true
        // Para registro manual, no pre-llenar datos de Google
        googleUserEmail = ""
        googleUserName = ""
        googleUserPhotoUrl = null
    }

    // ✅ Volver al login
    fun onBackToLogin() {
        resetRegistrationState()
        clearError()
    }
}