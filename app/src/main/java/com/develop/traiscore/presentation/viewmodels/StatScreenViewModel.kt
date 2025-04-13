package com.develop.traiscore.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.develop.traiscore.core.TimeRange
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject


@HiltViewModel
class StatScreenViewModel @Inject constructor(
) : ViewModel() {
    private val _selectedTimeRange = MutableStateFlow<TimeRange>(TimeRange.ONE_MONTH)
    val selectedTimeRange: StateFlow<TimeRange> = _selectedTimeRange
    private val _exerciseOptions = MutableStateFlow<List<String>>(emptyList())
    val exerciseOptions: StateFlow<List<String>> = _exerciseOptions

    // Otros datos existentes
    val progressData = MutableStateFlow<List<Pair<Float, Float>>>(emptyList())
    val circularData = MutableStateFlow(Triple(0f, 0, 0))

    init {
        fetchExerciseOptions()
    }
    private fun fetchExerciseOptions() {
        Firebase.firestore.collection("exercises")
            .get()
            .addOnSuccessListener { snapshot ->
                val names = snapshot.documents.mapNotNull { it.getString("name") }
                _exerciseOptions.value = names
            }
            .addOnFailureListener {
                println("❌ Error cargando ejercicios: ${it.message}")
            }
    }

    fun onExerciseSelected(selected: String) {
        // Manejar lógica cuando seleccionan un ejercicio
        println("📌 Ejercicio seleccionado: $selected")
    }

    fun onTimeRangeSelected(range: TimeRange?) {
        range?.let {
            _selectedTimeRange.value = it
            // Aquí más adelante podrás aplicar el filtro
        }
    }
}

