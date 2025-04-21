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
    axisColor: Color = Color.Gray.copy(alpha = 0.3f),
    backgroundChartColor: Color = Color.DarkGray,
    modifier: Modifier = Modifier
) {
    val pts = dataPoints.takeLast(12)
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
            val paddingY = 20.dp.toPx()
            val chartWidth = size.width - paddingX * 2
            val chartHeight = size.height - paddingY * 2
            val stepX = chartWidth / (pts.size - 1)

            // Dibujamos eje X
            drawLine(
                color = axisColor,
                start = Offset(paddingX, size.height - paddingY),
                end = Offset(size.width - paddingX, size.height - paddingY),
                strokeWidth = 1.dp.toPx()
            )

            // Calculamos rango Y
            val maxV = pts.maxOf { it.second }
            val minV = pts.minOf { it.second }
            val range = (maxV - minV).takeIf { it > 0f } ?: 1f

            // Creamos la Path de la curva con cubicTo para suavizarla
            val curvePath = Path().apply {
                var x0 = paddingX
                var y0 = size.height - paddingY - ((pts[0].second - minV) / range) * chartHeight
                moveTo(x0, y0)

                for (i in 1 until pts.size) {
                    val x1 = paddingX + stepX * i
                    val y1 = size.height - paddingY - ((pts[i].second - minV) / range) * chartHeight

                    // control points para cubic Bezier
                    val midX = (x0 + x1) / 2f
                    cubicTo(
                        midX, y0,
                        midX, y1,
                        x1, y1
                    )

                    x0 = x1
                    y0 = y1
                }
            }

            // Path de relleno
            val fillPath = Path().apply {
                addPath(curvePath)
                lineTo(paddingX + chartWidth, size.height - paddingY)
                lineTo(paddingX, size.height - paddingY)
                close()
            }

            // Degradado bajo la curva
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.3f), Color.Transparent),
                    startY = paddingY,
                    endY = size.height - paddingY
                )
            )

            // Trazo de la curva
            drawPath(
                path = curvePath,
                color = lineColor,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )

            // Dibujamos puntos y etiquetas
            pts.forEachIndexed { i, (_, v) ->
                val x = paddingX + stepX * i
                val y = size.height - paddingY - ((v - minV) / range) * chartHeight

                // punto
                drawCircle(
                    color = lineColor,
                    radius = 5.dp.toPx(),
                    center = Offset(x, y)
                )

                // etiqueta de valor centrada bajo el eje X
                drawContext.canvas.nativeCanvas.apply {
                    val text = v.toInt().toString()
                    val paint = android.graphics.Paint().apply {
                        color = axisColor.toArgb()
                        textSize = 10.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }
                    drawText(
                        text,
                        x,
                        size.height - paddingY + 14.dp.toPx(),
                        paint
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