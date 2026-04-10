package com.develop.traiscore.presentation.splashScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.develop.traiscore.data.repository.SessionRepository
import com.develop.traiscore.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _isDataReady = MutableStateFlow(false)
    val isDataReady: StateFlow<Boolean> = _isDataReady.asStateFlow()

    init {
        preloadData()
    }

    private fun preloadData() {
        viewModelScope.launch {
            try {
                // Precargar sesiones desde Room + iniciar sync con Firebase
                sessionRepository.getUserAvailableSessions()
                sessionRepository.syncPendingSessions()
                // Importar workouts de Firebase a Room si es necesario
                workoutRepository.importWorkoutsFromFirebaseToRoom()
            } catch (e: Exception) {
                // Si falla la precarga, continuamos igualmente
                android.util.Log.w("SplashViewModel", "Precarga con errores: ${e.message}")
            } finally {
                _isDataReady.value = true
            }
        }
    }
}
