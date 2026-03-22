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
import kotlinx.coroutines.tasks.await
import java.util.Date

fun updateRoutineInFirebase(documentId: String, routineDoc: RoutineDocument): Task<Void> {
    val db = Firebase.firestore
    val routineMap = mapOf(
        "clientName" to routineDoc.clientName,
        "routineName" to routineDoc.routineName,
        "type" to routineDoc.type, // 👈 IMPORTANTE: conservar el tipo raíz
        "createdAt" to FieldValue.serverTimestamp(),
        "trainerId" to routineDoc.trainerId,
        "sections" to routineDoc.sections.map { section ->
            mapOf(
                "type" to section.type,
                "exercises" to section.exercises.map { e ->
                    mapOf(
                        "name" to e.name,
                        "series" to e.series,
                        "reps" to e.reps,
                        "weight" to e.weight,
                        "rir" to e.rir
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
    exerciseDao: ExerciseDao,
    onComplete: (Boolean) -> Unit = {}
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser == null) {
        onComplete(false)
        return
    }

    val userId = currentUser.uid
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

    try {
        val snapshot = userExercisesCollection.get().await()
        val userExerciseDocs = snapshot.documents.filter { it.id.startsWith("userExer") }
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

        userExercisesCollection.document(newDocId).set(newExercise).await()

        exerciseDao.updateExercise(
            localExercise.copy(id = localId.toInt(), idIntern = newDocId)
        )
        onComplete(true)
    } catch (e: Exception) {
        onComplete(false)
    }
}