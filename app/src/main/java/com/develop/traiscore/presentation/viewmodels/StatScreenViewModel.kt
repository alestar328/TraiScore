package com.develop.traiscore.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.develop.traiscore.data.local.dao.WorkoutDao
import com.develop.traiscore.data.local.entity.WorkoutWithType
import com.develop.traiscore.domain.defaultExerciseEntities
import com.develop.traiscore.domain.model.toWorkoutModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class StatScreenViewModel @Inject constructor(
    private val workoutDao: WorkoutDao,
) : ViewModel() {
    private val _exerciseOptions = MutableStateFlow(defaultExerciseEntities.map { it.name })
    val exerciseOptions: StateFlow<List<String>> = _exerciseOptions.asStateFlow()

    private val _selectedExercise = MutableStateFlow<String?>(null)
    val selectedExercise: StateFlow<String?> = _selectedExercise

    private val _workoutData = MutableStateFlow<List<WorkoutWithType>>(emptyList())
    val workoutData: StateFlow<List<WorkoutWithType>> = _workoutData.asStateFlow()

    private val _progressData = MutableStateFlow<List<Pair<String, Float>>>(emptyList())
    val progressData: StateFlow<List<Pair<String, Float>>> = _progressData

    private val _circularData = MutableStateFlow<Triple<Double, Int, Int>>(Triple(0.0, 0, 0))
    val circularData: StateFlow<Triple<Double, Int, Int>> = _circularData

    fun onExerciseSelected(selected: String) {
        val exercise = defaultExerciseEntities.find { it.name == selected }
        if (exercise != null) {
            _selectedExercise.value = exercise.idIntern
            loadWorkoutData(exercise.idIntern)
        }
    }

    private fun loadWorkoutData(exerciseId: String) {
        viewModelScope.launch {
            try {
                val workoutTypes = workoutDao.getWorkoutTypesByExercise(exerciseId.toInt())

                val workoutsWithTypes = workoutTypes.map { workoutType ->
                    WorkoutWithType(
                        workoutModel = workoutType.toWorkoutModel(),
                        workoutType = workoutType
                    )
                }

                _workoutData.value = workoutsWithTypes
                updateAllGraphData()
            } catch (e: Exception) {
                e.printStackTrace()
                _workoutData.value = emptyList()
            }
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

        _progressData.value = workouts.map { (workoutModel, workoutType) ->
            val normalizedWeight =
                if (maxWeight != minWeight) ((workoutType.weight - minWeight) / (maxWeight - minWeight)).toFloat() else 1f
            workoutModel.timestamp.toString() to normalizedWeight
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
}

