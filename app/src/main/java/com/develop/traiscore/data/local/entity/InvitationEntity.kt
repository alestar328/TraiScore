package com.develop.traiscore.data.local.entity


import com.google.firebase.Timestamp

data class InvitationEntity(
    val id: String = "",
    val trainerId: String,
    val trainerName: String,
    val trainerEmail: String,
    val invitationCode: String, // Código único de 6-8 caracteres
    val createdAt: Timestamp = Timestamp.now(),
    val expiresAt: Timestamp? = null, // Opcional: expiración de la invitación
    val isActive: Boolean = true,
    val usedBy: String? = null, // UID del cliente que usó la invitación
    val usedAt: Timestamp? = null,
    val maxUses: Int = 1, // Número máximo de usos (1 = invitación única)
    val currentUses: Int = 0
) {
    /**
     * Verifica si la invitación está disponible para usar
     */
    fun isAvailable(): Boolean {
        return isActive &&
                currentUses < maxUses &&
                usedBy == null &&
                (expiresAt == null || expiresAt.toDate().after(java.util.Date()))
    }

    /**
     * Verifica si la invitación ha expirado
     */
    fun hasExpired(): Boolean {
        return expiresAt != null && expiresAt.toDate().before(java.util.Date())
    }

    /**
     * Genera el link de invitación
     */
    fun generateInviteLink(baseUrl: String = "https://traiscore.app/invite/"): String {
        return "$baseUrl$invitationCode"
    }

    /**
     * Convierte a mapa para Firestore
     */
    fun toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "trainerId" to trainerId,
            "trainerName" to trainerName,
            "trainerEmail" to trainerEmail,
            "invitationCode" to invitationCode,
            "createdAt" to createdAt,
            "expiresAt" to expiresAt,
            "isActive" to isActive,
            "usedBy" to usedBy,
            "usedAt" to usedAt,
            "maxUses" to maxUses,
            "currentUses" to currentUses
        )
    }

    companion object {
        /**
         * Genera un código de invitación único
         */
        fun generateInviteCode(): String {
            val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            return (1..6)
                .map { chars.random() }
                .joinToString("")
        }

        /**
         * Crea desde datos de Firestore
         */
        fun fromFirestore(data: Map<String, Any>, id: String): InvitationEntity {
            return InvitationEntity(
                id = id,
                trainerId = data["trainerId"] as? String ?: "",
                trainerName = data["trainerName"] as? String ?: "",
                trainerEmail = data["trainerEmail"] as? String ?: "",
                invitationCode = data["invitationCode"] as? String ?: "",
                createdAt = data["createdAt"] as? Timestamp ?: Timestamp.now(),
                expiresAt = data["expiresAt"] as? Timestamp,
                isActive = data["isActive"] as? Boolean ?: true,
                usedBy = data["usedBy"] as? String,
                usedAt = data["usedAt"] as? Timestamp,
                maxUses = (data["maxUses"] as? Number)?.toInt() ?: 1,
                currentUses = (data["currentUses"] as? Number)?.toInt() ?: 0
            )
        }
    }
}
