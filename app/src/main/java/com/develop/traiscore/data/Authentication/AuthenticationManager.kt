package com.develop.traiscore.data.Authentication

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.develop.traiscore.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.security.MessageDigest
import java.util.UUID

class AuthenticationManager(
    val context: Context
) {
    private val auth = Firebase.auth
    private val firestore = FirebaseFirestore.getInstance()


    private fun createNonce(): String {
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)

        return digest.fold("") { str, it ->
            str + "%02x".format(it)

        }
    }

    fun signInWithGoogle(): Flow<AuthResponse> = callbackFlow {
        try {
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(
                    GetGoogleIdOption.Builder()
                        .setServerClientId(context.getString(R.string.web_client_id))
                        .build()
                )
                .build()

            val response = CredentialManager.create(context)
                .getCredential(context, request)

            // detecta directamente el credential que retorna la librería
            val googleCred = response.credential as? GoogleIdTokenCredential
                ?: run {
                    trySend(AuthResponse.Error("Credencial Google no reconocida"))
                    close()
                    return@callbackFlow
                }

            // extrae el ID token y pásalo a Firebase
            val firebaseCred = GoogleAuthProvider
                .getCredential(googleCred.idToken, null)
            auth.signInWithCredential(firebaseCred)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid
                        Log.d("AuthDebug", "Firebase Auth Success. UID: $uid") // <-- Add this

                        if (uid == null) {
                            Log.e("AuthDebug", "UID is null after successful Firebase Auth?") // <-- Should not happen
                            trySend(AuthResponse.Error("No estas registrado"))
                            close()
                        } else {
                            // 🔍 Check if /users/{uid} exists
                            firestore
                                .collection("users")
                                .document(uid)
                                .get()
                                .addOnSuccessListener { doc ->
                                    if (doc.exists()) {
                                        Log.d("AuthDebug", "Firestore check success for UID: $uid") // <-- Add this
                                        trySend(AuthResponse.Success)
                                    } else {
                                        Log.d("AuthDebug", "Document does NOT exist. Signing out and sending AuthResponse.NewUser") // <-- Add this
                                        trySend(AuthResponse.NewUser)
                                    }
                                    close()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("AuthDebug", "Firestore check failed for UID: $uid", e) // <-- Add this
                                    trySend(AuthResponse.Error(e.message ?: "Error Firestore"))
                                    close()
                                }
                        }
                    } else {
                        Log.e("AuthDebug", "Firebase Auth failed", task.exception) // <-- Add this
                        trySend(AuthResponse.Error(
                            task.exception?.message ?: "Error desconocido"
                        ))
                        close()
                    }
                }
        } catch (e: Exception) {
            trySend(AuthResponse.Error(e.message ?: "Excepción inesperada"))
            close()
        }

        awaitClose { /* nothing to clean up */ }
    }
}

sealed interface AuthResponse {
    object Success : AuthResponse
    object NewUser : AuthResponse           // ← here
    data class Error(val message: String) : AuthResponse
}