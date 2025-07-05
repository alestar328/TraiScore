package com.develop.traiscore.data.firebaseData

import androidx.navigation.NavController
import com.develop.traiscore.data.local.dao.ExerciseDao
import com.develop.traiscore.data.local.entity.ExerciseEntity
import com.develop.traiscore.presentation.viewmodels.StatScreenViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

fun updateRoutineInFirebase(documentId: String, routineDoc: RoutineDocument): Task<Void> {
    val db = Firebase.firestore
    val routineMap = mapOf(
        "clientName" to routineDoc.clientName,
        "routineName" to routineDoc.routineName,
        "createdAt" to FieldValue.serverTimestamp(),
        "trainerId" to routineDoc.trainerId,
        "sections" to routineDoc.sections.map { (type, exercises) ->
            mapOf(
                "type" to type,
                "exercises" to exercises.map { exercise ->
                    mapOf(
                        "name" to exercise.name,
                        "series" to exercise.series,  // ✅ Debe guardarse
                        "reps" to exercise.reps,      // ✅ Ya se guarda
                        "weight" to exercise.weight,  // ✅ Debe guardarse
                        "rir" to exercise.rir         // ✅ Debe guardarse
                    )
                }
            )
        }
    )
    return db.collection("users")
        .document(routineDoc.userId)
        .collection("routines")
        .document(documentId)
        .set(routineMap)
}
fun calculateTodayDataAndNavigate(
    context: android.content.Context,
    navController: NavController,
    viewModel: StatScreenViewModel,
    oneRepMax: Float,
    maxReps: Int
) {
    viewModel.calculateSocialShareData { socialData ->
        if (socialData != null) {
            // Usar la nueva ruta integrada
            navController.navigate(
                "social_media_camera?" +
                        "exercise=${socialData.topExercise}&" +
                        "oneRepMax=${socialData.topWeight}&" +
                        "exerciseMaxReps=${socialData.maxRepsExercise}&" +
                        "maxReps=${socialData.maxReps}&" +
                        "totalWeight=${socialData.totalWeight}&" +
                        "trainingDays=${socialData.trainingDays}"
            )
        }
    }
}
suspend fun saveExerciseToFirebase(
    name: String,
    category: String,
    exerciseDao: ExerciseDao
) {
    // Primero guardar en local para generar ID
    val localExercise = ExerciseEntity(
        id = 0,
        idIntern = "",
        name = name,
        category = category,
        isDefault = false
    )

    val localId = exerciseDao.insertExercise(localExercise)

    // Luego sincronizar con Firebase (tu código existente)
    val db = Firebase.firestore
    val exercisesCollection = db.collection("exercises")

    exercisesCollection.get()
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
                "createdBy" to FirebaseAuth.getInstance().currentUser?.uid,
                "localId" to localId
            )

            exercisesCollection
                .document(newDocId)
                .set(newExercise)
                .addOnSuccessListener {
                    println("✅ Ejercicio guardado con ID: $newDocId")

                    // Actualizar el registro local con el Firebase ID
                    CoroutineScope(Dispatchers.IO).launch {
                        val updatedExercise = localExercise.copy(
                            id = localId.toInt(),
                            idIntern = newDocId
                        )
                        exerciseDao.updateExercise(updatedExercise)
                    }
                }
                .addOnFailureListener { e ->
                    println("❌ Error al guardar el ejercicio: ${e.message}")
                }
        }
        .addOnFailureListener { e ->
            println("❌ Error al obtener ejercicios existentes: ${e.message}")
        }
}