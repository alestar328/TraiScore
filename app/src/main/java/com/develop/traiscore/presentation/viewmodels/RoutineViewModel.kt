package com.develop.traiscore.presentation.viewmodels

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.develop.traiscore.core.ColumnType
import com.develop.traiscore.data.firebaseData.RoutineDocument
import com.develop.traiscore.data.firebaseData.RoutineSection
import com.develop.traiscore.data.firebaseData.SimpleExercise
import com.develop.traiscore.data.firebaseData.updateRoutineInFirebase
import com.develop.traiscore.exports.ImportRoutineViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import java.io.File

class RoutineViewModel : ViewModel() {
    // La rutina se almacena y actualiza aquí:
    var routineDocument by mutableStateOf<RoutineDocument?>(null)
    var hasShownEmptyDialog by mutableStateOf(false)
        private set
    var isLoading by mutableStateOf(true)
        private set
    private var targetClientId: String? = null
    private var isWorkingOnClientRoutines: Boolean = false
    val routineTypes = mutableStateListOf<RoutineDocument>()

    fun setTargetClient(clientId: String) {
        targetClientId = clientId
        isWorkingOnClientRoutines = true

        // ✅ LIMPIAR INMEDIATAMENTE al cambiar cliente
        routineTypes.clear()
        routineDocument = null
        hasShownEmptyDialog = false
        isLoading = true

        Log.d("RoutineViewModel", "Configurado para cliente: $clientId - Lista limpiada")
    }


    fun getTargetUserId(): String? {
        return targetClientId ?: FirebaseAuth.getInstance().currentUser?.uid
    }

    fun isClientMode(): Boolean = isWorkingOnClientRoutines


    fun markEmptyDialogShown() {
        hasShownEmptyDialog = true
    }

    fun validateInput(input: String, columnType: ColumnType): String {
        if (input.isEmpty()) return input

        // Validar que no sea negativo
        if (input.startsWith("-")) return input.dropLast(1)

        return when (columnType) {
            ColumnType.SERIES -> {
                // Solo 1 cifra, solo números enteros
                val filtered = input.filter { it.isDigit() }
                if (filtered.length > 1) filtered.take(1) else filtered
            }
            ColumnType.WEIGHT -> {
                val filteredInput = input.filter { it.isDigit() || it == '.' }
                val parts = filteredInput.split(".")

                val intPart = parts.getOrNull(0)?.take(3)?.filter { it.isDigit() } ?: ""
                val decPart = parts.getOrNull(1)?.take(2)?.filter { it.isDigit() } ?: ""

                return when {
                    input.count { it == '.' } > 1 -> filteredInput.dropLast(1) // evita múltiples puntos
                    parts.size == 1 -> intPart
                    else -> "$intPart.${decPart}"
                }
            }
            ColumnType.REPS -> {
                // Solo 2 cifras, solo números enteros
                val filtered = input.filter { it.isDigit() }
                if (filtered.length > 2) filtered.take(2) else filtered
            }
            ColumnType.RIR -> {
                // Solo 1 cifra, solo números enteros
                val filtered = input.filter { it.isDigit() }
                if (filtered.length > 1) filtered.take(1) else filtered
            }
        }
    }
    fun getFileName(context: Context, uri: Uri): String? {
        return try {
            when (uri.scheme) {
                "file" -> {
                    uri.path?.let { path ->
                        File(path).name
                    }
                }
                "content" -> {
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (nameIndex >= 0) cursor.getString(nameIndex) else null
                        } else null
                    }
                }
                else -> null
            }
        } catch (e: Exception) {
            Log.e("RoutineViewModel", "Error getting file name", e)
            null
        }
    }
    fun handleFileImport(
        context: Context,
        uri: Uri,
        importViewModel: ImportRoutineViewModel,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val fileName = getFileName(context, uri)
            Log.d("RoutineViewModel", "Attempting to import file: $fileName")

            // NO verificar extensión - validar contenido directamente
            // WhatsApp y otras apps pueden cambiar nombres de archivo
            Log.d("RoutineViewModel", "Validating file content...")

            if (isValidTraiScoreJson(context, uri)) {
                Log.d("RoutineViewModel", "Valid TraiScore JSON detected, importing...")

                importViewModel.importRoutineFromUri(
                    context = context,
                    uri = uri,
                    onSuccess = { routineName, routineId ->
                        onSuccess("🎉 Rutina '$routineName' importada exitosamente")
                        // Recargar la lista de rutinas
                        loadRoutines(context) { /* no necesitamos manejar resultado aquí */ }
                    },
                    onError = { error ->
                        Log.e("RoutineViewModel", "Import failed: $error")
                        onError("❌ Error al importar: $error")
                    }
                )
            } else {
                Log.w("RoutineViewModel", "File is not a valid TraiScore routine")
                onError("⚠️ El archivo no es una rutina válida de TraiScore")
            }
        }
    }

    fun loadRoutines(
        context: Context,
        onComplete: (Boolean) -> Unit
    ) {
        val userId = getTargetUserId()
        if (userId == null) {
            isLoading = false
            onComplete(false)
            return
        }

        Log.d("RoutineViewModel", "=== LOAD ROUTINES DEBUG ===")
        Log.d("RoutineViewModel", "Target user: $userId")
        Log.d("RoutineViewModel", "Current list size BEFORE clear: ${routineTypes.size}")

        // ✅ LIMPIAR SIEMPRE al inicio
        routineTypes.clear()

        Log.d("RoutineViewModel", "List cleared, size: ${routineTypes.size}")

        // ✅ MARCAR COMO CARGANDO
        isLoading = true

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("routines")
            .get()
            .addOnSuccessListener { result ->
                Log.d("RoutineViewModel", "Firebase query successful, documents: ${result.size()}")

                // ✅ VERIFICAR QUE LA LISTA SIGUE VACÍA (por si acaso)
                if (routineTypes.isNotEmpty()) {
                    Log.w("RoutineViewModel", "⚠️ Lista no estaba vacía después del clear! Forzando limpieza...")
                    routineTypes.clear()
                }

                val newRoutines = mutableListOf<RoutineDocument>()

                for (document in result) {
                    val docId = document.id
                    val clientName = document.getString("clientName") ?: "Cliente"
                    val routineName = document.getString("routineName") ?: clientName
                    val createdAt = document.getTimestamp("createdAt")
                    val trainerId = document.getString("trainerId")
                    val sectionsRaw = document.get("sections") as? List<Map<String, Any>> ?: emptyList()
                    val firstSectionType = sectionsRaw.firstOrNull()?.get("type") as? String ?: ""

                    newRoutines.add(
                        RoutineDocument(
                            userId = userId,
                            trainerId = trainerId,
                            type = firstSectionType,
                            documentId = docId,
                            createdAt = createdAt,
                            clientName = clientName,
                            routineName = routineName,
                            sections = emptyList()
                        )
                    )

                    Log.d("RoutineViewModel", "Routine added: $docId - $routineName")
                }

                // ✅ AGREGAR TODAS LAS RUTINAS DE UNA VEZ
                routineTypes.addAll(newRoutines)

                Log.d("RoutineViewModel", "Final list size: ${routineTypes.size}")
                isLoading = false
                onComplete(routineTypes.isNotEmpty())
            }
            .addOnFailureListener { exception ->
                Log.e("RoutineViewModel", "Error loading routines for user $userId", exception)
                routineTypes.clear() // ✅ Limpiar también en caso de error
                isLoading = false
                onComplete(false)
            }
    }

    private fun isValidTraiScoreJson(context: Context, uri: Uri): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val content = inputStream?.bufferedReader()?.use { it.readText() }

            Log.d("RoutineViewModel", "=== CONTENT VALIDATION DEBUG ===")
            Log.d("RoutineViewModel", "Content length: ${content?.length}")
            Log.d("RoutineViewModel", "Content preview: ${content?.take(300)}...")

            if (content.isNullOrBlank()) {
                Log.w("RoutineViewModel", "File content is empty")
                return false
            }

            // Verificar estructura JSON básica
            val looksLikeJson = content.trim().startsWith("{") && content.trim().endsWith("}")
            Log.d("RoutineViewModel", "Looks like JSON: $looksLikeJson")

            if (!looksLikeJson) {
                return false
            }

            // Usar Gson para validación precisa
            return try {
                val gson = com.google.gson.Gson()
                val jsonObject = gson.fromJson(content, com.google.gson.JsonObject::class.java)

                val hasFileType = jsonObject?.has("fileType") == true &&
                        jsonObject.get("fileType")?.asString == "TraiScore_Routine"
                val hasRoutineName = jsonObject?.has("routineName") == true
                val hasSections = jsonObject?.has("sections") == true
                val hasAppVersion = jsonObject?.has("appVersion") == true

                Log.d("RoutineViewModel", "GSON Validation results:")
                Log.d("RoutineViewModel", "- fileType = TraiScore_Routine: $hasFileType")
                Log.d("RoutineViewModel", "- Has routineName: $hasRoutineName")
                Log.d("RoutineViewModel", "- Has sections: $hasSections")
                Log.d("RoutineViewModel", "- Has appVersion: $hasAppVersion")

                val isValid = hasFileType && hasRoutineName && hasSections && hasAppVersion
                Log.d("RoutineViewModel", "Overall validation result: $isValid")

                return isValid

            } catch (jsonException: Exception) {
                Log.e("RoutineViewModel", "JSON parsing failed", jsonException)
                false
            }

        } catch (e: Exception) {
            Log.e("RoutineViewModel", "Error validating JSON content", e)
            false
        }
    }
    fun loadRoutine(documentId: String, userId: String? = null) {
        val targetUserId = userId ?: getTargetUserId() ?: return

        Log.d("RoutineViewModel", "=== LOAD ROUTINE DEBUG ===")
        Log.d("RoutineViewModel", "Loading routine: $documentId")
        Log.d("RoutineViewModel", "Target user: $targetUserId")
        Log.d("RoutineViewModel", "Is client mode: $isWorkingOnClientRoutines")

        Firebase.firestore
            .collection("users").document(targetUserId)
            .collection("routines").document(documentId)
            .get()
            .addOnSuccessListener { snap ->
                if (!snap.exists()) {
                    Log.e("RoutineViewModel", "Documento no existe: $documentId")
                    return@addOnSuccessListener
                }

                // 1) Campos básicos
                val clientName  = snap.getString("clientName") ?: ""
                val routineName = snap.getString("routineName") ?: ""
                val createdAt   = snap.getTimestamp("createdAt")
                val trainerId   = snap.getString("trainerId")

                Log.d("RoutineViewModel", "Routine data found: $routineName")

                // 2) Secciones (convertir a List<RoutineSection>)
                val sectionsRaw = snap.get("sections") as? List<Map<String,Any>> ?: emptyList()
                Log.d("RoutineViewModel", "Sections found: ${sectionsRaw.size}")

                val routineSections = sectionsRaw.mapNotNull { section ->
                    val type = section["type"] as? String ?: return@mapNotNull null
                    val exercisesRaw = section["exercises"] as? List<Map<String,Any>> ?: emptyList()
                    val exercises = exercisesRaw.mapNotNull { ex ->
                        SimpleExercise(
                            name   = ex["name"]   as? String ?: return@mapNotNull null,
                            series = (ex["series"] as? Number)?.toInt() ?: 0,
                            reps   = ex["reps"]   as? String ?: "",
                            weight = ex["weight"] as? String ?: "",
                            rir    = (ex["rir"]   as? Number)?.toInt() ?: 0
                        )
                    }
                    RoutineSection(type = type, exercises = exercises)
                }

                Log.d("RoutineViewModel", "Parsed sections: ${routineSections.size}")

                // 3) Asignamos a nuestro estado
                routineDocument = RoutineDocument(
                    userId      = targetUserId,
                    trainerId   = trainerId,
                    type        = "",
                    documentId  = documentId,
                    createdAt   = createdAt,
                    clientName  = clientName,
                    routineName = routineName,
                    sections    = routineSections
                )

                Log.d("RoutineViewModel", "✅ Routine loaded successfully: ${routineDocument?.routineName}")
            }
            .addOnFailureListener { e ->
                Log.e("RoutineVM", "Error cargando routine para usuario $targetUserId", e)
            }
    }


    fun getExercisesByType(type: String): List<SimpleExercise> {
        return routineDocument?.sections?.firstOrNull { it.type == type }?.exercises ?: emptyList()
    }

    // DESPUÉS:
    fun updateExerciseFieldInMemory(
        exerciseIndex: Int,
        trainingType: String,
        columnType: ColumnType,
        newValue: String
    ) {
        routineDocument = routineDocument?.let { data ->
            val updatedSections = data.sections.map { section ->
                if (section.type != trainingType) return@map section

                val updatedExercises = section.exercises.mapIndexed { idx, ex ->
                    if (idx != exerciseIndex) return@mapIndexed ex

                    when(columnType) {
                        ColumnType.SERIES -> {
                            val v = newValue.toIntOrNull() ?: 0
                            ex.copy(series = v)
                        }
                        ColumnType.WEIGHT -> ex.copy(weight = newValue)
                        ColumnType.REPS   -> ex.copy(reps = newValue)
                        ColumnType.RIR    -> {
                            val v = newValue.toIntOrNull() ?: 0
                            ex.copy(rir = v)
                        }
                    }
                }

                section.copy(exercises = updatedExercises)
            }

            data.copy(sections = updatedSections)
        }
    }

    fun saveRoutineToFirebase(
        documentId: String,
        onResult: (Boolean) -> Unit
    ) {
        val currentRoutine = routineDocument
        if (currentRoutine == null) {
            onResult(false)
            return
        }

        updateRoutineInFirebase(documentId, currentRoutine)
            .addOnSuccessListener {
                onResult(true)
            }
            .addOnFailureListener { exception ->
                Log.e("RoutineViewModel", "Error saving routine", exception)
                onResult(false)
            }
    }


    // ✅ MODIFICAR: deleteRoutineType para usar targetClientId cuando esté disponible
    fun deleteRoutineType(
        documentId: String,
        onResult: (Boolean) -> Unit
    ) {
        val uid = getTargetUserId() ?: return onResult(false) // ✅ CAMBIO: usar getTargetUserId()

        val docRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("routines")
            .document(documentId)

        docRef.delete()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun clearTargetClient() {
        targetClientId = null
        isWorkingOnClientRoutines = false

        // ✅ LIMPIAR COMPLETAMENTE al salir del modo cliente
        routineTypes.clear()
        routineDocument = null
        hasShownEmptyDialog = false
        isLoading = true

        Log.d("RoutineViewModel", "Cliente objetivo eliminado, estado limpiado completamente")
    }

    fun cleanRoutine() {
        routineDocument = routineDocument?.let { data ->
            val cleanedSections = data.sections.map { section ->
                val cleanedExercises = section.exercises.map { exercise ->
                    exercise.copy(reps = "")
                }
                section.copy(exercises = cleanedExercises)
            }
            data.copy(sections = cleanedSections)
        }
    }
}