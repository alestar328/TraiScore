package com.develop.traiscore.presentation.components

import android.text.TextPaint
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.develop.traiscore.R
import com.develop.traiscore.domain.model.ExerciseProgressData
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
    title: String = stringResource(id = R.string.stats_top_5_exer)
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
                .fillMaxWidth() 
                .height(250.dp) // ✅ Altura fija rectangular, no cuadrado
                .clip(RoundedCornerShape(12.dp))
                .background(Color.DarkGray.copy(alpha = 0.3f))
                .padding(12.dp), // ✅ Padding interno
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2
                val centerY = size.height / 2
                val centerPoint = Offset(centerX, centerY)
                val radius = min(size.width, size.height) / 2 - 40.dp.toPx() // ✅ REDUCIR margen de 60dp a 40dp

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
        ProgressLegend(
            topExercises = radarData.topExercises.take(3),
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
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
    val labelRadius = radius + 35.dp.toPx() // ✅ AUMENTAR de 25dp a 35dp

    categories.forEachIndexed { i, category ->
        val angle = angleStep * i - (Math.PI / 2).toFloat()
        val labelX = center.x + labelRadius * cos(angle)
        val labelY = center.y + labelRadius * sin(angle)

        // Truncar nombres largos
        val displayName = when {
            category.length <= 15 -> category // Mostrar completo si es corto
            category.contains(" ") -> {
                // Si tiene espacios, dividir en líneas
                val words = category.split(" ")
                if (words.size >= 2) {
                    "${words[0]}\n${words.drop(1).joinToString(" ")}"
                } else {
                    category
                }
            }
            else -> "${category.take(12)}..." // Solo truncar si es muy largo y sin espacios
        }

        drawContext.canvas.nativeCanvas.drawText(
            displayName,
            labelX - 40, // Ajuste para centrar mejor
            labelY + 5,
            TextPaint().apply {
                color = Color.White.toArgb()
                textSize = 12.sp.toPx()
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
                isFakeBoldText = true

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
    topExercises: List<ExerciseProgressData>,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(topExercises) {
        Log.d("RadarChart", "ProgressLegend called with ${topExercises.size} exercises")
        topExercises.take(3).forEachIndexed { index, exercise ->
            Log.d("RadarChart", "Exercise $index: ${exercise.exerciseName} = ${exercise.progressScore}%")
        }
    }
    Column(
        modifier = modifier
            .background(Color.Red.copy(alpha = 0.1f)) // ✅ TEMPORAL: fondo rojo para debug
            .padding(8.dp), // ✅ AGREGAR padding interno
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.stats_top_3_exer),
            style = MaterialTheme.typography.bodyMedium,
            color = traiOrange,
            fontWeight = FontWeight.Bold
        )

        val exercisesToShow = topExercises.take(3).filter {
            it.progressScore > 0f && !it.exerciseName.startsWith("Ejercicio")

        }

        if (exercisesToShow.isEmpty()) {
            Text(
                text = stringResource(id = R.string.stats_top_3_no_data),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        } else {
            exercisesToShow.forEachIndexed { index, exercise ->
                Text(
                    text = "${index + 1}. ${exercise.exerciseName}: ${exercise.getFormattedProgressScore()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(vertical = 2.dp) // ✅ AUMENTAR padding vertical
                )
            }
        }
    }
}
