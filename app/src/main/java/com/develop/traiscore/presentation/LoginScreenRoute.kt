package com.develop.traiscore.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.develop.traiscore.presentation.screens.LoginScreen
import com.develop.traiscore.presentation.viewmodels.LoginViewModel

@Composable
fun LoginScreenRoute(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit
) {
    // Obtain the ViewModel (scoped to the navGraph or activity)
    val loginViewModel: LoginViewModel = viewModel()
    val activityContext = LocalContext.current
    val authManager = remember { AuthenticationManager(activityContext) }

    // Collect email/password directly from state variables
    val email = loginViewModel.email
    val password = loginViewModel.password

    // Observe error message as State
    val errorMsg by loginViewModel.errorMsg.collectAsState()

    // React to login success events
    LaunchedEffect(Unit) {
        loginViewModel.loginSuccess.collect {
            onLoginSuccess()
        }
    }

    // Pass everything into the UI-only composable
    LoginScreen(
        email = email,
        onEmailChange = loginViewModel::onEmailChange,
        password = password,
        onPasswordChange = loginViewModel::onPasswordChange,
        errorMsg = errorMsg,
        onGoogleClick = { loginViewModel.signInWithGoogle(authManager) },
        onRegisterClick = onRegisterClick
    )
}

@Composable
fun LoginScreenPreviewRoute() {
    // In Preview we can pass empty lambdas; state flows are not collected here
    LoginScreenRoute(
        onLoginSuccess = {},
        onRegisterClick = {}
    )
}
