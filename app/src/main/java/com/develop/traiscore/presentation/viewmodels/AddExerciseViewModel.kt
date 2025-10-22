package com.develop.traiscore.presentation.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.develop.traiscore.core.DefaultCategoryExer
import com.develop.traiscore.data.firebaseData.SimpleExercise
import com.develop.traiscore.data.firebaseData.saveExerciseToFirebase
import com.develop.traiscore.data.local.dao.ExerciseDao
import com.develop.traiscore.data.local.entity.ExerciseEntity
import com.develop.traiscore.data.repository.SessionRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
    private val workoutEntryViewModel: WorkoutEntryViewModel,
    private val sessionRepository: SessionRepository,
) : ViewModel() {
    var exerciseNames by mutableStateOf<List<String>>(emptyList())
        private set

    private val userId = FirebaseAuth.getInstance().currentUser!!.uid
    private val firestore = Firebase.firestore

    var exercisesWithSource by mutableStateOf<List<ExerciseWithSource>>(emptyList())
        private set

    var exercisesWithCategory by mutableStateOf<List<Pair<String, String>>>(emptyList())
        private set

    private val _lastUsedExerciseName = mutableStateOf<String?>(null)
    val lastUsedExerciseName: String? get() = _lastUsedExerciseName.value

    private val _onExerciseAdded = MutableSharedFlow<Unit>(replay = 0)
    val onExerciseAdded = _onExerciseAdded.asSharedFlow()

    private val routinesRef = Firebase.firestore
        .collection("users")
        .document(userId)
        .collection("routines")


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
        // Consulta a la colección "exercises" en Firebase
        Firebase.firestore.collection("exercises")
            .get()
            .addOnSuccessListener { snapshot ->
                exerciseNames = snapshot.documents.mapNotNull { it.getString("name") }
            }
            .addOnFailureListener { exception ->
            }
    }
    init {
        loadAllExercisesWithSource()

        loadAllExercises()
    }
    fun deleteUserExercise(
        documentId: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            onComplete(false, "Usuario no autenticado")
            return
        }

        val userId = currentUser.uid

        Firebase.firestore.collection("users")
            .document(userId)
            .collection("exercises")
            .document(documentId)
            .delete()
            .addOnSuccessListener {
                println("✅ Ejercicio eliminado exitosamente")
                loadAllExercisesWithSource() // Refrescar la lista
                onComplete(true, null)
            }
            .addOnFailureListener { exception ->
                println("❌ Error al eliminar ejercicio: ${exception.message}")
                onComplete(false, exception.message)
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
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            onComplete(false, "Usuario no autenticado")
            return
        }

        val userId = currentUser.uid
        val updateData = hashMapOf<String, Any>(
            "name" to newName,
            "category" to newCategory
        )

        Firebase.firestore.collection("users")
            .document(userId)
            .collection("exercises")
            .document(documentId)
            .update(updateData)
            .addOnSuccessListener {
                println("✅ Ejercicio actualizado exitosamente")
                loadAllExercisesWithSource() // Refrescar la lista
                onComplete(true, null)
            }
            .addOnFailureListener { exception ->
                println("❌ Error al actualizar ejercicio: ${exception.message}")
                onComplete(false, exception.message)
            }
    }

    fun loadAllExercisesWithSource() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            println("❌ Usuario no autenticado")
            return
        }

        val userId = currentUser.uid
        val allExercisesWithSource = mutableListOf<ExerciseWithSource>()

        // Primero cargar ejercicios por defecto de la colección global
        Firebase.firestore.collection("exercises")
            .get()
            .addOnSuccessListener { globalSnapshot ->
                println("🔍 Ejercicios globales encontrados: ${globalSnapshot.size()}")
                val defaultExercises = globalSnapshot.documents.mapNotNull { doc ->
                    val name = doc.getString("name")
                    val category = doc.getString("category")
                    println("📄 Ejercicio global: $name - $category")
                    if (name != null && category != null) {
                        ExerciseWithSource(
                            name = name,
                            category = category,
                            isUserCreated = false,
                            documentId = null
                        )
                    } else null
                }
                allExercisesWithSource.addAll(defaultExercises)
                println("✅ ${defaultExercises.size} ejercicios por defecto cargados")

                // Luego cargar ejercicios personalizados de la subcolección del usuario
                Firebase.firestore.collection("users")
                    .document(userId)
                    .collection("exercises")
                    .get()
                    .addOnSuccessListener { userSnapshot ->
                        println("🔍 Ejercicios del usuario encontrados: ${userSnapshot.size()}")
                        val userExercises = userSnapshot.documents.mapNotNull { doc ->
                            val name = doc.getString("name")
                            val category = doc.getString("category")
                            println("📄 Ejercicio usuario: $name - $category - ID: ${doc.id}")
                            if (name != null && category != null) {
                                ExerciseWithSource(
                                    name = name,
                                    category = category,
                                    isUserCreated = true,
                                    documentId = doc.id
                                )
                            } else null
                        }
                        allExercisesWithSource.addAll(userExercises)
                        println("✅ ${userExercises.size} ejercicios del usuario cargados")

                        // Actualizar la lista combinada sin duplicados por nombre
                        exercisesWithSource = allExercisesWithSource.distinctBy { it.name }

                        // También actualizar la lista original para compatibilidad
                        exercisesWithCategory = allExercisesWithSource.map { Pair(it.name, it.category) }

                        println("🎯 Total ejercicios finales: ${exercisesWithSource.size}")
                        exercisesWithSource.forEach { exercise ->
                            println("📋 ${exercise.name} (${exercise.category}) - Usuario: ${exercise.isUserCreated}")
                        }
                    }
                    .addOnFailureListener { exception ->
                        println("❌ Error al cargar ejercicios del usuario: ${exception.message}")
                        exercisesWithSource = defaultExercises
                        exercisesWithCategory = defaultExercises.map { Pair(it.name, it.category) }
                    }
            }
            .addOnFailureListener { exception ->
                println("❌ Error al cargar ejercicios por defecto: ${exception.message}")
            }
    }

    fun loadAllExercisesWithCategory() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            println("❌ Usuario no autenticado")
            return
        }

        val userId = currentUser.uid
        val allExercisesWithCategory = mutableListOf<Pair<String, String>>()

        // Primero cargar ejercicios por defecto de la colección global
        Firebase.firestore.collection("exercises")
            .get()
            .addOnSuccessListener { globalSnapshot ->
                val defaultExercises = globalSnapshot.documents.mapNotNull { doc ->
                    val name = doc.getString("name")
                    val category = doc.getString("category")
                    if (name != null && category != null) Pair(name, category) else null
                }
                allExercisesWithCategory.addAll(defaultExercises)

                // Luego cargar ejercicios personalizados de la subcolección del usuario
                Firebase.firestore.collection("users")
                    .document(userId)
                    .collection("exercises")
                    .get()
                    .addOnSuccessListener { userSnapshot ->
                        val userExercises = userSnapshot.documents.mapNotNull { doc ->
                            val name = doc.getString("name")
                            val category = doc.getString("category")
                            if (name != null && category != null) Pair(name, category) else null
                        }
                        allExercisesWithCategory.addAll(userExercises)

                        // Actualizar la lista combinada sin duplicados (por nombre)
                        exercisesWithCategory = allExercisesWithCategory.distinctBy { it.first }
                    }
                    .addOnFailureListener { exception ->
                        println("❌ Error al cargar ejercicios del usuario: ${exception.message}")
                        // Si falla, al menos usar los ejercicios por defecto
                        exercisesWithCategory = defaultExercises
                    }
            }
            .addOnFailureListener { exception ->
                println("❌ Error al cargar ejercicios por defecto: ${exception.message}")
            }
    }

    private fun loadAllExercises() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            println("❌ Usuario no autenticado")
            return
        }

        val userId = currentUser.uid
        val allExercises = mutableListOf<String>()

        // Primero cargar ejercicios por defecto de la colección global
        Firebase.firestore.collection("exercises")
            .get()
            .addOnSuccessListener { globalSnapshot ->
                val defaultExercises = globalSnapshot.documents.mapNotNull { it.getString("name") }
                allExercises.addAll(defaultExercises)

                // Luego cargar ejercicios personalizados de la subcolección del usuario
                Firebase.firestore.collection("users")
                    .document(userId)
                    .collection("exercises")
                    .get()
                    .addOnSuccessListener { userSnapshot ->
                        val userExercises = userSnapshot.documents.mapNotNull { it.getString("name") }
                        allExercises.addAll(userExercises)

                        // Actualizar la lista combinada sin duplicados
                        exerciseNames = allExercises.distinct()
                    }
                    .addOnFailureListener { exception ->
                        println("❌ Error al cargar ejercicios del usuario: ${exception.message}")
                        // Si falla, al menos usar los ejercicios por defecto
                        exerciseNames = defaultExercises
                    }
            }
            .addOnFailureListener { exception ->
                println("❌ Error al cargar ejercicios por defecto: ${exception.message}")
            }
    }

    suspend fun saveExerciseToDatabase(name: String, category: String) {
        saveExerciseToFirebaseCompatible(name, category, exerciseDao) { success ->
            if (success) {
                loadAllExercisesWithSource() // Usar la nueva función de carga
            }
        }
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

    fun addExerciseToActiveSession(
        title: String,
        reps: Int,
        weight: Float,
        rir: Int
    ) {
        viewModelScope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch

                val activeSessionResponse = sessionRepository.getActiveSession()

                val workoutData = if (activeSessionResponse.success && activeSessionResponse.session != null) {
                    val session = activeSessionResponse.session
                    hashMapOf(
                        "title" to title,
                        "reps" to reps,
                        "weight" to weight,
                        "rir" to rir,
                        "timestamp" to Date(),
                        "sessionId" to session.sessionId,
                        "sessionName" to session.name,
                        "sessionColor" to session.color
                    )
                } else {
                    hashMapOf(
                        "title" to title,
                        "reps" to reps,
                        "weight" to weight,
                        "rir" to rir,
                        "timestamp" to Date()
                    )
                }

                Firebase.firestore
                    .collection("users")
                    .document(userId)
                    .collection("workoutEntries")
                    .add(workoutData)
                    .await()

                if (activeSessionResponse.success && activeSessionResponse.session != null) {
                    sessionRepository.incrementWorkoutCount(activeSessionResponse.session.sessionId)
                }

                _lastUsedExerciseName.value = title

                println("✅ Ejercicio agregado correctamente")

                // 🔹 Emitir evento de actualización
                _onExerciseAdded.emit(Unit)

            } catch (e: Exception) {
                println("❌ Error agregando ejercicio: ${e.message}")
                e.printStackTrace()
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
    fun createRoutineForUser(
        userId: String,
        clientName: String,
        trainerId: String? = null,
        routineType: String? = null, // 👈 nuevo opcional
        onComplete: (String?, String?) -> Unit
    ) {
        val routineDocument = hashMapOf(
            "clientName" to clientName,
            "routineName" to clientName,
            "type" to (routineType ?: "CUSTOM"), // 👈 guarda el tipo
            "createdAt" to FieldValue.serverTimestamp(),
            "trainerId" to trainerId,
            "sections" to emptyList<Map<String, Any>>()
        )
        firestore.collection("users").document(userId)
            .collection("routines")
            .add(routineDocument)
            .addOnSuccessListener { onComplete(it.id, null) }
            .addOnFailureListener { ex -> onComplete(null, ex.message) }
    }

    fun refreshExercises() {
        loadAllExercises() // Reutilizar la función que ya carga ambas fuentes
    }
    fun createRoutine(
        clientName: String,
        trainerId: String? = null,
        onComplete: (routineId: String?, error: String?) -> Unit
    ) {
        val docRef = routinesRef.document()
        val base = mapOf(
            "userId"     to userId,
            "trainerId"  to trainerId,
            "clientName" to clientName,
            "routineName" to clientName, // AGREGAR ESTE CAMPO
            "createdAt"  to com.google.firebase.Timestamp.now(),
            "sections"   to emptyList<Map<String,Any>>()
        )
        docRef.set(base)
            .addOnSuccessListener { onComplete(docRef.id, null) }
            .addOnFailureListener { e -> onComplete(null, e.message) }
    }



    fun saveSectionToRoutine(
        routineId: String,
        sectionType: DefaultCategoryExer,
        exercises: List<SimpleExercise>,
        onComplete: (success: Boolean, errorMsg: String?) -> Unit
    ) {
        val sectionObject = mapOf(
            "type" to sectionType.name,
            "exercises" to exercises.map { exe ->
                mapOf(
                    "name"   to exe.name,
                    "series" to exe.series,
                    "weight" to exe.weight,
                    "reps"   to exe.reps,
                    "rir"    to exe.rir
                )
            }
        )
        // 2) Hacemos update con arrayUnion para no borrar secciones previas
        routinesRef
            .document(routineId)
            .update("sections", FieldValue.arrayUnion(sectionObject))
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                onComplete(false, e.localizedMessage)
            }
    }

}

