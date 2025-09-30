package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.develop.traiscore.R
import com.develop.traiscore.presentation.components.TraiScoreTopBar
import com.develop.traiscore.presentation.navigation.NavigationRoutes
import com.develop.traiscore.presentation.theme.traiYellow
import com.develop.traiscore.presentation.theme.tsColors

@Composable
fun MyHealthWithIAScreen(
    navController: NavHostController,
    onScanDocument: (() -> Unit)? = null,
    onUploadDocument: (() -> Unit)? = null
) {
    Scaffold(
        topBar = {
            TraiScoreTopBar(
                leftIcon = {
                    androidx.compose.material3.IconButton(onClick = { navController.popBackStack() }) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = MaterialTheme.tsColors.ledCyan
                        )
                    }
                }, // (opcional) puedes añadir back si tu TopBar lo soporta
                rightIcon = {
                    Spacer(modifier = Modifier.size(48.dp))

                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .navigationBarsPadding()
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Botón 1: Escanear documento
            ProfileButton(
                text = "Escanear documento",
                iconSize = 36.dp,
                modifier = Modifier
                    .fillMaxWidth(),
                containerColor = Color.White, // mismo look&feel
                contentColor = Color.Black,
                painter = painterResource(id = R.drawable.scan_text_icon),
                onClick = { navController.navigate(NavigationRoutes.CameraScan.route) }
            )

            Spacer(Modifier.height(20.dp))

            // Botón 2: Cargar documento
            ProfileButton(
                text = "Cargar documento",
                iconSize = 36.dp,
                modifier = Modifier
                    .fillMaxWidth(),
                containerColor = traiYellow,
                contentColor = Color.Black,
                painter = painterResource(id = R.drawable.upload_doc),
                onClick = { onUploadDocument?.invoke() }
            )

        }
    }
}