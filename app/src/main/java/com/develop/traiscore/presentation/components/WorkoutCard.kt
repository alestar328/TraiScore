package com.develop.traiscore.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.develop.traiscore.domain.WorkoutModel
import com.develop.traiscore.domain.WorkoutType
import com.develop.traiscore.presentation.theme.traiBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WorkoutCard(
    workout: WorkoutModel,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = traiBlue, shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Título del ejercicio y botones
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = workout.type.title.uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontSize = 18.sp
                )
                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Black)
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Black)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Detalles del ejercicio
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally // Centrado opcional
                ) {
                    Text(
                        text = "${workout.type.reps}",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold, // Destacar el número
                        fontSize = 14.sp // Tamaño más grande para el dato
                    )
                    Text(
                        text = "Repes",
                        color = Color.Black,
                        fontSize = 12.sp // Tamaño más pequeño para la etiqueta
                    )

                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally // Centrado opcional

                ) {
                    Text(
                        text = "${workout.type.weight} kg",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold, // Destacar el número
                        fontSize = 14.sp // Tamaño más grande para el dato
                    )
                    Text(
                        text = "Peso",
                        color = Color.Black,
                        fontSize = 12.sp // Tamaño más pequeño para la etiqueta
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally // Centrado opcional

                ) {
                    Text(
                        text = "${workout.type.rir}",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold, // Destacar el número
                        fontSize = 14.sp // Tamaño más grande para el dato
                    )
                    Text(
                        text = "RIR",
                        color = Color.Black,
                        fontSize = 12.sp // Tamaño más pequeño para la etiqueta
                    )
                }

                Text(
                    text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(workout.timestamp),
                    fontSize = 12.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

        }
    }
}

@Preview
@Composable
fun WorkoutCardPreview() {
    val workout = WorkoutModel(
        timestamp = Date(),
        type = WorkoutType(
            title = "Sentadillas",
            weight = 100.0,
            reps = 8,
            rir = 2
        )
    )
    WorkoutCard(
        workout = workout,
        onEditClick = { /* Acción de editar */ },
        onDeleteClick = { /* Acción de eliminar */ }
    )
}