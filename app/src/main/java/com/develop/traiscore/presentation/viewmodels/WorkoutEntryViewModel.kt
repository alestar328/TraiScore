package com.develop.traiscore.presentation.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.develop.traiscore.data.local.entity.WorkoutEntry
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.util.Date
import androidx.compose.runtime.State
import androidx.lifecycle.viewModelScope
import com.develop.traiscore.data.repository.SessionRepository
import com.develop.traiscore.data.repository.WorkoutRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
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
    private val workoutRepository: WorkoutRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _entries = mutableStateOf<List<WorkoutEntry>>(emptyList())
    val entries: StateFlow<List<WorkoutEntry>> = workoutRepository.workouts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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
    fun addWorkout(workout: WorkoutEntry) {
        viewModelScope.launch {
            try {
                // Obtener sesión activa si existe
                val activeSession = sessionRepository.getActiveSession()

                val workoutToSave = if (activeSession.success && activeSession.session != null) {
                    val session = activeSession.session
                    workout.copy(
                        sessionId = session.sessionId,
                        sessionName = session.name,
                        sessionColor = session.color,
                        isSynced = false,
                        pendingAction = "CREATE"
                    )
                } else {
                    workout.copy(
                        isSynced = false,
                        pendingAction = "CREATE"
                    )
                }

                workoutRepository.addWorkout(workoutToSave)

                // Incrementar contador de workouts en la sesión si existe
                if (activeSession.success && activeSession.session != null) {
                    sessionRepository.incrementWorkoutCount(activeSession.session.sessionId)
                }

                println("✅ Workout guardado localmente: ${workoutToSave.title}")
            } catch (e: Exception) {
                println("❌ Error guardando workout: ${e.message}")
                e.printStackTrace()
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



    /** ✏️ Editar workout existente */
    fun updateWorkout(workout: WorkoutEntry) {
        viewModelScope.launch {
            try {
                workoutRepository.updateWorkout(workout)
                println("✅ Workout actualizado localmente: ${workout.title}")
            } catch (e: Exception) {
                println("❌ Error actualizando workout: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /** 🗑️ Eliminar workout */
    fun deleteWorkout(workout: WorkoutEntry) {
        viewModelScope.launch {
            try {
                workoutRepository.removeWorkout(workout)
                println("✅ Workout marcado para eliminación: ${workout.title}")
            } catch (e: Exception) {
                println("❌ Error eliminando workout: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    /** 🔍 Agrupación filtrada por búsqueda */
    fun groupWorkoutsByDateFiltered(exerciseName: String): Map<String, List<WorkoutEntry>> {
        val filtered = entries.value.filter {
            it.title.contains(exerciseName, ignoreCase = true)
        }
        return groupWorkoutsByDate(filtered)
    }

    /** Agrupación estándar */
    fun groupWorkoutsByDate(workouts: List<WorkoutEntry>): Map<String, List<WorkoutEntry>> {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return workouts
            .sortedByDescending { it.timestamp }
            .groupBy { sdf.format(it.timestamp) }
    }
}