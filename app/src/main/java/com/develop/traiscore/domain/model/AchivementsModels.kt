package com.develop.traiscore.domain.model

import com.develop.traiscore.core.WeightEquivalence

data class ExerciseAchievement(
    val exerciseName: String,
    val totalWeightKg: Float,
    val workoutCount: Int, // Número de entrenamientos registrados
    val lastWorkoutDate: String? = null,
    val equivalence: WeightEquivalence
) {
    fun getFormattedWeight(): String = "%.1f kg".format(totalWeightKg)

    fun getWorkoutCountText(): String = when (workoutCount) {
        1 -> "1 entrenamiento"
        else -> "$workoutCount entrenamientos"
    }
}

data class OverallAchievement(
    val totalWeightLifted: Float,
    val totalWorkouts: Int,
    val differentExercises: Int,
    val equivalence: WeightEquivalence,
    val nextGoal: WeightEquivalence?,
    val progressToNext: Float
) {
    fun getFormattedTotalWeight(): String = "%.1f kg".format(totalWeightLifted)

    fun getTotalWorkoutsText(): String = when (totalWorkouts) {
        1 -> "1 entrenamiento total"
        else -> "$totalWorkouts entrenamientos totales"
    }

    fun getDifferentExercisesText(): String = when (differentExercises) {
        1 -> "1 ejercicio dominado"
        else -> "$differentExercises ejercicios dominados"
    }

    fun getProgressPercentage(): Int = (progressToNext * 100).toInt()

    fun getRemainingWeight(): Float = nextGoal?.let { it.weightKg - totalWeightLifted } ?: 0f

    fun getFormattedRemainingWeight(): String = "%.1f kg".format(getRemainingWeight())
}

data class AchievementsData(
    val topExercises: List<ExerciseAchievement>,
    val overallAchievement: OverallAchievement
)