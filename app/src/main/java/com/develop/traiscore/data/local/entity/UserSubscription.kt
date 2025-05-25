package com.develop.traiscore.data.local.entity

import com.google.firebase.Timestamp

enum class SubscriptionPlan(
    val planId: String,
    val bodyStatsDocumentsLimit: Int,
    val price: Double,
    val features: List<String>
) {
    FREE("free", 4, 0.0, listOf("Hasta 4 registros de medidas", "Rutinas básicas")),
    PREMIUM("premium", -1, 9.99, listOf("Registros de medidas ilimitados", "Rutinas avanzadas", "Historial completo")),
    PRO("pro", -1, 19.99, listOf("Todo Premium", "Acceso trainer", "Analytics avanzados"))
}

data class UserSubscription(
    val userId: String,
    val currentPlan: SubscriptionPlan = SubscriptionPlan.FREE,
    val subscriptionId: String? = null,
    val startDate: Timestamp? = null,
    val endDate: Timestamp? = null,
    val isActive: Boolean = true,
    val bodyStatsDocumentsCount: Int = 0,
    val lastBodyStatsUpdate: Timestamp? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

data class SubscriptionLimits(
    val canCreateBodyStats: Boolean,
    val remainingDocuments: Int,
    val requiresUpgrade: Boolean,
    val currentPlan: SubscriptionPlan,
    val message: String
)
