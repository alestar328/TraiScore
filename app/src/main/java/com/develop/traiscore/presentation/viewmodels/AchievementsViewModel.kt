package com.develop.traiscore.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.develop.traiscore.core.WeightEquivalences
import com.develop.traiscore.domain.model.AchievementsData
import com.develop.traiscore.domain.model.ExerciseAchievement
import com.develop.traiscore.domain.model.OverallAchievement
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AchievementsViewModel @Inject constructor() : ViewModel() {

    private val db = Firebase.firestore

    private val _achievementsData = MutableStateFlow<AchievementsData?>(null)
    val achievementsData: StateFlow<AchievementsData?> = _achievementsData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // NUEVO: Estado para el cliente que se está visualizando
    private val _targetUserId = MutableStateFlow<String?>(null)
    val targetUserId: StateFlow<String?> = _targetUserId

    init {
        // Inicializar con el usuario actual
        _targetUserId.value = FirebaseAuth.getInstance().currentUser?.uid
        loadAchievements()
    }

    /**
     * Establecer el ID del usuario del cual queremos ver logros
     * @param userId ID del cliente o null para usar el usuario actual
     */
    fun setTargetUser(userId: String?) {
        _targetUserId.value = userId ?: FirebaseAuth.getInstance().currentUser?.uid
        loadAchievements()
    }

    /**
     * Obtener el ID del usuario objetivo (cliente o usuario actual)
     */
    private fun getTargetUserId(): String? {
        return _targetUserId.value ?: FirebaseAuth.getInstance().currentUser?.uid
    }

    fun loadAchievements() {
        val userId = getTargetUserId()
        if (userId == null) {
            Log.e("AchievementsVM", "Usuario no identificado")
            _errorMessage.value = "Usuario no identificado"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        Log.d("AchievementsVM", "Cargando logros para usuario: $userId")

        viewModelScope.launch {
            try {
                db.collection("users")
                    .document(userId)
                    .collection("workoutEntries")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        Log.d("AchievementsVM", "Documentos encontrados: ${snapshot.size()}")
                        processWorkoutData(snapshot.documents.mapNotNull { doc ->
                            try {
                                Triple(
                                    doc.getString("title") ?: return@mapNotNull null,
                                    doc.getDouble("weight")?.toFloat() ?: 0f,
                                    doc.getDate("timestamp")
                                )
                            } catch (e: Exception) {
                                Log.w("AchievementsVM", "Error procesando documento: ${doc.id}", e)
                                null
                            }
                        })
                        _isLoading.value = false
                    }
                    .addOnFailureListener { exception ->
                        Log.e("AchievementsVM", "Error cargando datos", exception)
                        _errorMessage.value = "Error cargando datos: ${exception.message}"
                        _isLoading.value = false
                    }
            } catch (exception: Exception) {
                Log.e("AchievementsVM", "Error inesperado", exception)
                _errorMessage.value = "Error inesperado: ${exception.message}"
                _isLoading.value = false
            }
        }
    }

    private fun processWorkoutData(workoutData: List<Triple<String, Float, java.util.Date?>>) {
        Log.d("AchievementsVM", "Procesando ${workoutData.size} entrenamientos")

        if (workoutData.isEmpty()) {
            Log.d("AchievementsVM", "No hay datos de entrenamientos")
            _achievementsData.value = AchievementsData(
                topExercises = emptyList(),
                overallAchievement = createEmptyOverallAchievement()
            )
            return
        }

        // Agrupar por ejercicio y sumar pesos
        val exerciseStats = workoutData.groupBy { it.first }
            .mapValues { (_, workouts) ->
                val totalWeight = workouts.sumOf { it.second.toDouble() }.toFloat()
                val workoutCount = workouts.size
                val lastDate = workouts.mapNotNull { it.third }.maxOrNull()
                Triple(totalWeight, workoutCount, lastDate)
            }

        Log.d("AchievementsVM", "Estadísticas por ejercicio: $exerciseStats")

        // Crear lista de logros por ejercicio
        val exerciseAchievements = exerciseStats.map { (exerciseName, stats) ->
            val (totalWeight, workoutCount, lastDate) = stats
            val equivalence = WeightEquivalences.getBestEquivalence(totalWeight)
            val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            ExerciseAchievement(
                exerciseName = exerciseName,
                totalWeightKg = totalWeight,
                workoutCount = workoutCount,
                lastWorkoutDate = lastDate?.let { dateFormatter.format(it) },
                equivalence = equivalence
            )
        }.sortedByDescending { it.totalWeightKg }
            .take(10) // Top 10

        // Calcular logro general
        val totalWeight = exerciseAchievements.sumOf { it.totalWeightKg.toDouble() }.toFloat()
        val totalWorkouts = exerciseAchievements.sumOf { it.workoutCount }
        val differentExercises = exerciseAchievements.size

        val overallEquivalence = WeightEquivalences.getBestEquivalence(totalWeight)
        val nextGoal = WeightEquivalences.getNextGoal(totalWeight)
        val progressToNext = WeightEquivalences.getProgressToNext(totalWeight)

        val overallAchievement = OverallAchievement(
            totalWeightLifted = totalWeight,
            totalWorkouts = totalWorkouts,
            differentExercises = differentExercises,
            equivalence = overallEquivalence,
            nextGoal = nextGoal,
            progressToNext = progressToNext
        )

        Log.d("AchievementsVM", "Logro general: peso total = $totalWeight kg, equivalencia = ${overallEquivalence.name}")

        _achievementsData.value = AchievementsData(
            topExercises = exerciseAchievements,
            overallAchievement = overallAchievement
        )
    }

    private fun createEmptyOverallAchievement(): OverallAchievement {
        return OverallAchievement(
            totalWeightLifted = 0f,
            totalWorkouts = 0,
            differentExercises = 0,
            equivalence = WeightEquivalences.getBestEquivalence(0f),
            nextGoal = WeightEquivalences.getNextGoal(0f),
            progressToNext = 0f
        )
    }

    fun clearError() {
        _errorMessage.value = null
    }
}