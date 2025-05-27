package com.develop.traiscore.data.local.entity

import com.develop.traiscore.core.Gender
import com.develop.traiscore.core.UserRole
import com.google.firebase.Timestamp

data class UserEntity(
    val uid: String,
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val birthYear: Int? = null,
    val gender: Gender? = null,
    val userRole: UserRole,
    val photoURL: String? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val isActive: Boolean = true
) {
    /**
     * Obtiene el nombre completo del usuario
     */
    fun getFullName(): String {
        return "$firstName $lastName".trim()
    }

    /**
     * Calcula la edad aproximada del usuario
     */
    fun getApproximateAge(): Int? {
        return birthYear?.let {
            java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) - it
        }
    }

    /**
     * Verifica si es un cliente
     */
    fun isClient(): Boolean = userRole == UserRole.CLIENT

    /**
     * Verifica si es un trainer
     */
    fun isTrainer(): Boolean = userRole == UserRole.TRAINER
}
