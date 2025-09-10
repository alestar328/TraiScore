package com.develop.traiscore.presentation.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.develop.traiscore.R
import com.develop.traiscore.presentation.theme.traiBlue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream


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
    var captureFunction by remember { mutableStateOf<(suspend () -> Bitmap)?>(null) } // ✅ CAMBIAR TIPO

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
                                // ✅ USAR LA FUNCIÓN DE CAPTURA SUSPENDIDA
                                val bitmap = captureFunction!!()

                                // ✅ GUARDAR Y COMPARTIR LA IMAGEN CAPTURADA
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
    onCaptureReady: (suspend () -> Bitmap) -> Unit, // ✅ CAMBIAR A SUSPEND FUNCTION
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val graphicsLayer = rememberGraphicsLayer()

    // ✅ NOTIFICAR QUE LA CAPTURA ESTÁ LISTA
    LaunchedEffect(Unit) {
        onCaptureReady {
            graphicsLayer.toImageBitmap().asAndroidBitmap()
        }
    }

    Box(
        modifier = modifier
            .drawWithCache {
                onDrawWithContent {
                    // Dibujar el contenido normalmente
                    drawContent()
                    // También dibujarlo en el graphics layer para captura
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
        // Overlay de datos centrado
        StatsOverlay(
            exerciseName = exerciseName,
            exerciseNameMaxReps = exerciseNameMaxReps,
            topWeight = topWeight,
            maxReps = maxReps,
            totalWeight = totalWeight,
            trainingDays = trainingDays,
            modifier = Modifier.fillMaxSize()
        )
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
            text = "TRAINING SESSION",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            ),
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
        )

        // ✅ CENTRAR LOS STATS MÁS ABAJO, LIGERAMENTE POR ENCIMA DE LOS BOTONES
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 120.dp), // Ajustado para estar por encima de los botones
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp) // ✅ ESPACIADO UNIFORME ENTRE ELEMENTOS
        ) {
            // Stat principal destacado
            HighlightStat(
                text = "${totalWeight.toInt()} kg",
                label = stringResource(id = R.string.filter_total_weight_today)
            )

            // ✅ CONVERTIR A ROWS INDIVIDUALES PARA MEJOR CONTROL DEL ESPACIADO
            // Bloque de estadísticas con mejor fondo
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color.Black.copy(alpha = 0.7f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp) // ✅ MAYOR ESPACIADO ENTRE ROWS
            ) {
                // ✅ ROW 1: Ejercicio principal
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

                // ✅ ROW 2: Repeticiones máximas
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

                // ✅ ROW 3: Sesiones de entrenamiento (centrado)
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

@Composable
private fun StatBlock(title: String, stat: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = stat,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }

        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = traiBlue
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
        // ✅ GUARDAR BITMAP EN ARCHIVO TEMPORAL
        val filename = "traiscore_share_${System.currentTimeMillis()}.jpg"
        val file = File(context.cacheDir, filename)

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }

        // ✅ CREAR URI PARA COMPARTIR
        val fileUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider", // dinámico con flavors
            file
        )

        // ✅ CAMBIAR AL HILO PRINCIPAL PARA COMPARTIR
        withContext(Dispatchers.Main) {
            shareImageToSocialMedia(context, fileUri, exerciseName, exerciseNameMaxReps,topWeight, maxReps)
        }

    } catch (e: Exception) {
        android.util.Log.e("SocialMediaScreen", "Error saving bitmap: ${e.message}")
    }
}

// ✅ FUNCIÓN PARA COMPARTIR EN REDES SOCIALES (ACTUALIZADA)
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
            type = "image/*" // Usa "image/*" para la imagen
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, shareText)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooserIntent = Intent.createChooser(shareIntent, "Compartir en redes sociales")

        // Esta línea es crucial si la función no se llama desde una Activity.
        // Asegúrate de que tu `context` sea una `Activity` o añade esta flag.
        if (context !is android.app.Activity) {
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(chooserIntent)

    } catch (e: Exception) {
        android.util.Log.e("SocialMediaScreen", "Error al compartir: ${e.message}")
    }
}
private fun buildInstagramStoryIntent(
    context: Context,
    imageUri: Uri
): Intent? {
    val pkg = "com.instagram.android"
    val pm = context.packageManager
    val isInstalled = try {
        pm.getPackageInfo(pkg, 0); true
    } catch (_: Exception) { false }
    if (!isInstalled) return null

    // Concede permiso a Instagram para leer tu content://
    context.grantUriPermission(
        pkg,
        imageUri,
        Intent.FLAG_GRANT_READ_URI_PERMISSION
    )

    // Intent oficial para historias
    return Intent("com.instagram.share.ADD_TO_STORY").apply {
        setPackage(pkg)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        // Dos vías que funcionan según versión de IG:
        // 1) Extra backgroundImage (nuevo)
        putExtra("com.instagram.sharedSticker.backgroundImage", imageUri)
        type = "image/*"

        // 2) (opcional) además fija data+type (antiguo, mantiene compat)
        setDataAndType(imageUri, "image/*")

        // Extras opcionales
        putExtra("source_application", context.packageName)
        // putExtra("top_background_color", "#000000")
        // putExtra("bottom_background_color", "#000000")
    }
}