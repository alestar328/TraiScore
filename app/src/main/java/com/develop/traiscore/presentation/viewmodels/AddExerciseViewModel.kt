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
    private val updateWorkoutUseCase: UpdateWorkoutUseCase

) : ViewModel() {


    private var _isEditing = mutableStateOf(false) // Indica si estamos en modo edición
    val isEditing: State<Boolean> get() = _isEditing

    private var _workoutId = mutableStateOf<Long?>(null) // Almacena el ID del entrenamiento (si es edición)
    val workoutId: State<Long?> get() = _workoutId

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
    //Determina si esta editando o creando
    fun setEditingMode(workoutId: Long?, exerciseData: ExerciseData?) {
        if (workoutId != null && exerciseData != null) {
            _isEditing.value = true
            _workoutId.value = workoutId
            _filterText.value = exerciseData.name
            _selectedExercise.value = exerciseData.name
            _exerciseReps.value = exerciseData.reps.toString()
            _exerciseWeight.value = exerciseData.weight.toString()
            _sliderValue.value = exerciseData.rir.toFloat()
        } else {
            resetInputFields()
        }
    }
    fun saveOrUpdateWorkout(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val reps = _exerciseReps.value.toIntOrNull()
        val weight = _exerciseWeight.value.toDoubleOrNull()

        if (reps == null || weight == null || _selectedExercise.value.isEmpty()) {
            onError("Por favor, completa todos los campos correctamente.")
            return
        }

        val exerciseData = ExerciseData(
            name = _selectedExercise.value,
            reps = reps,
            weight = weight,
            rir = _sliderValue.value.toInt(),
            timestamp = Date()
        )

        setIsSaving(true)

        viewModelScope.launch {
            val workoutModel = WorkoutModel(
                id = 0, // El ID será autogenerado por Room.
                exerciseId = 1, // Cambia a un ID válido según tu lógica.
                title = exerciseData.name,
                weight = exerciseData.weight,
                reps = exerciseData.reps,
                rir = exerciseData.rir,
                timestamp = Date() // Usa la fecha actual.
            )

            saveWorkoutUseCase(workoutModel).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        onSuccess()
                    }
                    is Resource.Error -> {
                        onError(result.message ?: "Error al guardar el entrenamiento.")
                    }
                    else -> {}
                }
            }
            setIsSaving(false)
        }
    }

    fun resetInputFields() {
        _isEditing.value = false
        _workoutId.value = null
        _filterText.value = ""
        _selectedExercise.value = ""
        _exerciseReps.value = ""
        _exerciseWeight.value = ""
        _sliderValue.value = 5f
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
            rir = _sliderValue.value.toInt(),
            timestamp = Date() // Añade el timestamp actual.
        )
    }

}
data class ExerciseData(val name: String,
                        val reps: Int,
                        val weight: Double,
                        val rir: Int,
                        val timestamp: Date)

