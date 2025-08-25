package com.develop.traiscore.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.develop.traiscore.data.local.entity.WorkoutEntry
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.develop.traiscore.R


@Composable
fun SessionWorkoutCard(
    sessionName: String,
    sessionColor: String,
    workouts: List<WorkoutEntry>,
    isActive: Boolean = false,
    onEditClick: (WorkoutEntry) -> Unit,
    onDeleteClick: (WorkoutEntry) -> Unit,
    onExpandClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    fun hexToColor(hex: String): Color {
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: Exception) {
            Color(0xFF355E58) // Color por defecto
        }
    }

    val hexColor = hexToColor(sessionColor)
    val backgroundColor = if (isActive) {
        hexColor.copy(alpha = 0.15f)
    } else {
        Color.DarkGray.copy(alpha = 0.8f)
    }

    val headerColor = if (isActive) hexColor else Color.Gray

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(8.dp)
    ) {
        // Header de la sesión
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .clickable { if (!isActive) onExpandClick() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Círculo del icono
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(headerColor.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.pesa_icon),
                    contentDescription = "Sesión",
                    tint = headerColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sessionName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isActive) Color.Black else Color.White
                )
                Text(
                    text = "${workouts.size} ejercicios",
                    fontSize = 12.sp,
                    color = if (isActive) Color.Black.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.7f)
                )
            }

            if (isActive) {
                Text(
                    text = "ACTIVA",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = headerColor,
                    modifier = Modifier
                        .background(
                            color = headerColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // Lista de ejercicios
        WorkoutCardList(
            workouts = workouts,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick
        )
    }
}