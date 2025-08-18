package com.develop.traiscore.presentation.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.develop.traiscore.data.local.entity.WorkoutEntry
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.util.Date
import androidx.compose.runtime.State
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

data class SessionWithWorkouts(
    val sessionId: String,
    val sessionName: String,
    val sessionColor: String,
    val workouts: List<WorkoutEntry>,
    val date: String
)

class WorkoutEntryViewModel @Inject constructor() : ViewModel() {

    private val _entries = mutableStateOf<List<WorkoutEntry>>(emptyList())
    val entries: State<List<WorkoutEntry>> = _entries

    private val _sessionWorkouts = mutableStateOf<Map<String, List<SessionWithWorkouts>>>(emptyMap())
    val sessionWorkouts: State<Map<String, List<SessionWithWorkouts>>> = _sessionWorkouts

    init {
        listenToWorkoutEntries()
    }
    private fun updateSessionGrouping(workouts: List<WorkoutEntry>) {
        val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        val sessionGrouped = workouts
            .groupBy { dateFormatter.format(it.timestamp) } // Agrupar por fecha
            .mapValues { (_, workoutsForDate) ->
                // Dentro de cada fecha, agrupar por sesión
                workoutsForDate
                    .groupBy { workout ->
                        // Si tiene sessionId, usar ese; sino crear uno temporal
                        workout.sessionId ?: "legacy_${dateFormatter.format(workout.timestamp)}"
                    }
                    .map { (sessionId, sessionWorkouts) ->
                        val firstWorkout = sessionWorkouts.first()
                        SessionWithWorkouts(
                            sessionId = sessionId,
                            sessionName = firstWorkout.sessionName ?: "Entrenamiento", // Default para datos legacy
                            sessionColor = firstWorkout.sessionColor ?: "#43f4ff", // Default cyan
                            workouts = sessionWorkouts,
                            date = dateFormatter.format(firstWorkout.timestamp)
                        )
                    }
                    .sortedByDescending { it.workouts.first().timestamp } // Ordenar por tiempo
            }

        _sessionWorkouts.value = sessionGrouped
    }

    // ⭐ NUEVA FUNCIÓN: Añadir workout a sesión activa
    fun addWorkoutToActiveSession(
        title: String,
        reps: Int,
        weight: Float,
        rir: Int,
        activeSessionId: String,
        activeSessionName: String,
        activeSessionColor: String
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            println("❌ Usuario no autenticado")
            return
        }

        val workoutData = mapOf(
            "title" to title,
            "reps" to reps,
            "weight" to weight,
            "rir" to rir,
            "timestamp" to Date(),
            "sessionId" to activeSessionId,        // ⭐ Asociar con sesión
            "sessionName" to activeSessionName,    // ⭐ Nombre de la sesión
            "sessionColor" to activeSessionColor   // ⭐ Color de la sesión
        )

        Firebase.firestore
            .collection("users")
            .document(userId)
            .collection("workoutEntries")
            .add(workoutData)
            .addOnSuccessListener { documentReference ->
                println("✅ Workout agregado a sesión: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                println("❌ Error al agregar workout: ${e.message}")
            }
    }


    private fun listenToWorkoutEntries() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            println("❌ Usuario no autenticado")
            return
        }

        Firebase.firestore.collection("users")
            .document(userId)
            .collection("workoutEntries")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("❌ Error escuchando workoutEntries: ${error.message}")
                    return@addSnapshotListener
                }

                val result = snapshot?.documents?.mapNotNull { doc ->
                    val title = doc.getString("title") ?: return@mapNotNull null
                    val reps = doc.getLong("reps")?.toInt() ?: 0
                    val weight = doc.getDouble("weight")?.toFloat() ?: 0.0f
                    val rir = doc.getLong("rir")?.toInt() ?: 0
                    val timestamp = doc.getDate("timestamp") ?: Date()

                    // ⭐ NUEVOS CAMPOS - Con valores por defecto para compatibilidad
                    val sessionId = doc.getString("sessionId")
                    val sessionName = doc.getString("sessionName")
                    val sessionColor = doc.getString("sessionColor")

                    WorkoutEntry(
                        uid = doc.id,
                        id = doc.id.hashCode(),
                        title = title,
                        reps = reps,
                        weight = weight,
                        rir = rir,
                        exerciseId = 0,
                        series = 0,
                        timestamp = timestamp,
                        sessionId = sessionId,
                        sessionName = sessionName,
                        sessionColor = sessionColor
                    )
                } ?: emptyList()

                _entries.value = result

                // ⭐ Actualizar agrupación por sesiones
                updateSessionGrouping(result)
            }
    }


    fun deleteWorkoutEntry(firebaseId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            println("❌ Usuario no autenticado")
            return
        }

        Firebase.firestore
            .collection("users")
            .document(userId)
            .collection("workoutEntries")
            .document(firebaseId)
            .delete()
            .addOnSuccessListener {
                println("✅ Documento eliminado correctamente.")
            }
            .addOnFailureListener { e ->
                println("❌ Error al eliminar el documento: ${e.message}")
            }
    }

    fun editWorkoutEntry(firebaseId: String, newData: Map<String, Any>) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            println("❌ Usuario no autenticado")
            return
        }
        Firebase.firestore
            .collection("users")
            .document(userId)
            .collection("workoutEntries")
            .document(firebaseId)
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

    // ⭐ NUEVA FUNCIÓN: Obtener sesiones para una fecha específica
    fun getSessionsForDate(date: String): List<SessionWithWorkouts> {
        return _sessionWorkouts.value[date] ?: emptyList()
    }

    // ⭐ NUEVA FUNCIÓN: Obtener todos los workouts de una sesión específica
    fun getWorkoutsForSession(sessionId: String): List<WorkoutEntry> {
        return _entries.value.filter { it.sessionId == sessionId }
    }

    fun groupWorkoutsByDateFiltered(entries: List<WorkoutEntry>, query: String): Map<String, List<WorkoutEntry>> {
        return entries
            .filter { it.title.contains(query, ignoreCase = true) }
            .groupBy {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it.timestamp)
            }
    }
}