package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.develop.traiscore.presentation.theme.TraiScoreTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.develop.traiscore.core.Gender
import com.develop.traiscore.presentation.components.BirthDatePicker
import java.time.LocalDate

@Composable
fun RegisterScreen(
    email: String,
    firstName: String,
    onFirstNameChange: (String) -> Unit,
    lastName: String,
    onLastNameChange: (String) -> Unit,
    gender: Gender?,
    onGenderSelect: (Gender) -> Unit,
    birthDate: LocalDate? = null, // ✅ Añade esto
    onBirthDateChange: (LocalDate) -> Unit = {}, // ✅ Añade esto
    errorMsg: String?,
    onRegisterClick: () -> Unit,
    onBack: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Completa tu perfil",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(32.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = {},
                enabled = false,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = firstName,
                onValueChange = onFirstNameChange,
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = lastName,
                onValueChange = onLastNameChange,
                label = { Text("Apellido") },
                modifier = Modifier.fillMaxWidth()
            )

            // --- SELECTOR DE FECHA ---
            OutlinedTextField(
                value = birthDate?.toString() ?: "Selecciona tu fecha",
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha de nacimiento") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                enabled = false, // Hacemos que el click lo maneje el Modifier para evitar foco del teclado
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            // ✅ Selector de género
            Text("Género", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = gender == Gender.MALE,
                        onClick = { onGenderSelect(Gender.MALE) }
                    )
                    Text("Hombre")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = gender == Gender.FEMALE,
                        onClick = { onGenderSelect(Gender.FEMALE) }
                    )
                    Text("Mujer")
                }
            }

            errorMsg?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = onRegisterClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = firstName.isNotBlank() && lastName.isNotBlank() && gender != null
            ) {
                Text("Continuar")
            }

            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver")
            }
        }
        if (showDatePicker) {
            BirthDatePicker(
                selectedDate = birthDate,
                onDateSelected = {
                    onBirthDateChange(it)
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }
    }
}
@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    TraiScoreTheme {
        RegisterScreen(
            email = "usuario@gmail.com",
            firstName = "Alejandro",
            onFirstNameChange = {},
            lastName = "Ormeño",
            onLastNameChange = {},
            gender = null,
            onGenderSelect = {},
            errorMsg = null,
            onRegisterClick = {},
            onBack = {}
        )
    }
}