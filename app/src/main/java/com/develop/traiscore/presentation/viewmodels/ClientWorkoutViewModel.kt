package com.develop.traiscore.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.develop.traiscore.data.local.entity.WorkoutEntry
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ClientWorkoutViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private val _groupedByDate = MutableStateFlow<Map<String, List<WorkoutEntry>>>(emptyMap())
    val groupedByDate: StateFlow<Map<String, List<WorkoutEntry>>> = _groupedByDate.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Fechas con entrenamientos (formato "dd/MM/yyyy") para el calendario
    val workoutDates: StateFlow<Set<String>> get() = _workoutDates
    private val _workoutDates = MutableStateFlow<Set<String>>(emptySet())

    fun loadWorkouts(clientId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val snapshot = firestore.collection("users")
                    .document(clientId)
                    .collection("workoutEntries")
                    .get()
                    .await()

                val workouts = snapshot.documents.mapNotNull { doc ->
                    val title = doc.getString("title") ?: return@mapNotNull null
                    val reps = doc.getLong("reps")?.toInt() ?: 0
                    val weight = doc.getDouble("weight")?.toFloat() ?: 0f
                    val rir = doc.getLong("rir")?.toInt() ?: 0
                    val timestamp = doc.getDate("timestamp") ?: Date()
                    val sessionId = doc.getString("sessionId")
                    val sessionName = doc.getString("sessionName")
                    val sessionColor = doc.getString("sessionColor")

                    WorkoutEntry(
                        uid = doc.id,
                        exerciseId = 0,
                        title = title,
                        reps = reps,
                        weight = weight,
                        rir = rir,
                        series = 0,
                        timestamp = timestamp,
                        sessionId = sessionId,
                        sessionName = sessionName,
                        sessionColor = sessionColor,
                        isSynced = true
                    )
                }.sortedByDescending { it.timestamp }

                val grouped = workouts.groupBy { dateFormat.format(it.timestamp) }
                _groupedByDate.value = grouped
                _workoutDates.value = grouped.keys.toSet()
            } catch (e: Exception) {
                _error.value = "Error al cargar entrenamientos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
