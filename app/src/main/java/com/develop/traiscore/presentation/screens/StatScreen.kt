package com.develop.traiscore.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.develop.traiscore.R
import com.develop.traiscore.domain.model.BodyMeasurementProgressData
import com.develop.traiscore.domain.model.BodyMeasurementType
import com.develop.traiscore.presentation.components.CircularProgressView
import com.develop.traiscore.presentation.components.FilterableDropdown
import com.develop.traiscore.presentation.components.LineChartView
import com.develop.traiscore.presentation.components.ProgressRadarChart
import com.develop.traiscore.presentation.components.ToggleButtonRowStats
import com.develop.traiscore.presentation.components.TraiScoreTopBar
import com.develop.traiscore.presentation.theme.TraiScoreTheme
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.theme.traiOrange
import com.develop.traiscore.presentation.theme.tsColors
import com.develop.traiscore.presentation.viewmodels.BodyStatsViewModel
import com.develop.traiscore.presentation.viewmodels.StatScreenViewModel
import java.io.File

@Composable
fun StatScreen(
    modifier: Modifier = Modifier,
    viewModel: StatScreenViewModel = hiltViewModel(),
    bodyStatsViewModel: BodyStatsViewModel = hiltViewModel(),
    clientId: String? = null,
    navController: NavController
) {
    val exerOptions by viewModel.exerciseOptions.collectAsState()
    val weightData by viewModel.weightProgress.collectAsState()
    val repsData by viewModel.repsProgress.collectAsState()
    val circularData by viewModel.circularData.collectAsState()
    val selected by viewModel.selectedExercise.collectAsState()
    var selectedTab by remember { mutableStateOf("Mis records") }

    val totalKg by viewModel.totalWeightSum.collectAsState()

    val (oneRepMax, maxReps, averageRIR) = circularData
    val weightByReps = remember(weightData) {
        weightData.map { (_, peso) ->
            peso.toInt().toString() to peso
        }
    }
    var bodyMeasurementData by remember { mutableStateOf<BodyMeasurementProgressData?>(null) }
    var availableBodyMetrics by remember { mutableStateOf<List<BodyMeasurementType>>(emptyList()) }
    var selectedBodyMetric by remember { mutableStateOf<BodyMeasurementType?>(null) }
    var bodyChartData by remember { mutableStateOf<List<Pair<String, Float>>>(emptyList()) }
    var isLoadingBodyData by remember { mutableStateOf(false) }

    var showAchievements by remember { mutableStateOf(false) }

    val radarChartData by viewModel.radarChartData.collectAsState()
    val isLoadingRadarData by viewModel.isLoadingRadarData.collectAsState()

    var photoUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    var showSocialShare by remember { mutableStateOf(false) }
    var capturedPhotoUri by remember { mutableStateOf<Uri?>(null) }
// Crear archivo temporal para la foto
    val photoFile = remember {
        File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
    }
    val todayTotalWeight by viewModel.todayTotalWeight.collectAsState()
    val currentMonthTrainingDays by viewModel.currentMonthTrainingDays.collectAsState()
    val scope = rememberCoroutineScope()
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri?.let { uri ->
                Log.d("StatScreen", "Foto capturada: $uri")
                capturedPhotoUri = uri
                showSocialShare = true // Mostrar el overlay
            }
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
            Log.d("StatScreen", "Permiso de cámara denegado")
        }
    }



    // Función para abrir la cámara
    fun openCamera() {
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
    LaunchedEffect(clientId) {
        clientId?.let { id ->
            bodyStatsViewModel.setTargetUser(id)
        }
    }
    LaunchedEffect(selected, weightData, repsData) {
        Log.d("StatScreen", "Ejercicio=$selected  pesos=$weightData  reps=$repsData")
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == "Mis medidas") {
            isLoadingBodyData = true
            bodyStatsViewModel.getBodyMeasurementProgressData { success, data, error ->
                isLoadingBodyData = false
                if (success && data != null) {
                    bodyMeasurementData = data
                    availableBodyMetrics = data.getAvailableMetricsForChart()
                    // Seleccionar la primera métrica disponible por defecto
                    if (availableBodyMetrics.isNotEmpty() && selectedBodyMetric == null) {
                        selectedBodyMetric = availableBodyMetrics.first()
                    }
                } else {
                    Log.e("StatScreen", "Error cargando datos corporales: $error")
                }
            }
        }
    }

    LaunchedEffect(selectedBodyMetric, bodyMeasurementData) {
        selectedBodyMetric?.let { metric ->
            bodyMeasurementData?.let { data ->
                bodyChartData = data.getChartDataFor(metric)
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = TraiScoreTheme.dimens.paddingMedium)
    ) {
        Scaffold(
            topBar = {
                TraiScoreTopBar(
                    leftIcon = {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clickable {
                                    navController.navigate(
                                        "social_media_camera?exercise=${selected ?: "Ejercicio"}&oneRepMax=$oneRepMax&maxReps=$maxReps&totalWeight=$todayTotalWeight&trainingDays=$currentMonthTrainingDays"
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_camara),
                                contentDescription = "Temporizador",
                                tint = MaterialTheme.tsColors.ledCyan,
                            )
                        }
                    },
                    rightIcon = {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clickable {
                                    showAchievements = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.trophy_icon),
                                contentDescription = "Logros",
                                tint = MaterialTheme.tsColors.ledCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                    }
                )
            },
            content = { paddingValues ->
                val isClientViewingOwnStats = clientId == null

                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .then(
                            if (isClientViewingOwnStats) {
                                Modifier.navigationBarsPadding()
                            } else {
                                Modifier
                            }
                        )
                        .background(MaterialTheme.tsColors.primaryBackgroundColor)
                        .fillMaxSize()
                ) {
                    // Toggle Buttons justo debajo del TopAppBar
                    ToggleButtonRowStats(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .animateContentSize(
                                animationSpec = tween(300)
                            )
                    )

                    // Contenido principal
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // TAB: Mis records
                        item {
                            AnimatedVisibility(
                                visible = selectedTab == "Mis records",
                                enter = fadeIn(tween(300)) + slideInHorizontally(
                                    initialOffsetX = { -it },
                                    animationSpec = tween(300)
                                ),
                                exit = fadeOut(tween(200)) + slideOutHorizontally(
                                    targetOffsetX = { -it },
                                    animationSpec = tween(200)
                                )
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    // Filtros
                                    Column {
                                        Text(
                                            text = "Filtrar por:",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = Color.White,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        FilterableDropdown(
                                            items = exerOptions,
                                            selectedValue = selected ?: "",
                                            placeholder = "Ejercicio",
                                            onItemSelected = { viewModel.onExerciseSelected(it) },
                                            modifier = Modifier.fillMaxWidth(),
                                            textFieldHeight = 48.dp,
                                            textSize = 13.sp,
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.2.dp)

                                            )
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }

                                    // Detalles del ejercicio
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.DarkGray, RoundedCornerShape(8.dp))
                                            .padding(10.dp)
                                    ) {
                                        // Peso
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            Text(
                                                text = "1RM: ${"%.1f".format(oneRepMax)}",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = traiBlue
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))

                                            CircularProgressView(
                                                progress = (oneRepMax / 150).toFloat()
                                                    .coerceIn(0f, 1f),
                                                maxLabel = "${"%.1f".format(oneRepMax)} Kg",
                                                modifier = Modifier.size(80.dp),
                                                strokeColor = Color.Cyan,
                                                backgroundColor = traiOrange
                                            )
                                        }
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            Text(
                                                text = "MR: $maxReps",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = traiBlue
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))

                                            CircularProgressView(
                                                progress = (maxReps / 15f).coerceIn(0f, 1f),
                                                maxLabel = "$maxReps reps",
                                                modifier = Modifier.size(80.dp),
                                                strokeColor = Color.Cyan,
                                                backgroundColor = traiOrange
                                            )
                                        }
                                        // RIR
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            Text(
                                                text = "Esfuerzo",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = traiBlue
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))

                                            CircularProgressView(
                                                progress = (averageRIR / 10f).coerceIn(0f, 1f),
                                                maxLabel = "$averageRIR RIR",
                                                modifier = Modifier.size(80.dp),
                                                strokeColor = Color.Cyan,
                                                backgroundColor = traiOrange
                                            )
                                        }

                                    }

                                    // Gráfica Por peso
                                    Column {
                                        Text(
                                            text = "Por peso:",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = Color.White,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        LineChartView(
                                            dataPoints = weightByReps,
                                            lineColor = Color.Cyan,
                                            backgroundChartColor = Color.DarkGray,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(120.dp)
                                                .padding(horizontal = 0.dp)
                                        )
                                    }

                                    // Gráfica Por repeticiones
                                    Column {
                                        Text(
                                            text = "Por repeticiones:",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = Color.White,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        LineChartView(
                                            dataPoints = repsData,
                                            lineColor = Color.Cyan,
                                            backgroundChartColor = Color.DarkGray,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(120.dp)
                                                .padding(horizontal = 0.dp)
                                        )
                                    }
                                    Column {

                                        if (isLoadingRadarData) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(300.dp)
                                                    .background(
                                                        Color.DarkGray,
                                                        RoundedCornerShape(8.dp)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    CircularProgressIndicator(color = traiBlue)
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        "Calculando progreso...",
                                                        color = Color.White,
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                }
                                            }
                                        } else if (radarChartData != null) {
                                            ProgressRadarChart(
                                                radarData = radarChartData!!,
                                                modifier = Modifier
                                                    .fillMaxWidth(),

                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(300.dp)
                                                    .background(
                                                        Color.DarkGray,
                                                        RoundedCornerShape(8.dp)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(
                                                        text = "📊",
                                                        style = MaterialTheme.typography.headlineLarge
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        "Sin datos de progreso",
                                                        color = Color.White,
                                                        style = MaterialTheme.typography.titleMedium
                                                    )
                                                    Text(
                                                        "Registra más entrenamientos para ver tu progreso",
                                                        color = Color.Gray,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }

                                        }
                                    }

                                }
                            }
                        }

                        // TAB: Mis medidas
                        item {
                            AnimatedVisibility(
                                visible = selectedTab == "Mis medidas",
                                enter = fadeIn(tween(300)) + slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(300)
                                ),
                                exit = fadeOut(tween(200)) + slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(200)
                                )
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    if (isLoadingBodyData) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                CircularProgressIndicator(color = traiBlue)
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text("Cargando medidas...", color = Color.White)
                                            }
                                        }
                                    } else if (availableBodyMetrics.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp)
                                                .background(
                                                    Color.DarkGray,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    "No hay suficientes datos",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = Color.White
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    "Necesitas al menos 2 registros de medidas para ver el progreso",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color.Gray,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    } else {
                                        // Dropdown para seleccionar métrica corporal
                                        Text(
                                            text = "Seleccionar medida:",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = Color.White,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        FilterableDropdown(
                                            items = availableBodyMetrics.map { it.displayName },
                                            selectedValue = selectedBodyMetric?.displayName ?: "",
                                            placeholder = "Medida corporal",
                                            onItemSelected = { selectedName ->
                                                selectedBodyMetric =
                                                    availableBodyMetrics.find { it.displayName == selectedName }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            textFieldHeight = 48.dp,
                                            textSize = 13.sp,
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Gráfico de progreso de medidas corporales
                                        selectedBodyMetric?.let { metric ->
                                            Text(
                                                text = "Progreso de ${metric.displayName}:",
                                                style = MaterialTheme.typography.titleLarge,
                                                color = Color.White,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )

                                            if (bodyChartData.isNotEmpty()) {
                                                LineChartView(
                                                    dataPoints = bodyChartData,
                                                    lineColor = traiBlue,
                                                    backgroundChartColor = Color.DarkGray,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(120.dp)
                                                        .padding(horizontal = 0.dp)
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(120.dp)
                                                        .background(
                                                            Color.DarkGray,
                                                            RoundedCornerShape(8.dp)
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        "Sin datos para esta medida",
                                                        color = Color.Gray
                                                    )
                                                }
                                            }

                                            // Resumen de estadísticas
                                            bodyMeasurementData?.getProgressSummary()?.get(metric)
                                                ?.let { summary ->
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .background(
                                                                Color.DarkGray,
                                                                RoundedCornerShape(8.dp)
                                                            )
                                                            .padding(16.dp)
                                                    ) {
                                                        Text(
                                                            text = "Resumen de ${metric.displayName}",
                                                            style = MaterialTheme.typography.titleMedium,
                                                            color = Color.White,
                                                            modifier = Modifier.padding(bottom = 8.dp)
                                                        )

                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Column {
                                                                Text(
                                                                    "Valor actual:",
                                                                    color = Color.Gray,
                                                                    style = MaterialTheme.typography.bodySmall
                                                                )
                                                                Text(
                                                                    summary.getFormattedLatestValue(),
                                                                    color = traiBlue
                                                                )
                                                            }
                                                            Column {
                                                                Text(
                                                                    "Cambio total:",
                                                                    color = Color.Gray,
                                                                    style = MaterialTheme.typography.bodySmall
                                                                )
                                                                Text(
                                                                    summary.getFormattedChange(),
                                                                    color = traiBlue
                                                                )
                                                            }
                                                            Column {
                                                                Text(
                                                                    "Registros:",
                                                                    color = Color.Gray,
                                                                    style = MaterialTheme.typography.bodySmall
                                                                )
                                                                Text(
                                                                    "${summary.totalRecords}",
                                                                    color = traiBlue
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
        )
        AchivementsUI(
            isVisible = showAchievements,
            onDismiss = { showAchievements = false },
            clientId = clientId // Pasar el clientId para mostrar logros del cliente correcto
        )




    }
}