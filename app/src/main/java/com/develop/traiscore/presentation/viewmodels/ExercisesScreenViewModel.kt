package com.develop.traiscore.presentation.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.develop.traiscore.data.local.entity.ExerciseEntity
import com.develop.traiscore.domain.model.WorkoutModel
import com.develop.traiscore.data.local.entity.WorkoutType
import com.develop.traiscore.data.local.entity.WorkoutWithExercise
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ExercisesScreenViewModel @Inject constructor()  : ViewModel() {

    private val _exercises = mutableStateListOf<WorkoutWithExercise>()
    val exercises: List<WorkoutWithExercise> get() = _exercises

    fun addExercise(exercise: ExerciseData) {
        val workoutType = WorkoutType(
            id = _exercises.size + 1, // Genera un ID único
            exerciseId = _exercises.size + 1, // También genera un ID para el ejercicio
            title = exercise.name,
            weight = exercise.weight,
            reps = exercise.reps,
            rir = exercise.rir
        )

        val workoutModel = WorkoutModel(
            id = _exercises.size + 1, // Genera un ID único
            timestamp = Date(),
            workoutTypeId = workoutType.id
        )

        val exerciseEntity = ExerciseEntity(
            id = workoutType.exerciseId,
            idIntern = exercise.name.lowercase(),
            name = exercise.name,
            isDefault = false
        )

        val newWorkoutWithExercise = WorkoutWithExercise(
            workoutModel = workoutModel,
            workoutType = workoutType,
            exerciseEntity = exerciseEntity
        )

        if (_exercises.none { it.workoutType.title == exercise.name }) {
            _exercises.add(newWorkoutWithExercise)
            println("Exercise added: $newWorkoutWithExercise")
        } else {
            println("Duplicate exercise detected: $exercise")
        }
    }
}