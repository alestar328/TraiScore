package com.develop.traiscore.presentation.components

import android.text.TextPaint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.copy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.sp
import com.develop.traiscore.presentation.theme.TraiScoreTheme

@Composable
fun LineChartView(
    dataPoints: List<Pair<String, Float>>, // Lista de pares (fecha, valor)
    lineColor: Color = Color.Blue,
    axisColor: Color = Color.Gray,
    backgroundChartColor: Color = Color.White,
    maxYValue: Float? = null,
    minYValue: Float? = null,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val padding = 20.dp.toPx()
        val chartWidth = size.width - padding * 2
        val chartHeight = size.height - padding * 2
        val stepX = chartWidth / (dataPoints.size - 1)

        val maxValue = maxYValue ?: dataPoints.maxOfOrNull { it.second } ?: 100f
        val minValue = minYValue ?: dataPoints.minOfOrNull { it.second } ?: 0f
        val valueRange = maxValue - minValue

        // Fondo de la gráfica
        drawRect(
            color = backgroundChartColor,
            size = size,
            topLeft = Offset.Zero
        )

        // Dibujar ejes
        drawLine(
            color = axisColor,
            start = Offset(padding, size.height - padding),
            end = Offset(size.width - padding, size.height - padding),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = axisColor,
            start = Offset(padding, size.height - padding),
            end = Offset(padding, padding),
            strokeWidth = 1.dp.toPx()
        )

        // Crear línea del gráfico con curvas
        val path = Path().apply {
            dataPoints.forEachIndexed { index, point ->
                val x = padding + index * stepX
                val y = size.height - padding - ((point.second - minValue) / valueRange) * chartHeight
                if (index == 0) {
                    moveTo(x, y)
                } else {
                    cubicTo(
                        x - stepX / 2, size.height - padding, // Control Point 1
                        x - stepX / 2, y,                   // Control Point 2
                        x, y                                // Target Point
                    )
                }
            }
        }

        // Dibujar línea del gráfico
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Dibujar gradiente bajo la línea
        val gradientPath = path.copy()
        gradientPath.lineTo(size.width - padding, size.height - padding)
        gradientPath.lineTo(padding, size.height - padding)
        gradientPath.close()

        drawPath(
            path = gradientPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.3f), Color.Transparent),
                startY = padding,
                endY = size.height - padding
            )
        )

        // Dibujar puntos en los cruces
        dataPoints.forEachIndexed { index, point ->
            val x = padding + index * stepX
            val y = size.height - padding - ((point.second - minValue) / valueRange) * chartHeight
            drawCircle(
                color = lineColor,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )

            // Etiquetas del eje X
            if (index % 2 == 0) { // Mostrar cada dos puntos
                drawIntoCanvas {
                    val textPaint = TextPaint().apply {
                        color = axisColor.toArgb()
                        textSize = 10.sp.toPx()
                        isAntiAlias = true
                    }
                    it.nativeCanvas.drawText(
                        point.first, // Fecha
                        x - 15, // Ajustar posición
                        size.height - padding + 20, // Debajo del eje X
                        textPaint
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LineChartViewPreview() {
    val sampleData = listOf(
        "2025-01-01" to 10f,
        "2025-01-02" to 15f,
        "2025-01-03" to 8f,
        "2025-01-04" to 12f,
        "2025-01-05" to 20f
    )
    TraiScoreTheme {
        LineChartView(
            dataPoints = sampleData,
            lineColor = Color.Cyan,
            axisColor = Color.Gray,
            backgroundChartColor = Color.DarkGray,
            modifier = Modifier.size(300.dp, 150.dp)
        )
    }
}