package com.develop.traiscore.domain.usecase

import com.develop.traiscore.domain.model.Resource
import com.develop.traiscore.domain.model.WorkoutModel
import com.develop.traiscore.domain.repository.LocalStorageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetWorkoutUseCase @Inject constructor(
    private val repository: LocalStorageRepository
) {
    operator fun invoke(date: String): Flow<Resource<WorkoutModel>> = flow {
        try{
            emit(Resource.Loading())
            emit(
                Resource.Success(
                    data = repository.getWorkout(date)
                )
            )

        }catch (e:Exception){
            emit(
                Resource.Error(e.message ?: "Error desconocido SaveWorkoutUseCase")
            )
        }
    }
}