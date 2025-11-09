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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ExercisesScreenViewModel @Inject constructor(
    private val deleteWorkoutUseCase: DeleteWorkoutUseCase,
    private val getAllWorkoutsUseCase: GetAllWorkoutsUseCase,
    private val saveWorkoutUseCase: SaveWorkoutUseCase
) : ViewModel() {

    private val _exercises = mutableStateListOf<WorkoutWithExercise>()
    val exercises: List<WorkoutWithExercise> get() = _exercises

    /** 🔹 Cargar todos los workouts (desde Room) */
    fun getExercises(onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                getAllWorkoutsUseCase().collectLatest { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            _exercises.clear()
                            _exercises.addAll(resource.data ?: emptyList())
                        }
                        is Resource.Error -> {
                            onError(resource.message ?: "Error al obtener los ejercicios.")
                        }
                        else -> Unit
                    }
                }
            } catch (e: Exception) {
                onError(e.message ?: "Error desconocido al obtener los ejercicios.")
            }
        }
    }

    /** 🔹 Eliminar un workout localmente */
    fun deleteExercise(workoutId: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            deleteWorkoutUseCase(workoutId).collectLatest { result ->
                when (result) {
                    is Resource.Success -> {
                        // ✅ Ahora usamos workout.id, no workoutModel.id
                        _exercises.removeAll { it.workout.id == workoutId }
                        onSuccess()
                    }
                    is Resource.Error -> {
                        onError(result.message ?: "Error al eliminar el ejercicio.")
                    }
                    else -> Unit
                }
            }
        }
    }
}