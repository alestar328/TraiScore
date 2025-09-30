package com.develop.traiscore.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.develop.traiscore.presentation.components.TraiScoreTopBar
import com.develop.traiscore.presentation.theme.tsColors
import com.develop.traiscore.presentation.viewmodels.CameraScanViewModel
import kotlinx.coroutines.android.awaitFrame


@Composable
fun CameraScanScreen(
    onBack: () -> Unit,
    onPhotoCaptured: (android.net.Uri) -> Unit,
    vm: CameraScanViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember { mutableStateOf(false) }
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            hasCameraPermission = true
        }
    }

    // Controller (CameraX high-level) SOLO captura
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(
                androidx.camera.view.CameraController.IMAGE_CAPTURE
            )
        }
    }

    // Vincular controller al lifecycle cuando hay permiso
    LaunchedEffect(hasCameraPermission) {
        if (hasCameraPermission) {
            cameraController.bindToLifecycle(lifecycleOwner)
            // Asegurar un frame antes de capturar para que el preview esté listo
            awaitFrame()
        }
    }

    Scaffold(
        topBar = {
            TraiScoreTopBar(
                leftIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = MaterialTheme.tsColors.ledCyan
                        )
                    }
                },
                rightIcon = { Spacer(Modifier.size(48.dp)) }
            )
        }
    ) { padding ->
        if (!hasCameraPermission) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Text("Se necesita permiso de cámara")
            }
            return@Scaffold
        }

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Preview CameraX a pantalla completa
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setBackgroundColor(android.graphics.Color.BLACK)
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        controller = cameraController
                    }
                }
            )

            // Botón disparador
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                FilledIconButton(
                    onClick = {
                        try {
                            val output = vm.buildOutputOptions(context)
                            cameraController.takePicture(
                                output,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                        val uri = outputFileResults.savedUri
                                        if (uri != null) onPhotoCaptured(uri)
                                        else {
                                            // Si no devuelve URI, no hacemos nada (puedes mostrar un snackbar/log)
                                        }
                                    }
                                    override fun onError(exception: ImageCaptureException) {
                                        // Log o Snackbar si quieres
                                    }
                                }
                            )
                        } catch (_: Exception) {
                            // Log o Snackbar si quieres
                        }
                    },
                    shape = CircleShape,
                    modifier = Modifier.size(74.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.tsColors.ledCyan,
                        contentColor = Color.Black
                    )
                ) {
                    // Círculo interior (estético)
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .background(Color.White, CircleShape)
                    )
                }
            }
        }
    }
}