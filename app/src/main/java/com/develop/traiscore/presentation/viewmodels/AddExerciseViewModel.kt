package com.develop.traiscore.presentation.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.develop.traiscore.domain.defaultExerciseEntities
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddExerciseViewModel @Inject constructor() : ViewModel() {

    private val _allExercises = defaultExerciseEntities.map { it.name }
    // State for filtering dropdown items
    private val _filterText = mutableStateOf("")
    val filterText: State<String> get() = _filterText

    // Filtered exerciseEntity list based on user input
    val filteredExercises: State<List<String>> = derivedStateOf {
        _allExercises.filter { it.contains(_filterText.value, ignoreCase = true) }
    }
    // State for the selected exerciseEntity
    private val _selectedExercise = mutableStateOf("")
    val selectedExercise: State<String> get() = _selectedExercise


    private val _exerciseReps = mutableStateOf("")
    val exerciseReps: State<String> get() = _exerciseReps

    private val _exerciseWeight = mutableStateOf("")
    val exerciseWeight: State<String> get() = _exerciseWeight

    private val _sliderValue = mutableStateOf(5f)
    val sliderValue: State<Float> get() = _sliderValue

    //Flag para evitar el dobleclickeo y doble guardado
    private val _isSaving = mutableStateOf(false)
    val isSaving: State<Boolean> get() = _isSaving


    fun setIsSaving(value: Boolean) {
        _isSaving.value = value
    }

    // Métodos para actualizar estados
    fun updateFilterText(newText: String) {
        _filterText.value = newText
        _selectedExercise.value = newText // Permitir que el texto filtrado sea también el ejercicio seleccionado
    }

    fun updateExerciseReps(reps: String) {
        _exerciseReps.value = reps
    }

    fun updateExerciseWeight(weight: String) {
        _exerciseWeight.value = weight
    }

    fun updateSliderValue(valueSlide: Float) {
        _sliderValue.value = valueSlide
    }

    fun getExerciseData(): ExerciseData? {
        val reps = _exerciseReps.value.toIntOrNull()
        val weight = _exerciseWeight.value.toDoubleOrNull()

        if (reps == null || weight == null || _selectedExercise.value.isEmpty()) {
            return null
        }
        return ExerciseData(
            name = _selectedExercise.value,
            reps = reps,
            weight = weight,
            rir = _sliderValue.value.toInt()
        )
    }
    fun resetInputFields() {
        _filterText.value = ""
        _selectedExercise.value = ""
        _exerciseReps.value = ""
        _exerciseWeight.value = ""
        _sliderValue.value = 5f // Valor por defecto del slider
    }
}
data class ExerciseData(val name: String,
                        val reps: Int,
                        val weight: Double,
                        val rir: Int)

