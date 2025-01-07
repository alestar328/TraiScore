package com.develop.traiscore.domain.model

enum class TimeFilter (val displayName: String) {
    TODAY("Hoy"),
    THIS_WEEK("Semana"),
    THIS_MONTH("Mes"),
    THIS_YEAR("Año");

    companion object {
        // Para convertir un string a un enum
        fun fromDisplayName(name: String): TimeFilter? {
            return entries.find { it.displayName == name }
        }
    }
}