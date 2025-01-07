package com.develop.traiscore.presentation.components

import android.text.TextPaint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.min
import androidx.compose.ui.graphics.Path
import kotlin.math.sin
import kotlin.math.cos
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp


@Composable
fun RadarChartView(
    dataPoints: List<Int>, // Datos de entrada: número de series por ejercicio
    maxValue: Int, // Máximo valor
    categories: List<String>, // Nombres de las categorías
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val centerPoint = Offset(centerX, centerY)
        val radius = min(size.width, size.height) / 2 - 40.dp.toPx()

        // Dibujar el polígono de fondo
        drawBackgroundPolygon(centerPoint, radius, categories.size, Color.LightGray.copy(alpha = 0.1f))

        // Dibujar el polígono de datos
        drawDataPolygon(centerPoint, radius, dataPoints, maxValue, categories.size, Color.Cyan.copy(alpha = 0.3f))

        // Dibujar etiquetas de las categorías
        drawCategoryLabels(centerPoint, radius, categories)
    }
}
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBackgroundPolygon(
    center: androidx.compose.ui.geometry.Offset,
    radius: Float,
    sides: Int,
    color: Color
) {
    val angleStep = (2 * Math.PI / sides).toFloat()
    val path = Path()

    for (i in 0 until sides) {
        val angle = angleStep * i
        val pointX = center.x + radius * cos(angle)
        val pointY = center.y + radius * sin(angle)

        if (i == 0) {
            path.moveTo(pointX, pointY)
        } else {
            path.lineTo(pointX, pointY)
        }
    }
    path.close()

    drawPath(
        path = path,
        color = color,
        style = Fill
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDataPolygon(
    center: androidx.compose.ui.geometry.Offset,
    radius: Float,
    dataPoints: List<Int>,
    maxValue: Int,
    sides: Int,
    color: Color
) {
    val angleStep = (2 * Math.PI / sides).toFloat()
    val path = Path()

    for (i in dataPoints.indices) {
        val angle = angleStep * i
        val valueRatio = dataPoints[i].toFloat() / maxValue
        val pointX = center.x + radius * valueRatio * cos(angle)
        val pointY = center.y + radius * valueRatio * sin(angle)

        if (i == 0) {
            path.moveTo(pointX, pointY)
        } else {
            path.lineTo(pointX, pointY)
        }
    }
    path.close()

    drawPath(
        path = path,
        color = color,
        style = Fill
    )

    drawPath(
        path = path,
        color = color.copy(alpha = 1f),
        style = Stroke(width = 2.dp.toPx())
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCategoryLabels(
    center: androidx.compose.ui.geometry.Offset,
    radius: Float,
    categories: List<String>
) {
    val angleStep = (2 * Math.PI / categories.size).toFloat()

    for (i in categories.indices) {
        val angle = angleStep * i
        val labelRadius = radius + 20.dp.toPx()
        val labelX = center.x + labelRadius * cos(angle)
        val labelY = center.y + labelRadius * sin(angle)

        drawContext.canvas.nativeCanvas.drawText(
            categories[i],
            labelX - 20,
            labelY + 10,
            TextPaint().apply {
                color = Color.Cyan.toArgb()
                textSize = 12.sp.toPx()
                isAntiAlias = true
            }
        )
    }
}
@Preview
@Composable
fun RadarChartViewPreview() {
    // Datos de prueba para visualizar el componente
    val dataPoints = listOf(10, 15, 12, 8, 20) // Número de series por ejercicio
    val maxValue = 20 // Máximo número de series
    val categories = listOf("Press Banca", "Sentadilla", "Peso Muerto", "Press Militar", "Remo")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        RadarChartView(
            dataPoints = dataPoints,
            maxValue = maxValue,
            categories = categories,
            modifier = Modifier.size(300.dp) // Ajusta el tamaño según tus necesidades
        )
    }
}
