package com.develop.traiscore.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import com.develop.traiscore.presentation.theme.traiBlue

@Composable
fun RIRSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxValue: Int = 4,
    thumbColor: Color = traiBlue,
    trackColor: Color = Color.LightGray,
) {
    var sliderWidth by remember { mutableFloatStateOf(0f) }
    val thumbRadius = 24f

    val animatedThumbX by animateFloatAsState(
        targetValue = (value.toFloat() / maxValue) * sliderWidth,
        label = "Thumb Animation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 16.dp)
            .pointerInput(sliderWidth) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val touchX = change.position.x.coerceIn(0f, sliderWidth)
                    val newValue = ((touchX / sliderWidth) * maxValue).toInt().coerceIn(0, maxValue)
                    onValueChange(newValue)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            sliderWidth = size.width
            val centerY = size.height / 2
            val thumbX = (value.toFloat() / maxValue) * sliderWidth

            drawLine(
                color = trackColor,
                start = Offset(0f, centerY),
                end = Offset(sliderWidth, centerY),
                strokeWidth = 8f
            )

            drawCircle(
                color = thumbColor,
                radius = thumbRadius,
                center = Offset(animatedThumbX, centerY)
            )
        }
    }
}
