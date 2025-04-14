package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.develop.traiscore.presentation.theme.traiBlue

@Composable
fun RoutineMenu(
    onRoutineClick: (String) -> Unit,
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.DarkGray)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            RoutineItem("Rutina Pecho", Color.Red, onClick = { onRoutineClick("Rutina Pecho") })
            RoutineItem("Rutina Piernas", Color(0xFFCDDC39), onClick = { onRoutineClick("Rutina Piernas") }) // lime
            RoutineItem("Rutina Espalda", Color(0xFF3F51B5), onClick = { onRoutineClick("Rutina Espalda") }) // blue
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clickable(onClick = onAddClick)
                    .background(color = traiBlue, shape = MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar rutina",
                    tint = Color.Black

                )
            }
        }
    }
}

@Composable
fun RoutineItem(name: String, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onClick)
            .background(color)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = name, fontSize = 25.sp, color = Color.Black)
        Icon(imageVector = Icons.Default.Add, contentDescription = "Ir a rutina", tint = Color.White)
    }
}

@Preview(showBackground = true)
@Composable
fun RoutineMenuPreview() {
    RoutineMenu(
        onRoutineClick = { println("Clicked: $it") },
        onAddClick = { println("Add new routine") }
    )
}