package com.develop.traiscore.data.firebaseData

import com.google.firebase.Timestamp



data class RoutineDocument(
    val userId: String = "",
    val trainerId: String? = null,
    val documentId: String = "",
    val type: String,
    val createdAt: Timestamp? = null,
    val clientName: String = "",
    val routineName: String,
    val sections: List<RoutineSection> = emptyList(),
)


data class SimpleExercise(
    val name: String,
    val series: Int = 0,
    val reps: String,
    val weight: String,
    val rir: Int = 0
)

data class RoutineSection(
    val type: String = "",
    val exercises: List<SimpleExercise> = emptyList()
)

