package com.develop.traiscore.domain.model

import androidx.annotation.StringRes
import com.develop.traiscore.R

enum class MeasurementType(
    val key: String,
    @StringRes val displayNameRes: Int,
    @StringRes val unitRes: Int
) {
    HEIGHT("Height", R.string.measurement_height, R.string.unit_cm),
    WEIGHT("Weight", R.string.measurement_weight, R.string.unit_kg),
    NECK("Neck", R.string.measurement_neck, R.string.unit_cm),
    CHEST("Chest", R.string.measurement_chest, R.string.unit_cm),
    ARMS("Arms", R.string.measurement_arms, R.string.unit_cm),
    WAIST("Waist", R.string.measurement_waist, R.string.unit_cm),
    THIGH("Thigh", R.string.measurement_thigh, R.string.unit_cm),
    HIP("Hip", R.string.measurement_hip, R.string.unit_cm),
    CALF("Calf", R.string.measurement_calf, R.string.unit_cm);


    companion object {
        fun getAllMeasurements(): List<MeasurementType> = values().toList()

        fun fromKey(key: String): MeasurementType? = values().find { it.key == key }

        fun getDefaultMeasurements(): Map<String, String> =
            values().associate { it.key to "" }
    }
}