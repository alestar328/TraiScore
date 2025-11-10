package com.develop.traiscore.presentation.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.develop.traiscore.R
import com.develop.traiscore.core.TimeRange
import com.develop.traiscore.data.local.entity.WorkoutEntry
import com.develop.traiscore.data.repository.WorkoutRepository
import com.develop.traiscore.domain.model.ExerciseProgressCalculator
import com.develop.traiscore.domain.model.RadarChartData
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

data class SocialShareData(
    val topExercise: String,
    val maxRepsExercise : String,
    val topWeight: Float,
    val maxReps: Int,
    val totalWeight: Double,
    val trainingDays: Int
)

@HiltViewModel
class StatScreenViewModel @Inject constructor(
    private val context: Context,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {
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

    private val _rirProgress = MutableStateFlow<List<Pair<String, Float>>>(emptyList())
    val rirProgress: StateFlow<List<Pair<String, Float>>> = _rirProgress

    // Datos circulares
    private val _circularData = MutableStateFlow(Triple(0f, 0, 0))
    val circularData: StateFlow<Triple<Float, Int, Int>> = _circularData

    private val _repVsWeight = MutableStateFlow<List<Pair<Float, Float>>>(emptyList())
    val repVsWeight: StateFlow<List<Pair<Float, Float>>> = _repVsWeight

    private val _totalWeightSum = MutableStateFlow(0f)
    val totalWeightSum: StateFlow<Float> = _totalWeightSum

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState

    private val _radarChartData = MutableStateFlow<RadarChartData?>(null)
    val radarChartData: StateFlow<RadarChartData?> = _radarChartData

    private val _isLoadingRadarData = MutableStateFlow(false)
    val isLoadingRadarData: StateFlow<Boolean> = _isLoadingRadarData



    private val _currentMonthTrainingDays = MutableStateFlow(0)
    val currentMonthTrainingDays: StateFlow<Int> = _currentMonthTrainingDays



    fun getCurrentMonthTrainingDays(): Int {
        return _currentMonthTrainingDays.value
    }
    init {
        viewModelScope.launch {
            // Inicializar con el usuario actual si no se especifica otro
            if (_targetUserId.value == null) {
                _targetUserId.value = FirebaseAuth.getInstance().currentUser?.uid
            }

            // Cargar ejercicios primero
            fetchExerciseOptions()
            loadRadarChartData()
            calculateCurrentMonthTrainingDays()
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
    fun calculateCurrentMonthTrainingDays() {
        viewModelScope.launch {
            workoutRepository.workouts.collect { allWorkouts ->
                if (allWorkouts.isEmpty()) {
                    _currentMonthTrainingDays.value = 0
                    return@collect
                }

                val calendar = Calendar.getInstance()

                // Primer día del mes
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfMonth = calendar.time

                // Último día del mes
                calendar.add(Calendar.MONTH, 1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.add(Calendar.DATE, -1)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val endOfMonth = calendar.time

                // Filtramos las fechas únicas dentro del mes actual
                val trainingDays = allWorkouts.mapNotNull { entry ->
                    entry.timestamp.takeIf { it.after(startOfMonth) && it.before(endOfMonth) }?.let {
                        val c = Calendar.getInstance().apply { time = it }
                        "${c.get(Calendar.YEAR)}-${c.get(Calendar.MONTH)}-${c.get(Calendar.DAY_OF_MONTH)}"
                    }
                }.toSet().size

                _currentMonthTrainingDays.value = trainingDays
                Log.d("StatsVM", "📅 Días únicos de entrenamiento local: $trainingDays")
            }
        }
    }
    fun calculateSocialShareData(callback: (SocialShareData?) -> Unit) {
        viewModelScope.launch {
            workoutRepository.workouts.collect { allWorkouts ->
                if (allWorkouts.isEmpty()) {
                    callback(
                        SocialShareData(
                            topExercise = context.getString(R.string.filter_no_main_exercise),
                            maxRepsExercise = context.getString(R.string.filter_no_max_reps),
                            topWeight = 0f,
                            maxReps = 0,
                            totalWeight = 0.0,
                            trainingDays = _currentMonthTrainingDays.value
                        )
                    )
                    return@collect
                }

                // Calcular rango del día actual
                val calendar = Calendar.getInstance()

                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfDay = calendar.time

                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val endOfDay = calendar.time

                val todayEntries = allWorkouts.filter { it.timestamp.after(startOfDay) && it.timestamp.before(endOfDay) }

                if (todayEntries.isEmpty()) {
                    callback(
                        SocialShareData(
                            topExercise = context.getString(R.string.filter_no_main_exercise),
                            maxRepsExercise = context.getString(R.string.filter_no_max_reps),
                            topWeight = 0f,
                            maxReps = 0,
                            totalWeight = 0.0,
                            trainingDays = _currentMonthTrainingDays.value
                        )
                    )
                    return@collect
                }

                var topExercise = ""
                var topWeight = 0f
                var maxReps = 0
                var totalWeight = 0.0
                var maxRepsExercise = ""

                todayEntries.forEach { entry ->
                    if (entry.weight > topWeight) {
                        topWeight = entry.weight
                        topExercise = entry.title
                    }
                    if (entry.reps > maxReps) {
                        maxReps = entry.reps
                        maxRepsExercise = entry.title
                    }
                    totalWeight += entry.weight
                }

                callback(
                    SocialShareData(
                        topExercise = topExercise,
                        maxRepsExercise = maxRepsExercise,
                        topWeight = topWeight,
                        maxReps = maxReps,
                        totalWeight = totalWeight,
                        trainingDays = _currentMonthTrainingDays.value
                    )
                )
            }
        }
    }


    fun loadRadarChartData() {
        _isLoadingRadarData.value = true
        viewModelScope.launch {
            try {
                workoutRepository.workouts.collect { allWorkouts ->
                    if (allWorkouts.isEmpty()) {
                        _radarChartData.value =
                            ExerciseProgressCalculator.generateRadarChartData(emptyList())
                        _isLoadingRadarData.value = false
                        return@collect
                    }

                    Log.d("StatsVM", "📊 ${allWorkouts.size} workouts obtenidos del almacenamiento local")

                    // Agrupar por ejercicio
                    val uniqueExercises = allWorkouts.map { it.title }.distinct()
                    val progressDataList = uniqueExercises.map { name ->
                        ExerciseProgressCalculator.calculateProgressScore(allWorkouts, name)
                    }

                    _radarChartData.value =
                        ExerciseProgressCalculator.generateRadarChartData(progressDataList)
                    _isLoadingRadarData.value = false
                }
            } catch (e: Exception) {
                Log.e("StatsVM", "Error cargando radar chart local", e)
                _radarChartData.value =
                    ExerciseProgressCalculator.generateRadarChartData(emptyList())
                _isLoadingRadarData.value = false
            }
        }
    }

    private fun processRadarChartData(workoutEntries: List<com.develop.traiscore.data.local.entity.WorkoutEntry>) {
        Log.d("StatsVM", "🔄 Procesando ${workoutEntries.size} workout entries para radar")

        if (workoutEntries.isEmpty()) {
            Log.d("StatsVM", "❌ No hay workout entries, generando datos vacíos")
            _radarChartData.value = ExerciseProgressCalculator.generateRadarChartData(emptyList())
            return
        }

        // ✅ LOGGING DETALLADO: Mostrar algunos ejemplos de workout entries
        workoutEntries.take(3).forEach { entry ->
            Log.d("StatsVM", "📄 Sample entry: ${entry.title} - ${entry.weight}kg x ${entry.reps} x ${entry.series} = ${entry.weight * entry.reps * entry.series}kg volumen")
        }

        // Obtener lista única de ejercicios
        val uniqueExercises = workoutEntries.map { it.title }.distinct()
        Log.d("StatsVM", "🏋️ Ejercicios únicos encontrados: $uniqueExercises")

        // Calcular progreso para cada ejercicio
        val progressDataList = uniqueExercises.map { exerciseName ->
            val exerciseEntries = workoutEntries.filter { it.title == exerciseName }
            Log.d("StatsVM", "📊 $exerciseName: ${exerciseEntries.size} entradas")

            ExerciseProgressCalculator.calculateProgressScore(workoutEntries, exerciseName)
        }

        // Log de resultados para debugging
        progressDataList.forEach { progress ->
            Log.d("StatsVM", "📈 ${progress.exerciseName}: ${progress.getFormattedProgressScore()} " +
                    "(Vol: ${progress.getFormattedVolume()}, Max: ${progress.getFormattedMaxWeight()}, " +
                    "Workouts: ${progress.workoutCount}, Consistency: ${"%.1f".format(progress.consistencyScore)})")
        }

        // Generar datos para el radar chart
        val radarData = ExerciseProgressCalculator.generateRadarChartData(progressDataList)
        _radarChartData.value = radarData

        Log.d("StatsVM", "✅ Radar chart data generado:")
        Log.d("StatsVM", "   Categories: ${radarData.categories}")
        Log.d("StatsVM", "   Data points: ${radarData.dataPoints}")
        Log.d("StatsVM", "   Top exercises: ${radarData.topExercises.map { "${it.exerciseName}:${it.progressScore}" }}")
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
            loadRadarChartData()
            calculateCurrentMonthTrainingDays()
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
        viewModelScope.launch {
            workoutRepository.workouts.collect { allWorkouts ->
                val entries = allWorkouts.filter { it.title == exerciseName }
                Log.d("StatsVM", "📈 ${entries.size} registros locales para $exerciseName")
                processLocalWorkoutData(entries)
            }
        }
    }
    private fun processLocalWorkoutData(entries: List<WorkoutEntry>) {
        if (entries.isEmpty()) {
            _weightProgress.value = emptyList()
            _repsProgress.value = emptyList()
            _rirProgress.value = emptyList()
            _circularData.value = Triple(0f, 0, 0)
            return
        }

        val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())

        // ✅ Simplemente ordenar por timestamp y tomar los últimos 12 registros
        val sorted = entries
            .sortedBy { it.timestamp }  // Orden cronológico (más antiguo primero)
            .takeLast(12)                // Últimos 12 registros

        // Mapear a las listas de gráficas
        val wData = sorted.map { sdf.format(it.timestamp) to it.weight }
        val rData = sorted.map { sdf.format(it.timestamp) to it.reps.toFloat() }
        val rirData = sorted.map { sdf.format(it.timestamp) to (it.rir ?: 0).toFloat() }
        val allRir = sorted.mapNotNull { it.rir }

        _weightProgress.value = wData
        _repsProgress.value = rData
        _rirProgress.value = rirData

        // Calcular datos circulares (máximos y promedio)
        val oneRm = wData.maxOfOrNull { it.second } ?: 0f
        val maxRp = rData.maxOfOrNull { it.second }?.toInt() ?: 0
        val avgRir = if (allRir.isNotEmpty()) allRir.average().roundToInt() else 0

        _circularData.value = Triple(oneRm, maxRp, avgRir)

        Log.d("StatsVM", "📊 Procesados ${sorted.size} registros del ejercicio")
        Log.d("StatsVM", "📈 Weight data: $wData")
        Log.d("StatsVM", "📊 Reps data: $rData")
        Log.d("StatsVM", "🎯 RIR data: $rirData")
    }


    private fun processWorkoutData(documents: List<DocumentSnapshot>) {
        val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
        val wData = mutableListOf<Pair<String, Float>>()
        val rData = mutableListOf<Pair<String, Float>>()
        val rirData = mutableListOf<Pair<String, Float>>() // ✅ NUEVA LISTA PARA RIR
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
            rirData.add(label to rir.toFloat()) // ✅ AGREGAR DATOS DE RIR
            allRir.add(rir)
        }

        Log.d("StatsVM", "📈 Weight data ordenado: $wData")
        Log.d("StatsVM", "📊 Reps data ordenado: $rData")
        Log.d("StatsVM", "🎯 RIR data ordenado: $rirData") // ✅ LOG PARA RIR

        _weightProgress.value = wData
        _repsProgress.value = rData
        _rirProgress.value = rirData // ✅ ASIGNAR DATOS DE RIR

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
        viewModelScope.launch {
            workoutRepository.workouts.collect { allWorkouts ->
                val total = allWorkouts
                    .filter { it.title == exerciseName }
                    .sumOf { it.weight.toDouble() }
                    .toFloat()
                _totalWeightSum.value = total
                Log.d("StatsVM", "✅ Peso total local calculado: $total kg para $exerciseName")
            }
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