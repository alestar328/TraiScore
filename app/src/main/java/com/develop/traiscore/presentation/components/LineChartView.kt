package com.develop.traiscore.presentation.components

import android.text.TextPaint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import kotlin.math.abs

@Composable
fun LineChartView(
    dataPoints: List<Pair<String, Float>>, // (etiqueta, valor)
    lineColor: Color = Color(0xFF00BCD4),   // un turquesa similar a Cyan
    axisColor: Color = Color.White.copy(alpha = 0.5f),
    backgroundChartColor: Color = Color.DarkGray,
    modifier: Modifier = Modifier
) {
    val pts = dataPoints
    if (pts.size < 2) {
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


            val paddingX = 12.dp.toPx()
            val paddingY = 30.dp.toPx()
            val chartWidth = size.width - paddingX * 2
            val chartHeight = size.height - paddingY * 2
            val stepX = chartWidth / (pts.size - 1)

            // Calculamos rango Y
            val maxV = pts.maxOf { it.second }
            val minV = 0f
            val range = (maxV - minV).takeIf { it > 0f } ?: 1f

            val coords = pts.mapIndexed { i, (_, v) ->
                val x = paddingX + stepX * i
                val y = size.height - paddingY - ((v - minV) / range) * chartHeight
                x to y
            }
            val smoothPath = Path().apply {
                val (startX, startY) = coords.first()
                moveTo(startX, startY)
                coords.zipWithNext { (xA, yA), (xB, yB) ->
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
                lineTo(coords.last().first, yAxis)
                lineTo(coords.first().first, yAxis)
                close()
            }

            // Dibujamos eje X
            drawPath(
                fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.3f), Color.Transparent),
                    startY = paddingY,
                    endY = yAxis
                )
            )
            // Degradado bajo la curva
            drawPath(
                smoothPath,
                color = lineColor,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
            // — puntos
            coords.forEach { (x, y) ->
                drawCircle(lineColor, radius = 5.dp.toPx(), center = Offset(x, y))
            }
            // — ejes y etiquetas (igual que antes)
            drawLine(
                axisColor,
                Offset(paddingX, yAxis),
                Offset(size.width - paddingX, yAxis),
                strokeWidth = 1.dp.toPx()
            )
            coords.forEachIndexed { i, (_, _) ->
                val x = coords[i].first
                // etiqueta en yAxis + offset
                drawContext.canvas.nativeCanvas.drawText(
                    pts[i].second.toInt().toString(),
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