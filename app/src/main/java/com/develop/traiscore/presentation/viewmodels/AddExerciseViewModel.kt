package com.develop.traiscore.presentation.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.develop.traiscore.core.DefaultCategoryExer
import com.develop.traiscore.data.firebaseData.SimpleExercise
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AddExerciseViewModel @Inject constructor() : ViewModel() {
    var exerciseNames by mutableStateOf<List<String>>(emptyList())
        private set
    var lastUsedExerciseName by mutableStateOf<String?>(null)
        private set

    private val routinesRef = Firebase.firestore.collection("routines")

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
    fun saveWorkoutEntry(
        title: String,
        reps: Int,
        weight: Double,
        rir: Int
    ) {
        val workoutData = hashMapOf(
            "title" to title,
            "reps" to reps,
            "weight" to weight,
            "rir" to rir,
            "timestamp" to Date()
        )

        Firebase.firestore.collection("workoutEntries")
            .add(workoutData)
            .addOnSuccessListener {
                println("✅ Entrada de entrenamiento guardada con ID: ${it.id}")
                updateLastUsedExercise(title)

            }
            .addOnFailureListener {
                println("❌ Error al guardar entrada: ${it.message}")
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

