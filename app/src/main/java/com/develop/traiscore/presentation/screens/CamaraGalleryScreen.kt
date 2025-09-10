package com.develop.traiscore.presentation.screens

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.develop.traiscore.presentation.theme.traiBlue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

data class GalleryPhoto(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val dateAdded: Long
)


@Composable
fun CameraGalleryScreen(
    exerciseName: String,
    exerciseNameMaxReps: String,
    oneRepMax: Float,
    maxReps: Int,
    totalWeight: Double,
    trainingDays: Int,
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estados principales
    var photos by remember { mutableStateOf<List<GalleryPhoto>>(emptyList()) }
    var selectedPhoto by remember { mutableStateOf<GalleryPhoto?>(null) }
    var showSocialShare by remember { mutableStateOf(false) }
    var hasGalleryPermission by remember { mutableStateOf(false) }
    var hasCameraPermission by remember { mutableStateOf(false) }
    var isLoadingPhotos by remember { mutableStateOf(true) }

    // ✅ ESTADO SIMPLE: GALERÍA ABIERTA O CERRADA
    var showGallery by remember { mutableStateOf(false) }

    // 📷 CameraX setup
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    // Permisos
    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasGalleryPermission = isGranted
        if (isGranted) {
            scope.launch {
                photos = loadPhotosFromGallery(context)
                isLoadingPhotos = false
            }
        } else {
            isLoadingPhotos = false
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    // Inicialización
    LaunchedEffect(Unit) {
        val galleryPermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        hasGalleryPermission = ContextCompat.checkSelfPermission(context, galleryPermission) == PackageManager.PERMISSION_GRANTED
        hasCameraPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

        if (hasGalleryPermission) {
            scope.launch {
                photos = loadPhotosFromGallery(context)
                isLoadingPhotos = false
            }
        } else {
            galleryPermissionLauncher.launch(galleryPermission)
        }

        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Función para tomar foto
    fun capturePhoto() {
        val imageCapture = imageCapture ?: return

        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis())
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(
            File(context.cacheDir, "TraiScore_$name.jpg")
        ).build()

        imageCapture.takePicture(
            outputFileOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraGallery", "❌ Error capturando foto", exception)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d("CameraGallery", "📷 Foto guardada exitosamente")
                    val savedUri = Uri.fromFile(File(context.cacheDir, "TraiScore_$name.jpg"))
                    selectedPhoto = GalleryPhoto(
                        id = System.currentTimeMillis(),
                        uri = savedUri,
                        displayName = "Nueva foto",
                        dateAdded = System.currentTimeMillis()
                    )
                    showSocialShare = true
                }
            }
        )
    }

    // UI Principal
    if (showSocialShare && selectedPhoto != null) {
        SocialMediaScreen(
            photo = selectedPhoto!!.uri,
            exerciseName = exerciseName,
            exerciseNameMaxReps = exerciseNameMaxReps,
            topWeight = oneRepMax,
            totalWeight = totalWeight,
            maxReps = maxReps,
            trainingDays = trainingDays,
            onDismiss = {
                navController.popBackStack()
            },
            onShare = { bitmap ->
                Log.d("CameraGallery", "📤 Compartir bitmap")
            }
        )
    } else if (showGallery) {
        // ✅ PANTALLA COMPLETA DE GALERÍA
        FullGalleryScreen(
            photos = photos,
            isLoading = isLoadingPhotos,
            hasPermission = hasGalleryPermission,
            onPhotoSelected = { photo ->
                selectedPhoto = photo
                showGallery = false
                showSocialShare = true
            },
            onRequestPermission = {
                val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_IMAGES
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
                galleryPermissionLauncher.launch(permission)
            },
            onClose = { showGallery = false }
        )
    } else {
        // ✅ PANTALLA DE CÁMARA CON BARRA INFERIOR
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // 📷 Vista de cámara REAL
            if (hasCameraPermission) {
                RealCameraView(
                    onCameraSetup = { capture, provider ->
                        imageCapture = capture
                        cameraProvider = provider
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Mostrar mensaje de permisos
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Se requiere permiso de cámara",
                            color = Color.White,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }
                        ) {
                            Text("Permitir Cámara")
                        }
                    }
                }
            }

            // ❌ Botón cerrar (top-left)
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // 📸 Botón de captura (bottom-center)
            FloatingActionButton(
                onClick = { capturePhoto() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 150.dp) // Espacio para la barra inferior
                    .size(80.dp),
                containerColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color.White, CircleShape)
                            .clip(CircleShape)
                    )
                }
            }

            // ✅ BARRA INFERIOR ESTILO INSTAGRAM
            InstagramGalleryBar(
                onClick = { showGallery = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
            )
        }
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            cameraProvider?.unbindAll()
        }
    }
}

// ✅ NUEVO COMPOSABLE: BARRA INFERIOR ESTILO INSTAGRAM
@Composable
private fun InstagramGalleryBar(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "Galería",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Galería",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Abrir",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ✅ NUEVO COMPOSABLE: PANTALLA COMPLETA DE GALERÍA
@Composable
private fun FullGalleryScreen(
    photos: List<GalleryPhoto>,
    isLoading: Boolean,
    hasPermission: Boolean,
    onPhotoSelected: (GalleryPhoto) -> Unit,
    onRequestPermission: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header con botón cerrar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "Seleccionar foto",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                // Spacer para balance
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Contenido de la galería
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = traiBlue)
                    }
                }

                !hasPermission -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "📷",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Acceso a Galería",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Permite acceso para ver tus fotos",
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onRequestPermission,
                            colors = ButtonDefaults.buttonColors(containerColor = traiBlue)
                        ) {
                            Text("Permitir Acceso")
                        }
                    }
                }

                photos.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay fotos",
                            fontSize = 18.sp,
                            color = Color.Gray
                        )
                    }
                }

                else -> {
                    // Grid de fotos
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(photos) { photo ->
                            PhotoGridItem(
                                photo = photo,
                                isSelected = false,
                                onClick = { onPhotoSelected(photo) },
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}

// ✅ FUNCIÓN MEJORADA PARA CARGAR TODAS LAS FOTOS
private suspend fun loadPhotosFromGallery(context: Context): List<GalleryPhoto> {
    return withContext(Dispatchers.IO) {
        val photos = mutableListOf<GalleryPhoto>()

        try {
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED
            )

            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val dateAdded = cursor.getLong(dateColumn)

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    photos.add(
                        GalleryPhoto(
                            id = id,
                            uri = contentUri,
                            displayName = name,
                            dateAdded = dateAdded
                        )
                    )
                }
            }

            Log.d("CameraGallery", "📸 Cargadas ${photos.size} fotos")
            photos // ✅ TODAS LAS FOTOS, SIN LÍMITE
        } catch (e: Exception) {
            Log.e("CameraGallery", "❌ Error cargando fotos", e)
            emptyList()
        }
    }
}

@Composable
private fun RealCameraView(
    onCameraSetup: (ImageCapture, ProcessCameraProvider) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val preview = remember { Preview.Builder().build() }
    val imageCapture = remember { ImageCapture.Builder().build() }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                preview.setSurfaceProvider(previewView.surfaceProvider)

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture
                    )
                    onCameraSetup(imageCapture, cameraProvider)
                } catch (exc: Exception) {
                    Log.e("CameraGallery", "Error iniciando cámara", exc)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = modifier
    )
}

// ✅ COMPOSABLE: PhotoGridItem
@Composable
private fun PhotoGridItem(
    photo: GalleryPhoto,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(modifier = modifier) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(photo.uri)
                .build(),
            contentDescription = "Foto de galería",
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClick() },
            contentScale = ContentScale.Crop
        )

        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(traiBlue.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Seleccionada",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}