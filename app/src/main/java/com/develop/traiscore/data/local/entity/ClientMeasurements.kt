package com.develop.traiscore.data.local.entity

import com.google.firebase.Timestamp

data class ClientMeasurements(
    val height: Double = 0.0,      // cm
    val weight: Double = 0.0,      // kg
    val neck: Double = 0.0,        // cm
    val chest: Double = 0.0,       // cm
    val arms: Double = 0.0,        // cm
    val waist: Double = 0.0,       // cm
    val thigh: Double = 0.0,       // cm
    val calf: Double = 0.0,        // cm
    val lastUpdated: Timestamp? = null
) {
    /**
     * Convierte las medidas al formato Map<String, String> usado en BodyStatsViewModel
     */
    fun toMeasurementsMap(): Map<String, String> {
        return mapOf(
            "Height" to if (height > 0) height.toString() else "",
            "Weight" to if (weight > 0) weight.toString() else "",
            "Neck" to if (neck > 0) neck.toString() else "",
            "Chest" to if (chest > 0) chest.toString() else "",
            "Arms" to if (arms > 0) arms.toString() else "",
            "Waist" to if (waist > 0) waist.toString() else "",
            "Thigh" to if (thigh > 0) thigh.toString() else "",
            "Calf" to if (calf > 0) calf.toString() else ""
        )
    }

    companion object {
        /**
         * Crea ClientMeasurements desde el Map<String, String> usado en BodyStatsViewModel
         */
        fun fromMeasurementsMap(measurements: Map<String, String>): ClientMeasurements {
            return ClientMeasurements(
                height = measurements["Height"]?.toDoubleOrNull() ?: 0.0,
                weight = measurements["Weight"]?.toDoubleOrNull() ?: 0.0,
                neck = measurements["Neck"]?.toDoubleOrNull() ?: 0.0,
                chest = measurements["Chest"]?.toDoubleOrNull() ?: 0.0,
                arms = measurements["Arms"]?.toDoubleOrNull() ?: 0.0,
                waist = measurements["Waist"]?.toDoubleOrNull() ?: 0.0,
                thigh = measurements["Thigh"]?.toDoubleOrNull() ?: 0.0,
                calf = measurements["Calf"]?.toDoubleOrNull() ?: 0.0
            )
        }
    }
}
