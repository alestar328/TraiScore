package com.develop.traiscore.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AddExerciseDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit // nombre, categoría
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank() && category.isNotBlank()) {
                    onSave(name, category)
                    onDismiss()
                }
            }) {
                Text("Guardar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.White)
            }
        },
        title = {
            Text("Nuevo Ejercicio", fontSize = 20.sp, color = Color.Cyan)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoría") },
                    singleLine = true
                )
            }
        },
        containerColor = Color.DarkGray,
        shape = RoundedCornerShape(16.dp)
    )
}