package com.develop.traiscore.presentation.components

import android.text.TextPaint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.develop.traiscore.domain.model.RadarChartData
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.theme.traiOrange
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin


@Composable
fun ProgressRadarChart(
    radarData: RadarChartData,
    modifier: Modifier = Modifier,
    title: String = "Top 5 Ejercicios - Progreso"
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Radar Chart
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.DarkGray.copy(alpha = 0.3f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2
                val centerY = size.height / 2
                val centerPoint = Offset(centerX, centerY)
                val radius = min(size.width, size.height) / 2 - 60.dp.toPx()

                // Dibujar grilla de fondo (círculos concéntricos)
                drawBackgroundGrid(centerPoint, radius)

                // Dibujar líneas de ejes hacia cada categoría
                drawAxisLines(centerPoint, radius, radarData.categories.size)

                // Dibujar el polígono de datos del usuario
                drawProgressPolygon(centerPoint, radius, radarData.dataPoints, radarData.maxValue)

                // Dibujar etiquetas de categorías
                drawCategoryLabels(centerPoint, radius, radarData.categories)

                // Dibujar valores de progreso
                drawProgressValues(centerPoint, radius, radarData.dataPoints, radarData.categories)
            }
        }

        // Leyenda de progreso
        ProgressLegend(
            topExercises = radarData.topExercises.take(3), // Solo mostrar top 3 en leyenda
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

private fun DrawScope.drawBackgroundGrid(
    center: Offset,
    radius: Float
) {
    val gridLevels = listOf(0.2f, 0.4f, 0.6f, 0.8f, 1.0f)
    val gridColor = Color.Gray.copy(alpha = 0.3f)

    gridLevels.forEach { level ->
        drawCircle(
            color = gridColor,
            radius = radius * level,
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )
    }
}

private fun DrawScope.drawAxisLines(
    center: Offset,
    radius: Float,
    sides: Int
) {
    val angleStep = (2 * Math.PI / sides).toFloat()
    val axisColor = Color.Gray.copy(alpha = 0.5f)

    for (i in 0 until sides) {
        val angle = angleStep * i - (Math.PI / 2).toFloat() // Start from top
        val endX = center.x + radius * cos(angle)
        val endY = center.y + radius * sin(angle)

        drawLine(
            color = axisColor,
            start = center,
            end = Offset(endX, endY),
            strokeWidth = 1.dp.toPx()
        )
    }
}

private fun DrawScope.drawProgressPolygon(
    center: Offset,
    radius: Float,
    dataPoints: List<Int>,
    maxValue: Int
) {
    if (dataPoints.isEmpty()) return

    val angleStep = (2 * Math.PI / dataPoints.size).toFloat()
    val path = Path()

    // Crear el polígono de datos
    dataPoints.forEachIndexed { i, value ->
        val angle = angleStep * i - (Math.PI / 2).toFloat() // Start from top
        val valueRatio = value.toFloat() / maxValue
        val pointRadius = radius * valueRatio
        val pointX = center.x + pointRadius * cos(angle)
        val pointY = center.y + pointRadius * sin(angle)

        if (i == 0) {
            path.moveTo(pointX, pointY)
        } else {
            path.lineTo(pointX, pointY)
        }
    }
    path.close()

    // Dibujar área rellena
    drawPath(
        path = path,
        color = traiBlue.copy(alpha = 0.3f),
        style = Fill
    )

    // Dibujar borde del polígono
    drawPath(
        path = path,
        color = traiBlue,
        style = Stroke(width = 2.dp.toPx())
    )

    // Dibujar puntos en los vértices
    dataPoints.forEachIndexed { i, value ->
        val angle = angleStep * i - (Math.PI / 2).toFloat()
        val valueRatio = value.toFloat() / maxValue
        val pointRadius = radius * valueRatio
        val pointX = center.x + pointRadius * cos(angle)
        val pointY = center.y + pointRadius * sin(angle)

        drawCircle(
            color = traiOrange,
            radius = 4.dp.toPx(),
            center = Offset(pointX, pointY)
        )
    }
}

private fun DrawScope.drawCategoryLabels(
    center: Offset,
    radius: Float,
    categories: List<String>
) {
    val angleStep = (2 * Math.PI / categories.size).toFloat()
    val labelRadius = radius + 25.dp.toPx()

    categories.forEachIndexed { i, category ->
        val angle = angleStep * i - (Math.PI / 2).toFloat()
        val labelX = center.x + labelRadius * cos(angle)
        val labelY = center.y + labelRadius * sin(angle)

        // Truncar nombres largos
        val displayName = if (category.length > 12) {
            "${category.take(9)}..."
        } else {
            category
        }

        drawContext.canvas.nativeCanvas.drawText(
            displayName,
            labelX - 40, // Ajuste para centrar mejor
            labelY + 5,
            TextPaint().apply {
                color = Color.White.toArgb()
                textSize = 11.sp.toPx()
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
            }
        )
    }
}

private fun DrawScope.drawProgressValues(
    center: Offset,
    radius: Float,
    dataPoints: List<Int>,
    categories: List<String>
) {
    val angleStep = (2 * Math.PI / dataPoints.size).toFloat()

    dataPoints.forEachIndexed { i, value ->
        val angle = angleStep * i - (Math.PI / 2).toFloat()
        val valueRatio = value.toFloat() / 100f
        val valueRadius = radius * valueRatio * 0.7f // Posicionar un poco hacia adentro
        val valueX = center.x + valueRadius * cos(angle)
        val valueY = center.y + valueRadius * sin(angle)

        if (value > 0) {
            drawContext.canvas.nativeCanvas.drawText(
                "${value}%",
                valueX,
                valueY + 4,
                TextPaint().apply {
                    color = traiOrange.toArgb()
                    textSize = 10.sp.toPx()
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                }
            )
        }
    }
}

@Composable
private fun ProgressLegend(
    topExercises: List<com.develop.traiscore.domain.model.ExerciseProgressData>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🏆 Top 3 Ejercicios",
            style = MaterialTheme.typography.bodyMedium,
            color = traiOrange,
            fontWeight = FontWeight.Bold
        )

        topExercises.forEachIndexed { index, exercise ->
            if (exercise.progressScore > 0) {
                Text(
                    text = "${index + 1}. ${exercise.exerciseName}: ${exercise.getFormattedProgressScore()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun ProgressRadarChartPreview() {
    // Datos de prueba
    val mockProgressData = listOf(
        com.develop.traiscore.domain.model.ExerciseProgressData(
            exerciseName = "Press Banca",
            progressScore = 85f,
            totalVolume = 12500f,
            maxWeight = 80f,
            maxReps = 12,
            workoutCount = 25,
            averageRIR = 2.1f,
            consistencyScore = 78f,
            improvementRate = 65f
        ),
        com.develop.traiscore.domain.model.ExerciseProgressData(
            exerciseName = "Sentadilla",
            progressScore = 92f,
            totalVolume = 15800f,
            maxWeight = 120f,
            maxReps = 10,
            workoutCount = 30,
            averageRIR = 1.8f,
            consistencyScore = 88f,
            improvementRate = 72f
        ),
        com.develop.traiscore.domain.model.ExerciseProgressData(
            exerciseName = "Peso Muerto",
            progressScore = 78f,
            totalVolume = 11200f,
            maxWeight = 140f,
            maxReps = 8,
            workoutCount = 22,
            averageRIR = 2.5f,
            consistencyScore = 70f,
            improvementRate = 58f
        ),
        com.develop.traiscore.domain.model.ExerciseProgressData(
            exerciseName = "Press Militar",
            progressScore = 65f,
            totalVolume = 6800f,
            maxWeight = 45f,
            maxReps = 15,
            workoutCount = 18,
            averageRIR = 3.2f,
            consistencyScore = 62f,
            improvementRate = 48f
        ),
        com.develop.traiscore.domain.model.ExerciseProgressData(
            exerciseName = "Remo",
            progressScore = 70f,
            totalVolume = 8900f,
            maxWeight = 65f,
            maxReps = 12,
            workoutCount = 20,
            averageRIR = 2.8f,
            consistencyScore = 68f,
            improvementRate = 55f
        )
    )

    val mockRadarData = com.develop.traiscore.domain.model.ExerciseProgressCalculator.generateRadarChartData(mockProgressData)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        ProgressRadarChart(
            radarData = mockRadarData,
            modifier = Modifier.size(400.dp)
        )
    }
}