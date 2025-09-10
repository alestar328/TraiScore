package com.develop.traiscore.presentation.screens

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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.develop.traiscore.R
import com.develop.traiscore.data.firebaseData.calculateTodayDataAndNavigate
import com.develop.traiscore.domain.model.BodyMeasurementProgressData
import com.develop.traiscore.domain.model.BodyMeasurementType
import com.develop.traiscore.presentation.components.ChronoScreen
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
import com.develop.traiscore.statics.StatTab
import java.io.File

@Composable
fun StatScreen(
    modifier: Modifier = Modifier,
    viewModel: StatScreenViewModel = hiltViewModel(),
    bodyStatsViewModel: BodyStatsViewModel = hiltViewModel(),
    clientId: String? = null,
    navController: NavController
) {
    var selectedTab by remember { mutableStateOf(StatTab.RECORDS) }
    val exerOptions by viewModel.exerciseOptions.collectAsState()
    val weightData by viewModel.weightProgress.collectAsState()
    val repsData by viewModel.repsProgress.collectAsState()
    val circularData by viewModel.circularData.collectAsState()
    val selected by viewModel.selectedExercise.collectAsState()
    var showChronoScreen by remember { mutableStateOf(false) }


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


    val radarChartData by viewModel.radarChartData.collectAsState()
    val isLoadingRadarData by viewModel.isLoadingRadarData.collectAsState()

    var photoUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    var showSocialShare by remember { mutableStateOf(false) }
    var capturedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val photoFile = remember {
        File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
    }
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




    LaunchedEffect(clientId) {
        clientId?.let { id ->
            bodyStatsViewModel.setTargetUser(id)
        }
    }
    LaunchedEffect(selected, weightData, repsData) {
        Log.d("StatScreen", "Ejercicio=$selected  pesos=$weightData  reps=$repsData")
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == StatTab.MEASUREMENTS) {
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
                                    calculateTodayDataAndNavigate(
                                        context = context,
                                        navController = navController,
                                        viewModel = viewModel,
                                        oneRepMax = oneRepMax,
                                        maxReps = maxReps
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_camara),
                                contentDescription = "Compartir sesión",
                                tint = MaterialTheme.tsColors.ledCyan,
                            )
                        }
                    },
                    rightIcon = {
                        FloatingActionButton(
                            onClick = {
                                println("⏱️ Icono de cronometro")
                                showChronoScreen = true

                            },
                            modifier = Modifier.size(30.dp),
                            containerColor = MaterialTheme.tsColors.ledCyan,
                            contentColor = Color.White,
                            elevation = FloatingActionButtonDefaults.elevation(0.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.timer_icon),
                                contentDescription = "Temporizador",
                                tint = Color.Black
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
                        .background(MaterialTheme.colorScheme.background)
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
                                visible = selectedTab == StatTab.RECORDS,
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
                                            text = stringResource(id = R.string.stats_filter),
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        FilterableDropdown(
                                            items = exerOptions,
                                            selectedValue = selected ?: "",
                                            placeholder = stringResource(id = R.string.stats_exercises),
                                            onItemSelected = { viewModel.onExerciseSelected(it) },
                                            modifier = Modifier.fillMaxWidth(),
                                            textFieldHeight = 55.dp,
                                            textSize = 15.sp,
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
                                                text = stringResource(id = R.string.stats_effort),
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
                                            text = stringResource(id = R.string.stats_by_weight),
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.onBackground,
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
                                            text = stringResource(id = R.string.stats_by_reps),
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.onBackground,
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
                                                        color = MaterialTheme.colorScheme.onBackground,
                                                        style = MaterialTheme.typography.titleMedium
                                                    )
                                                    Text(
                                                        "Registra más entrenamientos para ver tu progreso",
                                                        color = MaterialTheme.colorScheme.onBackground,
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
                                visible = selectedTab == StatTab.MEASUREMENTS,
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
                                            text = stringResource(id = R.string.stats_size_select),
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        FilterableDropdown(
                                            items = availableBodyMetrics.map { it.displayName },
                                            selectedValue = selectedBodyMetric?.displayName ?: "",
                                            placeholder = stringResource(id = R.string.stats_size_placeholder),
                                            onItemSelected = { selectedName ->
                                                selectedBodyMetric =
                                                    availableBodyMetrics.find { it.displayName == selectedName }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            textFieldHeight = 55.dp,
                                            textSize = 15.sp,
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Gráfico de progreso de medidas corporales
                                        selectedBodyMetric?.let { metric ->
                                            Text(
                                                text = stringResource(id = R.string.stats_size_progress) + "${metric.displayName}:",
                                                style = MaterialTheme.typography.titleLarge,
                                                color = MaterialTheme.colorScheme.onBackground,
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
                                                        .padding(horizontal = 4.dp)
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
                                                            text = stringResource(id = R.string.stats_size_resume) + "${metric.displayName}",
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
                                                                    stringResource(id = R.string.stats_actual_value),
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
                                                                    stringResource(id = R.string.stats_total_change),
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
                                                                    stringResource(id = R.string.stats_size_records)
                                                                    ,
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
        ChronoScreen(
            isVisible = showChronoScreen,
            onDismiss = {
                Log.d("StatScreen", "⏱️ Cerrando cronómetro")
                showChronoScreen = false
            }
        )



    }
}