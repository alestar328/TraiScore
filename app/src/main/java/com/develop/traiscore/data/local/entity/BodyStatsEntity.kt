package com.develop.traiscore.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "body_stats",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["createdAt"]),
        Index(value = ["firebaseId"], unique = true)
    ]
)
data class BodyStatsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Identificación
    val firebaseId: String = "",  // ID del documento en Firebase
    val userId: String,

    // Datos principales
    val gender: String = "Male",

    // Medidas (siguiendo el mismo formato que UserMeasurements)
    val height: Double = 0.0,      // cm
    val weight: Double = 0.0,      // kg
    val neck: Double = 0.0,        // cm
    val chest: Double = 0.0,       // cm
    val arms: Double = 0.0,        // cm
    val waist: Double = 0.0,       // cm
    val thigh: Double = 0.0,       // cm
    val calf: Double = 0.0,        // cm

    // Control de sincronización (siguiendo el patrón de ExerciseEntity)
    val isSynced: Boolean = false,
    val pendingAction: String? = null,  // null, "CREATE", "UPDATE", "DELETE"

    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Convierte a UserMeasurements para compatibilidad con código existente
     */
    fun toUserMeasurements(): UserMeasurements {
        return UserMeasurements(
            height = height,
            weight = weight,
            neck = neck,
            chest = chest,
            arms = arms,
            waist = waist,
            thigh = thigh,
            calf = calf,
            lastUpdated = null // Se maneja con createdAt/updatedAt
        )
    }

    /**
     * Convierte al formato Map<String, String> usado en BodyStatsViewModel
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
         * Crea BodyStatsEntity desde el Map<String, String> usado en ViewModel
         */
        fun fromMeasurementsMap(
            userId: String,
            gender: String,
            measurements: Map<String, String>,
            firebaseId: String = "",
            isSynced: Boolean = false,
            pendingAction: String? = null
        ): BodyStatsEntity {
            return BodyStatsEntity(
                firebaseId = firebaseId,
                userId = userId,
                gender = gender,
                height = measurements["Height"]?.toDoubleOrNull() ?: 0.0,
                weight = measurements["Weight"]?.toDoubleOrNull() ?: 0.0,
                neck = measurements["Neck"]?.toDoubleOrNull() ?: 0.0,
                chest = measurements["Chest"]?.toDoubleOrNull() ?: 0.0,
                arms = measurements["Arms"]?.toDoubleOrNull() ?: 0.0,
                waist = measurements["Waist"]?.toDoubleOrNull() ?: 0.0,
                thigh = measurements["Thigh"]?.toDoubleOrNull() ?: 0.0,
                calf = measurements["Calf"]?.toDoubleOrNull() ?: 0.0,
                isSynced = isSynced,
                pendingAction = pendingAction,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}