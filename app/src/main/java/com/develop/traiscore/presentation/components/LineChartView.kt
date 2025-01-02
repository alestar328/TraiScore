package com.develop.traiscore.presentation.components

import android.text.TextPaint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.sp
import com.develop.traiscore.presentation.theme.TraiScoreTheme

@Composable
fun LineChart(
    data: List<Pair<Float, Float>>, // Lista de puntos (x, y)
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val padding = 40.dp.toPx()
        val chartWidth = size.width - padding * 2
        val chartHeight = size.height - padding * 2
        val stepX = chartWidth / (data.size - 1)

        // Dibujar ejes
        drawLine(
            color = Color.Gray,
            start = androidx.compose.ui.geometry.Offset(padding, size.height - padding),
            end = androidx.compose.ui.geometry.Offset(size.width - padding, size.height - padding),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = Color.Gray,
            start = androidx.compose.ui.geometry.Offset(padding, size.height - padding),
            end = androidx.compose.ui.geometry.Offset(padding, padding),
            strokeWidth = 2.dp.toPx()
        )
        // Dibujar líneas horizontales
        val numberOfLines = 5
        val stepY = chartHeight / numberOfLines
        for (i in 0..numberOfLines) {
            val y = size.height - padding - i * stepY
            drawLine(
                color = Color.LightGray,
                start = androidx.compose.ui.geometry.Offset(padding, y),
                end = androidx.compose.ui.geometry.Offset(size.width - padding, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )

            // Dibujar etiquetas de eje Y
            drawIntoCanvas {
                val textPaint = TextPaint().apply {
                    color = Color.Gray.toArgb()
                    textSize = 12.sp.toPx()
                }
                it.nativeCanvas.drawText(
                    "${(i * 20)}",
                    padding - 30,
                    y + 5,
                    textPaint
                )
            }
        }

        // Crear el Path para la línea
        val path = Path().apply {
            data.forEachIndexed { index, point ->
                val x = padding + index * stepX
                val y = size.height - padding - (point.second * chartHeight)
                if (index == 0) {
                    moveTo(x, y)
                } else {
                    lineTo(x, y)
                }
            }
        }

        // Dibujar la línea en el Canvas
        drawPath(
            path = path,
            color = Color.Blue,
            style = Stroke(
                width = 4.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Dibujar puntos en cada cruce
        data.forEachIndexed { index, point ->
            val x = padding + index * stepX
            val y = size.height - padding - (point.second * chartHeight)
            // Etiquetas del eje X
            drawIntoCanvas {
                val textPaint = TextPaint().apply {
                    color = Color.Gray.toArgb()
                    textSize = 12.sp.toPx()
                }
                it.nativeCanvas.drawText(
                    "${(index + 1)}", // Etiqueta simple (puedes reemplazarlo con fechas u otros datos)
                    x - 10, // Centrar etiqueta
                    size.height - padding + 20, // Debajo del eje X
                    textPaint
                )
            }


            drawCircle(
                color = Color.Cyan,
                radius = 6.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
        }
    }
}
@Preview(
    name = "LineChartPreview",
    showBackground = true
)
@Composable
fun LineChartPreview() {
    val sampleData = listOf(
        0.1f to 0.2f,
        0.2f to 0.4f,
        0.3f to 0.8f,
        0.4f to 0.6f,
        0.5f to 0.9f
    )
    TraiScoreTheme {
        LineChart(data = sampleData)
    }
}