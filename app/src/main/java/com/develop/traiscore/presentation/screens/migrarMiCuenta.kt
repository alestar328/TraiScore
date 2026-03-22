package com.develop.traiscore.presentation.screens


import android.util.Log
import com.develop.traiscore.data.local.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

suspend fun migrarDesdeRoomAFirestore(
    database: AppDatabase
) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    val currentUser = auth.currentUser
    if (currentUser == null) {
        Log.e("Migration", "❌ No hay usuario autenticado")
        return
    }

    val targetUid = currentUser.uid
    val email = currentUser.email ?: ""

    // ───────────────────────────────────────────────
    // 1️⃣ DETECTAR UID ORIGEN EN ROOM
    // ───────────────────────────────────────────────
    val roomUserIds = database.sessionDao().getDistinctUserIds()

    val sourceUid = roomUserIds.firstOrNull { it != targetUid } ?: targetUid

    try {
        /* ───────────────────────────────────────────────
         * 2️⃣ RUTINAS
         * ─────────────────────────────────────────────── */
        val routines = database.routineDao().getRoutines(sourceUid)

        for (routine in routines) {
            val map = mapOf(
                "routineIdFirebase" to routine.routineIdFirebase,
                "userId" to targetUid,
                "trainerId" to routine.trainerId,
                "type" to routine.type,
                "createdAt" to routine.createdAt,
                "clientName" to routine.clientName,
                "routineName" to routine.routineName,
                "isSynced" to true
            )

            val docId = routine.routineIdFirebase ?: routine.id.toString()

            firestore.collection("users")
                .document(targetUid)
                .collection("routines")
                .document(docId)
                .set(map)
                .await()
        }

        /* ───────────────────────────────────────────────
         * 3️⃣ EJERCICIOS PERSONALIZADOS
         * ─────────────────────────────────────────────── */
        val exercises = database.exerciseDao()
            .getAllExercises()
            .filter { !it.isDefault }

        for (e in exercises) {
            val map = mapOf(
                "idIntern" to e.idIntern,
                "name" to e.name,
                "category" to e.category,
                "createdBy" to targetUid,
                "createdAt" to e.createdAt,
                "updatedAt" to e.updatedAt,
                "isDefault" to false
            )

            val docId = e.idIntern.ifBlank { e.id.toString() }

            firestore.collection("users")
                .document(targetUid)
                .collection("exercises")
                .document(docId)
                .set(map)
                .await()
        }

        /* ───────────────────────────────────────────────
         * 4️⃣ SESIONES
         * ─────────────────────────────────────────────── */
        val sessions = database.sessionDao().getAvailableSessionsList(sourceUid)

        for (s in sessions) {
            val map = mapOf(
                "sessionId" to s.sessionId,
                "name" to s.name,
                "color" to s.color,
                "userId" to targetUid,
                "createdAt" to s.createdAt,
                "endedAt" to s.endedAt,
                "isActive" to s.isActive,
                "isFinished" to s.isFinished,
                "lastModified" to s.lastModified
            )

            firestore.collection("users")
                .document(targetUid)
                .collection("sessions")
                .document(s.sessionId)
                .set(map)
                .await()
        }

        /* ───────────────────────────────────────────────
         * 5️⃣ BODY STATS
         * ─────────────────────────────────────────────── */
        val stats = database.bodyStatsDao().getBodyStatsForUser(sourceUid)

        for (b in stats) {
            val map = mapOf(
                "userId" to targetUid,
                "gender" to b.gender,
                "height" to b.height,
                "weight" to b.weight,
                "neck" to b.neck,
                "chest" to b.chest,
                "arms" to b.arms,
                "waist" to b.waist,
                "thigh" to b.thigh,
                "calf" to b.calf,
                "createdAt" to b.createdAt,
                "updatedAt" to b.updatedAt
            )

            val docId = b.firebaseId.ifBlank { b.id.toString() }

            firestore.collection("users")
                .document(targetUid)
                .collection("bodyStats")
                .document(docId)
                .set(map)
                .await()
        }

        /* ───────────────────────────────────────────────
         * 6️⃣ WORKOUT ENTRIES
         * ─────────────────────────────────────────────── */
        val workouts = database.workoutDao().getAllWorkoutsWithExercise()

        for (entry in workouts) {
            val w = entry.workout

            val map = mapOf(
                "uid" to targetUid,
                "exerciseId" to w.exerciseId,
                "title" to w.title,
                "series" to w.series,
                "reps" to w.reps,
                "weight" to w.weight,
                "rir" to w.rir,
                "type" to w.type,
                "timestamp" to w.timestamp,
                "sessionId" to w.sessionId,
                "sessionName" to w.sessionName,
                "sessionColor" to w.sessionColor
            )

            firestore.collection("users")
                .document(targetUid)
                .collection("workoutEntries")
                .document(w.id.toString())
                .set(map)
                .await()
        }

    } catch (e: Exception) {
        Log.e("Migration", "❌ Error durante la migración", e)
    }
}