package com.develop.traiscore.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.develop.traiscore.R
import com.develop.traiscore.core.Gender
import com.develop.traiscore.core.UserRole
import androidx.compose.material.OutlinedTextField
import androidx.compose.material3.ElevatedButton
import androidx.compose.ui.zIndex
import com.develop.traiscore.presentation.components.DatePickerSection
import com.develop.traiscore.presentation.components.FullScreenDatePicker
import com.develop.traiscore.presentation.components.ToggleButtonRowUser
import com.develop.traiscore.presentation.theme.Black
import com.develop.traiscore.presentation.theme.Roboto
import com.develop.traiscore.presentation.theme.TraiScoreTheme
import com.develop.traiscore.presentation.theme.traiBlue
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun LoginScreen(

    errorMsg: String?,
    onGoogleClick: () -> Unit,
    onRegisterClick: () -> Unit,
    isNewUser: Boolean = false,
    onCompleteRegistration: (firstName: String, lastName: String, birthYear: LocalDate, gender: Gender, UserRole: UserRole) -> Unit = { _, _, _, _, _ -> }

) {
    var showDatePicker by remember { mutableStateOf(false) }  // ← Añadir esto
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface {
            Column(modifier = Modifier.fillMaxSize()) {

                TopSection()
                Spacer(modifier = Modifier.height(20.dp))
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 30.dp)
                ) {
                    if (!isNewUser) {
                        SocialMediaSection(onGoogleClick = onGoogleClick)


                        val uiColor = if (isSystemInDarkTheme()) Color.White else Black

                        Box(
                            modifier = Modifier
                                .fillMaxHeight(fraction = 0.8f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                                Text(
                                    text = "¿No tienes una cuenta?",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 14.sp,
                                    fontFamily = Roboto,
                                    fontWeight = FontWeight.Normal
                                )

                                Text(
                                    text = "Créala ahora",
                                    color = uiColor,
                                    fontSize = 14.sp,
                                    fontFamily = Roboto,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .clickable { onRegisterClick() }
                                        .padding(vertical = 8.dp)
                                )
                            }
                        }
                    } else {
                        // Pantalla de Registro - completar perfil
                        RegistrationSection(
                            onCompleteRegistration = onCompleteRegistration,
                            errorMsg = errorMsg,
                            selectedDate = selectedDate,          // ← Pasar estado
                            onDateSelected = { selectedDate = it }, // ← Pasar callback
                            onShowDatePicker = { showDatePicker = true }
                        )
                    // ← Pasar callback
                    }
                }
            }

        }
        // DatePicker de pantalla completa - FUERA del Surface
        AnimatedVisibility(
            visible = showDatePicker,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(300)
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300)
            ),
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1000f)
        ) {
            FullScreenDatePicker(
                selectedDate = selectedDate,
                onDateSelected = { date ->
                    selectedDate = date
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }
    }
}

@Composable
private fun RegistrationSection(
    onCompleteRegistration: (String, String, LocalDate, Gender, UserRole) -> Unit,
    errorMsg: String?,
    selectedDate: LocalDate?,                    // ← Añadir parámetro
    onDateSelected: (LocalDate) -> Unit,         // ← Añadir parámetro
    onShowDatePicker: () -> Unit                 // ← Añadir parámetro
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf<Gender?>(null) }
    var selectedUserType by remember { mutableStateOf<UserRole?>(null) }
    val isFormValid = firstName.isNotBlank() &&
            lastName.isNotBlank() &&
            selectedDate != null &&
            selectedGender != null &&
            selectedUserType != null

    val defaultColors = TextFieldDefaults.outlinedTextFieldColors(
        focusedBorderColor = traiBlue,
        unfocusedBorderColor = Color.Gray,
        cursorColor = traiBlue,
        textColor = Color.Black,
        focusedLabelColor = traiBlue,
        unfocusedLabelColor = Color.Black,
        placeholderColor = Color.Black
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(
            bottom = 50.dp
        )
    ) {
        item {
            Text(
                text = "Completa tu perfil",
                style = MaterialTheme.typography.headlineSmall,
                color = Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = defaultColors
            )
        }

        item {
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Apellido") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = defaultColors
            )
        }

        item {
            DatePickerSection(
                selectedDate = selectedDate,
                onDateSelected = onDateSelected,  // No se usa aquí, pero mantenemos compatibilidad
                onShowDatePicker = onShowDatePicker,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Text(
                text = "Género:",
                style = MaterialTheme.typography.bodyMedium,
                color = Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            ToggleButtonRowUser(
                selectedTab = selectedGender?.displayName ?: "",
                onTabSelected = { displayName ->
                    selectedGender = Gender.entries.find { it.displayName == displayName }
                },
                options = Gender.entries.map { it.displayName },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Text(
                text = "Tipo de cuenta:",
                style = MaterialTheme.typography.bodyMedium,
                color = Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            ToggleButtonRowUser(
                selectedTab = when (selectedUserType) {
                    UserRole.CLIENT -> "Atleta"
                    UserRole.TRAINER -> "Trainer"
                    else -> ""
                },
                onTabSelected = { tab ->
                    selectedUserType = when (tab) {
                        "Atleta" -> UserRole.CLIENT
                        "Trainer" -> UserRole.TRAINER
                        else -> null
                    }
                },
                options = listOf("Atleta", "Trainer"),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Mensaje de error
        errorMsg?.let { error ->
            item {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        item {
            ElevatedButton(
                onClick = {
                    selectedDate?.let { date ->
                        if (isFormValid) {
                            onCompleteRegistration(
                                firstName,
                                lastName,
                                date,
                                selectedGender!!,
                                selectedUserType!!
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isFormValid,
                elevation = ButtonDefaults.elevatedButtonElevation(
                    defaultElevation = if (isFormValid) 8.dp else 2.dp,
                    pressedElevation = 12.dp,
                    disabledElevation = 0.dp
                ),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = if (isFormValid) traiBlue else Color.Gray,
                    contentColor = Color.White,
                    disabledContainerColor = Color.LightGray,
                    disabledContentColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Completar Registro",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            Text(
                text = "Todos los campos son obligatorios",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }

}

@Composable
private fun SocialMediaSection(
    onGoogleClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ElevatedButton(
            onClick = onGoogleClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            elevation = ButtonDefaults.elevatedButtonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 2.dp
            ),
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = Color.White,
                contentColor = Color.Unspecified
            ),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.google),
                contentDescription = "Google sign-in",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Accede con Google",
                color = Color.Black,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}


@Composable
private fun TopSection() {
    val uiColor = if (isSystemInDarkTheme()) Color.White else Black

    Box(
        contentAlignment = Alignment.TopCenter
    ) {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(fraction = 0.46f),
            painter = painterResource(id = R.drawable.shape),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.Black) // Aplica un tinte negro

        )
        Row(
            modifier = Modifier.padding(top = 80.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(85.dp),
                painter = painterResource(id = R.drawable.tslogo),
                contentDescription = stringResource(id = R.string.app_logo),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = stringResource(id = R.string.trai_Score),
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 36.sp),
                    color = Color.White
                )
                Text(
                    text = stringResource(id = R.string.s_logan),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                    color = Color.White

                )
            }
        }
        Text(
            modifier = Modifier
                .padding(bottom = 7.dp)
                .align(alignment = Alignment.BottomCenter),
            text = stringResource(id = R.string.login),
            style = MaterialTheme.typography.headlineLarge,
            color = uiColor

        )
    }
}

@Preview(
    name = "LoginScreenPreview",
    showBackground = true
)
@Composable
fun LoginScreenPreview() {
    TraiScoreTheme {
        LoginScreen(
            errorMsg = null,
            onGoogleClick = {},
            onRegisterClick = {}
        )
    }
}