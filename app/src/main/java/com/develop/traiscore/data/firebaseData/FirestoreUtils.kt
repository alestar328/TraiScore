package com.develop.traiscore.data.firebaseData

import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore

fun updateRoutineInFirebase(documentId: String, routineDoc: RoutineDocument): Task<Void> {
    val db = Firebase.firestore
    val routineMap = mapOf(
        "clientName" to routineDoc.clientName,
        "routineName" to routineDoc.routineName,
        "createdAt" to FieldValue.serverTimestamp(),
        "trainerId" to routineDoc.trainerId,
        "sections" to routineDoc.routineExer.map { (type, exercises) ->
            mapOf(
                "type" to type,
                "exercises" to exercises.map { exercise ->
                    mapOf(
                        "name" to exercise.name,
                        "series" to exercise.series,  // ✅ Debe guardarse
                        "reps" to exercise.reps,      // ✅ Ya se guarda
                        "weight" to exercise.weight,  // ✅ Debe guardarse
                        "rir" to exercise.rir         // ✅ Debe guardarse
                    )
                }
            )
        }
    )
    return db.collection("users")
        .document(routineDoc.userId)
        .collection("routines")
        .document(documentId)
        .set(routineMap)
}

fun saveExerciseToFirebase(name: String, category: String) {
    val db = Firebase.firestore
    val exercisesCollection = db.collection("exercises")

    // Paso 1: Obtener todos los documentos que empiecen con "userExer"
    exercisesCollection.get()
        .addOnSuccessListener { snapshot ->
            val userExerciseDocs = snapshot.documents.filter {
                it.id.startsWith("userExer")
            }

            // Paso 2: Obtener el siguiente número disponible
            val nextNumber = (userExerciseDocs.mapNotNull {
                it.id.removePrefix("userExer").toIntOrNull()
            }.maxOrNull() ?: 0) + 1

            val newDocId = "userExer$nextNumber"

            // Paso 3: Guardar el nuevo ejercicio
            val newExercise = hashMapOf(
                "name" to name,
                "category" to category,
                "isDefault" to false,
                "createdBy" to null
            )

            exercisesCollection
                .document(newDocId)
                .set(newExercise)
                .addOnSuccessListener {
                    println("✅ Ejercicio guardado con ID: $newDocId")
                }
                .addOnFailureListener { e ->
                    println("❌ Error al guardar el ejercicio: ${e.message}")
                }
        }
        .addOnFailureListener { e ->
            println("❌ Error al obtener ejercicios existentes: ${e.message}")
        }
}

