package com.develop.traiscore.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import java.io.File

@Composable
fun CameraSocialScreen(
    exerciseName: String,
    oneRepMax: Float,
    maxReps: Int,
    totalWeight: Double,
    trainingDays: Int,
    navController: NavController
) {
    val context = LocalContext.current
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var showSocialShare by remember { mutableStateOf(false) }
    var capturedPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // Crear archivo temporal para la foto
    val photoFile = remember {
        File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri?.let { uri ->
                Log.d("CameraSocialScreen", "Foto capturada: $uri")
                capturedPhotoUri = uri
                showSocialShare = true
            }
        } else {
            // Si no se capturó foto, volver atrás
            navController.popBackStack()
        }
    }

    // Launcher para solicitar permiso de cámara
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permiso concedido, abrir cámara
            val uri = FileProvider.getUriForFile(
                context,
                "com.develop.traiscore.fileprovider",
                photoFile
            )
            photoUri = uri
            cameraLauncher.launch(uri)
        } else {
            Log.d("CameraSocialScreen", "Permiso de cámara denegado")
            navController.popBackStack()
        }
    }

    // Verificar permiso y abrir cámara al entrar
    LaunchedEffect(Unit) {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permiso ya concedido, abrir cámara directamente
                val uri = FileProvider.getUriForFile(
                    context,
                    "com.develop.traiscore.fileprovider",
                    photoFile
                )
                photoUri = uri
                cameraLauncher.launch(uri)
            }
            else -> {
                // Solicitar permiso
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    if (showSocialShare && capturedPhotoUri != null) {
        // FULLSCREEN - Sin Scaffold, sin navigation bars
        SocialMediaScreen(
            photo = capturedPhotoUri!!,
            exerciseName = exerciseName,
            topWeight = oneRepMax,
            totalWeight = totalWeight,
            maxReps = maxReps,
            trainingDays = trainingDays, // Podrías pasarlo como parámetro también
            onDismiss = {
                navController.popBackStack()
            },
            onShare = { bitmap ->
                Log.d("CameraSocialScreen", "Compartir bitmap")
                // Aquí implementar la lógica de compartir
            }
        )
    } else {
        // Mostrar loading mientras se abre la cámara
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}