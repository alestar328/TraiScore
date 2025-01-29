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
    operator fun invoke(startDate: Long, endDate: Long): Flow<Resource<WorkoutModel>> = flow {
        try{
            emit(Resource.Loading())
            val workout = repository.getWorkout(startDate, endDate)
            emit(Resource.Success(data = workout))

        }catch (e:Exception){
            emit(
                Resource.Error(e.message ?: "Error desconocido SaveWorkoutUseCase")
            )
        }
    }
}