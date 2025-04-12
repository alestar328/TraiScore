package com.develop.traiscore.data.firebaseData

import com.develop.traiscore.presentation.screens.RoutineData
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore

fun updateRoutineInFirebase(documentId: String, routineData: RoutineData): Task<Void> {
    val db = Firebase.firestore
    val updateMap = mapOf(
        "clientName" to routineData.clientName,
        "createdAt" to FieldValue.serverTimestamp(),
        "routine" to routineData.routine.mapValues { (_, exercises) ->
            exercises.map { exercise ->
                mapOf(
                    "name" to exercise.name,
                    "series" to exercise.series.toString(),
                    "reps" to exercise.reps,
                    "weight" to exercise.weight,
                    "rir" to exercise.rir
                )
            }
        }
    )
    return db.collection("routines")
        .document(documentId)
        .set(updateMap, SetOptions.merge())
}
