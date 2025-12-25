package com.develop.traiscore.presentation.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.develop.traiscore.data.local.entity.SubscriptionLimits
import com.develop.traiscore.data.local.entity.SubscriptionPlan
import com.develop.traiscore.data.local.entity.UserSubscription
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SubscriptionViewModel @Inject constructor() : ViewModel() {

    var userSubscription by mutableStateOf<UserSubscription?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set
    var actualDocumentsCount by mutableStateOf(0)
        private set
    private var localDocumentsCount by mutableStateOf(0)

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    fun checkCanCreateNewDocumentWithCount(
        currentCount: Int,
        onComplete: (canCreate: Boolean, message: String?) -> Unit
    ) {
        Log.d("SubscriptionVM", "Verificando límites con conteo local: $currentCount")

        // Actualizar contador local
        localDocumentsCount = currentCount

        val subscription = userSubscription
        if (subscription == null) {
            onComplete(false, "Error al cargar suscripción")
            return
        }

        val currentPlan = subscription.currentPlan
        val limit = currentPlan.bodyStatsDocumentsLimit

        when {
            // Plan premium/pro - sin límites
            limit == -1 -> {
                Log.d("SubscriptionVM", "Plan sin límites - puede crear")
                onComplete(true, null)
            }
            // Verificar contra límite
            currentCount < limit -> {
                val remaining = limit - currentCount
                Log.d("SubscriptionVM", "Puede crear - Quedan $remaining de $limit")
                onComplete(true, null)
            }
            // Límite alcanzado
            else -> {
                val message = "Has alcanzado el límite de $limit registros para tu plan ${currentPlan.name}"
                Log.d("SubscriptionVM", "Límite alcanzado: $message")
                onComplete(false, message)
            }
        }
    }

    fun updateBodyStatsCount(newCount: Int) {
        Log.d("SubscriptionVM", "Actualizando contador local: $localDocumentsCount → $newCount")
        localDocumentsCount = newCount
        actualDocumentsCount = newCount

        // Opcionalmente, sincronizar con Firebase en background
        syncDocumentCountInSubscription(newCount)
    }

    fun checkBodyStatsLimitsLocal(): SubscriptionLimits {
        val subscription = userSubscription ?: return SubscriptionLimits(
            canCreateBodyStats = false,
            remainingDocuments = 0,
            requiresUpgrade = true,
            currentPlan = SubscriptionPlan.FREE,
            message = "Error al cargar suscripción"
        )

        val currentPlan = subscription.currentPlan
        // Usar contador local si está disponible, sino usar actualDocumentsCount
        val currentCount = if (localDocumentsCount > 0) localDocumentsCount else actualDocumentsCount

        Log.d("SubscriptionVM", "=== VERIFICANDO LÍMITES LOCALES ===")
        Log.d("SubscriptionVM", "Plan: ${currentPlan.planId}")
        Log.d("SubscriptionVM", "Límite: ${currentPlan.bodyStatsDocumentsLimit}")
        Log.d("SubscriptionVM", "Documentos locales: $currentCount")

        return when {
            currentPlan.bodyStatsDocumentsLimit == -1 -> {
                SubscriptionLimits(
                    canCreateBodyStats = true,
                    remainingDocuments = -1,
                    requiresUpgrade = false,
                    currentPlan = currentPlan,
                    message = "Registros ilimitados"
                )
            }
            currentCount < currentPlan.bodyStatsDocumentsLimit -> {
                val remaining = currentPlan.bodyStatsDocumentsLimit - currentCount
                SubscriptionLimits(
                    canCreateBodyStats = true,
                    remainingDocuments = remaining,
                    requiresUpgrade = false,
                    currentPlan = currentPlan,
                    message = "Te quedan $remaining registros"
                )
            }
            else -> {
                SubscriptionLimits(
                    canCreateBodyStats = false,
                    remainingDocuments = 0,
                    requiresUpgrade = true,
                    currentPlan = currentPlan,
                    message = "Has alcanzado el límite de ${currentPlan.bodyStatsDocumentsLimit} registros"
                )
            }
        }
    }
    fun checkBodyStatsLimitsWithCount(onComplete: (SubscriptionLimits) -> Unit) {
        // Si tenemos contador local actualizado, usarlo directamente
        if (localDocumentsCount > 0) {
            val limits = checkBodyStatsLimitsLocal()
            onComplete(limits)
        } else {
            // Fallback: contar desde Firebase
            countActualBodyStatsDocuments { realCount ->
                val limits = checkBodyStatsLimits()
                onComplete(limits)
            }
        }
    }

    /**
     * Carga la suscripción del usuario actual
     */
    fun loadUserSubscription(onComplete: (UserSubscription?) -> Unit = {}) {
        val userId = auth.currentUser?.uid ?: return

        isLoading = true

        db.collection("subscriptions")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val data = document.data!!
                    userSubscription = UserSubscription(
                        userId = userId,
                        currentPlan = SubscriptionPlan.fromPlanId(
                            data["currentPlan"] as? String
                        ),
                        subscriptionId = data["subscriptionId"] as? String,
                        startDate = data["startDate"] as? Timestamp,
                        endDate = data["endDate"] as? Timestamp,
                        isActive = data["isActive"] as? Boolean ?: true,
                        bodyStatsDocumentsCount = (data["bodyStatsDocumentsCount"] as? Number)?.toInt()
                            ?: 0,
                        lastBodyStatsUpdate = data["lastBodyStatsUpdate"] as? Timestamp,
                        createdAt = data["createdAt"] as? Timestamp,
                        updatedAt = data["updatedAt"] as? Timestamp
                    )
                    countActualBodyStatsDocuments { realCount ->
                        isLoading = false
                        onComplete(userSubscription)
                    }
                } else {
                    createDefaultSubscription { newSub ->
                        userSubscription = newSub
                        // Después de crear, contar documentos
                        countActualBodyStatsDocuments { realCount ->
                            isLoading = false
                            onComplete(userSubscription)
                        }
                    }
                    return@addOnSuccessListener
                }
                // DESPUÉS de cargar suscripción, contar documentos reales
                countActualBodyStatsDocuments { realCount ->
                    isLoading = false
                    // userSubscription YA fue asignada arriba
                    onComplete(userSubscription)
                }
            }
            .addOnFailureListener { exception ->
                isLoading = false
                Log.e("SubscriptionVM", "Error loading subscription", exception)
                onComplete(null)
            }
    }

    private fun syncDocumentCountInSubscription(realCount: Int) {
        val userId = auth.currentUser?.uid ?: return
        val currentStoredCount = userSubscription?.bodyStatsDocumentsCount ?: 0

        // Solo actualizar si hay diferencia
        if (realCount != currentStoredCount) {
            Log.d("SubscriptionVM", "Sincronizando contador: $currentStoredCount → $realCount")

            db.collection("subscriptions")
                .document(userId)
                .update(
                    mapOf(
                        "bodyStatsDocumentsCount" to realCount,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .addOnSuccessListener {
                    // Actualizar estado local
                    userSubscription = userSubscription?.copy(
                        bodyStatsDocumentsCount = realCount
                    )
                }
                .addOnFailureListener {
                    Log.e("SubscriptionVM", "Error sincronizando contador", it)
                }
        }
    }

    fun countActualBodyStatsDocuments(onComplete: (Int) -> Unit) {
        val userId = auth.currentUser?.uid ?: return onComplete(0)

        Log.d("SubscriptionVM", "Contando documentos para usuario: $userId")

        db.collection("users")
            .document(userId)
            .collection("bodyStats")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val count = querySnapshot.size()
                actualDocumentsCount = count

                Log.d("SubscriptionVM", "✅ Documentos encontrados: $count")

                // Listar documentos para debug
                querySnapshot.documents.forEachIndexed { index, doc ->
                    Log.d("SubscriptionVM", "Doc $index: ${doc.id}")
                }

                // Sincronizar contador
                syncDocumentCountInSubscription(count)
                onComplete(count)
            }
            .addOnFailureListener { exception ->
                Log.e("SubscriptionVM", "❌ Error contando documentos", exception)
                onComplete(0)
            }
    }

    /**
     * Verifica si el usuario puede crear más bodyStats
     */
    fun checkBodyStatsLimits(): SubscriptionLimits {
        val subscription = userSubscription ?: return SubscriptionLimits(
            canCreateBodyStats = false,
            remainingDocuments = 0,
            requiresUpgrade = true,
            currentPlan = SubscriptionPlan.FREE,
            message = "Error al cargar suscripción"
        )

        val currentPlan = subscription.currentPlan
        val currentCount = actualDocumentsCount

        // AGREGAR LOGGING PARA DEBUG
        Log.d("SubscriptionVM", "=== VERIFICANDO LÍMITES ===")
        Log.d("SubscriptionVM", "Plan actual: ${currentPlan.planId}")
        Log.d("SubscriptionVM", "Límite del plan: ${currentPlan.bodyStatsDocumentsLimit}")
        Log.d("SubscriptionVM", "Documentos actuales: $currentCount")
        Log.d(
            "SubscriptionVM",
            "¿Puede crear?: ${currentCount < currentPlan.bodyStatsDocumentsLimit}"
        )

        return when {
            // Plan premium/pro - sin límites
            currentPlan.bodyStatsDocumentsLimit == -1 -> {
                Log.d("SubscriptionVM", "Plan premium/pro - sin límites")
                SubscriptionLimits(
                    canCreateBodyStats = true,
                    remainingDocuments = -1,
                    requiresUpgrade = false,
                    currentPlan = currentPlan,
                    message = "Registros ilimitados"
                )
            }

            // Plan gratuito - verificar límite
            currentCount < currentPlan.bodyStatsDocumentsLimit -> {
                val remaining = currentPlan.bodyStatsDocumentsLimit - currentCount
                Log.d("SubscriptionVM", "Plan gratuito - puede crear. Quedan: $remaining")
                SubscriptionLimits(
                    canCreateBodyStats = true,
                    remainingDocuments = remaining,
                    requiresUpgrade = false,
                    currentPlan = currentPlan,
                    message = "Te quedan $remaining registros"
                )
            }

            // Límite alcanzado
            else -> {
                Log.d("SubscriptionVM", "Límite alcanzado - no puede crear más")
                SubscriptionLimits(
                    canCreateBodyStats = false,
                    remainingDocuments = 0,
                    requiresUpgrade = true,
                    currentPlan = currentPlan,
                    message = "Has alcanzado el límite de ${currentPlan.bodyStatsDocumentsLimit} registros. Actualiza tu plan."
                )
            }
        }
    }
    /**
     * Incrementa el contador de bodyStats
     */


    /**
     * Crea suscripción gratuita por defecto
     */
    fun checkCanCreateNewDocument(onComplete: (Boolean, String?) -> Unit) {
        Log.d("SubscriptionVM", "Verificando si puede crear nuevo documento...")

        // Contar documentos y verificar límites en secuencia
        checkBodyStatsLimitsWithCount { limits ->
            Log.d(
                "SubscriptionVM",
                "Límites verificados: canCreate=${limits.canCreateBodyStats}, remaining=${limits.remainingDocuments}"
            )

            if (limits.canCreateBodyStats) {
                onComplete(true, null)
            } else {
                onComplete(false, limits.message)
            }
        }
    }

    private fun createDefaultSubscription(onComplete: (UserSubscription) -> Unit) {
        val userId = auth.currentUser?.uid ?: return

        val defaultSubscription = UserSubscription(
            userId = userId,
            currentPlan = SubscriptionPlan.FREE,
            bodyStatsDocumentsCount = 0,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )

        val subscriptionMap = mapOf(
            "userId" to userId,
            "currentPlan" to defaultSubscription.currentPlan.planId,
            "subscriptionId" to null,
            "startDate" to null,
            "endDate" to null,
            "isActive" to true,
            "bodyStatsDocumentsCount" to 0,
            "lastBodyStatsUpdate" to null,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        db.collection("subscriptions")
            .document(userId)
            .set(subscriptionMap)
            .addOnSuccessListener {
                userSubscription = defaultSubscription
                Log.d("SubscriptionVM", "Default subscription created")
                onComplete(defaultSubscription)
            }
            .addOnFailureListener { exception ->
                Log.e("SubscriptionVM", "Error creating default subscription", exception)
            }
    }

    /**
     * Simula upgrade a premium (para futuro sistema de pagos)
     */
    fun upgradeToPremium(onComplete: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return onComplete(false)

        // TODO: Aquí se integrará el sistema de pagos
        // Por ahora solo simula el upgrade

        val updateData = mapOf(
            "currentPlan" to SubscriptionPlan.PREMIUM.planId,
            "isActive" to true,
            "startDate" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        db.collection("subscriptions")
            .document(userId)
            .update(updateData)
            .addOnSuccessListener {
                loadUserSubscription()
                onComplete(true)
            }
            .addOnFailureListener {
                Log.e("SubscriptionVM", "Error upgrading subscription", it)
                onComplete(false)
            }
    }
}