package com.develop.traiscore.presentation.screens

import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.develop.traiscore.data.firebaseData.FirestoreExercise
import com.develop.traiscore.data.firebaseData.FirestoreRoutineDoc
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

@Composable
fun FirebaseRoutineScreen(
    documentId: String,
    selectedType: String, // <- añadimos este nuevo parámetro
    onBack: () -> Unit
) {
    var routineDoc by remember { mutableStateOf<FirestoreRoutineDoc?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var createdAtString by remember { mutableStateOf("") }

    LaunchedEffect(documentId) {
        val db = Firebase.firestore
        db.collection("routines")
            .document(documentId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val clientName = snapshot.getString("clientName") ?: ""
                    createdAtString = snapshot.getTimestamp("createdAt")?.toDate()?.toString() ?: ""
                    val sections = snapshot.get("sections") as? List<Map<String, Any>> ?: emptyList()

                    val grouped = sections.associate { section ->
                        val type = section["type"] as? String ?: "Otro"
                        val exercisesRaw = section["exercises"] as? List<Map<String, Any>> ?: emptyList()

                        type to exercisesRaw.map { ex ->
                            FirestoreExercise( // ✅ Usamos la clase correcta
                                name = ex["name"] as? String ?: "",
                                series = ex["series"] as? String ?: "",
                                reps = ex["reps"] as? String ?: "",
                                weight = ex["weight"] as? String ?: "",
                                rir = (ex["rir"] as? Number)?.toInt() ?: 0
                            )
                        }
                    }

                    routineDoc = FirestoreRoutineDoc(
                        clientName = clientName,
                        routine = grouped
                    )
                } else {
                    Log.d("FirebaseRoutineScreen", "El documento $documentId no existe.")
                }
                isLoading = false
            }
            .addOnFailureListener { error ->
                Log.e("FirebaseRoutineScreen", "Error al obtener el documento", error)
                isLoading = false
            }
    }

    if (isLoading) {
        Text("Cargando datos de Firebase...")
    } else if (routineDoc == null) {
        Text("No se encontró la rutina con ID: $documentId")
    } else {
        val routineData = RoutineData(
            clientName = routineDoc!!.clientName,
            createdAt = createdAtString,
            routine = routineDoc!!.routine.mapValues { entry ->
                entry.value.map { fsExercise ->
                    SimpleExercise(
                        name = fsExercise.name,
                        series = fsExercise.series.toIntOrNull() ?: 0,
                        reps = fsExercise.reps,
                        weight = fsExercise.weight,
                        rir = fsExercise.rir
                    )
                }
            }
        )

        RoutineScreen(
            routineData = routineData,
            documentId = documentId,
            selectedType = selectedType, // <- aquí enviamos el tipo seleccionado
            onBack = onBack
        )
    }
}