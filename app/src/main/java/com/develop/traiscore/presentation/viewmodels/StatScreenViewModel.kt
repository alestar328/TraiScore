package com.develop.traiscore.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.develop.traiscore.data.local.dao.WorkoutTypeDao
import com.develop.traiscore.data.local.entity.WorkoutType
import com.develop.traiscore.data.local.entity.WorkoutWithType
import com.develop.traiscore.data.workoutDataMap
import com.develop.traiscore.domain.model.WorkoutModel
import com.develop.traiscore.domain.defaultExerciseEntities
import com.develop.traiscore.domain.model.TimeFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class StatScreenViewModel @Inject constructor(
    private val workoutTypeDao: WorkoutTypeDao
) : ViewModel() {
    suspend fun getWorkoutTypeById(workoutTypeId: Int): WorkoutType? {
        return workoutTypeDao.getWorkoutTypeById(workoutTypeId)
    }
    private val _timeOptions = MutableStateFlow(TimeFilter.values().map { it.displayName })
    val timeOptions: StateFlow<List<String>> = _timeOptions.asStateFlow()

    private val _exerciseOptions = MutableStateFlow(defaultExerciseEntities.map { it.name })
    val exerciseOptions: StateFlow<List<String>> = _exerciseOptions.asStateFlow()

    private val _selectedTime = MutableStateFlow<String?>(null)
    val selectedTime: StateFlow<String?> = _selectedTime

    private val _selectedExercise = MutableStateFlow<String?>(null)
    val selectedExercise: StateFlow<String?> = _selectedExercise

    private val _workoutData = MutableStateFlow<List<WorkoutWithType>>(emptyList())
    val workoutData: StateFlow<List<WorkoutWithType>> = _workoutData.asStateFlow()

    private val _progressData = MutableStateFlow<List<Pair<String, Float>>>(emptyList())
    val progressData: StateFlow<List<Pair<String, Float>>> = _progressData

    private val _circularData = MutableStateFlow<Triple<Double, Int, Int>>(Triple(0.0, 0, 0))
    val circularData: StateFlow<Triple<Double, Int, Int>> = _circularData

    fun onTimeSelected(selected: String) {
        val validSelection = TimeFilter.fromDisplayName(selected)
        if (validSelection != null) {
            _selectedTime.value = selected
            updateAllGraphData()
        }
    }

    fun onExerciseSelected(selected: String) {
        val exercise = defaultExerciseEntities.find { it.name == selected }
        if (exercise != null) {
            _selectedExercise.value = exercise.idIntern
            loadWorkoutData(exercise.idIntern)
        }
    }


    private fun updateAllGraphData() {
        updateLineChartData()
        updateCircularChartData()
    }

    private fun updateLineChartData() {
        val workouts = _workoutData.value
        if (workouts.isEmpty()) {
            _progressData.value = emptyList()
            return
        }

        val maxWeight = workouts.maxOfOrNull { it.workoutType.weight } ?: 1.0
        val minWeight = workouts.minOfOrNull { it.workoutType.weight } ?: 0.0

        _progressData.value = workouts.map { (workout, workoutType) ->
            val normalizedWeight =
                if (maxWeight != minWeight) ((workoutType.weight - minWeight) / (maxWeight - minWeight)).toFloat() else 1f
            workout.timestamp.toString() to normalizedWeight
        }
    }


    private fun updateCircularChartData() {
        val workouts = _workoutData.value
        if (workouts.isEmpty()) {
            _circularData.value = Triple(0.0, 0, 0)
            return
        }

        val oneRepMax = workouts.maxOfOrNull { (_, workoutType) ->
            workoutType.weight * (1 + workoutType.reps / 30.0)
        } ?: 0.0

        val maxReps = workouts.maxOfOrNull { it.workoutType.reps } ?: 0

        val averageRIR = workouts.mapNotNull { it.workoutType.rir }.average().toInt()

        _circularData.value = Triple(oneRepMax, maxReps, averageRIR)
    }


    private fun getWorkoutType(workoutTypeId: Int, callback: (WorkoutType?) -> Unit) {
        viewModelScope.launch {
            val workoutType = workoutTypeDao.getWorkoutTypeById(workoutTypeId)
            callback(workoutType)
        }
    }
    private fun loadWorkoutData(exerciseId: String) {
        viewModelScope.launch {
            val workouts = workoutDataMap[exerciseId] ?: emptyList()
            val workoutTypeIds = workouts.map { it.workoutTypeId }
            val workoutTypes = workoutTypeDao.getWorkoutTypesByIds(workoutTypeIds)

            val workoutsWithTypes = workouts.mapNotNull { workout ->
                val workoutType = workoutTypes.find { it.id == workout.workoutTypeId }
                if (workoutType != null) {
                    WorkoutWithType(workout, workoutType)
                } else null
            }

            _workoutData.value = workoutsWithTypes
            updateAllGraphData()
        }
    }
}
