package com.develop.traiscore.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.develop.traiscore.core.TimeRange
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt


@HiltViewModel
class StatScreenViewModel @Inject constructor() : ViewModel() {
    private val db = Firebase.firestore

    // Estado de selección
    private val _selectedExercise = MutableStateFlow<String?>(null)
    val selectedExercise: StateFlow<String?> = _selectedExercise

    private val _selectedTimeRange = MutableStateFlow(TimeRange.ONE_MONTH)
    val selectedTimeRange: StateFlow<TimeRange> = _selectedTimeRange

    // Opciones de ejercicios (para el dropdown)
    private val _exerciseOptions = MutableStateFlow<List<String>>(emptyList())
    val exerciseOptions: StateFlow<List<String>> = _exerciseOptions

    // Datos para las gráficas
    private val _weightProgress = MutableStateFlow<List<Pair<String, Float>>>(emptyList())
    val weightProgress: StateFlow<List<Pair<String, Float>>> = _weightProgress

    private val _repsProgress = MutableStateFlow<List<Pair<String, Float>>>(emptyList())
    val repsProgress: StateFlow<List<Pair<String, Float>>> = _repsProgress

    // Datos circulares
    private val _circularData = MutableStateFlow(Triple(0f, 0, 0))
    val circularData: StateFlow<Triple<Float, Int, Int>> = _circularData

    private val _repVsWeight = MutableStateFlow<List<Pair<Float, Float>>>(emptyList())
    val repVsWeight: StateFlow<List<Pair<Float, Float>>> = _repVsWeight

    private val _totalWeightSum = MutableStateFlow(0f)
    val totalWeightSum: StateFlow<Float> = _totalWeightSum

    init {
        fetchExerciseOptions()
        fetchLastWorkoutEntry()
        // Cuando el usuario elige un ejercicio, recargar historial completo
        viewModelScope.launch {
            _selectedExercise
                .filterNotNull()
                .distinctUntilChanged()
                .collect { ex ->
                    loadAllProgressFor(ex)
                    calculateTotalWeightLifted(ex)
                }
        }
    }

    private fun fetchExerciseOptions() {
        // Queremos tanto logging como un posible estado de error en UI
        db.collection("exercises")
            .orderBy("name", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { snap ->
                _exerciseOptions.value = snap.documents
                    .mapNotNull { it.getString("name") }
            }
            .addOnFailureListener { e ->
                Log.e("StatsVM", "No se pudo cargar la lista de ejercicios", e)
                // Aquí podrías actualizar un StateFlow de error, p.ej.:
                // _loadError.value = "Error al cargar ejercicios"
            }
    }

    fun onExerciseSelected(name: String) {
        _selectedExercise.value = name
    }


    private fun loadAllProgressFor(exerciseName: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e("StatsVM", "Usuario no autenticado, no puedo cargar progreso")
            return
        }

        db.collection("users")
            .document(userId)
            .collection("workoutEntries")
            .whereEqualTo("title", exerciseName)
            .orderBy("timestamp")
            .limit(10)
            .get()
            .addOnSuccessListener { snap ->
                val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
                val wData = mutableListOf<Pair<String, Float>>()
                val rData = mutableListOf<Pair<String, Float>>()
                val allRir = mutableListOf<Int>()

                for (doc in snap.documents) {
                    val ts = doc.getDate("timestamp") ?: continue
                    val label = sdf.format(ts)
                    // Adaptar a cómo guardas weight/reps/rir en Firestore:
                    val weight = (doc.getLong("weight") ?: 0L).toFloat()
                    val reps = (doc.getLong("reps") ?: 0L).toFloat()
                    val rir = (doc.getLong("rir") ?: 0L).toInt()

                    wData += label to weight
                    rData += label to reps
                    allRir += rir
                }

                // Si no hay datos, mete un punto 0 en la fecha de hoy
                if (wData.isEmpty()) {
                    val hoy = sdf.format(Date())
                    wData += hoy to 0f
                    rData += hoy to 0f
                }

                _weightProgress.value = wData
                _repsProgress.value = rData

                // Calcula: 1RM = _max_ peso, MR = _max_ reps, RIR = avg RIR
                val oneRm = wData.maxByOrNull { it.second }?.second ?: 0f
                val maxRp = rData.maxByOrNull { it.second }?.second?.toInt() ?: 0
                val avgRir = if (allRir.isNotEmpty()) allRir.average().roundToInt() else 0

                _circularData.value = Triple(oneRm, maxRp, avgRir)
            }
            .addOnFailureListener { e ->
                Log.e("StatsVM", "no pude cargar workoutEntries para '$exerciseName'", e)
            }
    }

    private fun calculateTotalWeightLifted(exerciseName: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e("StatsVM", "Usuario no autenticado, no puedo calcular total weight")
            return
        }
        db.collection("users")
            .document(userId)
            .collection("workoutEntries")
            .whereEqualTo("title", exerciseName)
            .get()
            .addOnSuccessListener { snap ->
                // Sumar el campo "weight" de cada documento
                val total = snap.documents.fold(0f) { acc, doc ->
                    acc + (doc.getLong("weight") ?: 0L).toFloat()
                }
                _totalWeightSum.value = total
            }
            .addOnFailureListener { e ->
                Log.e("StatsVM", "error sumando totalWeight para '$exerciseName'", e)
            }
    }

    private fun fetchLastWorkoutEntry() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e("StatsVM", "Usuario no autenticado, no puedo cargar la última entrada")
            return
        }
        db.collection("users")
            .document(userId)
            .collection("workoutEntries")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { snap ->
                val lastTitle = snap.documents
                    .firstOrNull()
                    ?.getString("title")
                lastTitle?.let {
                    _selectedExercise.value = it
                }
            }
            .addOnFailureListener { e ->
                // opcional: maneja el fallo
            }
    }
}