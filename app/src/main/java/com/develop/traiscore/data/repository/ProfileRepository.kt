package com.develop.traiscore.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val auth: FirebaseAuth,           // ← SIN valores por defecto
    private val firestore: FirebaseFirestore, // ← SIN valores por defecto
    private val storage: FirebaseStorage
) {

    suspend fun uploadProfilePhoto(imageUri: Uri): String {
        val currentUser = auth.currentUser
            ?: throw Exception("Usuario no autenticado")

        // Crear referencia única para la imagen
        val imageFileName = "profile_${currentUser.uid}_${UUID.randomUUID()}.jpg"
        val storageRef = storage.reference
            .child("profile_photos")
            .child(imageFileName)

        // Subir imagen a Firebase Storage
        val uploadTask = storageRef.putFile(imageUri).await()

        // Obtener URL de descarga
        val downloadUrl = uploadTask.storage.downloadUrl.await().toString()

        // Actualizar URL en Firestore
        updateUserPhotoUrl(currentUser.uid, downloadUrl)

        return downloadUrl
    }

    private suspend fun updateUserPhotoUrl(uid: String, photoUrl: String) {
        firestore.collection("users")
            .document(uid)
            .update("photoURL", photoUrl)
            .await()
    }

    suspend fun getCurrentUserPhotoUrl(): String? {
        val currentUser = auth.currentUser ?: return null

        val userDoc = firestore.collection("users")
            .document(currentUser.uid)
            .get()
            .await()

        return userDoc.getString("photoURL")
    }
}