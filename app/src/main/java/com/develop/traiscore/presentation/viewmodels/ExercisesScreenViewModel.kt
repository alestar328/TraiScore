package com.develop.traiscore.presentation.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.develop.traiscore.domain.model.WorkoutModel
import com.develop.traiscore.data.local.entity.WorkoutEntry
import com.develop.traiscore.data.local.entity.WorkoutWithExercise
import com.develop.traiscore.domain.model.Resource
import com.develop.traiscore.domain.usecase.DeleteWorkoutUseCase
import com.develop.traiscore.domain.usecase.GetAllWorkoutsUseCase
import com.develop.traiscore.domain.usecase.SaveWorkoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ExercisesScreenViewModel @Inject constructor(
    private val deleteWorkoutUseCase: DeleteWorkoutUseCase, // Caso de uso de eliminación
    private val getAllWorkoutsUseCase: GetAllWorkoutsUseCase, // Caso de uso para obtener ejercicios
    private val saveWorkoutUseCase: SaveWorkoutUseCase // Caso de uso para guardar ejercicios

) : ViewModel() {

    private val _exercises = mutableStateListOf<WorkoutWithExercise>()
    val exercises: List<WorkoutWithExercise> get() = _exercises
    // Función para obtener ejercicios
    fun getExercises(onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val result = getAllWorkoutsUseCase().collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            _exercises.clear()
                            _exercises.addAll(resource.data ?: emptyList())
                        }
                        is Resource.Error -> {
                            onError(resource.message ?: "Error al obtener los ejercicios.")
                        }
                        else -> {} // No hacer nada en caso de Loading
                    }
                }
            } catch (e: Exception) {
                onError(e.message ?: "Error desconocido al obtener los ejercicios.")
            }
        }
    }
    fun deleteExercise(workoutId: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            deleteWorkoutUseCase(workoutId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        // Eliminar el ejercicio localmente si la operación fue exitosa
                        _exercises.removeAll { it.workoutModel.id == workoutId }
                        onSuccess()
                    }

                    is Resource.Error -> {
                        onError(result.message ?: "Error al eliminar el ejercicio.")
                    }

                    else -> {}
                }
            }
        }
    }




}