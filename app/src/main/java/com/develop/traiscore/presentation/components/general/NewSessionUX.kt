package com.develop.traiscore.presentation.components.general

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.develop.traiscore.R
import com.develop.traiscore.presentation.theme.tsColors
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import com.develop.traiscore.presentation.viewmodels.NewSessionViewModel


@Composable
fun NewSessionUX(
    onDismiss: () -> Unit,
    onSessionCreated: () -> Unit,
    viewModel: NewSessionViewModel = hiltViewModel()

){
    val defaultColor = MaterialTheme.tsColors.ledCyan
    var sessionName by remember { mutableStateOf("")}
    var colorSeleccionado by remember { mutableStateOf(defaultColor) }

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Mostrar errores si existen
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Aquí podrías mostrar un Snackbar o Toast
            println("Error: $errorMessage")
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Nueva Sesión")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = sessionName,
                    onValueChange = {sessionName = it},
                    label = {Text("Nómbrala")},
                    placeholder = { Text("Ej: Fullbody")},
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    enabled = !isLoading
                )
                ColorBarUX(
                    selectedColor = colorSeleccionado,
                    onColorSelected = { nuevoColor ->
                        colorSeleccionado = nuevoColor
                        if (!isLoading) {
                            colorSeleccionado = nuevoColor
                        }
                        println("Color seleccionado: $nuevoColor")
                    }
                )
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp))  // Esquinas redondeadas opcionales
                        .background(colorSeleccionado),
                    contentAlignment = Alignment.Center  // ⭐ CENTRAR EL CONTENIDO
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.pesa_icon),
                        contentDescription = "Icono de pesa",
                        modifier = Modifier.size(48.dp),  // Tamaño del icono
                        tint = Color.Black  // ⭐ SIEMPRE CYAN
                    )
                }
            }
        },
        confirmButton = {
            IconButton(
                onClick = {
                    if (sessionName.isNotBlank() && !isLoading) {
                        viewModel.createSession(
                            name = sessionName.trim(),
                            color = colorSeleccionado
                        )
                        onSessionCreated()
                        onDismiss()
                    }
                },
                enabled = sessionName.isNotBlank() && !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Confirmar creación",
                    tint = if (sessionName.isNotBlank() && !isLoading) {
                        MaterialTheme.tsColors.ledCyan
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Cancelar")
            }
        }

    )
}