package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

@Composable
fun RegisterScreen(
    name: String,
    onNameChange: (String) -> Unit,
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
    onAtletaClick: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                text = "Registro",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 32.sp,
                    fontFamily = Roboto
                ),
                color = Black,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = {
                    Text(
                        text = "Email",
                    )},
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
                    )},
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
                    )},
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
                    )},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 24.dp)
                    .align(Alignment.CenterHorizontally)
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




            // Campos adicionales para Atleta
            if (isAtleta) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Sexo",
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
                        Text(text = "Hombre")
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onGenderSelect("female") }
                    ) {
                        RadioButton(
                            selected = gender == "female",
                            onClick = { onGenderSelect("female") }
                        )
                        Text(text = "Mujer")
                    }
                }

                Text(
                    text = "Medidas",
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
                            label = { Text(
                                text = "Altura",
                            ) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = neck,
                            onValueChange = onNeckChange,
                            label = { Text(
                                text = "Cuello",
                            ) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = arms,
                            onValueChange = onArmsChange,
                            label = { Text(
                                text = "Brazos",
                            ) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = thigh,
                            onValueChange = onThighChange,
                            label = { Text(
                                text = "Muslo",
                            ) },
                            modifier = Modifier
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = weight,
                            onValueChange = onWeightChange,
                            label = { Text(
                                text = "Peso",
                            ) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = chest,
                            onValueChange = onChestChange,
                            label = { Text(
                                text = "Pecho",
                            ) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = waist,
                            onValueChange = onWaistChange,
                            label = { Text(
                                text = "Cintura",
                            ) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = calf,
                            onValueChange = onCalfChange,
                            label = { Text(
                                text = "Pantorrilla",
                            ) },
                            modifier = Modifier
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
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
            onAtletaClick = {}
        )
    }
}
