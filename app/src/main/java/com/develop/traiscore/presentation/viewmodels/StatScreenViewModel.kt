package com.develop.traiscore.presentation.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.develop.traiscore.R
import com.develop.traiscore.core.TimeRange
import com.develop.traiscore.data.local.entity.WorkoutEntry
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
class StatScreenViewModel @Inject constructor(private val context: Context) : ViewModel() {
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
    fun calculateSocialShareData(callback: (SocialShareData?) -> Unit) {
        val userId = getTargetUserId()
        if (userId == null) {
            Log.e("StatsVM", "Usuario no identificado")
            callback(null)
            return
        }
        val today = java.time.LocalDate.now()
        val calendar = java.util.Calendar.getInstance()
        calendar.set(today.year, today.monthValue - 1, today.dayOfMonth)

        // Inicio del día
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val startOfDay = calendar.time

        // Final del día
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
        calendar.set(java.util.Calendar.MINUTE, 59)
        calendar.set(java.util.Calendar.SECOND, 59)
        calendar.set(java.util.Calendar.MILLISECOND, 999)
        val endOfDay = calendar.time

        Log.d("StatsVM", "🎯 Calculando datos sociales para: $userId")

        // UNA SOLA CONSULTA para datos del día
        db.collection("users")
            .document(userId)
            .collection("workoutEntries")
            .whereGreaterThanOrEqualTo("timestamp", startOfDay)
            .whereLessThanOrEqualTo("timestamp", endOfDay)
            .get()
            .addOnSuccessListener { todaySnapshot ->
                Log.d("StatsVM", "📊 Entrenamientos de hoy: ${todaySnapshot.size()}")

                if (todaySnapshot.documents.isEmpty()) {
                    val defaultNoWorkouts = context.getString(R.string.filter_no_main_exercise)
                    val defaultNoRecords = context.getString(R.string.filter_no_max_reps)
                    Log.d("StatsVM", "❌ No hay entrenamientos hoy")
                    callback(
                        SocialShareData(
                            topExercise = defaultNoWorkouts, // ✅ USAR directamente el string localizado
                            maxRepsExercise = defaultNoRecords,
                            topWeight = 0f,
                            maxReps = 0,
                            totalWeight = 0.0,
                            trainingDays = _currentMonthTrainingDays.value
                        )
                    )
                    return@addOnSuccessListener
                }

                // Procesar datos del día
                var topExercise = ""
                var topWeight = 0f
                var maxReps = 0
                var totalWeight = 0.0
                var maxRepsExercise = ""


                todaySnapshot.documents.forEach { doc ->
                    val weight = doc.getDouble("weight")?.toFloat() ?: 0f
                    val reps = doc.getLong("reps")?.toInt() ?: 0
                    val exerciseName = doc.getString("title") ?: "Ejercicio"

                    // Ejercicio con mayor peso
                    if (weight > topWeight) {
                        topWeight = weight
                        topExercise = exerciseName
                    }

                    // Máximo de repeticiones
                    if (reps > maxReps) {
                        maxReps = reps
                        maxRepsExercise = exerciseName
                    }

                    // Peso total (igual que TodayViewScreen)
                    totalWeight += weight
                }

                Log.d("StatsVM", "✅ Datos calculados:")
                Log.d("StatsVM", "   Top exercise: $topExercise ($topWeight kg)")
                Log.d("StatsVM", "   Max reps: $maxReps")
                Log.d("StatsVM", "   Total weight: $totalWeight kg")
                Log.d("StatsVM", "   Training days this month: ${_currentMonthTrainingDays.value}")

                callback(
                    SocialShareData(
                        topExercise = topExercise,
                        topWeight = topWeight,
                        maxRepsExercise = maxRepsExercise,
                        maxReps = maxReps,
                        totalWeight = totalWeight,
                        trainingDays = _currentMonthTrainingDays.value
                    )
                )
            }
            .addOnFailureListener { e ->
                Log.e("StatsVM", "Error calculando datos sociales", e)
                callback(null)
            }
    }
    private fun calculateCurrentMonthTrainingDays() {
        val userId = getTargetUserId()
        if (userId == null) {
            Log.e("StatsVM", "Usuario no identificado, no puedo calcular días de entrenamiento")
            return
        }

        // Obtener primer y último día del mes actual
        val today = java.time.LocalDate.now()
        val firstDayOfMonth = today.withDayOfMonth(1)
        val lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth())

        // Convertir a Date para Firebase
        val calendar = java.util.Calendar.getInstance()

        // Inicio del mes (00:00:00)
        calendar.set(firstDayOfMonth.year, firstDayOfMonth.monthValue - 1, firstDayOfMonth.dayOfMonth, 0, 0, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.time

        // Final del mes (23:59:59)
        calendar.set(lastDayOfMonth.year, lastDayOfMonth.monthValue - 1, lastDayOfMonth.dayOfMonth, 23, 59, 59)
        calendar.set(java.util.Calendar.MILLISECOND, 999)
        val endOfMonth = calendar.time

        Log.d("StatsVM", "📅 Calculando días de entrenamiento del mes - Usuario: $userId")
        Log.d("StatsVM", "🗓️ Rango: $startOfMonth a $endOfMonth")

        db.collection("users")
            .document(userId)
            .collection("workoutEntries")
            .whereGreaterThanOrEqualTo("timestamp", startOfMonth)
            .whereLessThanOrEqualTo("timestamp", endOfMonth)
            .get()
            .addOnSuccessListener { snap ->
                // ✅ USAR LA MISMA LÓGICA QUE YearViewScreen: contar días únicos
                val workoutDates = snap.documents.mapNotNull { doc ->
                    val timestamp = doc.getDate("timestamp")
                    timestamp?.let { date ->
                        val calendar = java.util.Calendar.getInstance()
                        calendar.time = date

                        // Crear LocalDate para comparación (solo año, mes, día - sin hora)
                        java.time.LocalDate.of(
                            calendar.get(java.util.Calendar.YEAR),
                            calendar.get(java.util.Calendar.MONTH) + 1,
                            calendar.get(java.util.Calendar.DAY_OF_MONTH)
                        )
                    }
                }.toSet() // ✅ toSet() elimina duplicados - días únicos

                val trainingDaysCount = workoutDates.size
                _currentMonthTrainingDays.value = trainingDaysCount

                Log.d("StatsVM", "✅ Días de entrenamiento del mes calculados: $trainingDaysCount días únicos")
                Log.d("StatsVM", "📊 Fechas de entrenamiento: $workoutDates")
            }
            .addOnFailureListener { e ->
                Log.e("StatsVM", "Error calculando días de entrenamiento del mes", e)
                _currentMonthTrainingDays.value = 0
            }
    }


    fun loadRadarChartData() {
        val userId = getTargetUserId()
        if (userId == null) {
            Log.e("StatsVM", "Usuario no identificado para radar chart")
            return
        }

        _isLoadingRadarData.value = true
        Log.d("StatsVM", "🎯 Cargando datos para radar chart - usuario: $userId")

        viewModelScope.launch {
            try {
                db.collection("users")
                    .document(userId)
                    .collection("workoutEntries")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        Log.d("StatsVM", "📊 Documentos para radar: ${snapshot.size()}")

                        // Convertir documentos a WorkoutEntry
                        val workoutEntries = snapshot.documents.mapNotNull { doc ->
                            try {
                                val series = doc.getLong("series")?.toInt() ?: 1 // ✅ Leer series de Firebase
                                Log.d("StatsVM", "📄 Doc: ${doc.getString("title")} - series from Firebase: $series") // ✅ Log series

                                WorkoutEntry(
                                    id = 0,
                                    uid = doc.id,
                                    exerciseId = 0,
                                    title = doc.getString("title") ?: return@mapNotNull null,
                                    weight = doc.getDouble("weight")?.toFloat() ?: 0f,
                                    series = series, // ✅ Usar valor real en lugar de hardcodear 1
                                    reps = doc.getLong("reps")?.toInt() ?: 0,
                                    rir = doc.getLong("rir")?.toInt() ?: 0,
                                    type = "",
                                    timestamp = doc.getDate("timestamp") ?: return@mapNotNull null
                                )
                            } catch (e: Exception) {
                                Log.w("StatsVM", "Error procesando workout entry: ${doc.id}", e)
                                null
                            }
                        }

                        processRadarChartData(workoutEntries)
                        _isLoadingRadarData.value = false
                    }
                    .addOnFailureListener { exception ->
                        Log.e("StatsVM", "Error cargando datos para radar chart", exception)
                        _isLoadingRadarData.value = false
                        _radarChartData.value = null
                    }
            } catch (exception: Exception) {
                Log.e("StatsVM", "Error inesperado en radar chart", exception)
                _isLoadingRadarData.value = false
                _radarChartData.value = null
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