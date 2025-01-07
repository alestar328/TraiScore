package com.develop.traiscore.data

import com.develop.traiscore.data.local.entity.WorkoutType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class WorkoutRepository {

    private val _workouts = MutableStateFlow<List<WorkoutType>>(emptyList())
    val workouts: StateFlow<List<WorkoutType>> = _workouts

    // Agregar un nuevo ejercicio a la lista
    fun addWorkout(workout: WorkoutType) {
        val updatedList = _workouts.value.toMutableList()
        updatedList.add(workout)
        _workouts.value = updatedList
    }
    // Eliminar un ejercicio de la lista
    fun removeWorkout(workout: WorkoutType) {
        val updatedList = _workouts.value.toMutableList()
        updatedList.remove(workout)
        _workouts.value = updatedList
    }
}