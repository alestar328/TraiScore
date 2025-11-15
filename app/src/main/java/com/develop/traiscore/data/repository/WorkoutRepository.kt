package com.develop.traiscore.data.repository

import com.develop.traiscore.data.local.dao.WorkoutDao
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

    /** ✅ GUARDAR SOLO LOCAL - Sin sincronización inmediata */
    suspend fun addWorkout(workout: WorkoutEntry) {
        val workoutWithPending = workout.copy(
            isSynced = false,
            pendingAction = "CREATE"
        )
        // ✅ Solo guardar en Room - Firebase se sincroniza después
        workoutDao.insertWorkout(workoutWithPending)
        println("📝 Workout guardado localmente (ID: ${workoutWithPending.id})")
    }

    /** ✅ ACTUALIZAR SOLO LOCAL */
    suspend fun updateWorkout(workout: WorkoutEntry) {
        workoutDao.updateWorkout(
            workout.copy(
                isSynced = false,
                pendingAction = "UPDATE"
            )
        )
        println("✏️ Workout actualizado localmente (ID: ${workout.id})")
    }

    /** ✅ IMPORTAR desde Firebase (solo al inicio) */
    suspend fun importWorkoutsFromFirebaseToRoom() {
        val userId = auth.currentUser?.uid ?: return

        // Verificar si ya hay datos locales
        val localCount = workoutDao.getAllWorkoutsWithExercise().size
        if (localCount > 0) {
            println("📦 Ya hay $localCount workouts locales, saltando importación")
            return
        }

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

    /** ✅ ELIMINAR SOLO LOCAL */
    suspend fun removeWorkout(workout: WorkoutEntry) {
        workoutDao.deleteWorkout(workout)
        println("🗑️ Workout eliminado localmente (ID: ${workout.id})")
    }

    /** 🔄 SINCRONIZACIÓN MANUAL - Se llamará 1 vez al día */
    suspend fun syncPendingWorkouts() {
        val userId = auth.currentUser?.uid ?: return
        val unsynced = workoutDao.getAllWorkoutsWithExercise()
            .filter { !it.workout.isSynced }

        println("🔄 Sincronizando ${unsynced.size} workouts pendientes...")

        unsynced.forEach { workoutWithExercise ->
            val workout = workoutWithExercise.workout
            try {
                when (workout.pendingAction) {
                    "CREATE", "UPDATE" -> {
                        val data = hashMapOf(
                            "title" to workout.title,
                            "weight" to workout.weight,
                            "series" to workout.series,
                            "reps" to workout.reps,
                            "rir" to workout.rir,
                            "timestamp" to workout.timestamp,
                            "sessionId" to workout.sessionId,
                            "sessionName" to workout.sessionName,
                            "sessionColor" to workout.sessionColor
                        )

                        // ✅ Usar UID de Firebase si existe, sino crear nuevo
                        val docId = workout.uid ?: "workout_${System.currentTimeMillis()}_${workout.id}"

                        firestore.collection("users")
                            .document(userId)
                            .collection("workoutEntries")
                            .document(docId)
                            .set(data)
                            .await()

                        // Actualizar con el UID de Firebase
                        workoutDao.updateWorkout(
                            workout.copy(
                                uid = docId,
                                isSynced = true,
                                pendingAction = null
                            )
                        )
                        println("✅ Workout ${workout.id} sincronizado")
                    }

                    "DELETE" -> {
                        workout.uid?.let { firebaseId ->
                            firestore.collection("users")
                                .document(userId)
                                .collection("workoutEntries")
                                .document(firebaseId)
                                .delete()
                                .await()
                        }
                        workoutDao.deleteWorkout(workout)
                        println("🗑️ Workout ${workout.id} eliminado de Firebase")
                    }
                }
            } catch (e: Exception) {
                println("❌ Error sincronizando workout ${workout.id}: ${e.message}")
            }
        }

        println("✅ Sincronización completada")
    }
}