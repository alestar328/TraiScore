package com.develop.traiscore.presentation.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.develop.traiscore.domain.defaultExerciseEntities
import com.develop.traiscore.domain.model.Resource
import com.develop.traiscore.domain.model.WorkoutModel
import com.develop.traiscore.domain.usecase.SaveWorkoutUseCase
import com.develop.traiscore.domain.usecase.UpdateWorkoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AddExerciseViewModel @Inject constructor(
    private val saveWorkoutUseCase: SaveWorkoutUseCase,
) : ViewModel() {

    // Estado para los campos de entrada
    private val _exerciseData = mutableStateOf(ExerciseData(name = "", reps = 0, weight = 0.0, rir = 5, timestamp = Date()))
    val exerciseData: State<ExerciseData> get() = _exerciseData

    private val _allExercises = defaultExerciseEntities.map { it.name }

    // Estado para filtrar ejercicios
    private val _filterText = mutableStateOf("")
    val filterText: State<String> get() = _filterText

    val filteredExercises: State<List<String>> = derivedStateOf {
        _allExercises.filter { it.contains(_filterText.value, ignoreCase = true) }
    }

    private val _isSaving = mutableStateOf(false)
    val isSaving: State<Boolean> get() = _isSaving

    fun updateFilterText(newText: String) {
        _filterText.value = newText
        _exerciseData.value = _exerciseData.value.copy(name = newText)
    }

    fun updateExerciseReps(reps: String) {
        _exerciseData.value = _exerciseData.value.copy(reps = reps.toIntOrNull() ?: 0)
    }

    fun updateExerciseWeight(weight: String) {
        _exerciseData.value = _exerciseData.value.copy(weight = weight.toDoubleOrNull() ?: 0.0)
    }

    fun updateSliderValue(value: Float) {
        _exerciseData.value = _exerciseData.value.copy(rir = value.toInt())
    }

    fun saveWorkout(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (_exerciseData.value.name.isBlank() || _exerciseData.value.reps <= 0 || _exerciseData.value.weight <= 0.0) {
            onError("Por favor, completa todos los campos correctamente.")
            return
        }

        _isSaving.value = true

        viewModelScope.launch {
            val workoutModel = WorkoutModel(
                id = 0, // Generado automáticamente
                exerciseId = 1, // ID de ejemplo
                title = _exerciseData.value.name,
                weight = _exerciseData.value.weight,
                reps = _exerciseData.value.reps,
                rir = _exerciseData.value.rir,
                timestamp = Date()
            )

            saveWorkoutUseCase(workoutModel).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        onSuccess()
                    }
                    is Resource.Error -> {
                        onError(result.message ?: "Error desconocido al guardar.")
                    }
                    else -> {}
                }
            }
            _isSaving.value = false
        }
    }

    fun resetInputFields() {
        _exerciseData.value = ExerciseData(name = "", reps = 0, weight = 0.0, rir = 5, timestamp = Date())
        _filterText.value = ""
    }
}
data class ExerciseData(val name: String,
                        val reps: Int,
                        val weight: Double,
                        val rir: Int,
                        val timestamp: Date)

