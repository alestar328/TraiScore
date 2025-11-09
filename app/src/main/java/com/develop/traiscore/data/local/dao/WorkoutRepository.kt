package com.develop.traiscore.data.local.dao

import com.develop.traiscore.data.local.entity.WorkoutEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepository @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    val workouts: Flow<List<WorkoutEntry>> = workoutDao.getAllWorkoutsFlow()

    suspend fun addWorkout(workout: WorkoutEntry) {
        val workoutWithPending = workout.copy(
            isSynced = false,
            pendingAction = "CREATE"
        )
        workoutDao.insertWorkout(workoutWithPending)
    }

    suspend fun updateWorkout(workout: WorkoutEntry) {
        workoutDao.updateWorkout(
            workout.copy(
                isSynced = false,
                pendingAction = "UPDATE"
            )
        )
    }
    suspend fun importWorkoutsFromFirebaseToRoom() {
        val userId = auth.currentUser?.uid ?: return
        val snapshot = firestore.collection("users")
            .document(userId)
            .collection("workoutEntries")
            .get()
            .await()

        val workouts = snapshot.documents.mapNotNull { doc ->
            val title = doc.getString("title") ?: return@mapNotNull null
            val reps = doc.getLong("reps")?.toInt() ?: 0
            val weight = doc.getDouble("weight")?.toFloat() ?: 0f
            val rir = doc.getLong("rir")?.toInt() ?: 0
            val timestamp = doc.getDate("timestamp") ?: Date()
            val sessionId = doc.getString("sessionId")
            val sessionName = doc.getString("sessionName")
            val sessionColor = doc.getString("sessionColor")

            WorkoutEntry(
                uid = doc.id,
                exerciseId = 0,
                title = title,
                reps = reps,
                weight = weight,
                rir = rir,
                series = 0,
                timestamp = timestamp,
                sessionId = sessionId,
                sessionName = sessionName,
                sessionColor = sessionColor,
                isSynced = true
            )
        }

        workoutDao.insertWorkouts(workouts)
        println("✅ Importadas ${workouts.size} entradas desde Firebase a Room")
    }
    suspend fun removeWorkout(workout: WorkoutEntry) {
        workoutDao.updateWorkout(
            workout.copy(
                pendingAction = "DELETE",
                isSynced = false
            )
        )
    }

    suspend fun syncPendingWorkouts() {
        val userId = auth.currentUser?.uid ?: return
        val unsynced = workoutDao.getAllWorkoutsWithExercise() // o getUnsyncedWorkouts()

        unsynced.forEach { workout ->
            try {
                when (workout.workout.pendingAction) {
                    "CREATE", "UPDATE" -> {
                        val data = hashMapOf(
                            "id" to workout.workout.id,
                            "exerciseId" to workout.workout.exerciseId,
                            "title" to workout.workout.title,
                            "weight" to workout.workout.weight,
                            "series" to workout.workout.series,
                            "reps" to workout.workout.reps,
                            "rir" to workout.workout.rir,
                            "timestamp" to workout.workout.timestamp
                        )
                        firestore.collection("users")
                            .document(userId)
                            .collection("workouts")
                            .document(workout.workout.id.toString())
                            .set(data)
                            .await()
                    }
                    "DELETE" -> {
                        firestore.collection("users")
                            .document(userId)
                            .collection("workouts")
                            .document(workout.workout.id.toString())
                            .delete()
                            .await()
                        workoutDao.deleteWorkout(workout.workout)
                    }
                }
                workoutDao.updateWorkout(
                    workout.workout.copy(isSynced = true, pendingAction = null)
                )
            } catch (e: Exception) {
                println("Error sincronizando workout ${workout.workout.id}: ${e.message}")
            }
        }
    }
}