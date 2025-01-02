package com.develop.traiscore.presentation.components

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.develop.traiscore.presentation.theme.traiBlue
import androidx.compose.ui.Modifier

@Composable
fun RIRSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxValue: Int = 10,
    color: Color = traiBlue
) {
    // Convertimos el valor del slider para que vaya de 0 a maxValue
    val sliderValue = remember { mutableStateOf(value.toFloat()) }

    Slider(
        value = sliderValue.value,
        onValueChange = { newValue ->
            sliderValue.value = newValue
            onValueChange(newValue.toInt())
        },
        valueRange = 1f..maxValue.toFloat(),
        steps = maxValue - 1, // Divisiones del slider
        colors = SliderDefaults.colors(
            thumbColor = color, // Color del deslizador circular
            activeTrackColor = color, // Color de la barra activa
            inactiveTrackColor = Color.Gray // Color de la barra inactiva
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp) // Espaciado lateral opcional
    )
}