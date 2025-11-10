package com.develop.traiscore.domain.model

import com.develop.traiscore.data.local.entity.WorkoutEntry
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

data class ExerciseProgressData(
    val exerciseName: String,
    val progressScore: Float,
    val totalVolume: Float,
    val maxWeight: Float,
    val maxReps: Int,
    val workoutCount: Int,
    val averageRIR: Float,
    val consistencyScore: Float,
    val improvementRate: Float
) {
    fun getFormattedProgressScore(): String = "${progressScore.toInt()}%"
    fun getFormattedVolume(): String = "%.1f kg".format(totalVolume)
    fun getFormattedMaxWeight(): String = "%.1f kg".format(maxWeight)
}

data class RadarChartData(
    val topExercises: List<ExerciseProgressData>,
    val categories: List<String>,
    val dataPoints: List<Int>,
    val maxValue: Int = 100
)

object ExerciseProgressCalculator {

    /**
     * Calcula el progreso con ponderaciones realistas y suavizadas:
     * - Volumen normalizado semanal (25%)
     * - Intensidad relativa (25%)
     * - Consistencia temporal (20%)
     * - Tasa de mejora (20%)
     * - Eficiencia técnica (RIR óptimo) (10%)
     */
    fun calculateProgressScore(
        allEntries: List<WorkoutEntry>,
        exerciseName: String
    ): ExerciseProgressData {
        val entries = allEntries
            .filter { it.title == exerciseName }
            .sortedBy { it.timestamp }

        if (entries.isEmpty()) return createEmptyProgressData(exerciseName)

        val totalVolume = calculateTotalVolume(entries)
        val normalizedVolume = normalizeVolumePerWeek(entries, totalVolume)

        val maxWeight = entries.maxOfOrNull { it.weight } ?: 0f
        val maxReps = entries.maxOfOrNull { it.reps } ?: 0
        val workoutCount = entries.size
        val avgRIR = entries.mapNotNull { it.rir }.average().toFloat()

        val volumeScore = calculateVolumeScore(normalizedVolume)
        val strengthScore = calculateStrengthScore(entries)
        val consistencyScore = calculateConsistencyScore(entries)
        val improvementScore = calculateImprovementScore(entries)
        val efficiencyScore = calculateEfficiencyScore(avgRIR)

        val progressScore = (
                volumeScore * 0.25f +
                        strengthScore * 0.25f +
                        consistencyScore * 0.20f +
                        improvementScore * 0.20f +
                        efficiencyScore * 0.10f
                ).coerceIn(0f, 100f)

        return ExerciseProgressData(
            exerciseName = exerciseName,
            progressScore = progressScore,
            totalVolume = totalVolume,
            maxWeight = maxWeight,
            maxReps = maxReps,
            workoutCount = workoutCount,
            averageRIR = avgRIR,
            consistencyScore = consistencyScore,
            improvementRate = improvementScore
        )
    }

    /** 🔹 Volumen total = peso × reps × series */
    private fun calculateTotalVolume(entries: List<WorkoutEntry>): Float =
        entries.sumOf { (it.weight * it.reps * max(it.series, 1)).toDouble() }.toFloat()

    /** 🔹 Volumen semanal promedio: más estable para evaluar progreso real */
    private fun normalizeVolumePerWeek(entries: List<WorkoutEntry>, totalVolume: Float): Float {
        val days = (entries.last().timestamp.time - entries.first().timestamp.time) / (1000 * 60 * 60 * 24)
        val weeks = max(days / 7f, 1f)
        return totalVolume / weeks
    }

    /** 🔹 Escala logarítmica para volumen semanal */
    private fun calculateVolumeScore(volume: Float): Float = when {
        volume <= 0 -> 0f
        volume < 1000f -> (volume / 1000f) * 25f
        volume < 5000f -> 25f + ((volume - 1000f) / 4000f) * 30f
        volume < 15000f -> 55f + ((volume - 5000f) / 10000f) * 30f
        volume < 30000f -> 85f + ((volume - 15000f) / 15000f) * 15f
        else -> 100f
    }

    /** 🔹 Intensidad = top 5% pesos promedio ponderado por RIR */
    private fun calculateStrengthScore(entries: List<WorkoutEntry>): Float {
        if (entries.isEmpty()) return 0f
        val sorted = entries.sortedByDescending { it.weight }
        val top5 = sorted.take(max(1, (entries.size * 0.05).toInt()))
        val weighted = top5.map {
            it.weight * (1 - ((it.rir ?: 0) / 10f))
        }.average().toFloat()

        return when {
            weighted < 20f -> (weighted / 20f) * 10f
            weighted < 60f -> 10f + ((weighted - 20f) / 40f) * 30f
            weighted < 100f -> 40f + ((weighted - 60f) / 40f) * 30f
            weighted < 150f -> 70f + ((weighted - 100f) / 50f) * 30f
            else -> 100f
        }
    }

    /** 🔹 Consistencia = regularidad temporal y frecuencia semanal */
    private fun calculateConsistencyScore(entries: List<WorkoutEntry>): Float {
        if (entries.size < 2) return 0f

        val timestamps = entries.map { it.timestamp.time }
        val gaps = timestamps.zipWithNext { a, b -> (b - a) / (1000 * 60 * 60 * 24).toFloat() }
        val avgGap = gaps.average().toFloat()
        val stdGap = gaps.standardDeviation()

        val freqScore = (7f / avgGap).coerceIn(0f, 1f)
        val stability = (1f - (stdGap / 14f)).coerceIn(0f, 1f)

        return ((freqScore * 0.6f + stability * 0.4f) * 100f).coerceIn(0f, 100f)
    }

    /** 🔹 Tasa de mejora = % cambio suavizado entre inicio y fin */
    private fun calculateImprovementScore(entries: List<WorkoutEntry>): Float {
        if (entries.size < 3) return 0f

        val firstVol = entries.first().let { it.weight * it.reps * max(it.series, 1) }
        val lastVol = entries.last().let { it.weight * it.reps * max(it.series, 1) }

        val gain = ((lastVol - firstVol) / max(firstVol, 1f)) * 100f
        return when {
            gain <= 0f -> 0f
            gain < 10f -> gain * 3f
            gain < 25f -> 30f + (gain - 10f) * 2f
            gain < 50f -> 60f + (gain - 25f) * 1.2f
            else -> 90f + (gain / 100f).coerceAtMost(10f)
        }.coerceIn(0f, 100f)
    }

    /** 🔹 RIR óptimo: 1–3 ideal */
    private fun calculateEfficiencyScore(avgRIR: Float): Float = when {
        avgRIR in 1f..3f -> 100f
        avgRIR < 1f -> 80f
        avgRIR in 4f..5f -> 70f
        avgRIR in 6f..8f -> 50f
        else -> 30f
    }

    private fun List<Float>.standardDeviation(): Float {
        if (isEmpty()) return 0f
        val mean = average().toFloat()
        val variance = map { (it - mean).pow(2) }.average()
        return sqrt(variance).toFloat()
    }

    private fun createEmptyProgressData(name: String) = ExerciseProgressData(
        name, 0f, 0f, 0f, 0, 0, 0f, 0f, 0f
    )

    /** 🔹 Radar chart con top 5 ejercicios */
    fun generateRadarChartData(allProgressData: List<ExerciseProgressData>): RadarChartData {
        val top = allProgressData.sortedByDescending { it.progressScore }.take(5)
        val padded = top.toMutableList().apply {
            while (size < 5) add(createEmptyProgressData("Ejercicio ${size + 1}"))
        }

        return RadarChartData(
            topExercises = padded,
            categories = padded.map { it.exerciseName },
            dataPoints = padded.map { it.progressScore.toInt() },
            maxValue = 100
        )
    }
}

