package com.develop.traiscore.presentation.components.bodyMeasurements

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.develop.traiscore.presentation.theme.*


@Composable
fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = traiBlue)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Cargando historial...", color = Color.White)
        }
    }
}