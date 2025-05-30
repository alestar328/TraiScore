package com.develop.traiscore.core

enum class UserRole {
    CLIENT,     // Usuario regular que usa la app
    TRAINER    // Entrenador que puede tener clientes
}

enum class Gender(val displayName: String, val value: String) {
    MALE("Masculino", "MALE"),
    FEMALE("Femenino", "FEMALE"),
    OTHER("Otro", "OTHER");

    companion object {
        fun fromValue(value: String?): Gender? {
            return values().find { it.value.equals(value, ignoreCase = true) }
        }
    }
}