package com.develop.traiscore.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.develop.traiscore.data.WorkoutRepository
import com.develop.traiscore.data.local.entity.WorkoutType
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WorkoutViewModel (private val repository: WorkoutRepository) : ViewModel() {
    val workouts: StateFlow<List<WorkoutType>> = repository.workouts

    fun addWorkout(workout: WorkoutType) {
        viewModelScope.launch {
            repository.addWorkout(workout)
        }
    }

    fun removeWorkout(workout: WorkoutType) {
        viewModelScope.launch {
            repository.removeWorkout(workout)
        }
    }
}