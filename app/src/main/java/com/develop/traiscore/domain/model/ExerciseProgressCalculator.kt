package com.develop.traiscore.domain.model

import com.develop.traiscore.data.local.entity.WorkoutEntry

data class ExerciseProgressData(
    val exerciseName: String,
    val progressScore: Float, // Puntuación de 0-100
    val totalVolume: Float, // Peso total levantado
    val maxWeight: Float, // Peso máximo
    val maxReps: Int, // Repeticiones máximas
    val workoutCount: Int, // Número de entrenamientos
    val averageRIR: Float, // RIR promedio
    val consistencyScore: Float, // Qué tan consistente es el entrenamiento
    val improvementRate: Float // Tasa de mejora a lo largo del tiempo
) {
    fun getFormattedProgressScore(): String = "${progressScore.toInt()}%"
    fun getFormattedVolume(): String = "%.1f kg".format(totalVolume)
    fun getFormattedMaxWeight(): String = "%.1f kg".format(maxWeight)
}

data class RadarChartData(
    val topExercises: List<ExerciseProgressData>,
    val categories: List<String>,
    val dataPoints: List<Int>, // Valores de 0-100 para el radar
    val maxValue: Int = 100
)

object ExerciseProgressCalculator {

    /**
     * Calcula el progreso usando una fórmula híbrida que considera:
     * - Volumen total (peso × reps × series) - 30%
     * - Peso máximo alcanzado - 25%
     * - Consistencia en entrenamientos - 20%
     * - Tasa de mejora temporal - 15%
     * - Eficiencia (RIR bajo = mejor técnica) - 10%
     */
    fun calculateProgressScore(
        workoutEntries: List<com.develop.traiscore.data.local.entity.WorkoutEntry>,
        exerciseName: String
    ): ExerciseProgressData {

        if (workoutEntries.isEmpty()) {
            return createEmptyProgressData(exerciseName)
        }

        // Filtrar y ordenar por fecha
        val exerciseEntries = workoutEntries
            .filter { it.title == exerciseName }
            .sortedBy { it.timestamp }

        if (exerciseEntries.isEmpty()) {
            return createEmptyProgressData(exerciseName)
        }

        // Calcular métricas base
        val totalVolume = calculateTotalVolume(exerciseEntries)
        val maxWeight = exerciseEntries.maxOfOrNull { it.weight } ?: 0f
        val maxReps = exerciseEntries.maxOfOrNull { it.reps } ?: 0
        val workoutCount = exerciseEntries.size

        // ✅ CORRECCIÓN: Manejar RIR nullable y convertir a Double antes de average()
        val averageRIR = exerciseEntries
            .mapNotNull { it.rir } // Filtrar nulls
            .map { it.toDouble() } // Convertir a Double
            .takeIf { it.isNotEmpty() } // Solo si hay elementos
            ?.average()?.toFloat() ?: 0f // Calcular promedio y convertir a Float

        // Calcular puntuaciones componentes
        val volumeScore = calculateVolumeScore(totalVolume)
        val strengthScore = calculateStrengthScore(maxWeight)
        val consistencyScore = calculateConsistencyScore(exerciseEntries)
        val improvementScore = calculateImprovementScore(exerciseEntries)
        val efficiencyScore = calculateEfficiencyScore(averageRIR)

        // Fórmula híbrida ponderada
        val progressScore = (
                volumeScore * 0.30f +
                        strengthScore * 0.25f +
                        consistencyScore * 0.20f +
                        improvementScore * 0.15f +
                        efficiencyScore * 0.10f
                ).coerceIn(0f, 100f)

        return ExerciseProgressData(
            exerciseName = exerciseName,
            progressScore = progressScore,
            totalVolume = totalVolume,
            maxWeight = maxWeight,
            maxReps = maxReps,
            workoutCount = workoutCount,
            averageRIR = averageRIR,
            consistencyScore = consistencyScore,
            improvementRate = improvementScore
        )
    }

    private fun calculateTotalVolume(entries: List<WorkoutEntry>): Float {
        return entries.sumOf { (it.weight * it.reps).toDouble() }.toFloat()
    }

    private fun calculateVolumeScore(totalVolume: Float): Float {
        // Escala logarítmica para volumen (más realista)
        // 1000kg = 20 puntos, 5000kg = 50 puntos, 15000kg = 80 puntos, 30000kg = 100 puntos
        return when {
            totalVolume <= 0f -> 0f
            totalVolume < 1000f -> (totalVolume / 1000f) * 20f
            totalVolume < 5000f -> 20f + ((totalVolume - 1000f) / 4000f) * 30f
            totalVolume < 15000f -> 50f + ((totalVolume - 5000f) / 10000f) * 30f
            totalVolume < 30000f -> 80f + ((totalVolume - 15000f) / 15000f) * 20f
            else -> 100f
        }.coerceIn(0f, 100f)
    }

    private fun calculateStrengthScore(maxWeight: Float): Float {
        // Escala para peso máximo
        // 20kg = 10 puntos, 60kg = 40 puntos, 100kg = 70 puntos, 150kg+ = 100 puntos
        return when {
            maxWeight <= 0f -> 0f
            maxWeight < 20f -> (maxWeight / 20f) * 10f
            maxWeight < 60f -> 10f + ((maxWeight - 20f) / 40f) * 30f
            maxWeight < 100f -> 40f + ((maxWeight - 60f) / 40f) * 30f
            maxWeight < 150f -> 70f + ((maxWeight - 100f) / 50f) * 30f
            else -> 100f
        }.coerceIn(0f, 100f)
    }

    private fun calculateConsistencyScore(entries: List<WorkoutEntry>): Float {
        if (entries.size < 2) return 0f

        // Calcular la distribución temporal de entrenamientos
        val timeSpanDays = (entries.last().timestamp.time - entries.first().timestamp.time) / (1000 * 60 * 60 * 24)
        val idealFrequency = timeSpanDays / 7.0 * 2.5 // ~2.5 entrenamientos por semana ideal
        val actualCount = entries.size.toDouble()

        val frequencyScore = (actualCount / idealFrequency).coerceIn(0.0, 1.0)

        // Penalizar gaps largos entre entrenamientos
        val gaps = entries.zipWithNext { a, b ->
            (b.timestamp.time - a.timestamp.time) / (1000 * 60 * 60 * 24)
        }
        val averageGap = gaps.average()
        val gapPenalty = when {
            averageGap <= 7 -> 1.0 // Excelente
            averageGap <= 14 -> 0.8 // Bueno
            averageGap <= 21 -> 0.6 // Regular
            else -> 0.3 // Necesita mejorar
        }

        return ((frequencyScore * gapPenalty) * 100).toFloat().coerceIn(0f, 100f)
    }

    private fun calculateImprovementScore(entries: List<WorkoutEntry>): Float {
        if (entries.size < 3) return 0f

        // Calcular tendencia de mejora usando regresión lineal simple
        val xValues = entries.indices.map { it.toDouble() }
        val yValues = entries.map { (it.weight * it.reps).toDouble() } // Volumen por sesión

        val n = entries.size
        val sumX = xValues.sum()
        val sumY = yValues.sum()
        val sumXY = xValues.zip(yValues) { x, y -> x * y }.sum()
        val sumX2 = xValues.map { it * it }.sum()

        // Pendiente de la regresión lineal
        val slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX)

        // Normalizar la pendiente a escala 0-100
        val normalizedSlope = when {
            slope <= 0 -> 0f // Sin mejora o empeorando
            slope < 5 -> (slope / 5.0 * 30).toFloat() // Mejora lenta
            slope < 15 -> (30 + (slope - 5) / 10.0 * 40).toFloat() // Mejora moderada
            slope < 30 -> (70 + (slope - 15) / 15.0 * 25).toFloat() // Mejora rápida
            else -> 100f // Mejora excepcional
        }

        return normalizedSlope.coerceIn(0f, 100f)
    }

    private fun calculateEfficiencyScore(averageRIR: Float): Float {
        // RIR más bajo = mejor eficiencia técnica
        // RIR 0-1 = 100 puntos, RIR 2-3 = 80 puntos, RIR 4-5 = 60 puntos, RIR 6+ = 20 puntos
        return when {
            averageRIR <= 1f -> 100f
            averageRIR <= 3f -> 80f - ((averageRIR - 1f) / 2f) * 20f
            averageRIR <= 5f -> 60f - ((averageRIR - 3f) / 2f) * 20f
            averageRIR <= 8f -> 40f - ((averageRIR - 5f) / 3f) * 20f
            else -> 20f
        }.coerceIn(0f, 100f)
    }

    private fun createEmptyProgressData(exerciseName: String): ExerciseProgressData {
        return ExerciseProgressData(
            exerciseName = exerciseName,
            progressScore = 0f,
            totalVolume = 0f,
            maxWeight = 0f,
            maxReps = 0,
            workoutCount = 0,
            averageRIR = 0f,
            consistencyScore = 0f,
            improvementRate = 0f
        )
    }

    /**
     * Genera datos para el radar chart con los top 5 ejercicios
     */
    fun generateRadarChartData(allProgressData: List<ExerciseProgressData>): RadarChartData {
        val top5 = allProgressData
            .sortedByDescending { it.progressScore }
            .take(5)

        // Si tenemos menos de 5, rellenar con ejercicios vacíos
        val paddedExercises = top5.toMutableList()
        while (paddedExercises.size < 5) {
            paddedExercises.add(
                createEmptyProgressData("Ejercicio ${paddedExercises.size + 1}")
            )
        }

        return RadarChartData(
            topExercises = paddedExercises,
            categories = paddedExercises.map { it.exerciseName },
            dataPoints = paddedExercises.map { it.progressScore.toInt() },
            maxValue = 100
        )
    }
}

