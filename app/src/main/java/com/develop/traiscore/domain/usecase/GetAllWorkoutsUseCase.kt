package com.develop.traiscore.domain.usecase

import com.develop.traiscore.data.local.entity.WorkoutWithExercise
import com.develop.traiscore.domain.model.Resource
import com.develop.traiscore.domain.model.WorkoutModel
import com.develop.traiscore.domain.repository.LocalStorageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetAllWorkoutsUseCase @Inject constructor(
    private val repository: LocalStorageRepository
) {
    operator fun invoke(): Flow<Resource<List<WorkoutWithExercise>>> = flow {
        try{
            emit(Resource.Loading())
            val workouts = repository.getAllWorkouts()
            emit(Resource.Success(data = workouts))
        }catch (e:Exception){
            emit(
                Resource.Error(e.message ?: "Error desconocido GetAllWorkoutsUseCase")
            )
        }
    }
}