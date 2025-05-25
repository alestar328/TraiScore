package com.develop.traiscore.domain.model

enum class BodyMeasurementType(val displayName: String, val unit: String, val key: String) {
    HEIGHT("Altura", "cm", "Height"),
    WEIGHT("Peso", "kg", "Weight"),
    NECK("Cuello", "cm", "Neck"),
    CHEST("Pecho", "cm", "Chest"),
    ARMS("Brazos", "cm", "Arms"),
    WAIST("Cintura", "cm", "Waist"),
    THIGH("Muslo", "cm", "Thigh"),
    CALF("Pantorrilla", "cm", "Calf");

    companion object {
        fun fromKey(key: String): BodyMeasurementType? {
            return values().find { it.key == key }
        }

        fun getAllKeys(): List<String> {
            return values().map { it.key }
        }
    }
}