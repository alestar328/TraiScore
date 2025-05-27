package com.develop.traiscore.data.local.entity

import com.develop.traiscore.core.UserRole

data class ClientEntity(
    val user: UserEntity,
    val measurements: ClientMeasurements = ClientMeasurements(),
    val subscription: UserSubscription? = null,
    val routineIds: List<String> = emptyList(), // IDs de rutinas asignadas
    val activeRoutineId: String? = null,
    val trainerId: String? = null // ID del trainer asignado (si tiene)
) {
    init {
        require(user.userRole == UserRole.CLIENT) {
            "UserEntity debe ser de tipo CLIENT para crear un ClientEntity"
        }
    }

    /**
     * Verifica si el cliente tiene un plan premium o pro
     */
    fun hasPremiumAccess(): Boolean {
        return subscription?.currentPlan in listOf(SubscriptionPlan.PREMIUM, SubscriptionPlan.PRO)
    }

    /**
     * Verifica si el cliente tiene un trainer asignado
     */
    fun hasTrainer(): Boolean = trainerId != null

    /**
     * Verifica si el cliente tiene rutinas asignadas
     */
    fun hasRoutines(): Boolean = routineIds.isNotEmpty()

    /**
     * Verifica si el cliente tiene una rutina activa
     */
    fun hasActiveRoutine(): Boolean = activeRoutineId != null
}
