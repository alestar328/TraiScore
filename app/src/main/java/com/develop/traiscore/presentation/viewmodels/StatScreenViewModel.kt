package com.develop.traiscore.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.develop.traiscore.core.TimeRange
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.take
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

    // NUEVO: Estado para el cliente que se está visualizando
    private val _targetUserId = MutableStateFlow<String?>(null)
    val targetUserId: StateFlow<String?> = _targetUserId

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

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState

    init {
        viewModelScope.launch {
            // Inicializar con el usuario actual si no se especifica otro
            if (_targetUserId.value == null) {
                _targetUserId.value = FirebaseAuth.getInstance().currentUser?.uid
            }

            // Cargar ejercicios primero
            fetchExerciseOptions()

            // Esperar a que se carguen los ejercicios antes de seleccionar el último
            _exerciseOptions
                .filter { it.isNotEmpty() }
                .take(1) // Solo la primera emisión con datos
                .collect {
                    fetchLastWorkoutEntry()
                }
        }

        // Observar cambios en ejercicio seleccionado
        viewModelScope.launch {
            _selectedExercise
                .filterNotNull()
                .distinctUntilChanged()
                .collect { exercise ->
                    // Ejecutar en paralelo para mejor performance
                    launch { loadAllProgressFor(exercise) }
                    launch { calculateTotalWeightLifted(exercise) }
                }
        }
    }

    /**
     * NUEVO: Establecer el ID del usuario del cual queremos ver estadísticas
     * @param userId ID del cliente o null para usar el usuario actual
     */
    fun setTargetUser(userId: String?) {
        _targetUserId.value = userId ?: FirebaseAuth.getInstance().currentUser?.uid

        // Recargar datos para el nuevo usuario
        viewModelScope.launch {
            fetchExerciseOptions()
            _selectedExercise.value?.let { exercise ->
                loadAllProgressFor(exercise)
                calculateTotalWeightLifted(exercise)
            }
        }
    }

    /**
     * NUEVO: Obtener el ID del usuario objetivo (cliente o usuario actual)
     */
    private fun getTargetUserId(): String? {
        return _targetUserId.value ?: FirebaseAuth.getInstance().currentUser?.uid
    }

    private fun handleError(operation: String, exception: Exception) {
        Log.e("StatsVM", "Error en $operation", exception)
        _errorState.value = "Error en $operation: ${exception.message}"

        // Auto-limpiar error después de 5 segundos
        viewModelScope.launch {
            delay(5000)
            _errorState.value = null
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
            }
    }

    fun onExerciseSelected(name: String) {
        _selectedExercise.value = name
    }

    private fun loadAllProgressFor(exerciseName: String) {
        val userId = getTargetUserId() ?: return

        Log.d("StatsVM", "Cargando progreso de $exerciseName para usuario: $userId")

        // Usar una sola consulta con límite apropiado
        db.collection("users")
            .document(userId)
            .collection("workoutEntries")
            .whereEqualTo("title", exerciseName)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50) // Aumentar límite para mejor análisis
            .get()
            .addOnSuccessListener { snap ->
                Log.d("StatsVM", "Encontrados ${snap.size()} documentos para $exerciseName")
                processWorkoutData(snap.documents)
            }
            .addOnFailureListener { e ->
                handleError("loadAllProgressFor", e)
            }
    }

    private fun processWorkoutData(documents: List<DocumentSnapshot>) {
        val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
        val wData = mutableListOf<Pair<String, Float>>()
        val rData = mutableListOf<Pair<String, Float>>()
        val allRir = mutableListOf<Int>()

        Log.d("StatsVM", "📊 Procesando ${documents.size} documentos")

        // CAMBIO CRÍTICO: Ordenar por timestamp ASCENDENTE y tomar últimos 12
        val sortedDocuments = documents.sortedBy { it.getDate("timestamp") ?: Date(0) }
            .takeLast(12) // Solo últimos 12 registros

        sortedDocuments.forEach { doc ->
            val ts = doc.getDate("timestamp") ?: return@forEach
            val label = sdf.format(ts)

            val weight = doc.getDouble("weight")?.toFloat() ?: 0f
            val reps = doc.getLong("reps")?.toFloat() ?: 0f
            val rir = doc.getLong("rir")?.toInt() ?: 0

            Log.d("StatsVM", "📄 Doc: weight=$weight, reps=$reps, rir=$rir, fecha=$label")

            wData.add(label to weight)
            rData.add(label to reps)
            allRir.add(rir)
        }

        Log.d("StatsVM", "📈 Weight data ordenado: $wData")
        Log.d("StatsVM", "📊 Reps data ordenado: $rData")

        _weightProgress.value = wData
        _repsProgress.value = rData

        updateCircularData(wData, rData, allRir)
    }

    private fun updateCircularData(wData: List<Pair<String, Float>>, rData: List<Pair<String, Float>>, allRir: List<Int>) {
        // Calcula: 1RM = _max_ peso, MR = _max_ reps, RIR = avg RIR
        val oneRm = wData.maxByOrNull { it.second }?.second ?: 0f
        val maxRp = rData.maxByOrNull { it.second }?.second?.toInt() ?: 0
        val avgRir = if (allRir.isNotEmpty()) allRir.average().roundToInt() else 0

        Log.d("StatsVM", "🎯 Circular data: 1RM=$oneRm, MaxReps=$maxRp, AvgRIR=$avgRir")

        _circularData.value = Triple(oneRm, maxRp, avgRir)
    }

    private fun calculateTotalWeightLifted(exerciseName: String) {
        val userId = getTargetUserId()
        if (userId == null) {
            Log.e("StatsVM", "Usuario no identificado, no puedo calcular total weight")
            return
        }

        Log.d("StatsVM", "Calculando peso total de $exerciseName para usuario: $userId")

        db.collection("users")
            .document(userId)
            .collection("workoutEntries")
            .whereEqualTo("title", exerciseName)
            .get()
            .addOnSuccessListener { snap ->
                // FIX CRÍTICO: Cambiar getLong por getDouble
                val total = snap.documents.fold(0f) { acc, doc ->
                    acc + (doc.getDouble("weight")?.toFloat() ?: 0f)
                }
                _totalWeightSum.value = total
                Log.d("StatsVM", "✅ Total weight calculado: $total kg para ejercicio: $exerciseName (usuario: $userId)")
            }
            .addOnFailureListener { e ->
                handleError("calculateTotalWeightLifted", e)
            }
    }

    private fun fetchLastWorkoutEntry() {
        val userId = getTargetUserId()
        if (userId == null) {
            Log.e("StatsVM", "Usuario no identificado")
            // Seleccionar primer ejercicio disponible como fallback
            viewModelScope.launch {
                _exerciseOptions.collect { options ->
                    if (options.isNotEmpty() && _selectedExercise.value == null) {
                        _selectedExercise.value = options.first()
                    }
                }
            }
            return
        }

        Log.d("StatsVM", "Buscando último workout entry para usuario: $userId")

        db.collection("users")
            .document(userId)
            .collection("workoutEntries")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { snap ->
                val lastTitle = snap.documents.firstOrNull()?.getString("title")
                if (lastTitle != null) {
                    Log.d("StatsVM", "Último ejercicio encontrado: $lastTitle")
                    _selectedExercise.value = lastTitle
                } else {
                    Log.d("StatsVM", "No se encontraron ejercicios, usando fallback")
                    // Fallback: usar primer ejercicio disponible
                    _exerciseOptions.value.firstOrNull()?.let {
                        _selectedExercise.value = it
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("StatsVM", "Error cargando último ejercicio", e)
                // Fallback en caso de error
                _exerciseOptions.value.firstOrNull()?.let {
                    _selectedExercise.value = it
                }
            }
    }
}