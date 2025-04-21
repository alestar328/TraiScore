package com.develop.traiscore.presentation.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun AddRestButton(
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    var showAltButton by remember { mutableStateOf(false) }

    val gestureModifier = Modifier.pointerInput(Unit) {
        detectTapGestures(
            onLongPress = {
                showAltButton = true
            },
            onTap = {
                onAdd()
            }
        )
    }

    Box {
        // Botón de eliminar flotante que aparece solo si el usuario hizo long press
        if (showAltButton) {
            FloatingActionButton(
                onClick = {
                    showAltButton = false
                    onRemove()
                },
                containerColor = Color.Red,
                modifier = Modifier
                    .padding(bottom = 130.dp) // posición arriba del botón principal
                    .align(Alignment.BottomEnd)
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar ejercicio")
            }
        }

        // Botón principal con + (agregar)
        FloatingActionButton(
            onClick = { onAdd() },
            containerColor = com.develop.traiscore.presentation.theme.traiBlue,
            modifier = gestureModifier
                .padding(bottom = 60.dp)
                .align(Alignment.BottomEnd)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Agregar ejercicio")
        }
    }
}