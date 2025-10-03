package com.develop.traiscore.presentation.screens

import android.graphics.BitmapFactory
import android.net.Uri
import com.develop.traiscore.presentation.theme.traiOrange
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.graphics.Color
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
import kotlin.math.abs

enum class BrushTag { NAME, VALUE , CLEAN}

@Composable
fun PhotoPreviewTempScreen(
    photoUri: String,
    onConfirm: (List<com.develop.traiscore.data.local.entity.LabEntry>) -> Unit,
    onRetake: () -> Unit
) {
    val context = LocalContext.current
    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    var selectedName by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var selectedValue by remember { mutableStateOf<Set<Int>>(emptySet()) }

    // Estado con palabras detectadas (coords normalizadas 0..1 respecto a la imagen original)
    var words by remember { mutableStateOf<List<DetectedWord>>(emptyList()) }
    var fullText by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf(BrushTag.NAME) }

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
               /* DetectedWordsOverlayImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    words = words,
                    imageWidthPx = imgWidth,
                    imageHeightPx = imgHeight,
                    contentScale = ContentScale.Fit
                )*/
                FingerBrushUI(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    words = words,
                    imageWidthPx = imgWidth,
                    imageHeightPx = imgHeight,
                    activeTag = selectedTag,                 // 👈 radio activo (Nombre/Valor)
                    selectedName = selectedName,            // 👈 selección azul
                    selectedValue = selectedValue,          // 👈 selección naranja
                    onSelectedNameChange = { selectedName = it },
                    onSelectedValueChange = { selectedValue = it },
                    brushColorName = traiBlue,              // 👈 color del pincel para Nombre
                    brushColorValue = traiOrange,           // 👈 color del pincel para Valor
                    brushRadiusDp = 28.dp,
                    contentScale = ContentScale.Fit
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedTag == BrushTag.NAME,
                        onClick = { selectedTag = BrushTag.NAME },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = traiBlue,
                            unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                    Text("Nombre", modifier = Modifier.padding(start = 6.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedTag == BrushTag.VALUE,
                        onClick = { selectedTag = BrushTag.VALUE },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = traiOrange,
                            unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                    Text("Valor", modifier = Modifier.padding(start = 6.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedTag == BrushTag.CLEAN,
                        onClick = { selectedTag = BrushTag.CLEAN },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.onSurface, // o el color que prefieras
                            unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                    Text("Limpiar", modifier = Modifier.padding(start = 6.dp))
                }
            }

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 70.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onRetake,
                    modifier = Modifier.weight(1f)
                ) { Text("Repetir") }
                Button(
                    onClick = {
                        val entries = buildEntriesFromBrush(
                            words = words,
                            selectedName = selectedName,
                            selectedValue = selectedValue
                        )
                        onConfirm(entries)
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
private fun namesBrushToEntries(
    words: List<DetectedWord>,
    selectedName: Set<Int>,
    lineThreshold: Float = 0.03f // tolerancia de diferencia en 'top' para considerar misma línea
): List<LabEntry> {
    if (selectedName.isEmpty()) return emptyList()

    // Orden: primero por top (fila), luego por left (orden dentro de la línea)
    val sorted = selectedName
        .map { idx -> idx to words[idx] }
        .sortedWith(compareBy({ it.second.top }, { it.second.left }))

    val groups = mutableListOf<MutableList<Pair<Int, DetectedWord>>>()
    var currentTop = Float.NaN

    for ((_, dw) in sorted) {
        if (groups.isEmpty()) {
            groups += mutableListOf(0 to dw).apply { clear(); add(0 to dw) } // “truco” para crear lista; limpiamos y añadimos
            currentTop = dw.top
        } else {
            if (abs(dw.top - currentTop) <= lineThreshold) {
                groups.last().add(0 to dw)
            } else {
                groups += mutableListOf(0 to dw).apply { clear(); add(0 to dw) }
                currentTop = dw.top
            }
        }
    }

    // Para cada grupo (línea), ordena por left y junta los textos
    return groups.map { line ->
        val lineText = line
            .sortedBy { it.second.left }
            .joinToString(separator = " ") { it.second.text }
            .trim()
            .replace(Regex("\\s+"), " ")

        LabEntry(
            id = UUID.randomUUID().toString(),
            test = lineText,
            value = null,
            unit = null
        )
    }.filter { it.test.isNotBlank() }
}

private fun sortByReadingOrder(
    indices: Set<Int>,
    words: List<DetectedWord>
): List<Int> = indices.sortedWith(
    compareBy(
        { words[it].top },
        { words[it].left }
    )
)

/** Extrae el primer número de un texto (permite coma o punto). */
private fun parseFirstNumberOrNull(text: String): Double? {
    val m = Regex("""[+-]?\d+(?:[.,]\d+)?""").find(text) ?: return null
    return m.value.replace(',', '.').toDoubleOrNull()
}

/**
 * 1 fila por palabra pintada con NAME (orden de arriba→abajo, izq→der).
 * Los VALUE pintados se asignan en el mismo orden a las primeras filas.
 */
private fun buildEntriesFromBrush(
    words: List<DetectedWord>,
    selectedName: Set<Int>,
    selectedValue: Set<Int>
): List<LabEntry> {
    if (selectedName.isEmpty()) return emptyList()

    val nameIdx = sortByReadingOrder(selectedName, words)
    val valueIdx = sortByReadingOrder(selectedValue, words)

    // Parseamos los values (si existen)
    val parsedValues: List<Double?> = valueIdx.map { idx ->
        parseFirstNumberOrNull(words[idx].text)
    }

    // Creamos filas: una por cada NAME
    return nameIdx.mapIndexed { i, nIdx ->
        val testText = words[nIdx].text.trim().replace(Regex("\\s+"), " ")
        val value = parsedValues.getOrNull(i) // si hay menos VALUE, quedará null
        LabEntry(
            id = UUID.randomUUID().toString(),
            test = testText,
            value = value,
            unit = null
        )
    }
}