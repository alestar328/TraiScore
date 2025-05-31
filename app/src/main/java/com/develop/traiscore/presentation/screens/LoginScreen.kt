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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.zIndex
import com.develop.traiscore.presentation.components.DatePickerSection
import com.develop.traiscore.presentation.components.FullScreenDatePicker
import com.develop.traiscore.presentation.components.ToggleButtonRowUser
import com.develop.traiscore.presentation.theme.Black
import com.develop.traiscore.presentation.theme.Roboto
import com.develop.traiscore.presentation.theme.TraiScoreTheme
import com.develop.traiscore.presentation.theme.traiBlue
import java.time.LocalDate

@Composable
fun LoginScreen(
    errorMsg: String?,
    onGoogleClick: () -> Unit,
    onRegisterClick: () -> Unit,
    isNewUser: Boolean = false,
    onCompleteRegistration: (firstName: String, lastName: String, birthYear: LocalDate, gender: Gender, UserRole: UserRole) -> Unit = { _, _, _, _, _ -> },
    email: String = "",
    password: String = "",
    onEmailChange: (String) -> Unit = {},
    onPasswordChange: (String) -> Unit = {},
    onEmailSignIn: () -> Unit = {},
    onEmailSignUp: () -> Unit = {},
    onBackToLogin: () -> Unit = {},
    onForgotPassword: (String) -> Unit = {}


) {
    var showDatePicker by remember { mutableStateOf(false) }  // ← Añadir esto
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var isRegistering by remember { mutableStateOf(false) } // ✅ AÑADIR estado para tracking


    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        Surface {
            Column(modifier = Modifier.fillMaxSize()) {

                TopSection(
                    title = if (isNewUser || isRegistering) "Registro" else "Login",
                    showBackButton = isNewUser || isRegistering, // ✅ NUEVO
                    onBackClick = {
                        isRegistering = false
                        onBackToLogin()
                    }
                )
                Spacer(modifier = Modifier.height(15.dp))
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 30.dp)
                ) {
                    if (!isNewUser) {
                        SocialMediaSection(
                            onGoogleClick = onGoogleClick,
                            email = email,
                            password = password,
                            onEmailChange = onEmailChange,
                            onPasswordChange = onPasswordChange,
                            onEmailSignIn = onEmailSignIn,
                            onEmailSignUp = {
                                isRegistering = true
                                onEmailSignUp()
                            },
                            onForgotPassword = {
                                println("🔥 DEBUG: onForgotPassword called with email: '$email'")

                                onForgotPassword(email) // ✅ CAMBIAR: Siempre llamar, que el ViewModel valide
                            }
                        )


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
                                        .clickable {
                                            isRegistering =
                                                true // ✅ AÑADIR: Marcar como registrando
                                            onRegisterClick()
                                        }
                                        .padding(vertical = 8.dp)
                                )
                            }
                        }
                    } else {
                        // Pantalla de Registro - completar perfil
                        RegistrationSection(
                            onCompleteRegistration = onCompleteRegistration,
                            errorMsg = errorMsg,
                            selectedDate = selectedDate,
                            onDateSelected = { selectedDate = it },
                            onShowDatePicker = { showDatePicker = true },
                            // ✅ NUEVOS PARÁMETROS:
                            initialEmail = email,
                            initialPassword = password,
                            onEmailChange = onEmailChange,
                            onPasswordChange = onPasswordChange
                        )
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
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onShowDatePicker: () -> Unit,
    initialEmail: String = "",
    initialPassword: String = "",
    onEmailChange: (String) -> Unit = {},
    onPasswordChange: (String) -> Unit = {}
) {
    var email by remember { mutableStateOf(initialEmail) }
    var password by remember { mutableStateOf(initialPassword) }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf<Gender?>(null) }
    var selectedUserType by remember { mutableStateOf<UserRole?>(null) }

    val isFormValid = email.isNotBlank() &&
            password.isNotBlank() &&
            firstName.isNotBlank() &&
            lastName.isNotBlank() &&
            selectedDate != null &&
            selectedGender != null &&
            selectedUserType != null

    val defaultColors = TextFieldDefaults.outlinedTextFieldColors(
        focusedBorderColor = traiBlue,
        unfocusedBorderColor = Color.Gray,
        cursorColor = traiBlue,
        textColor = Color.Black,
        focusedLabelColor = traiBlue,           // Label cuando tiene foco
        unfocusedLabelColor = Color.Black,      // Label cuando no tiene foco
        placeholderColor = Color.Gray,          // Placeholder más suave
        disabledLabelColor = Color.Black,       // Label cuando está deshabilitado
        errorLabelColor = Color.Red,            // Label cuando hay error
        backgroundColor = Color.Transparent     // Fondo transparente
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
                value = email,
                onValueChange = { newEmail ->
                    email = newEmail
                    onEmailChange(newEmail) // ✅ Propagar el cambio al padre
                },
                label = {
                    Text(
                        text = "Email",
                        color = if (email.isEmpty()) Color.Black else traiBlue
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = defaultColors
            )
        }

        item {
            OutlinedTextField(
                value = password,
                onValueChange = { newPassword ->
                    password = newPassword
                    onPasswordChange(newPassword) // ✅ Propagar el cambio al padre
                },
                label = {
                    Text(
                        text = "Contraseña",
                        color = if (password.isEmpty()) Color.Black else traiBlue
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                colors = defaultColors
            )
        }
        item {
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = {
                    Text(
                        text = "Nombre",
                        color = if (firstName.isEmpty()) Color.Black else traiBlue // Negro cuando vacío, azul cuando tiene contenido
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = defaultColors,
                keyboardOptions = KeyboardOptions.Default.copy( // ✅ AÑADIR: Configuración del teclado
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
            )
        }

        item {
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = {
                    Text(
                        text = "Apellido",
                        color = if (lastName.isEmpty()) Color.Black else traiBlue // Negro cuando vacío, azul cuando tiene contenido
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = defaultColors,
                keyboardOptions = KeyboardOptions.Default.copy( // ✅ AÑADIR: Configuración del teclado
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
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
    onGoogleClick: () -> Unit,
    email: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onEmailSignIn: () -> Unit,
    onEmailSignUp: () -> Unit,
    onForgotPassword: () -> Unit = {}

) {
    val defaultColors = TextFieldDefaults.outlinedTextFieldColors(
        focusedBorderColor = traiBlue,
        unfocusedBorderColor = Color.Gray,
        cursorColor = traiBlue,
        textColor = Color.Black,
        focusedLabelColor = traiBlue,
        unfocusedLabelColor = Color.Black,
        placeholderColor = Color.Gray,
        backgroundColor = Color.Transparent
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Campos de email y contraseña
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = {
                Text(
                    text = "Email",
                    color = if (email.isEmpty()) Color.Black else traiBlue
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = defaultColors
        )

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = {
                Text(
                    text = "Contraseña",
                    color = if (password.isEmpty()) Color.Black else traiBlue
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            colors = defaultColors
        )

        // Botones de acción
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ElevatedButton(
                onClick = onEmailSignIn,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = traiBlue,
                    contentColor = Color.White
                )
            ) {
                Text("Ingresar")
            }

            ElevatedButton(
                onClick = onEmailSignUp,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = Color.Gray,
                    contentColor = Color.White
                )
            ) {
                Text("Registrarse")
            }
        }
        TextButton(

            onClick = {
                println("🔥 DEBUG: TextButton clicked!") // ✅ AÑADIR: Debug para verificar

                onForgotPassword() },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(
                text = "¿Olvidaste tu contraseña?",
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium

            )
        }

        // Divider
        Text(
            text = "O",
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 8.dp)
        )





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
private fun TopSection(
    title: String = "Login",
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {}
) {
    val uiColor = if (isSystemInDarkTheme()) Color.White else Black

    Box(
        contentAlignment = Alignment.TopCenter
    ) {
        if (showBackButton) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, top = 16.dp)
                    .zIndex(1f)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver al login",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(fraction = 0.38f),
            painter = painterResource(id = R.drawable.shape),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.Black) // Aplica un tinte negro

        )
        Row(
            modifier = Modifier.padding(top = 60.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(75.dp),
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
            text = title,
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