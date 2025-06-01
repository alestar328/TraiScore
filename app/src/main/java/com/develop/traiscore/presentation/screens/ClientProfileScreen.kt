package com.develop.traiscore.presentation.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.develop.traiscore.core.Gender
import com.develop.traiscore.data.local.entity.UserEntity
import com.develop.traiscore.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientProfileScreen(
    client: UserEntity,
    onBack: () -> Unit,
    onStatsClick: (String) -> Unit, // Pasamos el UID del cliente
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Perfil del Cliente",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = navbarDay
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(traiBackgroundDay)
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Foto de perfil
            ProfileImageSection(client = client)

            Spacer(modifier = Modifier.height(24.dp))

            // Información personal
            PersonalInfoCard(client = client)

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de Estadísticas
            ActionButton(
                text = "Ver Estadísticas",
                icon = Icons.Default.Star,
                onClick = { onStatsClick(client.uid) },
                containerColor = traiBlue
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de Rutinas (futuro)
            ActionButton(
                text = "Rutinas Asignadas",
                icon = Icons.Default.Person,
                onClick = { /* TODO: Implementar rutinas */ },
                containerColor = traiOrange
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de Medidas Corporales (futuro)
            ActionButton(
                text = "Medidas Corporales",
                icon = Icons.Default.Person,
                onClick = { /* TODO: Implementar medidas */ },
                containerColor = Color(0xFF4CAF50)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Información adicional
            AdditionalInfoCard(client = client)
        }
    }
}

@Composable
private fun ProfileImageSection(client: UserEntity) {
    Box(
        modifier = Modifier
            .size(120.dp)
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
                modifier = Modifier.size(60.dp),
                tint = Color.Gray
            )
        }
    }
}

@Composable
private fun PersonalInfoCard(client: UserEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Nombre completo
            Text(
                text = client.getFullName(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Información en filas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Edad
                InfoColumn(
                    label = "Edad",
                    value = "${client.getApproximateAge() ?: "N/A"} años",
                    icon = Icons.Default.Person
                )

                // Género
                InfoColumn(
                    label = "Género",
                    value = when (client.gender) {
                        Gender.MALE -> "Masculino"
                        Gender.FEMALE -> "Femenino"
                        Gender.OTHER -> "Otro"
                        null -> "N/A"
                    },
                    icon = Icons.Default.Person
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Email
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Email,
                    contentDescription = null,
                    tint = traiBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = client.email,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun InfoColumn(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = traiBlue,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 16.sp,
            color = Color.Black,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    containerColor: Color
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun AdditionalInfoCard(client: UserEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Información Adicional",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Fecha de registro
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Cliente desde:",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = dateFormat.format(client.createdAt.toDate()),
                    fontSize = 14.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Estado:",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (client.isActive) Color(0xFF4CAF50) else Color.Red
                            )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (client.isActive) "Activo" else "Inactivo",
                        fontSize = 14.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}