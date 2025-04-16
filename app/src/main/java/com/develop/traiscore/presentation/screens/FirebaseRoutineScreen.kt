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
fun FirebaseRoutineScreen (
    documentId: String,
    onBack: () -> Unit
){
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
                    Log.d("FirebaseRoutineScreen", "El clientName obtenido es: $clientName")

                    createdAtString = snapshot.getTimestamp("createdAt")?.toDate()?.toString() ?: ""
                    val routineMap = snapshot.get("routine") as? Map<String, List<Map<String, Any>>>
                        ?: emptyMap()

                    val typedRoutine = routineMap.mapValues { (_, exerciseList) ->
                        exerciseList.map { exMap ->
                            FirestoreExercise(
                                name = exMap["name"] as? String ?: "",
                                series = exMap["series"] as? String ?: "",
                                reps = exMap["reps"] as? String ?: "",
                                weight = exMap["weight"] as? String ?: "",
                                rir = (exMap["rir"] as? Number)?.toInt() ?: 0
                            )
                        }
                    }
                    routineDoc = FirestoreRoutineDoc(
                        clientName = clientName,
                        routine = typedRoutine
                    )
                }else {
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
        // Convertir FirestoreRoutineDoc a RoutineData
        // Se convierte cada FirestoreExercise a SimpleExercise (nuestra versión para la tabla)
        val typedRoutine: Map<String, List<SimpleExercise>> = routineDoc!!.routine.mapValues { (_, exerciseList) ->
            exerciseList.map { fsExe ->
                SimpleExercise(
                    name = fsExe.name,
                    series = fsExe.series.toIntOrNull() ?: 0,
                    reps = fsExe.reps,
                    weight = fsExe.weight,
                    rir = fsExe.rir
                )
            }
        }

        val routineData = RoutineData(
            clientName = routineDoc!!.clientName,
            createdAt = createdAtString,
            routine = typedRoutine
        )
        // Llama al composable que muestra la rutina completa, con una tabla por cada tipo.
        RoutineScreen(
            routineData = routineData,
            documentId="XyV1ERd0yYturM1p9Sqp",
            onBack = onBack
        )
    }
}