package com.develop.traiscore.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ChronoState {
    STOPPED,
    RUNNING,
    PAUSED
}

@HiltViewModel
class ChronoViewModel @Inject constructor() : ViewModel() {

    private val _elapsedTime = MutableStateFlow(0L) // En milisegundos
    val elapsedTime: StateFlow<Long> = _elapsedTime

    private val _chronoState = MutableStateFlow(ChronoState.STOPPED)
    val chronoState: StateFlow<ChronoState> = _chronoState

    private val _isCountDown = MutableStateFlow(false)
    val isCountDown: StateFlow<Boolean> = _isCountDown

    private val _countDownTime = MutableStateFlow(0L) // Tiempo inicial del countdown en ms
    val countDownTime: StateFlow<Long> = _countDownTime

    private val _remainingTime = MutableStateFlow(0L) // Tiempo restante en countdown
    val remainingTime: StateFlow<Long> = _remainingTime

    private var chronoJob: Job? = null
    private var startTime = 0L
    private var pausedTime = 0L

    companion object {
        private const val UPDATE_INTERVAL = 10L // Actualizar cada 10ms para mayor precisión
    }

    /**
     * Inicia el cronómetro normal (hacia adelante)
     */
    fun startChrono() {
        Log.d("ChronoVM", "🚀 Iniciando cronómetro")

        if (_chronoState.value == ChronoState.PAUSED) {
            // Continuar desde pausa
            startTime = System.currentTimeMillis() - pausedTime
        } else {
            // Nuevo inicio
            startTime = System.currentTimeMillis()
            pausedTime = 0L
        }

        _isCountDown.value = false
        _chronoState.value = ChronoState.RUNNING
        startChronoLoop()
    }

    /**
     * Inicia el cronómetro en modo countdown
     */
    fun startCountDown(minutes: Int, seconds: Int = 0) {
        val totalMs = (minutes * 60L + seconds) * 1000L
        Log.d("ChronoVM", "⏰ Iniciando countdown: ${minutes}m ${seconds}s")

        _isCountDown.value = true
        _countDownTime.value = totalMs
        _remainingTime.value = totalMs
        _elapsedTime.value = 0L
        _chronoState.value = ChronoState.RUNNING

        startTime = System.currentTimeMillis()
        pausedTime = 0L

        startCountDownLoop()
    }

    /**
     * Pausa el cronómetro
     */
    fun pauseChrono() {
        Log.d("ChronoVM", "⏸️ Pausando cronómetro")

        chronoJob?.cancel()
        _chronoState.value = ChronoState.PAUSED

        if (_isCountDown.value) {
            pausedTime = _remainingTime.value
        } else {
            pausedTime = _elapsedTime.value
        }
    }

    /**
     * Continúa el cronómetro desde la pausa
     */
    fun resumeChrono() {
        Log.d("ChronoVM", "▶️ Reanudando cronómetro")

        if (_isCountDown.value) {
            startTime = System.currentTimeMillis()
            _chronoState.value = ChronoState.RUNNING
            startCountDownLoop()
        } else {
            startChrono()
        }
    }

    /**
     * Resetea el cronómetro
     */
    fun resetChrono() {
        Log.d("ChronoVM", "🔄 Reseteando cronómetro")

        chronoJob?.cancel()
        _chronoState.value = ChronoState.STOPPED
        _elapsedTime.value = 0L
        _remainingTime.value = 0L
        _countDownTime.value = 0L
        _isCountDown.value = false
        startTime = 0L
        pausedTime = 0L
    }

    /**
     * Loop principal del cronómetro normal
     */
    private fun startChronoLoop() {
        chronoJob?.cancel()
        chronoJob = viewModelScope.launch {
            while (_chronoState.value == ChronoState.RUNNING && !_isCountDown.value) {
                val currentTime = System.currentTimeMillis()
                _elapsedTime.value = currentTime - startTime
                delay(UPDATE_INTERVAL)
            }
        }
    }

    /**
     * Loop principal del countdown
     */
    private fun startCountDownLoop() {
        chronoJob?.cancel()
        chronoJob = viewModelScope.launch {
            while (_chronoState.value == ChronoState.RUNNING && _isCountDown.value) {
                val currentTime = System.currentTimeMillis()
                val elapsed = currentTime - startTime
                val remaining = if (_chronoState.value == ChronoState.PAUSED) {
                    pausedTime
                } else {
                    (_countDownTime.value - elapsed).coerceAtLeast(0L)
                }

                _remainingTime.value = remaining
                _elapsedTime.value = elapsed

                // Verificar si el countdown terminó
                if (remaining <= 0L) {
                    Log.d("ChronoVM", "⏰ ¡Countdown terminado!")
                    onCountDownFinished()
                    break
                }

                delay(UPDATE_INTERVAL)
            }
        }
    }

    /**
     * Se ejecuta cuando el countdown llega a 0
     */
    private fun onCountDownFinished() {
        _chronoState.value = ChronoState.STOPPED
        _remainingTime.value = 0L
        // Aquí podrías agregar notificaciones, sonidos, etc.
        Log.d("ChronoVM", "🔔 Tiempo terminado - agregar notificación aquí")
    }

    /**
     * Formatea el tiempo en mm:ss o mm:ss.ms
     */
    fun formatTime(timeMs: Long, showMilliseconds: Boolean = false): String {
        val minutes = (timeMs / 60000L).toInt()
        val seconds = ((timeMs % 60000L) / 1000L).toInt()
        val milliseconds = ((timeMs % 1000L) / 10L).toInt() // Centésimas

        return if (showMilliseconds) {
            "%02d:%02d.%02d".format(minutes, seconds, milliseconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
    }

    /**
     * Obtiene el tiempo actual a mostrar según el modo
     */
    fun getCurrentDisplayTime(): Long {
        return if (_isCountDown.value) {
            _remainingTime.value
        } else {
            _elapsedTime.value
        }
    }

    /**
     * Configurar tiempos predeterminados de descanso
     */
    fun setRestTime(minutes: Int) {
        startCountDown(minutes, 0)
    }

    override fun onCleared() {
        super.onCleared()
        chronoJob?.cancel()
        Log.d("ChronoVM", "🧹 ChronoViewModel limpiado")
    }
}