package com.develop.traiscore.data.local.entity

import com.develop.traiscore.core.UserRole

data class TrainerEntity(
    val user: UserEntity,
    val rating: Double = 0.0, // Rating promedio (0.0 a 5.0)
    val totalClients: Int = 0,
    val bio: String = "",
    val verified: Boolean = false, // Si el trainer está verificado por la plataforma
    val clientIds: List<String> = emptyList() // IDs de clientes asignados
) {
    init {
        require(user.userRole == UserRole.TRAINER) {
            "UserEntity debe ser de tipo TRAINER para crear un TrainerEntity"
        }
    }

    /**
     * Verifica si el trainer tiene clientes
     */
    fun hasClients(): Boolean = clientIds.isNotEmpty()

    /**
     * Obtiene el número actual de clientes
     */
    fun getCurrentClientCount(): Int = clientIds.size

    /**
     * Verifica si el trainer puede aceptar más clientes (máximo 50)
     */
    fun canAcceptMoreClients(): Boolean = clientIds.size < 50

    /**
     * Obtiene el rating formateado como string
     */
    fun getFormattedRating(): String {
        return if (rating > 0) String.format("%.1f", rating) else "Sin rating"
    }

    /**
     * Verifica si el trainer tiene un buen rating (>= 4.0)
     */
    fun hasGoodRating(): Boolean = rating >= 4.0
}
