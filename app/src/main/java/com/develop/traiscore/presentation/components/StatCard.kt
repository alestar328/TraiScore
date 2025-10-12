package com.develop.traiscore.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.develop.traiscore.presentation.theme.tsColors

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.tsColors.ledCyan,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}
@Preview(
    showBackground = true,
    name = "StatCard - Single",
    apiLevel = 33 // Usar API 33 para evitar problemas de compatibilidad
)
@Composable
fun StatCardPreview() {
    MaterialTheme {
        StatCard(
            title = "Ejercicios",
            value = "15",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(
    showBackground = true,
    name = "StatCard - Row of Cards",
    apiLevel = 33,
    device = "spec:width=411dp,height=891dp" // Especificar dispositivo
)
@Composable
fun StatCardRowPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Ejercicios",
                    value = "8",
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Series",
                    value = "24",
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Reps",
                    value = "180",
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Peso Total",
                    value = "2000 kg",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// Alternativa: Preview simplificado sin texto complejo
@Preview(showBackground = true, name = "StatCard - Simple")
@Composable
fun StatCardSimplePreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Version simplificada del StatCard para evitar problemas de rendering
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Usar BasicText o alternativas más simples
                Text(
                    text = "15",
                    color = Color.Cyan,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = "Ejercicios",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// Preview con tema personalizado limitado
@Preview(showBackground = true, name = "StatCard - Custom Theme")
@Composable
fun StatCardCustomPreview() {
    // Usar tema básico para evitar dependencias complejas
    MaterialTheme(
        colorScheme = lightColorScheme(
            surface = Color.White,
            onSurface = Color.Black,
            background = Color.Gray
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Cards con valores fijos y simples
            StatCardSimple(title = "Peso Max", value = "120kg")
            StatCardSimple(title = "Series", value = "45")
            StatCardSimple(title = "Tiempo", value = "2.5h")
        }
    }
}

// Componente auxiliar simplificado
@Composable
private fun StatCardSimple(
    title: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = Color.Blue,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = title,
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}