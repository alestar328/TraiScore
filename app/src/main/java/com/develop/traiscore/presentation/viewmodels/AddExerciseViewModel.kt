package com.develop.traiscore.presentation.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.develop.traiscore.core.DefaultCategoryExer
import com.develop.traiscore.data.firebaseData.SimpleExercise
import com.develop.traiscore.data.firebaseData.saveExerciseToFirebase
import com.develop.traiscore.data.local.dao.ExerciseDao
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AddExerciseViewModel @Inject constructor(
    private val exerciseDao: ExerciseDao,
) : ViewModel() {
    var exerciseNames by mutableStateOf<List<String>>(emptyList())
        private set
    var lastUsedExerciseName by mutableStateOf<String?>(null)
        private set
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid
    private val firestore = Firebase.firestore

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

    suspend fun saveExerciseToDatabase(name: String, category: String) {
        saveExerciseToFirebase(name, category, exerciseDao)
        refreshExercises() // ✅ Llamar refresh después de guardar
    }
    fun saveWorkoutEntry(
        title: String,
        exerciseId : Int,
        reps: Int,
        weight: Double,
        rir: Int
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
        Firebase.firestore.collection("exercises")
            .whereEqualTo("name", exerciseName)
            .get()
            .addOnSuccessListener { snapshot ->
                val categoryName = snapshot.documents.firstOrNull()?.getString("category")
                val matchedCategory = firebaseCategoryToEnum[categoryName]
                onResult(matchedCategory)
            }
            .addOnFailureListener {
                onResult(null)
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
        Firebase.firestore.collection("exercises")
            .get()
            .addOnSuccessListener { snapshot ->
                exerciseNames = snapshot.documents.mapNotNull { it.getString("name") }
            }
            .addOnFailureListener { exception ->
                println("❌ Error al refrescar ejercicios: ${exception.message}")
            }
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

