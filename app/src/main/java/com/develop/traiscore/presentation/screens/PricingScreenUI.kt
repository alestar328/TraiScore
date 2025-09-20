package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PricingScreenUI(
    onProIconClick: () -> Unit = {},
    onSkipClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Texto "TraiScore"
                        Text(
                            text = "TraiScore",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // ProIconUI al lado del texto
                        ProIconUI(
                            onClick = onProIconClick,
                            fontSize = 13.sp,

                            )
                    }
                },
                actions = {
                    // Botón "Omitir" en el lado derecho
                    Text(
                        text = "Omitir",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable { onSkipClick() }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        content = { paddingValues ->
            // Contenido principal de la pantalla de precios
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Ventajas del Plan Pro",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                item {
                    AdvantagesList()
                }
            }
        }
    )
}


// Estructura de datos para las ventajas
data class AdvantageData(
    val icon: Any, // Puede ser un ImageVector o un ID de drawable (Int)
    val advantage: String,
    val limitation: String
)

// Composable para una tarjeta de ventaja individual
@Composable
fun AdvantageCard(
    advantageData: AdvantageData,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono a la izquierda
            when (advantageData.icon) {
                is ImageVector -> {
                    Icon(
                        imageVector = advantageData.icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                is Int -> {
                    Icon(
                        painter = painterResource(id = advantageData.icon),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Contenido de texto a la derecha
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Ventaja principal (en negrita)
                Text(
                    text = advantageData.advantage,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Limitación actual (sin negrita)
                Text(
                    text = advantageData.limitation,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// Lista de ventajas
@Composable
fun AdvantagesList(
    modifier: Modifier = Modifier
) {
    val advantages = listOf(
        AdvantageData(
            icon = Icons.Default.Star,
            advantage = "Rutinas ilimitadas",
            limitation = "Límite gratuito: 4 rutinas"
        ),
        AdvantageData(
            icon = Icons.Default.Favorite,
            advantage = "Estadísticas avanzadas",
            limitation = "Límite gratuito: Estadísticas básicas"
        ),
        AdvantageData(
            icon = Icons.Default.ShoppingCart,
            advantage = "Sincronización en la nube",
            limitation = "Límite gratuito: Solo almacenamiento local"
        ),
        AdvantageData(
            icon = Icons.Default.AccountCircle,
            advantage = "Clientes ilimitados",
            limitation = "Límite gratuito: Máximo 3 clientes"
        ),
        AdvantageData(
            icon = Icons.Default.ShoppingCart,
            advantage = "Exportación de datos",
            limitation = "Límite gratuito: No disponible"
        ),
        AdvantageData(
            icon = Icons.Default.Call,
            advantage = "Soporte prioritario",
            limitation = "Límite gratuito: Soporte comunitario"
        )
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        advantages.forEach { advantage ->
            AdvantageCard(advantageData = advantage)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PricingScreenUIPreview() {
    MaterialTheme {
        PricingScreenUI(
            onProIconClick = { /* Acción ProIcon */ },
            onSkipClick = { /* Acción Omitir */ }
        )
    }
}
