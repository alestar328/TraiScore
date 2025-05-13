package com.develop.traiscore.presentation

import android.util.Log
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.develop.traiscore.data.Authentication.AuthenticationManager
import com.develop.traiscore.presentation.screens.LoginScreen
import com.develop.traiscore.presentation.viewmodels.LoginViewModel

@Composable
fun LoginScreenRoute(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit
) {
    // Obtain the ViewModel (scoped to the navGraph or activity)
    val loginViewModel: LoginViewModel = viewModel()
    val context = LocalContext.current
    val authManager = remember { AuthenticationManager(context) }
    val email = loginViewModel.email
    val password = loginViewModel.password
    var showNewUserDialog by remember { mutableStateOf(false) }

    // Observe error message as State
    val errorMsg by loginViewModel.errorMsg.collectAsState()

    // React to login success events
    LaunchedEffect(Unit) {
        loginViewModel.loginSuccess.collect {
            Log.d("AuthDebug", "Route received loginSuccess. Calling onLoginSuccess()") // <-- Add this
            onLoginSuccess()
        }
    }
    LaunchedEffect(Unit) {
        loginViewModel.requireRegistration.collect {
            Log.d("AuthDebug", "Route received requireRegistration. Setting showNewUserDialog = true") // <-- Add this
            showNewUserDialog = true
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
    if (showNewUserDialog) {
        AlertDialog(
            onDismissRequest = { showNewUserDialog = false },
            title = { Text("Usuario no registrado") },
            text = { Text("No estás registrado. ¿Registrar ahora?") },
            confirmButton = {
                TextButton(onClick = {
                    showNewUserDialog = false
                    onRegisterClick()
                }) {
                    Text("Registrar ahora")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewUserDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }


}

@Composable
fun LoginScreenPreviewRoute() {
    // In Preview we can pass empty lambdas; state flows are not collected here
    LoginScreenRoute(
        onLoginSuccess = {},
        onRegisterClick = {}
    )
}
