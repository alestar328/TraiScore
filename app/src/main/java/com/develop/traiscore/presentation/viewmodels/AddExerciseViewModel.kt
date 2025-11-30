package com.develop.traiscore.presentation.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.develop.traiscore.core.DefaultCategoryExer
import com.develop.traiscore.data.firebaseData.SimpleExercise
import com.develop.traiscore.data.local.dao.ExerciseDao
import com.develop.traiscore.data.local.entity.ExerciseEntity
import com.develop.traiscore.data.repository.ExerciseRepository
import com.develop.traiscore.data.repository.SessionRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class ExerciseWithSource(
    val name: String,
    val category: String,
    val isUserCreated: Boolean,
    val documentId: String? = null // Solo para ejercicios del usuario
)

@HiltViewModel
class AddExerciseViewModel @Inject constructor(
    private val exerciseDao: ExerciseDao,
    private val sessionRepository: SessionRepository,
    private val exerciseRepository: ExerciseRepository

) : ViewModel() {
    val exerciseNames: StateFlow<List<String>> =
        exerciseRepository.exercises
            .map { list ->
                list.map { it.name }
                    .distinct()
                    .sorted()
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val userId = FirebaseAuth.getInstance().currentUser!!.uid
    private val firestore = Firebase.firestore
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val exercisesWithSource: StateFlow<List<ExerciseWithSource>> =
        exerciseRepository.exercises
            .map { exercises ->
                exercises.map { ex ->
                    ExerciseWithSource(
                        name = ex.name,
                        category = ex.category,
                        isUserCreated = !ex.isDefault,
                        documentId = ex.idIntern.takeIf { it.isNotEmpty() }
                    )
                }
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val exercisesWithCategory: StateFlow<List<Pair<String, String>>> =
        exerciseRepository.exercises
            .map { exercises ->
                exercises.map { ex -> ex.name to ex.category }
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _lastUsedExerciseName = mutableStateOf<String?>(null)
    val lastUsedExerciseName: String? get() = _lastUsedExerciseName.value

    private val _onExerciseAdded = MutableSharedFlow<Unit>(replay = 0)
    val onExerciseAdded = _onExerciseAdded.asSharedFlow()

    private val routinesRef = Firebase.firestore
        .collection("users")
        .document(userId)
        .collection("routines")

    val filteredExercisesWithSource: StateFlow<List<ExerciseWithSource>> =
        exerciseRepository.exercises
            .map { exercises ->
                exercises.map { ex ->
                    ExerciseWithSource(
                        name = ex.name,
                        category = ex.category,
                        isUserCreated = !ex.isDefault,
                        documentId = ex.idIntern.takeIf { it.isNotEmpty() }
                    )
                }
            }
            .combine(searchQuery) { exercises, query ->
                if (query.isBlank()) {
                    exercises
                } else {
                    exercises.filter { exercise ->
                        exercise.name.contains(query, ignoreCase = true) ||
                                exercise.category.contains(query, ignoreCase = true)
                    }
                }
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }
    private val firebaseCategoryToEnum = mapOf(
        "Pecho" to DefaultCategoryExer.CHEST,
        "Espalda" to DefaultCategoryExer.BACK,
        "Gluteos" to DefaultCategoryExer.GLUTES,
        "Pierna" to DefaultCategoryExer.LEGS,
        "Brazos" to DefaultCategoryExer.ARMS,
        "Hombros" to DefaultCategoryExer.SHOULDERS,
        "Abdomen" to DefaultCategoryExer.CORE
    )
    init {
        viewModelScope.launch {
            // 🧹 Limpiar duplicados existentes (solo una vez)
          //  exerciseRepository.cleanupDuplicates()

            exerciseRepository.importGlobalExercisesIfNeeded()
            exerciseRepository.importUserExercises()
        }
    }

    fun setLastUsedExercise(name: String) {
        _lastUsedExerciseName.value = name
    }
    fun deleteUserExercise(
        documentId: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Buscar ejercicio por Firebase ID
                val exercises = exerciseRepository.getExercises()
                val exercise = exercises.find { it.idIntern == documentId }

                if (exercise != null) {
                    exerciseRepository.deleteExercise(exercise)
                    onComplete(true, null)
                } else {
                    onComplete(false, "Ejercicio no encontrado")
                }
            } catch (e: Exception) {
                onComplete(false, e.message)
            }
        }
    }

    private fun normalizeKey(raw: String?): String? {
        if (raw == null) return null
        val noDiacritics = Normalizer.normalize(raw, Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")
        return noDiacritics.trim().lowercase(Locale.getDefault())
            .replace(Regex("\\s+"), " ")
    }

    // Alias tolerantes (singular/plural, con/sin acento, español)
    private val aliasEsToEnum: Map<String, DefaultCategoryExer> = mapOf(
        "pecho" to DefaultCategoryExer.CHEST,
        "espalda" to DefaultCategoryExer.BACK,
        "gluteo" to DefaultCategoryExer.GLUTES,
        "gluteos" to DefaultCategoryExer.GLUTES,
        "pierna" to DefaultCategoryExer.LEGS,
        "piernas" to DefaultCategoryExer.LEGS,
        "brazo" to DefaultCategoryExer.ARMS,
        "brazos" to DefaultCategoryExer.ARMS,
        "hombro" to DefaultCategoryExer.SHOULDERS,
        "hombros" to DefaultCategoryExer.SHOULDERS,
        "abdomen" to DefaultCategoryExer.CORE,
        "core" to DefaultCategoryExer.CORE
    )
    fun resolveCategoryEnum(raw: String?): DefaultCategoryExer? {
        if (raw.isNullOrBlank()) return null

        // 1) Intentar como nombre de enum (CORE, CHEST, ...)
        runCatching { return DefaultCategoryExer.valueOf(raw.trim().uppercase(Locale.getDefault())) }

        // 2) Intentar alias/español normalizado (Abdomen, Pecho, Glúteos, Piernas, ...)
        val key = normalizeKey(raw)
        return aliasEsToEnum[key]
    }
    fun updateUserExercise(
        documentId: String,
        newName: String,
        newCategory: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Buscar ejercicio por Firebase ID
                val exercises = exerciseRepository.getExercises()
                val exercise = exercises.find { it.idIntern == documentId }

                if (exercise != null) {
                    val updated = exercise.copy(
                        name = newName,
                        category = newCategory
                    )
                    exerciseRepository.updateExercise(updated)
                    onComplete(true, null)
                } else {
                    onComplete(false, "Ejercicio no encontrado")
                }
            } catch (e: Exception) {
                onComplete(false, e.message)
            }
        }
    }

    suspend fun saveExerciseToDatabase(name: String, category: String) {
        exerciseRepository.addExercise(name, category)
    }

    private suspend fun saveExerciseToFirebaseCompatible(
        name: String,
        category: String,
        exerciseDao: ExerciseDao,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            println("❌ Usuario no autenticado")
            onComplete(false)
            return
        }

        val userId = currentUser.uid

        // Primero guardar en local para mantener compatibilidad
        val localExercise = ExerciseEntity(
            id = 0,
            idIntern = "",
            name = name,
            category = category,
            isDefault = false
        )

        val localId = exerciseDao.insertExercise(localExercise)

        val db = Firebase.firestore
        val userExercisesCollection = db.collection("users")
            .document(userId)
            .collection("exercises")

        userExercisesCollection.get()
            .addOnSuccessListener { snapshot ->
                val userExerciseDocs = snapshot.documents.filter {
                    it.id.startsWith("userExer")
                }

                val nextNumber = (userExerciseDocs.mapNotNull {
                    it.id.removePrefix("userExer").toIntOrNull()
                }.maxOrNull() ?: 0) + 1

                val newDocId = "userExer$nextNumber"

                val newExercise = hashMapOf(
                    "name" to name,
                    "category" to category,
                    "isDefault" to false,
                    "createdBy" to userId,
                    "localId" to localId
                )

                userExercisesCollection
                    .document(newDocId)
                    .set(newExercise)
                    .addOnSuccessListener {
                        println("✅ Ejercicio guardado con ID: $newDocId")

                        CoroutineScope(Dispatchers.IO).launch {
                            val updatedExercise = localExercise.copy(
                                id = localId.toInt(),
                                idIntern = newDocId
                            )
                            exerciseDao.updateExercise(updatedExercise)
                            onComplete(true) // ✅ Callback de éxito
                        }
                    }
                    .addOnFailureListener { e ->
                        println("❌ Error al guardar el ejercicio: ${e.message}")
                        onComplete(false) // ✅ Callback de error
                    }
            }
            .addOnFailureListener { e ->
                println("❌ Error al obtener ejercicios existentes: ${e.message}")
                onComplete(false) // ✅ Callback de error
            }
    }


    fun fetchCategoryFor(exerciseName: String, onResult: (DefaultCategoryExer?) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return onResult(null)
        val userId = currentUser.uid

        Firebase.firestore.collection("users")
            .document(userId)
            .collection("exercises")
            .whereEqualTo("name", exerciseName)
            .get()
            .addOnSuccessListener { userSnapshot ->
                val userRaw = userSnapshot.documents.firstOrNull()?.getString("category")
                val userCat = resolveCategoryEnum(userRaw)
                if (userCat != null) {
                    onResult(userCat)
                } else {
                    Firebase.firestore.collection("exercises")
                        .whereEqualTo("name", exerciseName)
                        .get()
                        .addOnSuccessListener { globalSnapshot ->
                            val globalRaw = globalSnapshot.documents.firstOrNull()?.getString("category")
                            val globalCat = resolveCategoryEnum(globalRaw)
                            onResult(globalCat)
                        }
                        .addOnFailureListener { onResult(null) }
                }
            }
            .addOnFailureListener {
                Firebase.firestore.collection("exercises")
                    .whereEqualTo("name", exerciseName)
                    .get()
                    .addOnSuccessListener { globalSnapshot ->
                        val globalRaw = globalSnapshot.documents.firstOrNull()?.getString("category")
                        val globalCat = resolveCategoryEnum(globalRaw)
                        onResult(globalCat)
                    }
                    .addOnFailureListener { onResult(null) }
            }
    }
    fun hasActiveSession(callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = sessionRepository.getActiveSession()
                if (response.success && response.session != null) {
                    callback(true, response.session.name)
                } else {
                    callback(false, null)
                }
            } catch (e: Exception) {
                callback(false, null)
            }
        }
    }



    private fun addWorkoutWithoutSession(
        title: String,
        reps: Int,
        weight: Float,
        rir: Int
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val workoutData = mapOf(
            "title" to title,
            "reps" to reps,
            "weight" to weight,
            "rir" to rir,
            "timestamp" to Date()
            // Sin sessionId, sessionName, sessionColor para compatibilidad legacy
        )

        Firebase.firestore
            .collection("users")
            .document(userId)
            .collection("workoutEntries")
            .add(workoutData)
            .addOnSuccessListener { documentReference ->
                println("✅ Workout legacy agregado: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                println("❌ Error al agregar workout legacy: ${e.message}")
            }
    }

    fun saveSectionToRoutineForUser(
        userId: String,
        routineId: String,
        sectionName: String, // ✅ CAMBIO: String en lugar de DefaultCategoryExer
        exercises: List<SimpleExercise>,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val section = hashMapOf(
            "type" to sectionName, // ✅ CAMBIO: Usar sectionName directamente
            "exercises" to exercises.map { exercise ->
                hashMapOf(
                    "name" to exercise.name,
                    "series" to exercise.series,
                    "reps" to exercise.reps,
                    "weight" to exercise.weight,
                    "rir" to exercise.rir
                )
            }
        )

        firestore
            .collection("users")
            .document(userId)
            .collection("routines")
            .document(routineId)
            .update("sections", FieldValue.arrayUnion(section))
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { exception ->
                onComplete(false, exception.message)
            }
    }


    fun refreshExercises() {
        viewModelScope.launch {
            exerciseRepository.importGlobalExercisesIfNeeded()
            exerciseRepository.importUserExercises()
            exerciseRepository.syncPendingExercises()
        }
    }

}

