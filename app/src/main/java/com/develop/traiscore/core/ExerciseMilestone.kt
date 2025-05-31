package com.develop.traiscore.core

enum class ExerciseMilestone(val text: String,  val thresholdKg: Int, val label: String) {
    SQUAT_100("Sentadilla: 100 kg", thresholdKg = 100, label = "¡Primeros 100 kg levantados!"),
    SQUAT_1000("Sentadilla: 1 000 kg", thresholdKg = 1_000, label = "¡Equivalente a un rinoceronte bebé!"),
    SQUAT_5000("Sentadilla: 5 000 kg", thresholdKg = 5_000, label = "¡Casi un elefante joven!"),
    SQUAT_10000("Sentadilla: 10 000 kg", thresholdKg = 10_000, label = "¡Has levantado un camión pequeño!"),
    DEADLIFT_100("Peso muerto: 100 kg", thresholdKg = 100, label = "¡Primeros 100 kg en peso muerto!"),
    // … y así para cada ejercicio/umbral que quieras
    ;

    companion object {
        /**
         * Dado un ejercicio (por ejemplo, "SQUAT") y un peso acumulado,
         * devuelve el máximo logro (enum) cuyo threshold ≤ totalKg.
         */
        fun bestMilestoneFor(exercise: String, accumulatedKg: Int): ExerciseMilestone? {
            return values()
                .filter { it.name.startsWith(exercise.uppercase()) }
                .filter { it.thresholdKg <= accumulatedKg }
                .maxByOrNull { it.thresholdKg }
        }
    }
}