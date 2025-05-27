package com.develop.traiscore.exports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.develop.traiscore.data.firebaseData.RoutineDocument
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportRoutineViewModel @Inject constructor() : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun importAndSaveRoutine(
        exportableRoutine: RoutineExportManager.ExportableRoutine,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val currentUserId = auth.currentUser?.uid
                if (currentUserId == null) {
                    onError("Usuario no autenticado")
                    return@launch
                }

                // Convertir a RoutineDocument
                val routineDocument = RoutineExportManager.convertToRoutineDocument(
                    exportableRoutine = exportableRoutine,
                    currentUserId = currentUserId
                )

                // Guardar en Firestore
                saveRoutineToFirestore(routineDocument, onSuccess, onError)

            } catch (e: Exception) {
                onError("Error al importar la rutina: ${e.message}")
            }
        }
    }

    private fun saveRoutineToFirestore(
        routine: RoutineDocument,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val routineData = hashMapOf(
            "clientName" to routine.clientName,
            "routineName" to routine.routineName,
            "createdAt" to routine.createdAt,
            "trainerId" to routine.trainerId,
            "sections" to routine.sections.map { section ->
                hashMapOf(
                    "type" to section.type,
                    "exercises" to section.exercises.map { exercise ->
                        hashMapOf(
                            "name" to exercise.name,
                            "series" to exercise.series,
                            "reps" to exercise.reps,
                            "weight" to exercise.weight,
                            "rir" to exercise.rir
                        )
                    }
                )
            }
        )

        firestore
            .collection("users")
            .document(routine.userId)
            .collection("routines")
            .add(routineData)
            .addOnSuccessListener { documentReference ->
                onSuccess(documentReference.id)
            }
            .addOnFailureListener { exception ->
                onError("Error al guardar en Firebase: ${exception.message}")
            }
    }
}