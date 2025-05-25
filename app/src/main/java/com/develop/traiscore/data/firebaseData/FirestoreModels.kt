package com.develop.traiscore.data.firebaseData

import com.google.firebase.Timestamp
import java.util.Date


data class FirestoreExercise(
    val name: String = "",
    val series: String = "",
    val reps: String = "",
    val weight: String = "",
    val rir: Int = 0
)


data class FirestoreRoutineDoc(
    val clientName: String = "",
    val routine: Map<String, List<FirestoreExercise>> = emptyMap(),
    val createdAt: com.google.firebase.Timestamp? = null

)

data class ExerciseEditable(
    val name: String,
    val series: Int,
    var reps: String, // mutable
    val weight: String,
    val rir: Int
)

data class RoutineDocument(
    val userId: String,
    val trainerId: String? = null,
    val type: String,
    val documentId: String,
    val createdAt: Timestamp?,
    val clientName: String,
    val routineName: String,
    val routineExer: Map<String, List<SimpleExercise>>
)


data class SimpleExercise(
    val name: String,
    val series: Int,
    val reps: String,
    val weight: String,
    val rir: Int
)