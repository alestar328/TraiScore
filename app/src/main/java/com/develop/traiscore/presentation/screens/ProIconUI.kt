package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun ProIconUI(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Yellow,
    textColor: Color = Color.Black,
    fontSize: TextUnit = 24.sp,
    cornerRadius: Dp = 12.dp,
    elevation: Dp = 4.dp
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(cornerRadius),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(horizontal = 5.dp, vertical = 4.dp)
        ) {
            Text(
                text = "PRO",
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Ejemplo de uso:
@Preview(showBackground = true)
@Composable
fun ProIconUIPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Versión básica
        ProIconUI(
            onClick = { /* Acción al hacer clic */ }
        )

        // Versión personalizada
        ProIconUI(
            onClick = { /* Acción al hacer clic */ },
            modifier = Modifier.size(120.dp, 50.dp),
            backgroundColor = Color(0xFFFFD700), // Amarillo dorado
            textColor = Color.Black,
            fontSize = 20.sp,
            cornerRadius = 16.dp,
            elevation = 8.dp
        )

        // Versión compacta
        ProIconUI(
            onClick = { /* Acción al hacer clic */ },
            modifier = Modifier.size(80.dp, 40.dp),
            fontSize = 16.sp,
            cornerRadius = 8.dp
        )
    }
}