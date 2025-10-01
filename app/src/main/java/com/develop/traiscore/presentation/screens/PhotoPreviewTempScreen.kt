package com.develop.traiscore.presentation.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.develop.traiscore.data.local.entity.LabEntry
import com.develop.traiscore.presentation.components.general.FingerBrushUI
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.viewmodels.DetectedWord
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.UUID

@Composable
fun PhotoPreviewTempScreen(
    photoUri: String,
    onConfirm: (List<com.develop.traiscore.data.local.entity.LabEntry>) -> Unit,
    onRetake: () -> Unit
) {
    val context = LocalContext.current
    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    var selected by remember { mutableStateOf<Set<Int>>(emptySet()) }

    // Estado con palabras detectadas (coords normalizadas 0..1 respecto a la imagen original)
    var words by remember { mutableStateOf<List<DetectedWord>>(emptyList()) }
    var fullText by remember { mutableStateOf("") }

    // Dimensiones reales de la imagen (px) — necesarias para normalizar y para el overlay con ContentScale.Fit
    var imgWidth by remember { mutableStateOf(0) }
    var imgHeight by remember { mutableStateOf(0) }

    // 1) Cargar bounds de la imagen y lanzar ML Kit sobre el Uri
    LaunchedEffect(photoUri) {
        val uri = Uri.parse(photoUri)

        // Obtener dimensiones reales del bitmap SIN decodificarlo completo
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, options)
        }
        imgWidth = options.outWidth
        imgHeight = options.outHeight

        // Procesar con ML Kit
        val inputImage = InputImage.fromFilePath(context, uri)
        val result = recognizer.process(inputImage).await() // usamos await() con extensión a continuación
        val detected = mutableListOf<DetectedWord>()
        fullText = result.text.orEmpty()

        val w = imgWidth.toFloat().coerceAtLeast(1f)
        val h = imgHeight.toFloat().coerceAtLeast(1f)

        for (block in result.textBlocks) {
            for (line in block.lines) {
                for (el in line.elements) {
                    val box = el.boundingBox ?: continue
                    detected += DetectedWord(
                        left = (box.left / w).coerceIn(0f, 1f),
                        top = (box.top / h).coerceIn(0f, 1f),
                        right = (box.right / w).coerceIn(0f, 1f),
                        bottom = (box.bottom / h).coerceIn(0f, 1f),
                        text = el.text
                    )
                }
            }
        }
        words = detected
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(12.dp))

            // Contenedor de la imagen + overlay (mismo tamaño/posición)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                val painter = rememberAsyncImagePainter(Uri.parse(photoUri))

                // Imagen mostrada con FIT (puede tener letterboxing)
                Image(
                    painter = painter,
                    contentDescription = "Foto capturada",
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    contentScale = ContentScale.Fit
                )

                // Overlay que se ajusta a la misma caja visible de la Image
                DetectedWordsOverlayImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    words = words,
                    imageWidthPx = imgWidth,
                    imageHeightPx = imgHeight,
                    contentScale = ContentScale.Fit
                )
                FingerBrushUI(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    words = words,
                    imageWidthPx = imgWidth,
                    imageHeightPx = imgHeight,
                    selected = selected,
                    onSelectedChange = { selected = it },
                    brushRadiusDp = 28.dp,               // ajustable
                    contentScale = ContentScale.Fit      // igual que la Image
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, bottom = 70.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onRetake,
                    modifier = Modifier.weight(1f)
                ) { Text("Repetir") }
                Button(
                    onClick = {
                        val selectedText = selected.sorted().joinToString(" ") { idx -> words[idx].text }

                        val parsed = parseLabEntriesFromText(selectedText)
                        onConfirm(parsed)
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Confirmar") }
            }
        }
    }
}

/**
 * Dibuja las cajas de palabras sobre la misma caja visible que ocupa la imagen,
 * teniendo en cuenta ContentScale.Fit (posibles márgenes para preservar aspect ratio).
 */
@Composable
private fun DetectedWordsOverlayImage(
    modifier: Modifier,
    words: List<DetectedWord>,
    imageWidthPx: Int,
    imageHeightPx: Int,
    contentScale: ContentScale = ContentScale.Fit
) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        if (imageWidthPx <= 0 || imageHeightPx <= 0) return@Canvas

        val canvasW = size.width
        val canvasH = size.height
        val imgW = imageWidthPx.toFloat()
        val imgH = imageHeightPx.toFloat()

        // Escala para FIT: misma escala en ambos ejes, centrado, pudiendo añadir letterboxing
        val scale = minOf(canvasW / imgW, canvasH / imgH)
        val drawW = imgW * scale
        val drawH = imgH * scale
        val leftOffset = (canvasW - drawW) / 2f
        val topOffset = (canvasH - drawH) / 2f

        // Dibuja cajas
        words.forEach { dw ->
            val left = leftOffset + dw.left * drawW
            val top = topOffset + dw.top * drawH
            val right = leftOffset + dw.right * drawW
            val bottom = topOffset + dw.bottom * drawH

            drawRect(
                color = traiBlue,
                topLeft = Offset(left, top),
                size = Size(right - left, bottom - top),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
            )
        }
    }
}

/** Pequeño helper para usar Tasks de ML Kit como suspensión */
private suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T =
    kotlinx.coroutines.suspendCancellableCoroutine { cont ->
        addOnSuccessListener { res -> if (cont.isActive) cont.resume(res, null) }
        addOnFailureListener { e -> if (cont.isActive) cont.resumeWith(Result.failure(e)) }
        addOnCanceledListener { if (cont.isActive) cont.cancel() }
    }

private fun parseLabEntriesFromText(text: String): List<LabEntry> {
    if (text.isBlank()) return emptyList()

    // Regex simple para patrones: "Glucosa 92 mg/dL", "Urea: 34", "Creatinina - 0,9 mg/dL", etc.
    val pattern = Regex(
        pattern = """(?i)\b(Glucosa|Urea|Creatinina|Ácido\s+úrico|Colesterol\s+total|HDL|LDL|Triglicéridos)\b[:\-]?\s*([+-]?\d+(?:[.,]\d+)?)\s*(mg/dL|mmol/L|µmol/L|g/L|g/dL|mg/L)?""",
        options = setOf(RegexOption.IGNORE_CASE)
    )

    val entries = mutableListOf<LabEntry>()
    pattern.findAll(text).forEach { m ->
        val test = m.groupValues.getOrNull(1)?.trim().orEmpty()
        val valueStr = m.groupValues.getOrNull(2)?.trim().orEmpty()
        val unit = m.groupValues.getOrNull(3)?.trim().takeIf { !it.isNullOrBlank() }

        val value = valueStr.replace(',', '.').toDoubleOrNull()
        if (test.isNotBlank()) {
            entries += LabEntry(
                id = UUID.randomUUID().toString(),
                test = test.replace(Regex("\\s+"), " "), // normaliza espacios
                value = value,
                unit = unit
            )
        }
    }
    return entries
}