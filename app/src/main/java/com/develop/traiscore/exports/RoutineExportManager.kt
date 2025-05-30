package com.develop.traiscore.exports

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.develop.traiscore.data.firebaseData.RoutineDocument
import com.develop.traiscore.data.firebaseData.RoutineSection
import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.io.File
import java.io.FileWriter
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object RoutineExportManager {

    private const val FILE_EXTENSION = ".json"  // Cambié a .json
    private const val MIME_TYPE = "application/json"  // MIME type estándar
    private const val FALLBACK_MIME_TYPE = "text/plain"

    // Serializer personalizado para Firebase Timestamp
    class TimestampSerializer : JsonSerializer<Timestamp> {
        override fun serialize(
            src: Timestamp?,
            typeOfSrc: Type?,
            context: JsonSerializationContext?
        ): JsonElement {
            return if (src != null) {
                JsonPrimitive(src.seconds * 1000 + src.nanoseconds / 1000000)
            } else {
                JsonPrimitive(0L)
            }
        }
    }

    // Deserializer personalizado para Firebase Timestamp
    class TimestampDeserializer : JsonDeserializer<Timestamp> {
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): Timestamp {
            return if (json != null && !json.isJsonNull) {
                Timestamp(Date(json.asLong))
            } else {
                Timestamp.now()
            }
        }
    }

    // Data class para exportación (sin campos internos de Firebase)
    data class ExportableRoutine(
        val appVersion: String = "1.0",
        val exportDate: String,
        val originalTrainerId: String?,
        val clientName: String,
        val routineName: String,
        val sections: List<RoutineSection>,
        val metadata: RoutineMetadata,
        // Identificador para que sea reconocible como archivo TraiScore
        val fileType: String = "TraiScore_Routine"
    )

    data class RoutineMetadata(
        val originalCreatedAt: String?,
        val exportedBy: String?,
        val description: String = "Rutina creada con TraiScore App"
    )

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Timestamp::class.java, TimestampSerializer())
        .registerTypeAdapter(Timestamp::class.java, TimestampDeserializer())
        .setPrettyPrinting()
        .create()

    fun exportRoutine(
        context: Context,
        routine: RoutineDocument,
        onSuccess: (Uri) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            // Crear el archivo temporal
            val fileName = generateFileName(routine.routineName)
            val file = createTempFile(context, fileName)

            // Convertir a formato exportable
            val exportableRoutine = createExportableRoutine(routine)

            // Escribir JSON al archivo
            writeJsonToFile(file, exportableRoutine)

            // Crear URI usando FileProvider
            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            Log.d("RoutineExport", "Routine exported successfully: $fileName")
            onSuccess(fileUri)

        } catch (e: Exception) {
            Log.e("RoutineExport", "Error exporting routine", e)
            onError("Error al exportar la rutina: ${e.message}")
        }
    }
    fun shareRoutineFile(
        context: Context,
        fileUri: Uri,
        routineName: String
    ) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = MIME_TYPE
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, "Rutina TraiScore: $routineName")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "📋 Te comparto esta rutina de TraiScore: $routineName\n\n" +
                            "💪 Abre este archivo JSON con la app TraiScore para importar la rutina completa.\n\n" +
                            "📱 Si no tienes TraiScore instalado, puedes descargarla desde Google Play Store."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // Crear selector personalizado
            val chooserIntent = Intent.createChooser(shareIntent, "Compartir rutina TraiScore")

            context.startActivity(chooserIntent)

        } catch (e: Exception) {
            Log.e("RoutineExport", "Error sharing routine", e)

            // Fallback: compartir como archivo de texto
            try {
                val fallbackShareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = FALLBACK_MIME_TYPE
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    putExtra(Intent.EXTRA_SUBJECT, "Rutina: $routineName")
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "📋 Rutina de TraiScore: $routineName"
                    )
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(fallbackShareIntent, "Compartir archivo"))
            } catch (fallbackError: Exception) {
                Log.e("RoutineExport", "Fallback share also failed", fallbackError)
            }
        }
    }


    /**
     * Importa una rutina desde un archivo .traiscore
     */
    fun importRoutine(
        context: Context,
        uri: Uri,
        onSuccess: (ExportableRoutine) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val jsonString = inputStream?.bufferedReader()?.use { it.readText() }

            if (jsonString.isNullOrEmpty()) {
                onError("El archivo está vacío o no se puede leer")
                return
            }

            val exportableRoutine = gson.fromJson(jsonString, ExportableRoutine::class.java)

            // Validar que sea un archivo TraiScore válido
            if (exportableRoutine.fileType != "TraiScore_Routine") {
                onError("El archivo no es una rutina válida de TraiScore")
                return
            }

            if (exportableRoutine.sections.isEmpty()) {
                onError("La rutina no contiene ejercicios válidos")
                return
            }

            Log.d("RoutineExport", "Routine imported successfully: ${exportableRoutine.routineName}")
            onSuccess(exportableRoutine)

        } catch (e: Exception) {
            Log.e("RoutineExport", "Error importing routine", e)
            onError("Error al importar la rutina: ${e.message}")
        }
    }
    /**
     * Convierte ExportableRoutine a RoutineDocument para guardar en Firebase
     */
    fun convertToRoutineDocument(
        exportableRoutine: ExportableRoutine,
        currentUserId: String
    ): RoutineDocument {
        return RoutineDocument(
            userId = currentUserId,
            trainerId = exportableRoutine.originalTrainerId,
            documentId = "", // Se asignará al guardar en Firebase
            type = "", // Se determinará por las secciones
            createdAt = Timestamp.now(),
            clientName = exportableRoutine.clientName,
            routineName = exportableRoutine.routineName,
            sections = exportableRoutine.sections
        )
    }

    // Funciones privadas

    private fun generateFileName(routineName: String): String {
        val cleanName = routineName
            .replace(Regex("[^a-zA-Z0-9\\s]"), "")
            .replace("\\s+".toRegex(), "_")
            .take(20)  // Reducido para archivos JSON

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())
            .format(Date())

        return "TraiScore_${cleanName}_$timestamp$FILE_EXTENSION"
    }


    private fun createTempFile(context: Context, fileName: String): File {
        val cacheDir = File(context.cacheDir, "shared_routines")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        return File(cacheDir, fileName)
    }

    private fun createExportableRoutine(routine: RoutineDocument): ExportableRoutine {
        val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date())

        val originalDate = routine.createdAt?.let { timestamp ->
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(timestamp.toDate())
        }

        return ExportableRoutine(
            exportDate = currentDate,
            originalTrainerId = routine.trainerId,
            clientName = routine.clientName,
            routineName = routine.routineName,
            sections = routine.sections,
            metadata = RoutineMetadata(
                originalCreatedAt = originalDate,
                exportedBy = routine.trainerId ?: routine.userId
            )
        )
    }
    private fun writeJsonToFile(file: File, exportableRoutine: ExportableRoutine) {
        FileWriter(file).use { writer ->
            gson.toJson(exportableRoutine, writer)
        }
    }
}