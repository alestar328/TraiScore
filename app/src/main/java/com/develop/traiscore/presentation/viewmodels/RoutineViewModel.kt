package com.develop.traiscore.presentation.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.develop.traiscore.data.firebaseData.RoutineDocument
import com.develop.traiscore.data.firebaseData.SimpleExercise
import com.develop.traiscore.data.firebaseData.updateRoutineInFirebase
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class RoutineViewModel : ViewModel() {
    // La rutina se almacena y actualiza aquí:
    var routineDocument by mutableStateOf<RoutineDocument?>(null)
    var hasShownEmptyDialog by mutableStateOf(false)
        private set

    fun markEmptyDialogShown() {
        hasShownEmptyDialog = true
    }
    fun loadRoutine(documentId: String, userId: String) {
        Firebase.firestore
            .collection("users").document(userId)
            .collection("routines").document(documentId)
            .get()
            .addOnSuccessListener { snap ->
                if (!snap.exists()) return@addOnSuccessListener

                // 1) Campos básicos
                val clientName  = snap.getString("clientName") ?: ""
                val routineName = snap.getString("routineName") ?: ""
                val createdAt   = snap.getTimestamp("createdAt")    // Timestamp?
                val trainerId   = snap.getString("trainerId")  // puede ser null

                // 2) Secciones (type + ejercicios)
                val sectionsRaw = snap.get("sections") as? List<Map<String,Any>> ?: emptyList()
                // Construimos el Map<String, List<SimpleExercise>>
                val routineExer = sectionsRaw.associate { section ->
                    val type = section["type"] as? String ?: return@associate "" to emptyList<SimpleExercise>()
                    val exercisesRaw = section["exercises"] as? List<Map<String,Any>> ?: emptyList()
                    val listExe = exercisesRaw.mapNotNull { ex ->
                        SimpleExercise(
                            name   = ex["name"]   as? String ?: return@mapNotNull null,
                            series = (ex["series"] as? Number)?.toInt() ?: 0,
                            reps   = ex["reps"]   as? String ?: "",
                            weight = ex["weight"] as? String ?: "",
                            rir    = (ex["rir"]   as? Number)?.toInt() ?: 0
                        )
                    }
                    type to listExe
                }

                // 3) Asignamos a nuestro estado
                routineDocument = RoutineDocument(
                    userId      = userId,
                    trainerId   = trainerId,
                    type        = "",             // lo filtrará Create/RoutineScreen
                    documentId  = documentId,
                    createdAt   = createdAt,
                    clientName  = clientName,
                    routineName = routineName,
                    routineExer = routineExer
                )
            }
            .addOnFailureListener { e ->
                Log.e("RoutineVM", "no pude cargar routine", e)
            }
    }


    fun updateReps(exerciseIndex: Int, trainingType: String, newReps: String) {
        routineDocument = routineDocument?.let { data ->
            // Actualiza la lista de ejercicios para el trainingType
            val updatedList = data.routineExer[trainingType]?.mapIndexed { index, exercise ->
                if (index == exerciseIndex) exercise.copy(reps = newReps) else exercise
            } ?: emptyList()
            val updatedRoutine = data.routineExer.toMutableMap()
            updatedRoutine[trainingType] = updatedList
            data.copy(routineExer = updatedRoutine)
        }
    }

    fun saveRoutine(documentId: String, onResult: (Boolean) -> Unit) {
        routineDocument?.let { data ->
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
    fun deleteRoutineType(
        documentId: String,
        onResult: (Boolean) -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return onResult(false)

        val docRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("routines")
            .document(documentId)

        docRef.delete()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }


    fun cleanRoutine() {
        routineDocument = routineDocument?.let { data ->
            // Para cada tipo de rutina, se limpia el campo 'reps' de cada ejercicio
            val cleanedRoutine = data.routineExer.mapValues { (_, exercises) ->
                exercises.map { exercise ->
                    exercise.copy(reps = "")
                }
            }
            data.copy(routineExer = cleanedRoutine)
        }
    }
}