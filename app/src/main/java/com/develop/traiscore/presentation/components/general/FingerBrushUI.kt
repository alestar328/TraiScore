package com.develop.traiscore.presentation.components.general

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.develop.traiscore.presentation.screens.BrushTag
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.viewmodels.DetectedWord
import kotlin.math.max
import kotlin.math.min


@Composable
fun FingerBrushUI(
    modifier: Modifier = Modifier,
    words: List<DetectedWord>,
    imageWidthPx: Int,
    imageHeightPx: Int,
    activeTag: BrushTag,                          // 👈 NUEVO
    selectedName: Set<Int>,                       // 👈 NUEVO
    selectedValue: Set<Int>,                      // 👈 NUEVO
    onSelectedNameChange: (Set<Int>) -> Unit,     // 👈 NUEVO
    onSelectedValueChange: (Set<Int>) -> Unit,     // 👈 NUEVO
    brushColorName: Color,                        // 👈 NUEVO
    brushColorValue: Color,                       // 👈 NUEVO
    brushRadiusDp: Dp = 28.dp,
    contentScale: ContentScale = ContentScale.Fit
) {
    val density = LocalDensity.current
    val brushRadiusPx = with(LocalDensity.current) { brushRadiusDp.toPx() }
    val primary = MaterialTheme.colorScheme.primary
    val unselectedStroke = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)

    val selectedFill = primary.copy(alpha = 0.70f)
    val selectedStroke = traiBlue
    var localName by remember(selectedName, words) { mutableStateOf(selectedName) }
    var localValue by remember(selectedValue, words) { mutableStateOf(selectedValue) }
    // Caja que coincide con el espacio de la imagen (mismo padre/medidas que la Image)
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(words, imageWidthPx, imageHeightPx, brushRadiusPx, activeTag) {
                    detectDragGestures(
                        onDragStart = { pos ->
                            val hit = hitTestIndicesAt(
                                pointer = pos,
                                words = words,
                                canvasSize = size.toSize(),
                                imageWidthPx = imageWidthPx,
                                imageHeightPx = imageHeightPx,
                                contentScale = contentScale,
                                brushRadiusPx = brushRadiusPx
                            )

                            val targets = when (activeTag) {
                                BrushTag.NAME  -> hit.filter { it !in localValue }.toSet() // no pisa naranja
                                BrushTag.VALUE -> hit.filter { it !in localName }.toSet()  // no pisa azul
                            }

                            if (targets.isNotEmpty()) {
                                when (activeTag) {
                                    BrushTag.NAME -> {
                                        localName = localName + targets
                                        onSelectedNameChange(localName)
                                    }
                                    BrushTag.VALUE -> {
                                        localValue = localValue + targets
                                        onSelectedValueChange(localValue)
                                    }
                                }
                            }
                        },
                        onDrag = { change, _ ->
                            val hit = hitTestIndicesAt(
                                pointer = change.position,
                                words = words,
                                canvasSize = size.toSize(),
                                imageWidthPx = imageWidthPx,
                                imageHeightPx = imageHeightPx,
                                contentScale = contentScale,
                                brushRadiusPx = brushRadiusPx
                            )

                            val targets = when (activeTag) {
                                BrushTag.NAME  -> hit.filter { it !in localValue }.toSet()
                                BrushTag.VALUE -> hit.filter { it !in localName }.toSet()
                            }

                            if (targets.isNotEmpty()) {
                                when (activeTag) {
                                    BrushTag.NAME -> {
                                        val newSet = localName + targets
                                        if (newSet !== localName) {
                                            localName = newSet
                                            onSelectedNameChange(localName)
                                        }
                                    }
                                    BrushTag.VALUE -> {
                                        val newSet = localValue + targets
                                        if (newSet !== localValue) {
                                            localValue = newSet
                                            onSelectedValueChange(localValue)
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
        ) {
            if (imageWidthPx <= 0 || imageHeightPx <= 0) return@Canvas

            // Calcula el rect de dibujo (letterboxing) para ContentScale.Fit
            val canvasW = size.width
            val canvasH = size.height
            val imgW = imageWidthPx.toFloat()
            val imgH = imageHeightPx.toFloat()

            val scale = when (contentScale) {
                ContentScale.Fit, ContentScale.Inside -> min(canvasW / imgW, canvasH / imgH)
                ContentScale.FillBounds -> max(canvasW / imgW, canvasH / imgH)
                ContentScale.Crop -> max(canvasW / imgW, canvasH / imgH)
                else -> min(canvasW / imgW, canvasH / imgH) // por defecto, tratamos como Fit
            }
            val drawW = imgW * scale
            val drawH = imgH * scale
            val leftOffset = (canvasW - drawW) / 2f
            val topOffset = (canvasH - drawH) / 2f

            // Dibuja los bounding boxes: seleccionados resaltados, no seleccionados semi-transparente
            words.forEachIndexed { index, dw ->
                val left = leftOffset + dw.left * drawW
                val top = topOffset + dw.top * drawH
                val right = leftOffset + dw.right * drawW
                val bottom = topOffset + dw.bottom * drawH
                val rect = Rect(Offset(left, top), Offset(right, bottom))

                when {
                    index in localName -> {
                        // Nombre → azul
                        drawRect(
                            color = brushColorName.copy(alpha = 0.22f),
                            topLeft = rect.topLeft,
                            size = Size(rect.width, rect.height)
                        )
                        drawRect(
                            color = brushColorName,
                            topLeft = rect.topLeft,
                            size = Size(rect.width, rect.height),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                        )
                    }
                    index in localValue -> {
                        // Valor → naranja
                        drawRect(
                            color = brushColorValue.copy(alpha = 0.22f),
                            topLeft = rect.topLeft,
                            size = Size(rect.width, rect.height)
                        )
                        drawRect(
                            color = brushColorValue,
                            topLeft = rect.topLeft,
                            size = Size(rect.width, rect.height),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                        )
                    }
                    else -> {
                        // No seleccionado: borde tenue opcional
                        drawRect(
                            color = unselectedStroke,
                            topLeft = rect.topLeft,
                            size = Size(rect.width, rect.height),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                        )
                    }
                }
            }

            // (Opcional) Puedes dibujar una pista del pincel último punto si quieres.
            // No la mantenemos en estado para no sobre-repintar demasiado.
        }
    }
}

/** Devuelve los índices de words cuyas cajas (en draw space) intersectan el círculo del pincel en 'pointer'. */
private fun androidx.compose.ui.input.pointer.PointerInputScope.hitTestIndicesAt(
    pointer: Offset,
    words: List<DetectedWord>,
    canvasSize: Size,
    imageWidthPx: Int,
    imageHeightPx: Int,
    contentScale: ContentScale,
    brushRadiusPx: Float
): Set<Int> {
    if (imageWidthPx <= 0 || imageHeightPx <= 0) return emptySet()

    val hits = mutableSetOf<Int>()

    val canvasW = canvasSize.width
    val canvasH = canvasSize.height
    val imgW = imageWidthPx.toFloat()
    val imgH = imageHeightPx.toFloat()

    val scale = when (contentScale) {
        ContentScale.Fit, ContentScale.Inside -> min(canvasW / imgW, canvasH / imgH)
        ContentScale.FillBounds -> max(canvasW / imgW, canvasH / imgH)
        ContentScale.Crop -> max(canvasW / imgW, canvasH / imgH)
        else -> min(canvasW / imgW, canvasH / imgH)
    }
    val drawW = imgW * scale
    val drawH = imgH * scale
    val leftOffset = (canvasW - drawW) / 2f
    val topOffset = (canvasH - drawH) / 2f

    val cx = pointer.x
    val cy = pointer.y
    val r = brushRadiusPx

    words.forEachIndexed { i, dw ->
        val left = leftOffset + dw.left * drawW
        val top = topOffset + dw.top * drawH
        val right = leftOffset + dw.right * drawW
        val bottom = topOffset + dw.bottom * drawH

        if (circleIntersectsRect(cx, cy, r, left, top, right, bottom)) {
            hits += i
        }
    }
    return hits
}

/** Colisión círculo-rectángulo (rect definido por left, top, right, bottom). */
private fun circleIntersectsRect(
    cx: Float, cy: Float, r: Float,
    left: Float, top: Float, right: Float, bottom: Float
): Boolean {
    val closestX = clamp(cx, left, right)
    val closestY = clamp(cy, top, bottom)
    val dx = cx - closestX
    val dy = cy - closestY
    return (dx * dx + dy * dy) <= r * r
}

private fun clamp(x: Float, a: Float, b: Float): Float = max(a, min(x, b))