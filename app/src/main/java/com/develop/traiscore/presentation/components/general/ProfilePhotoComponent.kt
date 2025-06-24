package com.develop.traiscore.presentation.components.general

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.develop.traiscore.R

@Composable
fun ProfilePhotoComponent(
    currentPhotoUrl: String?,
    isUploading: Boolean,
    onPhotoSelected: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LaunchedEffect(currentPhotoUrl, isUploading) {
        android.util.Log.d("ProfilePhoto", "🔄 ===== COMPONENT UPDATE =====")
        android.util.Log.d("ProfilePhoto", "📸 currentPhotoUrl: '$currentPhotoUrl'")
        android.util.Log.d("ProfilePhoto", "📸 currentPhotoUrl.isNullOrEmpty(): ${currentPhotoUrl.isNullOrEmpty()}")
        android.util.Log.d("ProfilePhoto", "📸 currentPhotoUrl?.isNotEmpty(): ${currentPhotoUrl?.isNotEmpty()}")
        android.util.Log.d("ProfilePhoto", "🔄 isUploading: $isUploading")
        android.util.Log.d("ProfilePhoto", "================================")
    }
    // Launcher para seleccionar imagen de la galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        android.util.Log.d("ProfilePhoto", "🖼️ Imagen seleccionada desde galería: $uri")
        uri?.let { onPhotoSelected(it) }
    }

    Box(
        modifier = modifier
            .size(96.dp)
            .clip(RoundedCornerShape(48.dp))
            .clickable {
                if (!isUploading) {
                    android.util.Log.d("ProfilePhoto", "📷 Usuario tocó el componente, abriendo galería...")
                    galleryLauncher.launch("image/*")
                } else {
                    android.util.Log.d("ProfilePhoto", "⏳ Usuario tocó el componente pero está subiendo...")
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val shouldShowAsyncImage = currentPhotoUrl?.isNotEmpty() == true
        android.util.Log.d("ProfilePhoto", "🤔 shouldShowAsyncImage: $shouldShowAsyncImage")

        if (shouldShowAsyncImage) {
            android.util.Log.d("ProfilePhoto", "✅ Mostrando AsyncImage con URL: $currentPhotoUrl")

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(currentPhotoUrl)
                    .crossfade(true)
                    .listener(
                        onStart = {
                            android.util.Log.d("ProfilePhoto", "⏳ AsyncImage: Iniciando carga de imagen...")
                        },
                        onSuccess = { _, result ->
                            android.util.Log.d("ProfilePhoto", "✅ AsyncImage: Imagen cargada exitosamente")
                            android.util.Log.d("ProfilePhoto", "📊 AsyncImage: Resultado: $result")
                        },
                        onError = { _, error ->
                            android.util.Log.e("ProfilePhoto", "❌ AsyncImage: Error cargando imagen", error.throwable)
                            android.util.Log.e("ProfilePhoto", "🔗 AsyncImage: URL que falló: $currentPhotoUrl")
                        }
                    )
                    .build(),
                contentDescription = "Foto de perfil",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                fallback = painterResource(R.drawable.user_png),
                error = painterResource(R.drawable.user_png)
            )
        } else {
            android.util.Log.d("ProfilePhoto", "🆕 Mostrando placeholder (sin foto válida)")

            Image(
                painter = painterResource(R.drawable.user_png),
                contentDescription = "Foto de perfil por defecto",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Overlay de carga
        if (isUploading) {
            android.util.Log.d("ProfilePhoto", "⏳ Mostrando overlay de carga...")

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            }
        } else {
            // Icono de cámara en la esquina
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-4).dp, y = (-4).dp)
                    .size(28.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Cambiar foto",
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
            }
        }
    }
}