package com.develop.traiscore.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.develop.traiscore.core.Gender
import com.develop.traiscore.data.Authentication.AuthenticationManager
import com.develop.traiscore.presentation.viewmodels.AuthUiState
import com.develop.traiscore.presentation.viewmodels.LoginViewModel
import java.time.LocalDate

@Composable
fun LoginScreenRoute(
    onLoginSuccess: () -> Unit
) {
    val loginViewModel = hiltViewModel<LoginViewModel>()
    val context = LocalContext.current
    val authManager = remember { AuthenticationManager(context) }
    val authState = loginViewModel.authUiState.collectAsState().value
    val errorMsg by loginViewModel.errorMsg.collectAsState()

    // Navegación declarativa
    LaunchedEffect(authState) {
        if (authState is AuthUiState.LoggedIn) {
            onLoginSuccess()
        }
    }

    when (authState) {
        AuthUiState.Login -> {
            LoginScreen(
                onGoogleClick = {
                    loginViewModel.signInWithGoogle(authManager)
                }
            )
        }

        AuthUiState.RegisterRequired -> {
            val (email, prefilledFirstName, prefilledLastName) = loginViewModel.getPrefilledData()

            RegisterScreen(
                email = email,
                firstName = loginViewModel.firstName.ifBlank { prefilledFirstName },
                onFirstNameChange = { loginViewModel.updateFirstName(it) },
                lastName = loginViewModel.lastName.ifBlank { prefilledLastName },
                onLastNameChange = { loginViewModel.updateLastName(it) },
                gender = loginViewModel.gender,
                onGenderSelect = { loginViewModel.gender = it }, // ✅ Asignación directa
                birthDate = loginViewModel.birthDate, // ✅ Pasamos la fecha del ViewModel
                onBirthDateChange = { loginViewModel.birthDate = it }, // ✅ Actualizamos la fecha
                errorMsg = errorMsg,
                onRegisterClick = {
                    // Ahora pasamos los datos reales del estado del ViewModel
                    loginViewModel.completeRegistration(
                        firstName = loginViewModel.firstName.ifBlank { prefilledFirstName },
                        lastName = loginViewModel.lastName.ifBlank { prefilledLastName },
                        birthDate = loginViewModel.birthDate ?: LocalDate.now(),
                        gender = loginViewModel.gender ?: Gender.OTHER
                    )
                },
                onBack = {
                    loginViewModel.onBackToLogin()
                }
            )
        }

        AuthUiState.Loading -> {
            // Opcional: LoadingScreen()
        }

        AuthUiState.LoggedIn -> Unit
    }
}