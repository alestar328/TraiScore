package com.develop.traiscore.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.sp
import com.develop.traiscore.presentation.theme.TraiScoreTheme
data class ProcessedDataPoint(
    val originalIndex: Int,
    val label: String,
    val value: Float,
    val normalizedValue: Float
)

@Composable
fun LineChartView(
    dataPoints: List<Pair<String, Float>>, // (etiqueta, valor)
    lineColor: Color = Color(0xFF00BCD4),   // un turquesa similar a Cyan
    axisColor: Color = Color.White.copy(alpha = 0.5f),
    backgroundChartColor: Color = Color.DarkGray,
    baselineOffset: Float = 0.35f,
    modifier: Modifier = Modifier
) {
    val processedData = remember(dataPoints) {
        if (dataPoints.size < 2) return@remember emptyList()

        val maxV = dataPoints.maxOf { it.second }
        val minV = dataPoints.minOf { it.second }
        val range = (maxV - minV).takeIf { it > 0f } ?: 1f

        val effectiveArea = 1f - baselineOffset

        dataPoints.mapIndexed { index, (label, value) ->
            ProcessedDataPoint(
                originalIndex = index,
                label = label,
                value = value,
                normalizedValue = baselineOffset + ((value - minV) / range) * effectiveArea
            )
        }
    }

    if (processedData.size < 2) {
        Box(
            modifier
                .background(backgroundChartColor, RoundedCornerShape(8.dp))
                .size(200.dp, 100.dp)
        )
        return
    }

    Box(
        modifier
            .background(backgroundChartColor, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .then(modifier)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val paddingX = 0.dp.toPx()
            val labelMargin = 8.dp.toPx()
            val paddingY = 30.dp.toPx()
            val chartWidth = size.width - labelMargin * 2
            val chartHeight = size.height - paddingY * 2
            val stepX = chartWidth / (processedData.size - 1)

            // Usar datos procesados para coordenadas
            val coords = processedData.mapIndexed { i, processedPoint ->
                val x = labelMargin + stepX * i  // Usar labelMargin para espaciar las etiquetas
                val y = size.height - paddingY - (processedPoint.normalizedValue * chartHeight)
                x to y
            }

            val lineCoords = processedData.mapIndexed { i, processedPoint ->
                val x = (size.width / (processedData.size - 1)) * i  // Sin margen, extremo a extremo
                val y = size.height - paddingY - (processedPoint.normalizedValue * chartHeight)
                x to y
            }

            val smoothPath = Path().apply {
                val (startX, startY) = lineCoords.first()
                moveTo(startX, startY)
                lineCoords.zipWithNext { (xA, yA), (xB, yB) ->
                    // usamos el punto medio en X como handle de control
                    val midX = (xA + xB) / 2f
                    cubicTo(
                        midX, yA,  // primer handle (alineado horizontalmente con A)
                        midX, yB,  // segundo handle (alineado horizontalmente con B)
                        xB, yB   // destino
                    )
                }
            }

            // Creamos la Path de la curva con cubicTo para suavizarla
            val yAxis = size.height - paddingY
            val fillPath = Path().apply {
                addPath(smoothPath)
                lineTo(lineCoords.last().first, yAxis)
                lineTo(lineCoords.first().first, yAxis)
                close()
            }

            // Dibujamos relleno bajo la curva
            drawPath(
                fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.3f), Color.Transparent),
                    startY = paddingY,
                    endY = size.height
                )
            )

            // Dibujamos la línea principal
            drawPath(
                smoothPath,
                color = lineColor,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )



            // Dibujamos eje X
            drawLine(
                axisColor,
                Offset(0f, yAxis),
                Offset(size.width , yAxis),
                strokeWidth = 1.dp.toPx()
            )

            // Etiquetas usando datos procesados
            coords.forEachIndexed { i, (x, _) ->
                drawContext.canvas.nativeCanvas.drawText(
                    processedData[i].value.toInt().toString(),
                    x,
                    yAxis + 14.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = axisColor.toArgb()
                        textSize = 10.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }
                )
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun LineChartViewPreview() {
    val sampleData = listOf(
        "A" to 20f, "B" to 20f, "C" to 22f, "D" to 22f,
        "E" to 20f, "F" to 25f, "G" to 25f, "H" to 20f,
        "I" to 25f, "J" to 25f, "K" to 20f, "L" to 20f
    )
    TraiScoreTheme {
        LineChartView(
            dataPoints = sampleData,
            modifier = Modifier
                .size(320.dp, 120.dp)
                .padding(16.dp)
        )
    }
}