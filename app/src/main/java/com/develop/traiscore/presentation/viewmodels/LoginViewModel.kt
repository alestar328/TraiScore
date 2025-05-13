package com.develop.traiscore.presentation.viewmodels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.develop.traiscore.data.Authentication.AuthResponse
import com.develop.traiscore.data.Authentication.AuthenticationManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {


    // Campos de estado para email y password
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set

    // Mensaje de error
    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg = _errorMsg.asStateFlow()

    private val _requireRegistration = MutableSharedFlow<Unit>()
    val requireRegistration = _requireRegistration.asSharedFlow()

    // Evento de éxito en login
    private val _loginSuccess = MutableSharedFlow<Unit>()
    val loginSuccess: SharedFlow<Unit> = _loginSuccess.asSharedFlow()

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
}
