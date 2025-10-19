package com.develop.traiscore.data.Authentication

import android.content.Context
import android.credentials.GetCredentialException
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.develop.traiscore.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

class AuthenticationManager(
    private val context: Context
) {
    private val auth = Firebase.auth
    private val firestore = FirebaseFirestore.getInstance()

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun signInWithGoogle(): Flow<AuthResponse> = callbackFlow {
        try {
            Log.d("jejeje", "=== STARTING GOOGLE SIGN-IN ===")

            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setFilterByAuthorizedAccounts(false) // Permite cuentas no autorizadas previamente
                .setAutoSelectEnabled(true) // Auto-selecciona si hay una sola cuenta
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            Log.d("AuthDebug", "Requesting credentials...")

            val credentialManager = CredentialManager.create(context)
            val credentialResponse = credentialManager.getCredential(context, request)

            Log.d("AuthDebug", "Credential type: ${credentialResponse.credential::class.java.simpleName}")
            Log.d("AuthDebug", "Credential data keys: ${credentialResponse.credential.data.keySet()}")

            // Parsear la credencial de Google
            val googleCredential = GoogleIdTokenCredential.createFrom(credentialResponse.credential.data)

            Log.d("AuthDebug", "Google credential parsed successfully")
            Log.d("AuthDebug", "User email: ${googleCredential.id}")
            Log.d("AuthDebug", "User name: ${googleCredential.displayName}")

            // Crear credencial de Firebase
            val firebaseCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)

            Log.d("AuthDebug", "Firebase credential created, signing in...")

            // Autenticar con Firebase
            val authResult = auth.signInWithCredential(firebaseCredential).await()
            val firebaseUser = authResult.user

            if (firebaseUser == null) {
                Log.e("AuthDebug", "Firebase user is null after successful sign-in")
                trySend(AuthResponse.Error("Error de autenticación"))
                close()
                return@callbackFlow
            }

            Log.d("AuthDebug", "Firebase Auth Success. UID: ${firebaseUser.uid}")
            Log.d("AuthDebug", "User email: ${firebaseUser.email}")
            Log.d("AuthDebug", "User name: ${firebaseUser.displayName}")

            // Verificar si el usuario ya existe en Firestore
            val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()

            if (userDoc.exists()) {
                Log.d("AuthDebug", "User exists in Firestore - Success")
                trySend(AuthResponse.Success)
            } else {
                Log.d("AuthDebug", "New user - needs registration")
                trySend(AuthResponse.NewUser(
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName ?: "",
                    photoUrl = firebaseUser.photoUrl?.toString()
                ))
            }

        } catch (e: GetCredentialException) {
            Log.e("AuthDebug", "GetCredentialException: ${e.message}", e)
            when (e.type) {
                "TYPE_USER_CANCELED" -> {
                    trySend(AuthResponse.Error("Autenticación cancelada"))
                }
                "TYPE_NO_CREDENTIAL" -> {
                    trySend(AuthResponse.Error("No hay cuentas Google disponibles"))
                }
                else -> {
                    trySend(AuthResponse.Error("Error de credenciales: ${e.message}"))
                }
            }
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("AuthDebug", "GoogleIdTokenParsingException: ${e.message}", e)
            trySend(AuthResponse.Error("Error procesando credencial de Google"))
        } catch (e: Exception) {
            Log.e("AuthDebug", "Unexpected exception: ${e.message}", e)
            trySend(AuthResponse.Error("Error inesperado: ${e.message}"))
        }

        awaitClose {
            Log.d("AuthDebug", "Google Sign-In flow closed")
        }
    }
}

sealed interface AuthResponse {
    object Success : AuthResponse
    data class NewUser(
        val email: String,
        val displayName: String,
        val photoUrl: String? = null
    ) : AuthResponse
    data class Error(val message: String) : AuthResponse
}