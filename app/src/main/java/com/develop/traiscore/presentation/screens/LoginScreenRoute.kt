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

    // ✅ Observar evento de login exitoso
    LaunchedEffect(Unit) {
        loginViewModel.loginSuccess.collect {
            onLoginSuccess()
        }
    }

    // ✅ Observar evento de registro requerido
    LaunchedEffect(Unit) {
        loginViewModel.requireRegistration.collect {
            // El ViewModel ya maneja isNewUser = true automáticamente
            // Solo necesitamos que el UI reaccione al cambio
        }
    }

    // ✅ Observar evento de registro completado exitosamente
    LaunchedEffect(Unit) {
        loginViewModel.registrationSuccess.collect {
            // Después del registro exitoso, navegar a la pantalla principal
            onLoginSuccess()
        }
    }

    // ✅ Obtener datos prellenados para el formulario
    val (prefilledEmail, prefilledFirstName, prefilledLastName) = loginViewModel.getPrefilledData()

    LoginScreen(
        errorMsg = loginViewModel.errorMsg.collectAsState().value,

        // ✅ Google Sign-In
        onGoogleClick = {
            loginViewModel.signInWithGoogle(authManager)
        },

        // ✅ Navegación entre pantallas
        onRegisterClick = {
            // Para registro manual con email (no Google)
            loginViewModel.onNavigateToRegister()
        },
        onBackToLogin = {
            loginViewModel.onBackToLogin()
        },

        // ✅ Recuperar contraseña
        onForgotPassword = { email ->
            loginViewModel.sendPasswordResetEmail(email)
        },

        // ✅ Estado del formulario
        isNewUser = loginViewModel.isNewUser,

        // ✅ Completar registro (tanto Google como email)
        onCompleteRegistration = { firstName, lastName, birthDate, gender, userRole ->
            loginViewModel.completeRegistration(
                firstName = firstName,
                lastName = lastName,
                birthDate = birthDate,
                gender = gender,
                userRole = userRole
            )
        },

        // ✅ Campos de email/password con datos prellenados
        email = if (loginViewModel.isNewUser) prefilledEmail else loginViewModel.email,
        password = loginViewModel.password,
        onEmailChange = loginViewModel::onEmailChange,
        onPasswordChange = loginViewModel::onPasswordChange,

        // ✅ Autenticación con email
        onEmailSignIn = loginViewModel::signInWithEmail,
        onEmailSignUp = loginViewModel::registerWithEmail,

        // ✅ NUEVOS: Datos prellenados de Google para el formulario
        prefilledFirstName = prefilledFirstName,
        prefilledLastName = prefilledLastName,
        isGoogleSignIn = loginViewModel.googleUserEmail.isNotEmpty()
    )
}