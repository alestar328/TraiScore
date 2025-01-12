package com.develop.traiscore.domain.usecase

import com.develop.traiscore.domain.model.Resource
import com.develop.traiscore.domain.model.WorkoutModel
import com.develop.traiscore.domain.repository.LocalStorageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DeleteWorkoutUseCase @Inject constructor(
    private val repository: LocalStorageRepository
) {
    operator fun invoke(workoutId: Int): Flow<Resource<Boolean>> = flow {
        try{
            emit(Resource.Loading())
            val result = repository.deleteWorkout(workoutId)

            emit(Resource.Success(result))

        }catch (e:Exception){
            emit(
                Resource.Error(e.message ?: "Error desconocido SaveWorkoutUseCase")
            )
        }
    }
}