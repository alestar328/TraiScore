package com.develop.traiscore.data.firebaseData

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