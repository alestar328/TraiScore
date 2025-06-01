package com.develop.traiscore.data.repository


import com.develop.traiscore.data.local.entity.InvitationEntity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class InvitationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val invitationsCollection = firestore.collection("invitations")
    private val usersCollection = firestore.collection("users")

    /**
     * Crea una nueva invitación
     */
    suspend fun createInvitation(
        trainerName: String,
        trainerEmail: String,
        expirationDays: Int? = null
    ): Result<InvitationEntity> {
        return try {
            val trainerId = auth.currentUser?.uid ?: return Result.failure(
                Exception("Usuario no autenticado")
            )

            // Verificar que el usuario es un trainer
            val userDoc = usersCollection.document(trainerId).get().await()
            val userRole = userDoc.getString("userRole")

            if (userRole != "TRAINER") {
                return Result.failure(Exception("Solo los entrenadores pueden crear invitaciones"))
            }

            // Generar código único
            var inviteCode: String
            var attempts = 0
            do {
                inviteCode = InvitationEntity.generateInviteCode()
                attempts++
                if (attempts > 10) {
                    return Result.failure(Exception("No se pudo generar un código único"))
                }
            } while (isCodeExists(inviteCode))

            val invitation = InvitationEntity(
                trainerId = trainerId,
                trainerName = trainerName,
                trainerEmail = trainerEmail,
                invitationCode = inviteCode,
                expiresAt = expirationDays?.let {
                    Timestamp(java.util.Date(System.currentTimeMillis() + it * 24 * 60 * 60 * 1000))
                }
            )

            val docRef = invitationsCollection.add(invitation.toFirestoreMap()).await()
            Result.success(invitation.copy(id = docRef.id))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception("Error al crear invitación: ${e.message}"))
        }
    }
    suspend fun deleteInvitation(invitationId: String): Result<Unit> {
        return try {
            val trainerId = auth.currentUser?.uid ?: return Result.failure(
                Exception("Usuario no autenticado")
            )

            // Verificar que la invitación pertenece al trainer actual
            val invitationDoc = invitationsCollection.document(invitationId).get().await()

            if (!invitationDoc.exists()) {
                return Result.failure(Exception("Invitación no encontrada"))
            }

            val invitation = InvitationEntity.fromFirestore(
                invitationDoc.data ?: emptyMap(),
                invitationDoc.id
            )

            if (invitation.trainerId != trainerId) {
                return Result.failure(Exception("No tienes permisos para eliminar esta invitación"))
            }

            // Eliminar el documento
            invitationsCollection.document(invitationId).delete().await()

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception("Error al eliminar invitación: ${e.message}"))
        }
    }
    /**
     * Verifica si un código ya existe
     */
    private suspend fun isCodeExists(code: String): Boolean {
        return try {
            val snapshot = invitationsCollection
                .whereEqualTo("invitationCode", code)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            !snapshot.isEmpty
        } catch (e: Exception) {
            true // Por seguridad, asumimos que existe si hay error
        }
    }

    /**
     * Busca una invitación por código
     */
    suspend fun findInvitationByCode(code: String): Result<InvitationEntity?> {
        return try {
            val snapshot = invitationsCollection
                .whereEqualTo("invitationCode", code.uppercase())
                .whereEqualTo("isActive", true)
                .get()
                .await()

            if (snapshot.isEmpty) {
                Result.success(null)
            } else {
                val doc = snapshot.documents.first()
                val invitation = InvitationEntity.fromFirestore(
                    doc.data ?: emptyMap(),
                    doc.id
                )
                Result.success(invitation)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Acepta una invitación
     */
    suspend fun acceptInvitation(invitationId: String, clientId: String): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val invitationRef = invitationsCollection.document(invitationId)
                val invitationDoc = transaction.get(invitationRef)

                if (!invitationDoc.exists()) {
                    throw Exception("Invitación no encontrada")
                }

                val invitation = InvitationEntity.fromFirestore(
                    invitationDoc.data ?: emptyMap(),
                    invitationDoc.id
                )

                if (!invitation.isAvailable()) {
                    throw Exception("La invitación no está disponible")
                }

                // Actualizar la invitación
                transaction.update(invitationRef, mapOf(
                    "usedBy" to clientId,
                    "usedAt" to Timestamp.now(),
                    "currentUses" to invitation.currentUses + 1,
                    "isActive" to (invitation.currentUses + 1 < invitation.maxUses)
                ))

                // Actualizar el cliente con el trainer
                val clientRef = usersCollection.document(clientId)
                transaction.update(clientRef, mapOf(
                    "linkedTrainerUid" to invitation.trainerId,
                    "updatedAt" to Timestamp.now()
                ))
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene las invitaciones del trainer actual
     */
    suspend fun getTrainerInvitations(): Result<List<InvitationEntity>> {
        return try {
            val trainerId = auth.currentUser?.uid ?: return Result.failure(
                Exception("Usuario no autenticado")
            )

            val snapshot = invitationsCollection
                .whereEqualTo("trainerId", trainerId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val invitations = snapshot.documents.map { doc ->
                InvitationEntity.fromFirestore(doc.data ?: emptyMap(), doc.id)
            }

            Result.success(invitations)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception("Error al obtener invitaciones: ${e.message}"))
        }
    }

    /**
     * Cancela una invitación
     */
    suspend fun cancelInvitation(invitationId: String): Result<Unit> {
        return try {
            invitationsCollection.document(invitationId)
                .update("isActive", false)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}