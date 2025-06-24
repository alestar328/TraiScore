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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.develop.traiscore.R
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.theme.traiOrange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.abs

data class GalleryPhoto(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val dateAdded: Long
)


@Composable
fun CameraGalleryScreen(
    exerciseName: String,
    oneRepMax: Float,
    maxReps: Int,
    totalWeight: Double,
    trainingDays: Int,
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }

    // Estados principales
    var photos by remember { mutableStateOf<List<GalleryPhoto>>(emptyList()) }
    var selectedPhoto by remember { mutableStateOf<GalleryPhoto?>(null) }
    var showSocialShare by remember { mutableStateOf(false) }
    var hasGalleryPermission by remember { mutableStateOf(false) }
    var hasCameraPermission by remember { mutableStateOf(false) }
    var isLoadingPhotos by remember { mutableStateOf(true) }

    // Estados de UI
    var galleryOffset by remember { mutableStateOf(screenHeight * 0.7f) } // Galería 70% oculta inicialmente
    var isDragging by remember { mutableStateOf(false) }
    var captureButtonScale by remember { mutableStateOf(1f) }

    // Animación suave del offset
    val animatedOffset by animateFloatAsState(
        targetValue = galleryOffset,
        animationSpec = tween(durationMillis = 300),
        label = "galleryOffset"
    )

    // Cámara
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    val photoFile = remember {
        File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri?.let { uri ->
                Log.d("CameraGallery", "📷 Foto capturada: $uri")
                selectedPhoto = GalleryPhoto(
                    id = System.currentTimeMillis(),
                    uri = uri,
                    displayName = "Nueva foto",
                    dateAdded = System.currentTimeMillis()
                )
                showSocialShare = true
            }
        }
    }

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
        // Verificar permisos de galería
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
        if (hasCameraPermission) {
            val uri = FileProvider.getUriForFile(
                context,
                "com.develop.traiscore.fileprovider",
                photoFile
            )
            photoUri = uri
            cameraLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // UI Principal
    if (showSocialShare && selectedPhoto != null) {
        SocialMediaScreen(
            photo = selectedPhoto!!.uri,
            exerciseName = exerciseName,
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
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // 📷 Área de cámara
            CameraViewArea(
                onClose = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize()
            )

            // 📸 Botón de captura
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp)
                    .scale(captureButtonScale)
            ) {
                FloatingActionButton(
                    onClick = { capturePhoto() },
                    modifier = Modifier.size(80.dp),
                    containerColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(Color.White, CircleShape)
                                .clip(CircleShape)
                                .clickable { capturePhoto() }
                        )
                    }
                }
            }

            // 🖼️ Panel de galería deslizable
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight.dp)
                    .graphicsLayer {
                        translationY = animatedOffset
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                isDragging = true
                            },
                            onDragEnd = {
                                isDragging = false
                                // Snap a posiciones específicas
                                val snapPosition = when {
                                    galleryOffset < screenHeight * 0.3f -> 0f // Galería completamente visible
                                    galleryOffset < screenHeight * 0.6f -> screenHeight * 0.5f // Galería media
                                    else -> screenHeight * 0.7f // Galería mínima
                                }
                                galleryOffset = snapPosition
                            }
                        ) { change, dragAmount ->
                            val newOffset = galleryOffset + dragAmount.y
                            galleryOffset = newOffset.coerceIn(0f, screenHeight * 0.8f)
                        }
                    }
            ) {
                GalleryPanel(
                    photos = photos,
                    selectedPhoto = selectedPhoto,
                    isLoading = isLoadingPhotos,
                    hasPermission = hasGalleryPermission,
                    onPhotoSelected = { photo ->
                        selectedPhoto = photo
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
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 📊 Indicador de galería (handle)
            GalleryHandle(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (animatedOffset - screenHeight).dp)
                    .padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun CameraViewArea(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(Color.Black)
    ) {
        // Simulación de vista de cámara (en una implementación real, usarías CameraX)
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_camara),
                contentDescription = "Cámara",
                tint = Color.Gray,
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Vista de Cámara",
                color = Color.Gray,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Desliza hacia arriba para ver la galería",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }

        // Botón cerrar
        IconButton(
            onClick = onClose,
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
    }
}

@Composable
private fun GalleryPanel(
    photos: List<GalleryPhoto>,
    selectedPhoto: GalleryPhoto?,
    isLoading: Boolean,
    hasPermission: Boolean,
    onPhotoSelected: (GalleryPhoto) -> Unit,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Handle visual
            GalleryHandle(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 8.dp)
            )

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
                        Text(
                            text = "No hay fotos",
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(photos) { photo ->
                            PhotoGridItem(
                                photo = photo,
                                isSelected = selectedPhoto?.id == photo.id,
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

@Composable
private fun GalleryHandle(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(40.dp)
            .height(4.dp)
            .background(
                Color.White.copy(alpha = 0.5f),
                RoundedCornerShape(2.dp)
            )
    )
}

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
                    .background(traiBlue.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Seleccionada",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// Función para cargar fotos (misma que antes)
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
            photos.take(100) // Limitar a 100 fotos más recientes
        } catch (e: Exception) {
            Log.e("CameraGallery", "❌ Error cargando fotos", e)
            emptyList()
        }
    }
}