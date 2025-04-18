package com.develop.traiscore.presentation.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.develop.traiscore.data.firebaseData.saveExerciseToFirebase
import com.develop.traiscore.domain.defaultExerciseEntities
import com.develop.traiscore.domain.model.Resource
import com.develop.traiscore.domain.model.WorkoutModel
import com.develop.traiscore.domain.usecase.SaveWorkoutUseCase
import com.develop.traiscore.domain.usecase.UpdateWorkoutUseCase
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AddExerciseViewModel @Inject constructor() : ViewModel() {
    var exerciseNames by mutableStateOf<List<String>>(emptyList())
        private set
    var lastUsedExerciseName by mutableStateOf<String?>(null)
        private set
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

}

