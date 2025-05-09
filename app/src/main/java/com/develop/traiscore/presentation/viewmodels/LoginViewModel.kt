package com.develop.traiscore.presentation.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.develop.traiscore.presentation.AuthResponse
import com.develop.traiscore.presentation.AuthenticationManager
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
                    when (resp) {
                        AuthResponse.Success -> _loginSuccess.emit(Unit)
                        is AuthResponse.Error -> _errorMsg.value = resp.message
                    }
                }
        }
    }
}
