package com.develop.traiscore.presentation.components.general

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.develop.traiscore.presentation.theme.traiBlue

@Composable
fun PricingCardUI(
    planName: String,
    period: String,
    price: String,
    currency: String = "€",
    billingInfo: String,
    savings: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    var isClicked by remember { mutableStateOf(false) }

    // Animación para el offset cuando está seleccionada
    val offsetY by animateFloatAsState(
        targetValue = if (isSelected) -8f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "cardOffset"
    )

    Box(
        modifier = modifier
            .width(160.dp)            // ✅ mismo ancho que la Card
            .offset(y = offsetY.dp)   // ✅ mover badge + card juntos
    ){
        // Badge de ahorro cuando está seleccionada
        if (isSelected && savings != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = traiBlue,
                        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                    )
                    .padding(vertical = 8.dp)
                    .zIndex(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AHORRA $savings",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = Color.White
                )
            }
        }

        // Card principal
        Card(
            modifier = Modifier
                .width(160.dp)
                .height(220.dp)
                .shadow(
                    elevation = if (isSelected) 12.dp else 2.dp,
                    shape = RoundedCornerShape(12.dp),
                    ambientColor = if (isSelected) traiBlue.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.1f),
                    spotColor = if (isSelected) traiBlue.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.1f)
                )
                .clickable {
                    isClicked = !isClicked
                    onClick()
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) Color.White else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(35.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Nombre del plan
                Text(
                    text = planName,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Período
                    Text(
                        text = period,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp
                        ),
                        color = if (isSelected) Color.Gray else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )

                    // Precio con moneda
                    Text(
                        text = buildAnnotatedString {
                            append(price)
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp
                                )
                            ) {
                                append(" $currency")
                            }
                        },
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        ),
                        color = if (isSelected) traiBlue else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }

                // Información de facturación
                Text(
                    text = billingInfo,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp
                    ),
                    color = if (isSelected) Color.Gray else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PricingCardUIPreview() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card no seleccionada
        PricingCardUI(
            planName = "Pro",
            period = "Mensual",
            price = "2,99",
            billingInfo = "Facturado mensualmente",
            onClick = { }
        )

        // Card seleccionada con ahorro
        PricingCardUI(
            planName = "Pro",
            period = "Anual",
            price = "29,99",
            billingInfo = "Facturado anualmente",
            savings = "6,89€",
            onClick = { },
            isSelected = true
        )
    }
}