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

@Singleton
class ProfileRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>
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
        val dir = File(context.filesDir, DIR_NAME).apply { if (!exists()) mkdirs() }
        val outFile = File(dir, FILE_NAME)

        context.contentResolver.openInputStream(imageUri)?.use { input ->
            outFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw IOException("No se pudo abrir el InputStream del Uri: $imageUri")

        // Persistir la ruta absoluta en DataStore
        dataStore.edit { prefs ->
            prefs[KEY_PHOTO_PATH] = outFile.absolutePath
        }

        return@withContext outFile.absolutePath
    }

    /**
     * Lee desde DataStore la ruta local de la foto (si existe).
     */
    suspend fun getCurrentUserPhotoUrl(): String? {
        val prefs = dataStore.data.first()
        return prefs[KEY_PHOTO_PATH]
    }
}