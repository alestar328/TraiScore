package com.develop.traiscore.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.develop.traiscore.presentation.theme.traiOrange

@Composable
fun CircularProgressView(
    progress: Float, // Progreso como porcentaje (0f a 1f)
    maxLabel: String, // Etiqueta dentro del círculo (como "54Kg")
    modifier: Modifier = Modifier,
    strokeColor: Color = Color.Cyan,
    backgroundColor: Color = traiOrange,
    strokeWidth: Float = 13f
) {
    // Animar el progreso
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(progress) {
        val validProgress = when {
            progress.isNaN() || progress.isInfinite() -> 0f
            progress < 0f -> 0f
            progress > 1f -> 1f
            else -> progress
        }

        animatedProgress.animateTo(
            targetValue = validProgress,
            animationSpec = tween(durationMillis = 1000)
        )
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.size(100.dp)) {
            // Fondo del círculo
            drawArc(
                color = backgroundColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            // Progreso
            drawArc(
                color = strokeColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress.value,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Etiqueta dentro del círculo
        Text(
            text = maxLabel,
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            color = strokeColor
        )
    }
}
@Preview
@Composable
fun CircularProgressViewPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressView(
            progress = 0.75f, // 75% de progreso
            maxLabel = "54Kg",
            modifier = Modifier.size(120.dp),
            strokeColor = Color.Cyan,
            backgroundColor = traiOrange
        )
    }
}