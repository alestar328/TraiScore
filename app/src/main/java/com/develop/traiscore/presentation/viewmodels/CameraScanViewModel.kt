package com.develop.traiscore.presentation.viewmodels

import android.content.ContentValues
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.provider.MediaStore
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

data class DetectedWord(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val text: String
)

@HiltViewModel
class CameraScanViewModel @Inject constructor() : ViewModel() {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val _words = MutableStateFlow<List<DetectedWord>>(emptyList())
    val words: StateFlow<List<DetectedWord>> = _words
    private var lastTs = 0L
    private val throttleMs = 250

    fun analyzeFrame(imageProxy: ImageProxy) {
        val now = System.currentTimeMillis()
        if (now - lastTs < throttleMs) {
            imageProxy.close()
            return
        }
        lastTs = now

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val rotation = imageProxy.imageInfo.rotationDegrees
        val input = InputImage.fromMediaImage(mediaImage, rotation)

        recognizer.process(input)
            .addOnSuccessListener { visionText ->
                val w = imageProxy.width.toFloat()
                val h = imageProxy.height.toFloat()
                val detected = mutableListOf<DetectedWord>()

                // Recorremos hasta nivel "element" (≈ palabra)
                for (block in visionText.textBlocks) {
                    for (line in block.lines) {
                        for (element in line.elements) {
                            val box: Rect? = element.boundingBox
                            if (box != null && w > 0 && h > 0) {
                                // Normalizamos a [0..1]
                                val left = max(0f, min(1f, box.left / w))
                                val top = max(0f, min(1f, box.top / h))
                                val right = max(0f, min(1f, box.right / w))
                                val bottom = max(0f, min(1f, box.bottom / h))
                                detected.add(
                                    DetectedWord(
                                        left = left, top = top, right = right, bottom = bottom,
                                        text = element.text
                                    )
                                )
                            }
                        }
                    }
                }
                _words.value = detected
            }
            .addOnFailureListener {
                // Ignoramos errores por ahora
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    fun buildOutputOptions(context: Context): ImageCapture.OutputFileOptions {
        val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "scan_$name")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/TraiScore-Scans")
            }
        }
        val resolver = context.contentResolver
        val imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        return ImageCapture.OutputFileOptions.Builder(resolver, imageUri, contentValues).build()
    }

    fun takePicture(
        imageCapture: ImageCapture,
        output: ImageCapture.OutputFileOptions,
        onSuccess: (String) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        imageCapture.takePicture(
            output,
            Runnable::run, // ejecutor inmediato (suficiente aquí)
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val uri = outputFileResults.savedUri?.toString()
                    if (uri != null) onSuccess(uri) else onError(IllegalStateException("Uri nula al guardar la imagen"))
                }
                override fun onError(exception: ImageCaptureException) = onError(exception)
            }
        )
    }
}