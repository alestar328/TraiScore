package com.develop.traiscore.presentation.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.develop.traiscore.data.local.entity.WorkoutEntry
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.util.Date
import androidx.compose.runtime.State
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class WorkoutEntryViewModel @Inject constructor() : ViewModel() {

    private val _entries = mutableStateOf<List<WorkoutEntry>>(emptyList())
    val entries: State<List<WorkoutEntry>> = _entries

    init {
        listenToWorkoutEntries()
    }

    private fun listenToWorkoutEntries() {
        Firebase.firestore.collection("workoutEntries")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("❌ Error escuchando workoutEntries: ${error.message}")
                    return@addSnapshotListener
                }

                val result = snapshot?.documents?.mapNotNull { doc ->
                    val title = doc.getString("title") ?: return@mapNotNull null
                    val reps = doc.getLong("reps")?.toInt() ?: 0
                    val weight = doc.getDouble("weight") ?: 0.0
                    val rir = doc.getLong("rir")?.toInt() ?: 0
                    val timestamp = doc.getDate("timestamp") ?: Date()

                    WorkoutEntry(
                        firebaseId = doc.id, // Guardamos el ID real
                        id = doc.id.hashCode(),
                        title = title,
                        reps = reps,
                        weight = weight,
                        rir = rir,
                        exerciseId = 0,
                        series = 0,
                        timestamp = timestamp
                    )
                } ?: emptyList()

                _entries.value = result
            }
    }

    fun deleteWorkoutEntry(firebaseId: String) {
        Firebase.firestore.collection("workoutEntries").document(firebaseId)
            .delete()
            .addOnSuccessListener {
                println("✅ Documento eliminado correctamente.")
            }
            .addOnFailureListener { e ->
                println("❌ Error al eliminar el documento: ${e.message}")
            }
    }

    fun editWorkoutEntry(firebaseId: String, newData: Map<String, Any>) {
        Firebase.firestore.collection("workoutEntries").document(firebaseId)
            .update(newData)
            .addOnSuccessListener {
                println("✅ Documento actualizado correctamente.")
            }
            .addOnFailureListener { e ->
                println("❌ Error al actualizar el documento: ${e.message}")
            }
    }
    fun groupWorkoutsByDate(workouts: List<WorkoutEntry>): Map<String, List<WorkoutEntry>> {
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return workouts.groupBy { formatter.format(it.timestamp) }
    }
}