package com.develop.traiscore.data.firebaseData

import com.google.firebase.Timestamp



data class RoutineDocument(
    val userId: String = "",          // dueño/creador (trainer o self)
    val trainerId: String? = null,
    val documentId: String = "",
    val type: String,
    val createdAt: Timestamp? = null,
    val clientId: String? = null,     // 👈 UID del cliente
    val clientEmail: String? = null,  // 👈 email del cliente (opcional)
    val clientName: String = "",      // 👈 display name (“Brazos de hierro” o nombre real)
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

