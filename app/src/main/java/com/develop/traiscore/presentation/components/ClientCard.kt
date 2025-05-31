package com.develop.traiscore.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.develop.traiscore.core.Gender
import com.develop.traiscore.data.local.entity.UserEntity

@Composable
fun ClientCard(
    client: UserEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Foto o icono por defecto
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (client.photoURL != null) {
                    AsyncImage(
                        model = client.photoURL,
                        contentDescription = "Foto de ${client.getFullName()}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Usuario",
                        modifier = Modifier.size(36.dp),
                        tint = Color.Gray
                    )
                }
            }

            // Información del cliente
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Nombre completo
                Text(
                    text = client.getFullName(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Género y edad
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Género
                    client.gender?.let { gender ->
                        Text(
                            text = when (gender) {
                                Gender.MALE -> "Masculino"
                                Gender.FEMALE -> "Femenino"
                                Gender.OTHER -> "Otro"
                            },
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }

                    // Edad
                    client.getApproximateAge()?.let { age ->
                        Text(
                            text = "$age años",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Email (opcional, si quieres mostrarlo)
                Text(
                    text = client.email,
                    fontSize = 12.sp,
                    color = Color.Gray.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Indicador de activo/inactivo
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        if (client.isActive) Color(0xFF4CAF50) else Color.Gray
                    )
            )
        }
    }
}