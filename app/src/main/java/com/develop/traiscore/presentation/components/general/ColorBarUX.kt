package com.develop.traiscore.presentation.components.general

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.develop.traiscore.presentation.theme.tsColors
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip

@Composable
fun ColorBarUX(
    selectedColor: Color = MaterialTheme.tsColors.ledCyan,
    onColorSelected: (Color) -> Unit = {}
){
    val colors = listOf(
        MaterialTheme.tsColors.ledCyan,      // Tu color existente
        Color(0xFFFFB3BA),  // Rosa pastel
        Color(0xFFFFDFBA),  // Durazno pastel
        Color(0xFFFFFFBA),  // Amarillo pastel
        Color(0xFFBAFFC9),  // Verde pastel
        Color(0xFFBAE1FF),  // Azul pastel
        Color(0xFFE0BAFF)   // Lavanda pastel
    )
    val firstRow = colors.take(4)
    val secondRow = colors.drop(4)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),  // Espacio entre filas
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Primera fila
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            firstRow.forEach { color ->
                ColorCircle(
                    color = color,
                    isSelected = color == selectedColor,
                    onClick = { onColorSelected(color) }
                )
            }
        }

        // Segunda fila
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            secondRow.forEach { color ->
                ColorCircle(
                    color = color,
                    isSelected = color == selectedColor,
                    onClick = { onColorSelected(color) }
                )
            }
        }
    }
}

@Composable
private fun ColorCircle(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clickable { onClick() }
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                shape = CircleShape
            )
            .clip(CircleShape) // Move clip after border
            .background(color)
    )
}