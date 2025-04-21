package com.develop.traiscore.presentation.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.develop.traiscore.data.firebaseData.updateRoutineInFirebase
import com.develop.traiscore.presentation.screens.RoutineData

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
    fun deleteExercise(index: Int, type: String) {
        routineData = routineData?.copy(
            routine = routineData!!.routine.toMutableMap().apply {
                val updatedList = this[type]?.toMutableList()?.apply { removeAt(index) }
                if (updatedList != null) {
                    this[type] = updatedList
                }
            }
        )
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