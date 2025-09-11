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
import com.develop.traiscore.data.repository.SessionRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AddExerciseViewModel @Inject constructor(
    private val exerciseDao: ExerciseDao,
    private val workoutEntryViewModel: WorkoutEntryViewModel,
    private val sessionRepository: SessionRepository,
) : ViewModel() {
    var exerciseNames by mutableStateOf<List<String>>(emptyList())
        private set
    var lastUsedExerciseName by mutableStateOf<String?>(null)
        private set
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid
    private val firestore = Firebase.firestore

    var exercisesWithCategory by mutableStateOf<List<Pair<String, String>>>(emptyList())
        private set


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
        // Cargar ejercicios de la colección global (ejercicios por defecto)
        // Y de la subcolección del usuario (ejercicios personalizados)
        loadAllExercises()
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
        saveExerciseToFirebase(name, category, exerciseDao)
        refreshExercises() // ✅ Llamar refresh después de guardar
    }
    fun saveWorkoutEntry(
        title: String,
        exerciseId : Int,
        reps: Int,
        weight: Double,
        rir: Int,
        sessionData: Triple<String, String, String>? = null
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            println("❌ Usuario no autenticado")
            return
        }

        val workoutData = hashMapOf(
            "uid" to userId,
            "title" to title,
            "exerciseId" to exerciseId,
            "reps" to reps,
            "weight" to weight,
            "rir" to rir,
            "timestamp" to Date()
        )
        sessionData?.let { (sessionId, sessionName, sessionColor) ->
            workoutData["sessionId"] = sessionId
            workoutData["sessionName"] = sessionName
            workoutData["sessionColor"] = sessionColor
        }


        // Guardar la entrada de entrenamiento en la subcolección workoutEntries del usuario
        Firebase.firestore.collection("users")
            .document(userId)  // Usamos el UID del usuario autenticado
            .collection("workoutEntries")
            .add(workoutData)
            .addOnSuccessListener {
                println("✅ Entrada de entrenamiento guardada con ID: ${it.id}")
                updateLastUsedExercise(title)
            }
            .addOnFailureListener { exception ->
                println("❌ Error al guardar entrada: ${exception.message}")
            }
    }
    fun updateLastUsedExercise(name: String) {
        lastUsedExerciseName = name
    }
    fun fetchCategoryFor(exerciseName: String, onResult: (DefaultCategoryExer?) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            onResult(null)
            return
        }

        val userId = currentUser.uid

        // Primero buscar en ejercicios personalizados del usuario
        Firebase.firestore.collection("users")
            .document(userId)
            .collection("exercises")
            .whereEqualTo("name", exerciseName)
            .get()
            .addOnSuccessListener { userSnapshot ->
                val userCategoryName = userSnapshot.documents.firstOrNull()?.getString("category")
                val userMatchedCategory = firebaseCategoryToEnum[userCategoryName]

                if (userMatchedCategory != null) {
                    // Encontrado en ejercicios del usuario
                    onResult(userMatchedCategory)
                } else {
                    // Si no se encuentra, buscar en ejercicios por defecto
                    Firebase.firestore.collection("exercises")
                        .whereEqualTo("name", exerciseName)
                        .get()
                        .addOnSuccessListener { globalSnapshot ->
                            val globalCategoryName = globalSnapshot.documents.firstOrNull()?.getString("category")
                            val globalMatchedCategory = firebaseCategoryToEnum[globalCategoryName]
                            onResult(globalMatchedCategory)
                        }
                        .addOnFailureListener {
                            onResult(null)
                        }
                }
            }
            .addOnFailureListener {
                // Si falla la búsqueda en ejercicios del usuario, buscar en globales
                Firebase.firestore.collection("exercises")
                    .whereEqualTo("name", exerciseName)
                    .get()
                    .addOnSuccessListener { globalSnapshot ->
                        val categoryName = globalSnapshot.documents.firstOrNull()?.getString("category")
                        val matchedCategory = firebaseCategoryToEnum[categoryName]
                        onResult(matchedCategory)
                    }
                    .addOnFailureListener {
                        onResult(null)
                    }
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
                val activeSessionResponse = sessionRepository.getActiveSession()

                if (activeSessionResponse.success && activeSessionResponse.session != null) {
                    val session = activeSessionResponse.session

                    // Agregar workout a la sesión activa
                    workoutEntryViewModel.addWorkoutToActiveSession(
                        title = title,
                        reps = reps,
                        weight = weight,
                        rir = rir,
                        activeSessionId = session.sessionId,
                        activeSessionName = session.name,
                        activeSessionColor = session.color
                    )

                    // Incrementar contador de workouts en la sesión
                    sessionRepository.incrementWorkoutCount(session.sessionId)
                    updateLastUsedExercise(title)

                    println("✅ Ejercicio agregado a sesión: ${session.name}")

                } else {
                    // No hay sesión activa - crear workout sin sesión (legacy)
                    addWorkoutWithoutSession(title, reps, weight, rir)
                    updateLastUsedExercise(title)
                    println("⚠️ Ejercicio agregado sin sesión activa")
                }

            } catch (e: Exception) {
                println("❌ Error agregando ejercicio: ${e.message}")
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
        onComplete: (String?, String?) -> Unit
    ) {
        val routineDocument = hashMapOf(
            "clientName" to clientName,
            "routineName" to clientName,
            "createdAt" to FieldValue.serverTimestamp(),
            "trainerId" to trainerId,
            "sections" to emptyList<Map<String, Any>>()
        )

        firestore
            .collection("users")
            .document(userId) // ✅ USAR el userId proporcionado
            .collection("routines")
            .add(routineDocument)
            .addOnSuccessListener { documentReference ->
                onComplete(documentReference.id, null)
            }
            .addOnFailureListener { exception ->
                onComplete(null, exception.message)
            }
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

