package com.develop.traiscore.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.develop.traiscore.data.Authentication.AuthenticationManager
import com.develop.traiscore.presentation.viewmodels.LoginViewModel

@Composable
fun LoginScreenRoute(
    onLoginSuccess: () -> Unit
) {
    val loginViewModel = hiltViewModel<LoginViewModel>()
    val context = LocalContext.current
    val authManager = remember { AuthenticationManager(context) }

    // Observar eventos del ViewModel
    LaunchedEffect(Unit) {
        loginViewModel.loginSuccess.collect {
            onLoginSuccess()
        }
    }

    LaunchedEffect(Unit) {
        loginViewModel.requireRegistration.collect {
            // Esto activa automáticamente isNewUser = true en el ViewModel
            // El LoginScreen detectará el cambio y mostrará el formulario de registro
        }
    }

    LaunchedEffect(Unit) {
        loginViewModel.registrationSuccess.collect {
            // Después del registro exitoso, ir a la pantalla principal
            onLoginSuccess()
        }
    }

    // Tu LoginScreen existente con todas las conexiones
    LoginScreen(
        errorMsg = loginViewModel.errorMsg.collectAsState().value,
        onGoogleClick = {
            loginViewModel.signInWithGoogle(authManager)
        },
        onRegisterClick = {
            loginViewModel.isNewUser = true
        },
        onBackToLogin = { // ✅ NUEVO
            loginViewModel.isNewUser = false
            loginViewModel.clearError()
        },
        onForgotPassword = { email -> // ✅ NUEVO
            println("🔥 DEBUG: LoginScreenRoute onForgotPassword called with: '$email'")

            loginViewModel.sendPasswordResetEmail(email)
        },
        isNewUser = loginViewModel.isNewUser,
        onCompleteRegistration = loginViewModel::completeRegistration,
        email = loginViewModel.email,
        password = loginViewModel.password,
        onEmailChange = loginViewModel::onEmailChange,
        onPasswordChange = loginViewModel::onPasswordChange,
        onEmailSignIn = loginViewModel::signInWithEmail,
        onEmailSignUp = loginViewModel::registerWithEmail
    )
}