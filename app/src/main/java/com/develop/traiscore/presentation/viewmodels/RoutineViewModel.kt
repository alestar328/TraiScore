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
import com.develop.traiscore.data.local.entity.RoutineHistoryEntity
import com.develop.traiscore.data.repository.RoutineRepository
import com.develop.traiscore.exports.ImportRoutineViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class RoutineViewModel @Inject constructor(
    private val routineRepository: RoutineRepository
) : ViewModel() {
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
    private var hasLoadedRoutines = false

    fun ensureRoutinesLoaded(context: Context, onComplete: (Boolean) -> Unit) {
        if (hasLoadedRoutines) {
            onComplete(routineTypes.isNotEmpty())
            return
        }
        hasLoadedRoutines = true
        loadRoutines(context, onComplete)
    }
    fun removeRoutineAt(index: Int) {
        val newList = routineTypes.toMutableList()
        newList.removeAt(index)
        routineTypes.clear()
        routineTypes.addAll(newList)
    }
    fun getTargetUserId(): String? {
        return targetClientId ?: FirebaseAuth.getInstance().currentUser?.uid
    }

    fun isClientMode(): Boolean = isWorkingOnClientRoutines


    fun markEmptyDialogShown() {
        hasShownEmptyDialog = true
    }
    fun createRoutineForUser(
        userId: String,
        clientName: String,
        trainerId: String? = null,
        routineType: String? = null,
        onComplete: (String?, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // 1) Crear la rutina en local
                val localId = routineRepository.createRoutineLocal(
                    userId = userId,
                    trainerId = trainerId,
                    type = routineType ?: "CUSTOM",
                    routineName = clientName
                )

                // 2) Devolver ID LOCAL a la UI
                onComplete(localId.toString(), null)

            } catch (e: Exception) {
                onComplete(null, e.message)
            }
        }
    }
    fun saveSectionToRoutineForUser(
        userId: String,
        routineId: String,
        sectionName: String,
        exercises: List<SimpleExercise>,
        onComplete: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val routineLocalId = routineId.toInt()

                // 1) Guardar sección local
                val sectionId = routineRepository.addSection(
                    routineLocalId = routineLocalId,
                    sectionName = sectionName
                )

                // 2) Guardar ejercicios locales asociados a la sección
                routineRepository.addExercises(
                    sectionId = sectionId,
                    exercises = exercises
                )

                onComplete(true, null)

                // 3) Backup en segundo plano
                routineRepository.backupRoutineToFirebase(routineLocalId)

            } catch (e: Exception) {
                onComplete(false, e.message)
            }
        }
    }
    fun deserializeSectionsSnapshot(snapshotJson: String): List<RoutineSection> {
        return routineRepository.deserializeSections(snapshotJson)
    }
    fun validateInput(input: String, columnType: ColumnType): String {
        if (input.isEmpty()) return input

        // Validar que no sea negativo
        if (input.startsWith("-")) return input.dropLast(1)

        return when (columnType) {

            ColumnType.EXERCISE_NAME -> {
                // No filtrar nada, nulo de validación → texto libre
                input
            }

            ColumnType.SERIES -> {
                val filtered = input.filter { it.isDigit() }
                if (filtered.length > 1) filtered.take(1) else filtered
            }

            ColumnType.WEIGHT -> {
                val filteredInput = input.filter { it.isDigit() || it == '.' }
                val parts = filteredInput.split(".")

                val intPart = parts.getOrNull(0)?.take(3)?.filter { it.isDigit() } ?: ""
                val decPart = parts.getOrNull(1)?.take(2)?.filter { it.isDigit() } ?: ""

                when {
                    input.count { it == '.' } > 1 -> filteredInput.dropLast(1)
                    parts.size == 1 -> intPart
                    else -> "$intPart.${decPart}"
                }
            }

            ColumnType.REPS -> {
                val filtered = input.filter { it.isDigit() }
                if (filtered.length > 2) filtered.take(2) else filtered
            }

            ColumnType.RIR -> {
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
            onComplete(false)
            return
        }

        viewModelScope.launch {
            isLoading = true
            try {
                // ✅ PRIMERO: Sincronizar desde Firebase si es necesario
                val localRoutines = routineRepository.getAllRoutines(userId)

                if (localRoutines.isEmpty()) {
                    Log.d("RoutineViewModel", "No hay rutinas en Room, intentando sincronizar desde Firebase")
                    try {
                        routineRepository.syncRoutinesFromFirebase(userId)
                        Log.d("RoutineViewModel", "Sincronización de Firebase completada")
                    } catch (e: Exception) {
                        Log.e("RoutineViewModel", "Error en sincronización inicial", e)
                    }
                }

                // ✅ SEGUNDO: Cargar rutinas (ahora deben estar en Room)
                val finalList = routineRepository.getAllRoutines(userId)
                routineTypes.clear()
                routineTypes.addAll(finalList)

                isLoading = false
                onComplete(finalList.isNotEmpty())

                Log.d("RoutineViewModel", "Rutinas cargadas: ${finalList.size}")

            } catch (e: Exception) {
                Log.e("RoutineViewModel", "Error cargando rutinas", e)
                isLoading = false
                onComplete(false)
            }
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

        viewModelScope.launch {
            try {
                val routines = routineRepository.getAllRoutines(targetUserId)
                val localId = documentId.toInt()

                routineDocument = routines.firstOrNull { it.documentId == documentId }
                    ?: routines.firstOrNull { it.documentId.isBlank() && it.userId == targetUserId }

            } catch (e: Exception) {
                Log.e("RoutineVM", "Error loading routine locally", e)
            }
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

                    when (columnType) {
                        ColumnType.EXERCISE_NAME -> ex.copy(name = newValue)
                        ColumnType.SERIES    -> ex.copy(series = newValue.toIntOrNull() ?: 0)
                        ColumnType.WEIGHT    -> ex.copy(weight = newValue)
                        ColumnType.REPS      -> ex.copy(reps = newValue)
                        ColumnType.RIR       -> ex.copy(rir = newValue.toIntOrNull() ?: 0)
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
        viewModelScope.launch {
            try {
                val localId = documentId.toInt()
                routineDocument?.let {
                    routineRepository.persistRoutineInRoom(localId, it)
                }
                // ✅ NUEVO: Guardar snapshot en historial con fecha actual
                routineRepository.saveRoutineSnapshot(localId)

                // Backup en Firebase (asíncrono)
                routineRepository.backupRoutineToFirebase(localId)

                onResult(true)
            } catch (e: Exception) {
                Log.e("RoutineViewModel", "Save failed", e)
                onResult(false)
            }
        }
    }
    fun getDatesWithRoutines(
        userId: String,
        onResult: (List<String>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val dates = routineRepository.getDatesWithRoutines(userId)
                onResult(dates)
            } catch (e: Exception) {
                Log.e("RoutineViewModel", "Error loading routine dates", e)
                onResult(emptyList())
            }
        }
    }

    /**
     * Obtiene rutinas guardadas en una fecha específica
     */
    fun getRoutinesByDate(
        userId: String,
        date: String,
        onResult: (List<RoutineHistoryEntity>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val routines = routineRepository.getRoutinesByDate(userId, date)
                onResult(routines)
            } catch (e: Exception) {
                Log.e("RoutineViewModel", "Error loading routines by date", e)
                onResult(emptyList())
            }
        }
    }
    // ✅ MODIFICAR: deleteRoutineType para usar targetClientId cuando esté disponible
    fun deleteRoutineType(
        documentId: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val localId = documentId.toInt()

                // Borramos todas las dependencias
                val sections = routineRepository.getSections(localId)
                val sectionIds = sections.map { it.id }

                routineRepository.deleteExercises(sectionIds)
                routineRepository.deleteSections(localId)
                routineRepository.deleteRoutine(localId)

                onResult(true)

            } catch (e: Exception) {
                onResult(false)
            }
        }
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
    fun addExerciseToSection(trainingType: String, newExercise: SimpleExercise) {
        val currentRoutine = routineDocument ?: return // ✅ Sin guión bajo

        val updatedSections = currentRoutine.sections.map { section ->
            if (section.type == trainingType) {
                // ✅ Añadir ejercicio al final de la lista
                section.copy(exercises = section.exercises + newExercise)
            } else {
                section
            }
        }

        routineDocument = currentRoutine.copy(sections = updatedSections) // ✅ Sin guión bajo
    }
}