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

    val routineTypes = mutableStateListOf<RoutineDocument>()

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

            if (fileName?.endsWith(".traiscore", ignoreCase = true) == true) {
                importViewModel.importRoutineFromUri(
                    context = context,
                    uri = uri,
                    onSuccess = { routineName, routineId ->
                        onSuccess("🎉 Rutina '$routineName' importada exitosamente")
                        // Recargar la lista de rutinas
                        loadRoutines(context) { /* no necesitamos manejar resultado aquí */ }
                    },
                    onError = { error ->
                        onError("❌ Error al importar: $error")
                    }
                )
            } else {
                onError("⚠️ Por favor selecciona un archivo .traiscore válido")
            }
        }
    }

    fun loadRoutines(
        context: Context,
        onComplete: (Boolean) -> Unit // true si hay rutinas, false si está vacío
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            isLoading = false
            onComplete(false)
            return
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("routines")
            .get()
            .addOnSuccessListener { result ->
                routineTypes.clear()
                val uniqueTypes = mutableSetOf<Pair<String, String>>()

                for (document in result) {
                    val docId = document.id
                    val clientName = document.getString("clientName") ?: "Cliente"
                    val routineName = document.getString("routineName") ?: clientName
                    val createdAt = document.getTimestamp("createdAt")
                    val trainerId = document.getString("trainerId")
                    val sectionsRaw = document.get("sections") as? List<Map<String, Any>> ?: emptyList()

                    for (section in sectionsRaw) {
                        val type = section["type"] as? String ?: continue
                        if (uniqueTypes.add(Pair(type, docId))) {
                            routineTypes.add(
                                RoutineDocument(
                                    userId = userId,
                                    trainerId = trainerId,
                                    type = type,
                                    documentId = docId,
                                    createdAt = createdAt,
                                    clientName = clientName,
                                    routineName = routineName,
                                    sections = emptyList()
                                )
                            )
                        }
                    }
                }
                isLoading = false
                onComplete(routineTypes.isNotEmpty())
            }
            .addOnFailureListener {
                Log.e("RoutineViewModel", "Error loading routines", it)
                isLoading = false
                onComplete(false)
            }
    }
    fun deleteRoutineWithUpdate(
        index: Int,
        routine: RoutineDocument,
        onResult: (Boolean, String) -> Unit
    ) {
        deleteRoutineType(routine.documentId) { success ->
            if (success) {
                routineTypes.removeAt(index)
                onResult(true, "Rutina eliminada")
            } else {
                onResult(false, "Error al eliminar rutina")
            }
        }
    }
    fun loadRoutine(documentId: String, userId: String) {
        Firebase.firestore
            .collection("users").document(userId)
            .collection("routines").document(documentId)
            .get()
            .addOnSuccessListener { snap ->
                if (!snap.exists()) return@addOnSuccessListener

                // 1) Campos básicos
                val clientName  = snap.getString("clientName") ?: ""
                val routineName = snap.getString("routineName") ?: ""
                val createdAt   = snap.getTimestamp("createdAt")
                val trainerId   = snap.getString("trainerId")

                // 2) Secciones (convertir a List<RoutineSection>)
                val sectionsRaw = snap.get("sections") as? List<Map<String,Any>> ?: emptyList()
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

                // 3) Asignamos a nuestro estado
                routineDocument = RoutineDocument(
                    userId      = userId,
                    trainerId   = trainerId,
                    type        = "",
                    documentId  = documentId,
                    createdAt   = createdAt,
                    clientName  = clientName,
                    routineName = routineName,
                    sections    = routineSections
                )
            }
            .addOnFailureListener { e ->
                Log.e("RoutineVM", "no pude cargar routine", e)
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


    fun deleteRoutineType(
        documentId: String,
        onResult: (Boolean) -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return onResult(false)

        val docRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("routines")
            .document(documentId)

        docRef.delete()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
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