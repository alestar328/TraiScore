package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.develop.traiscore.presentation.components.general.*
import com.develop.traiscore.presentation.viewmodels.LabResultsViewModel
import com.develop.traiscore.data.remote.dtos.MedicalReportDto
import com.develop.traiscore.presentation.components.TraiScoreTopBar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalHistoryScreenUI(
    onBack: () -> Unit,
    labResultsViewModel: LabResultsViewModel = hiltViewModel()
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    val scope = rememberCoroutineScope()
    var reports by remember { mutableStateOf<List<MedicalReportDto>>(emptyList()) }
    var expandedIndex by remember { mutableStateOf<Int?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            isLoading = true
            labResultsViewModel.getReports(userId, limit = 50) { result ->
                result.onSuccess { list ->
                    reports = list.sortedByDescending { it.createdAt }
                    isLoading = false
                }.onFailure { e ->
                    errorMessage = e.message
                    isLoading = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TraiScoreTopBar(
                leftIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                showLogo = true
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                errorMessage != null -> {
                    Text(
                        text = "Error al cargar: ${errorMessage}",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                reports.isEmpty() -> {
                    Text(
                        text = "No hay registros médicos disponibles.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(reports) { index, report ->
                            val date = Date(report.createdAt)
                            val summary = buildSummaryFromReport(report)
                            val details = buildDetailsFromReport(report)

                            MedicalMeasurementCard(
                                date = date,
                                summary = summary,
                                details = details,
                                isExpanded = expandedIndex == index,
                                onExpandToggle = {
                                    expandedIndex = if (expandedIndex == index) null else index
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
fun buildSummaryFromReport(report: MedicalReportDto): MedicalMeasurementsSummary {
    val entries = report.entries.associateBy { it.testLabel.lowercase() }

    val weight = entries["peso"]?.value ?: 0.0
    val height = entries["altura"]?.value ?: 0.0
    val spO2 = entries["spo2"]?.value?.toInt() ?: 0
    val fatIndex = entries["índice de grasa"]?.value ?: 0.0

    return MedicalMeasurementsSummary(
        weight = weight,
        height = height,
        fatIndex = fatIndex,
        spO2 = spO2
    )
}

/**
 * 🔹 Convierte los detalles completos de un MedicalReportDto en tu modelo detallado
 */
fun buildDetailsFromReport(report: MedicalReportDto): MedicalMeasurementsDetails {
    val entries = report.entries.associateBy { it.testLabel.lowercase() }

    return MedicalMeasurementsDetails(
        fatMass = entries["masa de grasa"]?.value ?: 0.0,
        leanMass = entries["masa sin grasa"]?.value ?: 0.0,
        glucose = entries["glucosa"]?.value ?: 0.0,
        hemoglobin = entries["hemoglobina"]?.value ?: 0.0,
        cholesterolTotal = entries["colesterol total"]?.value ?: 0.0,
        cholesterolLDL = entries["colesterol ldl"]?.value ?: 0.0,
        cholesterolHDL = entries["colesterol hdl"]?.value ?: 0.0,
        triglycerides = entries["triglicéridos"]?.value ?: 0.0,
        liverFunctionALT = entries["alt"]?.value ?: 0.0,
        liverFunctionAST = entries["ast"]?.value ?: 0.0,
        renalUrea = entries["urea"]?.value ?: 0.0,
        renalCreatinine = entries["creatinina"]?.value ?: 0.0,
        thyroidFunction = entries["tiroidea"]?.value ?: 0.0,
        albumin = entries["albúmina"]?.value ?: 0.0,
        prealbumin = entries["prealbúmina"]?.value ?: 0.0
    )
}