package com.develop.traiscore.data.Authentication

import android.annotation.SuppressLint
import android.util.Log
import com.develop.traiscore.core.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object UserRoleManager {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * Obtiene el rol del usuario actual usando Flow
     */
    fun getCurrentUserRoleFlow(): Flow<UserRole?> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = firestore
            .collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("UserRoleManager", "Error listening to user role", error)
                    trySend(null)
                    return@addSnapshotListener
                }

                if (snapshot?.exists() == true) {
                    val roleString = snapshot.getString("userRole") ?: "CLIENT"
                    val userRole = try {
                        UserRole.valueOf(roleString.uppercase())
                    } catch (e: IllegalArgumentException) {
                        Log.w("UserRoleManager", "Unknown role: $roleString, defaulting to CLIENT")
                        UserRole.CLIENT
                    }
                    trySend(userRole)
                } else {
                    // Si el documento no existe, asumir CLIENT por defecto
                    trySend(UserRole.CLIENT)
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Obtiene el rol del usuario actual usando callback
     */
    fun getCurrentUserRole(callback: (UserRole?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            callback(null)
            return
        }

        firestore
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val roleString = document.getString("userRole") ?: "CLIENT"
                    val userRole = try {
                        UserRole.valueOf(roleString.uppercase())
                    } catch (e: IllegalArgumentException) {
                        Log.w("UserRoleManager", "Unknown role: $roleString, defaulting to CLIENT")
                        UserRole.CLIENT
                    }
                    callback(userRole)
                } else {
                    // Si el documento no existe, asumir CLIENT por defecto
                    callback(UserRole.CLIENT)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("UserRoleManager", "Error getting user role", exception)
                callback(null)
            }
    }

    /**
     * Obtiene el rol del usuario actual usando suspend function
     */
    suspend fun getCurrentUserRoleSuspend(): UserRole? {
        val userId = auth.currentUser?.uid ?: return null

        return try {
            val document = firestore
                .collection("users")
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                val roleString = document.getString("userRole") ?: "CLIENT"
                try {
                    UserRole.valueOf(roleString.uppercase())
                } catch (e: IllegalArgumentException) {
                    Log.w("UserRoleManager", "Unknown role: $roleString, defaulting to CLIENT")
                    UserRole.CLIENT
                }
            } else {
                UserRole.CLIENT
            }
        } catch (e: Exception) {
            Log.e("UserRoleManager", "Error getting user role", e)
            null
        }
    }

    /**
     * Obtiene el rol de un usuario específico por ID
     */
    fun getUserRole(userId: String, callback: (UserRole?) -> Unit) {
        firestore
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val roleString = document.getString("userRole") ?: "CLIENT"
                    val userRole = try {
                        UserRole.valueOf(roleString.uppercase())
                    } catch (e: IllegalArgumentException) {
                        Log.w("UserRoleManager", "Unknown role: $roleString, defaulting to CLIENT")
                        UserRole.CLIENT
                    }
                    callback(userRole)
                } else {
                    callback(UserRole.CLIENT)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("UserRoleManager", "Error getting user role for $userId", exception)
                callback(null)
            }
    }

    /**
     * Actualiza el rol del usuario actual
     */
    fun updateCurrentUserRole(newRole: UserRole, callback: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            callback(false)
            return
        }

        firestore
            .collection("users")
            .document(userId)
            .update("userRole", newRole.name)
            .addOnSuccessListener {
                Log.d("UserRoleManager", "User role updated to ${newRole.name}")
                callback(true)
            }
            .addOnFailureListener { exception ->
                Log.e("UserRoleManager", "Error updating user role", exception)
                callback(false)
            }
    }

    /**
     * Verifica si el usuario actual tiene un rol específico
     */
    fun hasRole(requiredRole: UserRole, callback: (Boolean) -> Unit) {
        getCurrentUserRole { currentRole ->
            callback(currentRole == requiredRole)
        }
    }

    /**
     * Verifica si el usuario actual tiene uno de los roles especificados
     */
    fun hasAnyRole(requiredRoles: List<UserRole>, callback: (Boolean) -> Unit) {
        getCurrentUserRole { currentRole ->
            callback(currentRole != null && currentRole in requiredRoles)
        }
    }
    fun debugUserDocument() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.d("UserRoleManager", "debugUserDocument: No user logged in")
            return
        }

        Log.d("UserRoleManager", "debugUserDocument: Checking document for userId: $userId")

        firestore
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                Log.d("UserRoleManager", "=== DEBUG USER DOCUMENT ===")
                Log.d("UserRoleManager", "Document ID: ${document.id}")
                Log.d("UserRoleManager", "Document exists: ${document.exists()}")
                if (document.exists()) {
                    Log.d("UserRoleManager", "All document data: ${document.data}")

                    // Verificar todos los campos posibles
                    document.data?.forEach { (key, value) ->
                        Log.d("UserRoleManager", "Field '$key': '$value' (${value?.javaClass?.simpleName})")
                    }

                    // Verificar campos específicos
                    val role = document.getString("role")
                    val userRole = document.getString("userRole")

                    Log.d("UserRoleManager", "Specific field 'role': '$role'")
                    Log.d("UserRoleManager", "Specific field 'userRole': '$userRole'")
                }
                Log.d("UserRoleManager", "=== END DEBUG ===")
            }
            .addOnFailureListener { exception ->
                Log.e("UserRoleManager", "debugUserDocument: Error", exception)
            }
    }
}

// Extensión para usar desde Composables
// Extensión para usar desde Composables
@androidx.compose.runtime.Composable
fun rememberUserRole(): androidx.compose.runtime.State<UserRole?> {
    return androidx.compose.runtime.produceState<UserRole?>(initialValue = null) {
        UserRoleManager.getCurrentUserRoleFlow().collect { userRole ->
            value = userRole
        }
    }
}