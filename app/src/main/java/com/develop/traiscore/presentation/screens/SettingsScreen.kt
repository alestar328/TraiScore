package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.develop.traiscore.R
import com.develop.traiscore.data.firebaseData.saveExerciseToFirebase
import com.develop.traiscore.presentation.components.AddExerciseDialogToDB
import com.develop.traiscore.presentation.components.AutoResizedText
import com.develop.traiscore.presentation.theme.TraiScoreTheme
import com.develop.traiscore.presentation.viewmodels.AddExerciseViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onNavigateToScreenMode: () -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }
    val viewModel: AddExerciseViewModel = hiltViewModel()
    val coroutineScope = rememberCoroutineScope()

    if (showDialog) {
        AddExerciseDialogToDB(
            onDismiss = { showDialog = false },
            onSave = { name, category ->
                // ✅ CORRECCIÓN: Llamar desde coroutine y usar método del ViewModel
                coroutineScope.launch {
                    viewModel.saveExerciseToDatabase(name, category)
                }
            }
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Logo en lugar de texto
                    Image(
                        painter = painterResource(id = R.drawable.trailogoup),
                        contentDescription = "Logo cabecera",
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.DarkGray, // Fondo de la barra
                    titleContentColor = MaterialTheme.colorScheme.onSurface // Color del texto
                )
            )
        },
        content = { paddingValues ->
            // Contenido principal
            Column(
                modifier = Modifier
                    .background(Color.DarkGray)
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(TraiScoreTheme.dimens.paddingMedium)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AutoResizedText(
                    text = "Ajustes",
                    textStyle = MaterialTheme.typography.displayMedium.copy(
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(TraiScoreTheme.dimens.spacerMedium))
                SettingsOptionRow(
                    icon = { Icon(Icons.Default.Add, contentDescription = "Añadir ejercicio", tint = Color.Cyan) },
                    label = "Añadir ejercicio",
                    onClick = { showDialog = true }
                )
                SettingsOptionRow(
                    icon = { Icon(Icons.Default.Star, contentDescription = "Cambiar idioma", tint = Color.Cyan) },
                    label = "Cambiar idioma",
                    onClick = { /* TODO */ }
                )

                SettingsOptionRow(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Datos personales", tint = Color.Cyan) },
                    label = "Datos personales",
                    onClick = { /* TODO */ }
                )
                SettingsOptionRow(
                    icon = { Icon(Icons.Default.Build, contentDescription = "Modo", tint = Color.Cyan) },
                    label = "Cambiar modo de pantalla",
                    onClick = { onNavigateToScreenMode()}
                )
            }

        }
    )


}
@Composable
fun SettingsOptionRow(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.padding(start = 16.dp)) {
            icon()
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
@Preview(
    name = "SettingsScreenPreview",
    showBackground = true
)
@Composable
fun SettingsScreenPreview() {
    TraiScoreTheme {
        SettingsScreen()
    }
}