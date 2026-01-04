package com.develop.traiscore.data.repository

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

@Singleton
class ProfileRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val storage: FirebaseStorage
) {

    companion object {
        private val KEY_PHOTO_PATH = stringPreferencesKey("profile_photo_path")
        private const val DIR_NAME = "profile"
        private const val FILE_NAME = "profile.jpg"
    }

    /**
     * Copia la imagen a almacenamiento interno y guarda la ruta en DataStore.
     * Devuelve la ruta absoluta del archivo local.
     */
    suspend fun uploadProfilePhoto(imageUri: Uri): String = withContext(Dispatchers.IO) {
        val currentUserId = firebaseAuth.currentUser?.uid
            ?: throw IllegalStateException("Usuario no autenticado")

        android.util.Log.d("ProfileRepo", "🚀 Subiendo foto para UID: $currentUserId")
        android.util.Log.d("ProfileRepo", "📁 URI original: $imageUri")

        try {
            // 1. Subir a Firebase Storage
            val timestamp = System.currentTimeMillis()
            val storageRef = storage.reference
                .child("profile_photos/$currentUserId/$timestamp.jpg")

            android.util.Log.d("ProfileRepo", "📤 Subiendo a Storage...")

            storageRef.putFile(imageUri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()

            android.util.Log.d("ProfileRepo", "✅ Storage URL: $downloadUrl")

            // 2. Actualizar Firestore
            android.util.Log.d("ProfileRepo", "💾 Actualizando Firestore...")

            firestore.collection("users")
                .document(currentUserId)
                .update("photoURL", downloadUrl)
                .await()

            android.util.Log.d("ProfileRepo", "✅ Firestore actualizado correctamente")

            return@withContext downloadUrl

        } catch (e: Exception) {
            android.util.Log.e("ProfileRepo", "❌ Error subiendo foto", e)
            throw e
        }
    }

    /**
     * Lee desde DataStore la ruta local de la foto (si existe).
     */
    suspend fun getCurrentUserPhotoUrl(): String? = withContext(Dispatchers.IO) {
        val currentUserId = firebaseAuth.currentUser?.uid

        if (currentUserId == null) {
            android.util.Log.e("ProfileRepo", "❌ No hay usuario autenticado")
            return@withContext null
        }

        android.util.Log.d("ProfileRepo", "🔍 Obteniendo foto para UID: $currentUserId")

        try {
            val userDoc = firestore.collection("users")
                .document(currentUserId)
                .get()
                .await()

            val photoUrl = userDoc.getString("photoURL")

            android.util.Log.d("ProfileRepo", "📸 Foto obtenida: $photoUrl")

            return@withContext photoUrl

        } catch (e: Exception) {
            android.util.Log.e("ProfileRepo", "❌ Error obteniendo foto", e)
            return@withContext null
        }
    }
}