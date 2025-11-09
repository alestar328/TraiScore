package com.develop.traiscore.presentation.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.develop.traiscore.data.local.entity.WorkoutEntry
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.util.Date
import androidx.compose.runtime.State
import androidx.lifecycle.viewModelScope
import com.develop.traiscore.data.local.dao.WorkoutRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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

@HiltViewModel
class WorkoutEntryViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _entries = mutableStateOf<List<WorkoutEntry>>(emptyList())
    val entries: State<List<WorkoutEntry>> = _entries

    private val _sessionWorkouts =
        mutableStateOf<Map<String, List<SessionWithWorkouts>>>(emptyMap())
    val sessionWorkouts: State<Map<String, List<SessionWithWorkouts>>> = _sessionWorkouts

    init {
        viewModelScope.launch {
            initializeData()
        }
    }

    /** 🔁 Carga inicial: importa si Room está vacío y observa cambios locales */
    private suspend fun initializeData() {
        workoutRepository.importWorkoutsFromFirebaseToRoom()

        viewModelScope.launch {
            workoutRepository.workouts.collectLatest { localWorkouts ->
                _entries.value = localWorkouts
                updateSessionGrouping(localWorkouts)
                println("📦 Cargados ${localWorkouts.size} workouts desde Room")
            }
        }
    }

    /** 🧠 Agrupa workouts por fecha y sesión para la UI */
    private fun updateSessionGrouping(workouts: List<WorkoutEntry>) {
        val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        val sessionGrouped = workouts
            .groupBy { dateFormatter.format(it.timestamp) }
            .mapValues { (_, workoutsForDate) ->
                workoutsForDate
                    .groupBy { it.sessionId ?: "legacy_${dateFormatter.format(it.timestamp)}" }
                    .map { (sessionId, sessionWorkouts) ->
                        val first = sessionWorkouts.first()
                        SessionWithWorkouts(
                            sessionId = sessionId,
                            sessionName = first.sessionName ?: "Entrenamiento",
                            sessionColor = first.sessionColor ?: "#43f4ff",
                            workouts = sessionWorkouts,
                            date = dateFormatter.format(first.timestamp)
                        )
                    }
                    .sortedByDescending { it.workouts.first().timestamp }
            }

        _sessionWorkouts.value = sessionGrouped
    }

    /** ➕ Añadir un nuevo workout */
    fun addWorkout(workout: WorkoutEntry) {
        viewModelScope.launch {
            workoutRepository.addWorkout(workout)
            println("✅ Workout añadido: ${workout.title}")
        }
    }

    /** ✏️ Editar workout existente */
    fun updateWorkout(workout: WorkoutEntry) {
        viewModelScope.launch {
            workoutRepository.updateWorkout(workout)
            println("📝 Workout actualizado: ${workout.title}")
        }
    }

    /** 🗑️ Eliminar workout */
    fun deleteWorkout(workout: WorkoutEntry) {
        viewModelScope.launch {
            workoutRepository.removeWorkout(workout)
            println("🗑️ Workout eliminado: ${workout.title}")
        }
    }

    /** 🔍 Agrupación filtrada por búsqueda */
    fun groupWorkoutsByDateFiltered(
        query: String
    ): Map<String, List<WorkoutEntry>> {
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return _entries.value
            .filter { it.title.contains(query, ignoreCase = true) }
            .groupBy { formatter.format(it.timestamp) }
    }

    /** Agrupación estándar */
    fun groupWorkoutsByDate(workouts: List<WorkoutEntry>): Map<String, List<WorkoutEntry>> {
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return workouts.groupBy { formatter.format(it.timestamp) }
    }

}