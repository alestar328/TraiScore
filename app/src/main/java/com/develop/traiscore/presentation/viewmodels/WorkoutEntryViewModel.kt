package com.develop.traiscore.presentation.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.develop.traiscore.data.local.entity.WorkoutEntry
import androidx.compose.runtime.State
import androidx.lifecycle.viewModelScope
import com.develop.traiscore.data.repository.SessionRepository
import com.develop.traiscore.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
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

    // ✅ FORMATO ÓPTIMO: numérico, independiente del idioma
    companion object {
        val DATE_FORMAT = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    }

    init {
        viewModelScope.launch {
            initializeData()
        }
    }

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
    val groupedByDate: StateFlow<Map<String, List<WorkoutEntry>>> =
        entries.map { list -> groupWorkoutsByDate(list) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun addWorkout(workout: WorkoutEntry) {
        viewModelScope.launch {
            try {
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
                println("✅ Workout guardado localmente: ${workoutToSave.title}")

            } catch (e: Exception) {
                println("❌ Error guardando workout: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun updateSessionGrouping(workouts: List<WorkoutEntry>) {
        val sessionGrouped = workouts
            .groupBy { DATE_FORMAT.format(it.timestamp) }
            .mapValues { (_, workoutsForDate) ->
                workoutsForDate
                    .groupBy { it.sessionId ?: "legacy_${DATE_FORMAT.format(it.timestamp)}" }
                    .map { (sessionId, sessionWorkouts) ->
                        val first = sessionWorkouts.first()
                        SessionWithWorkouts(
                            sessionId = sessionId,
                            sessionName = first.sessionName ?: "Entrenamiento",
                            sessionColor = first.sessionColor ?: "#43f4ff",
                            workouts = sessionWorkouts,
                            date = DATE_FORMAT.format(first.timestamp)
                        )
                    }
                    .sortedByDescending { it.workouts.first().timestamp }
            }

        _sessionWorkouts.value = sessionGrouped
    }

    fun updateWorkout(workout: WorkoutEntry) {
        viewModelScope.launch {
            try {
                val updatedWorkout = workout.copy(
                    isSynced = false,
                    pendingAction = "UPDATE"
                )
                workoutRepository.updateWorkout(updatedWorkout)
                println("✅ Workout actualizado localmente: ${workout.title}")
            } catch (e: Exception) {
                println("❌ Error actualizando workout: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun deleteWorkout(workout: WorkoutEntry) {
        viewModelScope.launch {
            try {
                workoutRepository.removeWorkout(workout)
                println("✅ Workout eliminado localmente: ${workout.title}")
            } catch (e: Exception) {
                println("❌ Error eliminando workout: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun syncPendingWorkouts() {
        viewModelScope.launch {
            try {
                workoutRepository.syncPendingWorkouts()
                println("✅ Workouts sincronizados con Firebase")
            } catch (e: Exception) {
                println("❌ Error sincronizando: ${e.message}")
            }
        }
    }

    fun groupWorkoutsByDateFiltered(exerciseName: String): Map<String, List<WorkoutEntry>> {
        val filtered = entries.value.filter {
            it.title.contains(exerciseName, ignoreCase = true)
        }
        return groupWorkoutsByDate(filtered)
    }

    fun groupWorkoutsByDate(workouts: List<WorkoutEntry>): Map<String, List<WorkoutEntry>> {
        return workouts
            .sortedByDescending { it.timestamp }
            .groupBy { DATE_FORMAT.format(it.timestamp) }
    }
}