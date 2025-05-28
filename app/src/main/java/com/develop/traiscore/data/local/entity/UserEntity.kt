package com.develop.traiscore.data.local.entity

import com.develop.traiscore.core.Gender
import com.develop.traiscore.core.UserRole
import com.google.firebase.Timestamp

data class UserEntity(
    val uid: String,
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val photoURL: String? = null,
    val birthYear: Int? = null,
    val gender: Gender? = null,
    val userRole: UserRole,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val isActive: Boolean = true,
    val linkedTrainerUid: String? = null  // Nuevo: para coincidir con React

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

    fun toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "photoURL" to photoURL,
            "birthYear" to birthYear,
            "gender" to gender?.value,
            "userRole" to userRole.name,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "isActive" to isActive,
            "linkedTrainerUid" to linkedTrainerUid
        )
    }

    companion object {
        /**
         * Crea UserEntity desde datos de Firestore
         */
        fun fromFirestore(data: Map<String, Any>, uid: String): UserEntity {
            return UserEntity(
                uid = uid,
                firstName = data["firstName"] as? String ?: "",
                lastName = data["lastName"] as? String ?: "",
                email = data["email"] as? String ?: "",
                photoURL = data["photoURL"] as? String,
                birthYear = (data["birthYear"] as? Number)?.toInt(),
                gender = Gender.fromValue(data["gender"] as? String),
                userRole = UserRole.valueOf(data["userRole"] as? String ?: "CLIENT"),
                createdAt = data["createdAt"] as? Timestamp ?: Timestamp.now(),
                updatedAt = data["updatedAt"] as? Timestamp ?: Timestamp.now(),
                isActive = data["isActive"] as? Boolean ?: true,
                linkedTrainerUid = data["linkedTrainerUid"] as? String
            )
        }
    }

}
