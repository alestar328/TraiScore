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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import com.develop.traiscore.presentation.components.general.PricingCardUI
import com.develop.traiscore.presentation.theme.traiBlue


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PricingScreenUI(
    onProIconClick: () -> Unit = {},
    onSkipClick: () -> Unit = {},
    onSubscribeClick: (String) -> Unit = {}, // Callback para suscripción
    onNotNowClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Estado para manejar qué plan está seleccionado
    var selectedPlan by remember { mutableStateOf("Anual") } // Por defecto anual

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    // 👇 Spacer para balancear el espacio izquierdo
                    Spacer(modifier = Modifier.width(64.dp))
                    // (ajusta el valor según el ancho de "Omitir")
                },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Texto "TraiScore"
                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    style = SpanStyle(
                                        color = traiBlue // 👈 Color para "Trai"
                                    )
                                ) {
                                    append("Trai")
                                }
                                withStyle(
                                    style = SpanStyle(
                                        color = Color.White // 👈 Color para "Score"
                                    )
                                ) {
                                    append("Score")
                                }
                                append(" ") // espacio entre Score y PRO
                                withStyle(
                                    style = SpanStyle(
                                        color = Color.Yellow // 👈 Color para "PRO"
                                    )
                                ) {
                                    append("Pro")
                                }
                            },
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
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
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                item {
                    AdvantagesList()
                }

                // Sección de cards de pricing
                item {
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Elige tu plan",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )

                    // Row con las dos cards de pricing
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Card Mensual
                        PricingCardUI(
                            planName = "Pro",
                            period = "Mensual",
                            price = "2,99",
                            billingInfo = "Facturado mensualmente",
                            isSelected = selectedPlan == "Mensual",
                            onClick = { selectedPlan = "Mensual" }
                        )

                        // Card Anual
                        PricingCardUI(
                            planName = "Pro",
                            period = "Anual",
                            price = "29,99",
                            billingInfo = "Facturado anualmente",
                            savings = "6,89€",
                            isSelected = selectedPlan == "Anual",
                            onClick = { selectedPlan = "Anual" }
                        )
                    }
                }

                // Botón de suscripción
                item {
                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { onSubscribeClick(selectedPlan) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = traiBlue
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Suscribirse al plan $selectedPlan",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = Color.White
                        )
                    }
                }

                // Texto "Ahora no" clickable
                item {
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Ahora no",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier
                            .clickable { onNotNowClick() }
                            .padding(vertical = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }

                // Texto informativo sobre cancelación
                item {
                    Text(
                        text = "Cancela tu suscripción en cualquier momento",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
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
            onSkipClick = { /* Acción Omitir */ },
            onSubscribeClick = { plan -> /* Acción Suscribirse: $plan */ },
            onNotNowClick = { /* Acción Ahora no */ }
        )
    }
}