package com.develop.traiscore.domain.usecase

import com.develop.traiscore.domain.model.Resource
import com.develop.traiscore.domain.model.WorkoutModel
import com.develop.traiscore.domain.repository.LocalStorageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SaveWorkoutUseCase @Inject constructor(
    private val repository: LocalStorageRepository
) {
    operator fun invoke(workoutModel: WorkoutModel): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading()) // Emitir estado de carga
        val result = repository.saveWorkout(workoutModel)
        emit(Resource.Success(result)) // Emitir estado de éxito
    }.catch { exception ->
        emit(Resource.Error(exception.message ?: "Error desconocido al guardar el ejercicio."))
    }
}