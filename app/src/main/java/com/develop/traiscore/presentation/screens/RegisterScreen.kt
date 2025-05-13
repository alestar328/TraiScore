package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.develop.traiscore.presentation.theme.TraiScoreTheme
import com.develop.traiscore.presentation.theme.Black
import com.develop.traiscore.presentation.theme.Roboto
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import com.develop.traiscore.presentation.theme.traiBlue

@Composable
fun RegisterScreen(
    name: String,
    onNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    lastName: String,
    onLastNameChange: (String) -> Unit,
    birthDate: String,
    onBirthDateChange: (String) -> Unit,
    onPhotoClick: () -> Unit,
    isTrainer: Boolean,
    isAtleta: Boolean,
    gender: String?,
    onGenderSelect: (String) -> Unit,
    height: String,
    onHeightChange: (String) -> Unit,
    weight: String,
    onWeightChange: (String) -> Unit,
    neck: String,
    onNeckChange: (String) -> Unit,
    chest: String,
    onChestChange: (String) -> Unit,
    arms: String,
    onArmsChange: (String) -> Unit,
    waist: String,
    onWaistChange: (String) -> Unit,
    thigh: String,
    onThighChange: (String) -> Unit,
    calf: String,
    onCalfChange: (String) -> Unit,
    onTrainerClick: () -> Unit,
    onAtletaClick: () -> Unit,
    onRegisterClick:() -> Unit
) {
    Surface(
        modifier = Modifier
        .fillMaxSize(),
        color = Color.DarkGray,
       ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Registro",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 32.sp,
                        fontFamily = Roboto
                    ),
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
            item {
                OutlinedTextField(
                    value = email,
                    onValueChange = onNameChange,
                    label = {
                        Text(
                            text = "Email",
                            color = Color.White
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = traiBlue,
                        unfocusedBorderColor = traiBlue,
                        focusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = {
                        Text(
                            text = "Nombre",
                            color = Color.White
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = traiBlue,
                        unfocusedBorderColor = traiBlue
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = lastName,
                    onValueChange = onLastNameChange,
                    label = {
                        Text(
                            text = "Apellido",
                            color = Color.White
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = traiBlue,
                        unfocusedBorderColor = traiBlue
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = birthDate,
                    onValueChange = onBirthDateChange,
                    label = {
                        Text(
                            text = "Fecha nacimiento",
                            color = Color.White
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = traiBlue,
                        unfocusedBorderColor = traiBlue
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(8.dp),

                )
            }
            item {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .padding(bottom = 24.dp)
                        .clickable(onClick = onPhotoClick)
                        .background(
                            color = Color(0xFFF0F0F0),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Foto",
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onTrainerClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Trainer",
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = onAtletaClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Atleta",
                            color = Color.White
                        )
                    }
                }

            }
            item {
                // Campos adicionales para Atleta
                if (isAtleta) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Sexo",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onGenderSelect("male") }
                        ) {
                            RadioButton(
                                selected = gender == "male",
                                onClick = { onGenderSelect("male") }
                            )
                            Text(text = "Hombre",
                                color = Color.White)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onGenderSelect("female") }
                        ) {
                            RadioButton(
                                selected = gender == "female",
                                onClick = { onGenderSelect("female") }
                            )
                            Text(text = "Mujer",
                                color = Color.White)
                        }
                    }

                    Text(
                        text = "Medidas",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = height,
                                onValueChange = onHeightChange,
                                label = {
                                    Text(
                                        text = "Altura",
                                        color = Color.White
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = traiBlue,
                                    unfocusedBorderColor = traiBlue
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = neck,
                                onValueChange = onNeckChange,
                                label = {
                                    Text(
                                        text = "Cuello",
                                        color = Color.White
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = traiBlue,
                                    unfocusedBorderColor = traiBlue
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = arms,
                                onValueChange = onArmsChange,
                                label = {
                                    Text(
                                        text = "Brazos",
                                        color = Color.White
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = traiBlue,
                                    unfocusedBorderColor = traiBlue
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = thigh,
                                onValueChange = onThighChange,
                                label = {
                                    Text(
                                        text = "Muslo",
                                        color = Color.White
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = traiBlue,
                                    unfocusedBorderColor = traiBlue
                                ),
                                modifier = Modifier
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = weight,
                                onValueChange = onWeightChange,
                                label = {
                                    Text(
                                        text = "Peso",
                                        color = Color.White
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = traiBlue,
                                    unfocusedBorderColor = traiBlue
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = chest,
                                onValueChange = onChestChange,
                                label = {
                                    Text(
                                        text = "Pecho",
                                        color = Color.White
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = traiBlue,
                                    unfocusedBorderColor = traiBlue
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = waist,
                                onValueChange = onWaistChange,
                                label = {
                                    Text(
                                        text = "Cintura",
                                        color = Color.White
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = traiBlue,
                                    unfocusedBorderColor = traiBlue
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = calf,
                                onValueChange = onCalfChange,
                                label = {
                                    Text(
                                        text = "Pantorrilla",
                                        color = Color.White
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = traiBlue,
                                    unfocusedBorderColor = traiBlue
                                ),
                                modifier = Modifier
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onRegisterClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Registrar",
                            color = traiBlue
                        )
                    }

                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    TraiScoreTheme {
        RegisterScreen(
            name = "",
            email = "",
            onEmailChange = {},
            onNameChange = {},
            lastName = "",
            onLastNameChange = {},
            birthDate = "",
            onBirthDateChange = {},
            onPhotoClick = {},
            isTrainer = false,
            isAtleta = true,
            gender = null,
            onGenderSelect = {},
            height = "",
            onHeightChange = {},
            weight = "",
            onWeightChange = {},
            neck = "",
            onNeckChange = {},
            chest = "",
            onChestChange = {},
            arms = "",
            onArmsChange = {},
            waist = "",
            onWaistChange = {},
            thigh = "",
            onThighChange = {},
            calf = "",
            onCalfChange = {},
            onTrainerClick = {},
            onAtletaClick = {},
            onRegisterClick = {}
        )
    }
}
