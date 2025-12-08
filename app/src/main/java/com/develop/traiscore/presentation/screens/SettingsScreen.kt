package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.develop.traiscore.R
import com.develop.traiscore.presentation.components.AddExerciseDialogToDB
import com.develop.traiscore.presentation.components.AutoResizedText
import com.develop.traiscore.presentation.theme.TraiScoreTheme
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.viewmodels.AddExerciseViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onNavigateToScreenMode: () -> Unit = {},
    onNavigateToCreateCategory: () -> Unit = {},
    onNavigateToLanguage: () -> Unit = {},
    onConfigureTopBar: (left: @Composable () -> Unit, right: @Composable () -> Unit) -> Unit

) {
    var showDialog by remember { mutableStateOf(false) }

    val viewModel: AddExerciseViewModel = hiltViewModel()
    val coroutineScope = rememberCoroutineScope()

    if (showDialog) {
        AddExerciseDialogToDB(
            onDismiss = { showDialog = false },
            onSave = { name, category ->
                coroutineScope.launch {
                    try {
                        viewModel.saveExerciseToDatabase(name, category)
                        // El refresh ya se maneja en el callback interno
                    } catch (e: Exception) {
                        println("Error guardando ejercicio: ${e.message}")
                    }
                }
            }
        )
    }
    LaunchedEffect(Unit) {
        onConfigureTopBar(
            {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = traiBlue
                    )
                }
            },
            {
                // Espacio simétrico a la derecha
                Spacer(modifier = Modifier.size(48.dp))
            }
        )
    }
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxSize()
                    .padding(horizontal = TraiScoreTheme.dimens.paddingMedium)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Ajustes",
                    style = MaterialTheme.typography.displaySmall.copy(
                        color = Color.White,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(TraiScoreTheme.dimens.spacerMedium))

              /*PROXIMO DESARROLLO:  SettingsOptionRow(
                    painter  = rememberVectorPainter(Icons.Default.Person) ,
                    label = "Datos personales",
                    iconTint = Color.Cyan,
                    onClick = { /* TODO */ }
                )
                SettingsOptionRow(
                    painter  = painterResource(id = R.drawable.category_icon),
                    label = "Nueva categoria de ejercicio",
                    iconTint = Color.Cyan,
                    onClick = { onNavigateToCreateCategory() }
                )*/
                SettingsOptionRow(
                    painter  = painterResource(id = R.drawable.language_icon),
                    label = "Cambiar idioma",
                    iconTint = Color.Cyan,
                    onClick = { onNavigateToLanguage() }
                )

                SettingsOptionRow(
                    painter  = painterResource(id = R.drawable.color_screen_icon),
                    label = "Cambiar modo de pantalla",
                    iconTint = Color.Cyan,
                    onClick = { onNavigateToScreenMode()}
                )
            }
}
@Composable
fun SettingsOptionRow(
    painter: Painter? = null,             // 👈 Igual que ProfileButton
    label: String,
    onClick: () -> Unit,
    iconTint: Color = Color.Unspecified,  // 👈 Para poder controlar el color
    iconSize: Dp = 24.dp                  // 👈 Tamaño configurable
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (painter != null) {
            Icon(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(iconSize),
                tint = iconTint
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
