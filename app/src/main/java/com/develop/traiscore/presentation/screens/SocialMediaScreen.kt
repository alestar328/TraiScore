package com.develop.traiscore.presentation.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.develop.traiscore.R
import com.develop.traiscore.presentation.theme.TraiScoreTheme
import com.develop.traiscore.presentation.theme.traiBlue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt


@Composable
fun SocialMediaScreen(
    photo: Uri,
    exerciseName: String,
    exerciseNameMaxReps: String,
    topWeight: Float,
    maxReps: Int,
    trainingDays: Int,
    totalWeight: Double,
    onDismiss: () -> Unit,
    onShare: (Bitmap) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isCapturing by remember { mutableStateOf(false) }
    var captureFunction by remember { mutableStateOf<(suspend () -> Bitmap)?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .systemBarsPadding()
            .navigationBarsPadding()
    ) {
        // ✅ CONTENIDO A CAPTURAR (CON SU PROPIO drawWithCache)
        CapturedContent(
            photo = photo,
            exerciseName = exerciseName,
            exerciseNameMaxReps = exerciseNameMaxReps,
            topWeight = topWeight,
            maxReps = maxReps,
            totalWeight = totalWeight,
            trainingDays = trainingDays,
            onCaptureReady = { captureFunc ->
                captureFunction = captureFunc
            },
            modifier = Modifier.fillMaxSize()
        )

        // ✅ BOTONES FUERA DEL ÁREA DE CAPTURA
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FloatingActionButton(
                onClick = onDismiss,
                containerColor = Color.Red.copy(alpha = 0.9f)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = Color.White
                )
            }

            FloatingActionButton(
                onClick = {
                    if (!isCapturing && captureFunction != null) {
                        isCapturing = true
                        scope.launch {
                            try {
                                val bitmap = captureFunction!!()
                                shareImageWithData(
                                    context = context,
                                    bitmap = bitmap,
                                    exerciseName = exerciseName,
                                    exerciseNameMaxReps = exerciseNameMaxReps,
                                    topWeight = topWeight,
                                    maxReps = maxReps
                                )
                            } catch (e: Exception) {
                                android.util.Log.e("SocialMediaScreen", "Error capturing: ${e.message}")
                            } finally {
                                isCapturing = false
                            }
                        }
                    }
                },
                containerColor = if (isCapturing)
                    Color.Gray.copy(alpha = 0.7f)
                else
                    traiBlue.copy(alpha = 0.9f)
            ) {
                if (isCapturing) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.padding(4.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Compartir",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

// ✅ COMPOSABLE SEPARADO PARA EL CONTENIDO QUE SE CAPTURA
@Composable
private fun CapturedContent(
    photo: Uri,
    exerciseName: String,
    exerciseNameMaxReps: String,
    totalWeight: Double,
    topWeight: Float,
    maxReps: Int,
    trainingDays: Int,
    onCaptureReady: (suspend () -> Bitmap) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val graphicsLayer = rememberGraphicsLayer()
    val displayExerciseName = exerciseName.ifEmpty {
        stringResource(R.string.filter_no_main_exercise)
    }
    val displayExerciseMaxReps = exerciseNameMaxReps.ifEmpty {
        stringResource(R.string.filter_no_max_reps)
    }

    LaunchedEffect(Unit) {
        onCaptureReady {
            graphicsLayer.toImageBitmap().asAndroidBitmap()
        }
    }

    Box(
        modifier = modifier
            .drawWithCache {
                onDrawWithContent {
                    drawContent()
                    graphicsLayer.record {
                        this@onDrawWithContent.drawContent()
                    }
                }
            }
    ) {
        // Imagen de fondo
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(photo)
                .build(),
            contentDescription = "Foto capturada",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillHeight
        )

        // ✅ OVERLAY OSCURO QUE CUBRE TODA LA IMAGEN
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f),
                            Color.Black.copy(alpha = 0.2f),
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.6f)
                        )
                    )
                )
        )

        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.trailogoup),
            contentDescription = "TraiScore Logo",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(20.dp)
                .height(40.dp)
        )

        // ✅ OVERLAY CON ELEMENTOS TRANSFORMABLES
        TransformableStatsOverlay(
            exerciseName = displayExerciseName,
            exerciseNameMaxReps = displayExerciseMaxReps,
            topWeight = topWeight,
            maxReps = maxReps,
            totalWeight = totalWeight,
            trainingDays = trainingDays,
            modifier = Modifier.fillMaxSize()
        )
    }
}

// ✅ NUEVO COMPOSABLE CON ELEMENTOS TRANSFORMABLES
@Composable
private fun TransformableStatsOverlay(
    exerciseName: String,
    exerciseNameMaxReps: String,
    topWeight: Float,
    maxReps: Int,
    totalWeight: Double,
    trainingDays: Int,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    // Estados para HighlightStat
    var highlightScale by remember { mutableFloatStateOf(1f) }
    var highlightOffset by remember { mutableStateOf(Offset.Zero) }

    // Estados para FilterStats
    var filterScale by remember { mutableFloatStateOf(1f) }
    var filterOffset by remember { mutableStateOf(Offset.Zero) }

    // ✅ NUEVOS ESTADOS PARA EL COLOR DE LOS TEXTOS
    var isTitleBlack by remember { mutableStateOf(false) }
    var isHighlightStatBlack by remember { mutableStateOf(false) }

    // ✅ NUEVO ESTADO PARA CONTROLAR EL LAYOUT
    var isVerticalLayout by remember { mutableStateOf(false) }

    // Estados transformables para HighlightStat
    val highlightTransformableState = rememberTransformableState { zoomChange, panChange, _ ->
        highlightScale = (highlightScale * zoomChange).coerceIn(0.5f, 3f)
        highlightOffset += panChange
    }

    // Estados transformables para FilterStats
    val filterTransformableState = rememberTransformableState { zoomChange, panChange, _ ->
        filterScale = (filterScale * zoomChange).coerceIn(0.5f, 3f)
        filterOffset += panChange
    }

    Box(modifier = modifier) {
        // ✅ Título clickeable con toggle de color
        TrainingSessionTitle(
            isBlack = isTitleBlack,
            onClick = { isTitleBlack = !isTitleBlack },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
        )

        // ✅ HighlightStat transformable y clickeable con toggle de color
        TransformableHighlightStat(
            text = "${totalWeight.toInt()} kg",
            label = stringResource(id = R.string.filter_total_weight_today),
            scale = highlightScale,
            offset = highlightOffset,
            transformableState = highlightTransformableState,
            initialAlignment = Alignment.Center,
            initialOffsetY = -100.dp,
            isBlack = isHighlightStatBlack,
            onClick = { isHighlightStatBlack = !isHighlightStatBlack },
            modifier = Modifier.fillMaxSize()
        )

        // ✅ CONDICIÓN PARA MOSTRAR LAYOUT HORIZONTAL O VERTICAL
        if (isVerticalLayout) {
            TransformableFilterStatsVertical(
                exerciseName = exerciseName,
                exerciseNameMaxReps = exerciseNameMaxReps,
                topWeight = topWeight,
                maxReps = maxReps,
                trainingDays = trainingDays,
                scale = filterScale,
                offset = filterOffset,
                transformableState = filterTransformableState,
                initialAlignment = Alignment.BottomCenter,
                initialOffsetY = (-120).dp,
                onClick = { isVerticalLayout = false },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            TransformableFilterStats(
                exerciseName = exerciseName,
                exerciseNameMaxReps = exerciseNameMaxReps,
                topWeight = topWeight,
                maxReps = maxReps,
                trainingDays = trainingDays,
                scale = filterScale,
                offset = filterOffset,
                transformableState = filterTransformableState,
                initialAlignment = Alignment.BottomCenter,
                initialOffsetY = (-120).dp,
                onClick = { isVerticalLayout = true },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun TrainingSessionTitle(
    isBlack: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(id = R.string.filter_title),
        style = MaterialTheme.typography.headlineLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp
        ),
        color = if (isBlack) Color.Black else Color.White,
        modifier = modifier.clickable { onClick() }
    )
}
// ✅ HighlightStat transformable
@Composable
private fun TransformableHighlightStat(
    text: String,
    label: String,
    scale: Float,
    offset: Offset,
    transformableState: androidx.compose.foundation.gestures.TransformableState,
    initialAlignment: Alignment,
    initialOffsetY: androidx.compose.ui.unit.Dp,
    isBlack: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    Box(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(initialAlignment)
                .offset {
                    IntOffset(
                        (offset.x).roundToInt(),
                        (offset.y + with(density) { initialOffsetY.toPx() }).roundToInt()
                    )
                }
                .scale(scale)
                .transformable(transformableState)
                .clickable { onClick() } // ✅ AÑADIDO: Click para cambiar color
        ) {
            Text(
                text = text,
                fontSize = 56.sp,
                color = if (isBlack) Color.Black else Color.White,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = label,
                fontSize = 16.sp,
                color = if (isBlack) Color.Black.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// ✅ FilterStats transformable
@Composable
private fun TransformableFilterStats(
    exerciseName: String,
    exerciseNameMaxReps: String,
    topWeight: Float,
    maxReps: Int,
    trainingDays: Int,
    scale: Float,
    offset: Offset,
    transformableState: androidx.compose.foundation.gestures.TransformableState,
    initialAlignment: Alignment,
    initialOffsetY: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .align(initialAlignment)
                .offset {
                    IntOffset(
                        (offset.x).roundToInt(),
                        (offset.y + with(density) { initialOffsetY.toPx() }).roundToInt()
                    )
                }
                .scale(scale)
                .transformable(transformableState)
                .clickable { onClick() }
                .fillMaxWidth()
                .background(
                    Color.Black.copy(alpha = 0.7f),
                    RoundedCornerShape(16.dp)
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ROW 1: Ejercicio principal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(id = R.string.filter_main_exercise),
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = exerciseName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
                Text(
                    text = "${topWeight.toInt()} kg",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = traiBlue
                )
            }

            // ROW 2: Repeticiones máximas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(id = R.string.filter_max_reps),
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = exerciseNameMaxReps,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
                Text(
                    text = "$maxReps reps",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = traiBlue
                )
            }

            // ROW 3: Sesiones de entrenamiento
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$trainingDays " + stringResource(R.string.filter_total_sessions_month),
                    color = Color.White,
                    fontSize = 16.sp,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// ✅ MANTENER LOS COMPOSABLES ORIGINALES PARA COMPATIBILIDAD
@Composable
private fun FilterStats(
    exerciseName: String,
    exerciseNameMaxReps: String,
    topWeight: Float,
    maxReps: Int,
    trainingDays: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Color.Black.copy(alpha = 0.7f),
                RoundedCornerShape(16.dp)
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ROW 1: Ejercicio principal
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(id = R.string.filter_main_exercise),
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = exerciseName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
            Text(
                text = "${topWeight.toInt()} kg",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = traiBlue
            )
        }

        // ROW 2: Repeticiones máximas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(id = R.string.filter_max_reps),
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = exerciseNameMaxReps,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
            Text(
                text = "$maxReps reps",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = traiBlue
            )
        }

        // ROW 3: Sesiones de entrenamiento
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$trainingDays " + stringResource(R.string.filter_total_sessions_month),
                color = Color.White,
                fontSize = 16.sp,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
@Composable
private fun TransformableFilterStatsVertical(
    exerciseName: String,
    exerciseNameMaxReps: String,
    topWeight: Float,
    maxReps: Int,
    trainingDays: Int,
    scale: Float,
    offset: Offset,
    transformableState: androidx.compose.foundation.gestures.TransformableState,
    initialAlignment: Alignment,
    initialOffsetY: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .align(initialAlignment)
                .offset {
                    IntOffset(
                        (offset.x).roundToInt(),
                        (offset.y + with(density) { initialOffsetY.toPx() }).roundToInt()
                    )
                }
                .scale(scale)
                .transformable(transformableState)
                .clickable { onClick() }
                .width(IntrinsicSize.Min)
                .background(
                    Color.Black.copy(alpha = 0.7f),
                    RoundedCornerShape(20.dp)
                )
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // BLOQUE 1: Peso más alto / PR de hoy
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.filter_main_exercise),
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = exerciseName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = "${topWeight.toInt()} kg",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = traiBlue
                )
            }

            // Divider visual
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                color = Color.White.copy(alpha = 0.3f)
            )

            // BLOQUE 2: Máximas repeticiones
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.filter_max_reps),
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = exerciseNameMaxReps,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = "$maxReps reps",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = traiBlue
                )
            }

            // Divider visual
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                color = Color.White.copy(alpha = 0.3f)
            )

            // BLOQUE 3: Sesiones del mes
            Text(
                text = "$trainingDays " + stringResource(R.string.filter_total_sessions_month),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}
@Composable
private fun StatsOverlay(
    exerciseName: String,
    exerciseNameMaxReps: String,
    topWeight: Float,
    maxReps: Int,
    totalWeight: Double,
    trainingDays: Int,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Text(
            text = stringResource(id = R.string.filter_title),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            ),
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            HighlightStat(
                text = "${totalWeight.toInt()} kg",
                label = stringResource(id = R.string.filter_total_weight_today)
            )
            FilterStats(
                exerciseName = exerciseName,
                exerciseNameMaxReps = exerciseNameMaxReps,
                topWeight = topWeight,
                maxReps = maxReps,
                trainingDays = trainingDays
            )
        }
    }
}

@Composable
private fun HighlightStat(text: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = text,
            fontSize = 56.sp,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = label,
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private suspend fun shareImageWithData(
    context: Context,
    bitmap: Bitmap,
    exerciseName: String,
    exerciseNameMaxReps: String,
    topWeight: Float,
    maxReps: Int
) = withContext(Dispatchers.IO) {
    try {
        val filename = "traiscore_share_${System.currentTimeMillis()}.jpg"
        val file = File(context.cacheDir, filename)

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }

        val fileUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        withContext(Dispatchers.Main) {
            shareImageToSocialMedia(context, fileUri, exerciseName, exerciseNameMaxReps,topWeight, maxReps)
        }

    } catch (e: Exception) {
        android.util.Log.e("SocialMediaScreen", "Error saving bitmap: ${e.message}")
    }
}

private fun shareImageToSocialMedia(
    context: Context,
    imageUri: Uri,
    exerciseName: String,
    exerciseNameMaxReps: String,
    topWeight: Float,
    maxReps: Int
) {
    try {
        val shareText = buildString {
            append("💪 ¡Nuevo récord en el gym! 💪\n\n")
            append("🏋️ Mayor peso: $exerciseName (${topWeight.toInt()} kg)\n")
            append("🔥 Más reps: $exerciseNameMaxReps ($maxReps reps)\n\n")
            append("#TraiScore #Gym #Fitness #Training")
        }

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, shareText)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooserIntent = Intent.createChooser(shareIntent, "Compartir en redes sociales")

        if (context !is android.app.Activity) {
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(chooserIntent)

    } catch (e: Exception) {
        android.util.Log.e("SocialMediaScreen", "Error al compartir: ${e.message}")
    }
}

// Preview específico del overlay de estadísticas
@Preview(
    name = "StatsOverlay Only",
    showBackground = true,
    backgroundColor = 0xFF1a1a1a,
    heightDp = 400,
    apiLevel = 34
)
@Composable
fun StatsOverlayPreview() {
    TraiScoreTheme {
        StatsOverlay(
            exerciseName = "Press Banca",
            exerciseNameMaxReps = "Sentadillas",
            topWeight = 85.5f,
            maxReps = 12,
            totalWeight = 2450.0,
            trainingDays = 18,
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
        )
    }
}