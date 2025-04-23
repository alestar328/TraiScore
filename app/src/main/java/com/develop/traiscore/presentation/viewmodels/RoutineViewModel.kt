package com.develop.traiscore.presentation.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.develop.traiscore.data.firebaseData.updateRoutineInFirebase
import com.develop.traiscore.presentation.screens.RoutineData
import com.google.firebase.firestore.FirebaseFirestore

class RoutineViewModel : ViewModel() {
    // La rutina se almacena y actualiza aquí:
    var routineData by mutableStateOf<RoutineData?>(null)

    fun updateReps(exerciseIndex: Int, trainingType: String, newReps: String) {
        routineData = routineData?.let { data ->
            // Actualiza la lista de ejercicios para el trainingType
            val updatedList = data.routine[trainingType]?.mapIndexed { index, exercise ->
                if (index == exerciseIndex) exercise.copy(reps = newReps) else exercise
            } ?: emptyList()
            val updatedRoutine = data.routine.toMutableMap()
            updatedRoutine[trainingType] = updatedList
            data.copy(routine = updatedRoutine)
        }
    }

    fun saveRoutine(documentId: String, onResult: (Boolean) -> Unit) {
        routineData?.let { data ->
            updateRoutineInFirebase(documentId, data)
                .addOnSuccessListener {
                    Log.d("RoutineViewModel", "Rutina actualizada correctamente")
                    onResult(true) // Operación exitosa
                }
                .addOnFailureListener { e ->
                    Log.e("RoutineViewModel", "Error al actualizar la rutina", e)
                    onResult(false) // Ocurrió un error
                }
        }
    }
    fun deleteRoutineType(documentId: String, type: String, onResult: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("routines").document(documentId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val sections = document.get("sections") as? List<Map<String, Any>> ?: return@addOnSuccessListener

                    // Remove the section based on the type
                    val updatedSections = sections.filterNot { it["type"] == type }

                    // Update the document in Firebase with the modified sections
                    db.collection("routines").document(documentId)
                        .update("sections", updatedSections)
                        .addOnSuccessListener {
                            Log.d("RoutineViewModel", "Tipo de rutina eliminado correctamente")
                            onResult(true) // Success
                        }
                        .addOnFailureListener { e ->
                            Log.e("RoutineViewModel", "Error al eliminar el tipo de rutina", e)
                            onResult(false) // Error
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("RoutineViewModel", "Error al obtener rutina", e)
                onResult(false)
            }
    }


    fun cleanRoutine() {
        routineData = routineData?.let { data ->
            // Para cada tipo de rutina, se limpia el campo 'reps' de cada ejercicio
            val cleanedRoutine = data.routine.mapValues { (_, exercises) ->
                exercises.map { exercise ->
                    exercise.copy(reps = "")
                }
            }
            data.copy(routine = cleanedRoutine)
        }
    }
}