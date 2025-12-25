package com.develop.traiscore.presentation.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.develop.traiscore.data.local.entity.UserMeasurements
import com.develop.traiscore.presentation.components.bodyMeasurements.EmptyContent
import com.develop.traiscore.presentation.components.bodyMeasurements.ErrorContent
import com.develop.traiscore.presentation.components.bodyMeasurements.LoadingContent
import com.develop.traiscore.presentation.components.bodyMeasurements.MeasurementHistoryCard
import com.develop.traiscore.presentation.components.bodyMeasurements.NoResultsContent
import com.develop.traiscore.presentation.components.bodyMeasurements.QuickStatsCard
import com.develop.traiscore.presentation.components.bodyMeasurements.formatDate
import com.develop.traiscore.presentation.components.bodyMeasurements.loadHistoryData
import com.develop.traiscore.presentation.theme.*
import com.develop.traiscore.presentation.viewmodels.BodyStatsViewModel
import com.google.firebase.Timestamp

data class MeasurementHistoryItem(
    val id: String,
    val measurements: UserMeasurements,
    val gender: String,
    val createdAt: Timestamp,
    val isLatest: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyMeasurementsHistoryScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onEditMeasurement: (String) -> Unit = {},
    bodyStatsViewModel: BodyStatsViewModel = hiltViewModel(),
    onConfigureTopBar: (
        @Composable () -> Unit,
        @Composable () -> Unit,
        (@Composable () -> Unit)?
    ) -> Unit,
    onConfigureFAB: (fab: (@Composable () -> Unit)?) -> Unit
) {
    var historyItems by remember { mutableStateOf<List<MeasurementHistoryItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedSortOption by remember { mutableStateOf("Más reciente") }
    var showSortMenu by remember { mutableStateOf(false) }
    var expandedItemId by remember { mutableStateOf<String?>(null) }
    var showCompareMode by remember { mutableStateOf(false) }
    var selectedForComparison by remember { mutableStateOf<Set<String>>(emptySet()) }

    val context = LocalContext.current
    val sortOptions = listOf("Más reciente", "Más antiguo", "Por peso", "Por altura")

    // Cargar historial al inicializar
    LaunchedEffect(Unit) {
        loadHistoryData(bodyStatsViewModel) { items, error ->
            historyItems = items
            errorMessage = error
            isLoading = false
        }
    }

    // Filtrar y ordenar elementos
    val filteredAndSortedItems = remember(historyItems, searchQuery, selectedSortOption) {
        var filtered = historyItems

        // Filtrar por búsqueda
        if (searchQuery.isNotEmpty()) {
            filtered = filtered.filter { item ->
                val dateStr = formatDate(item.createdAt.toDate())
                val weightStr =
                    if (item.measurements.weight > 0) "${item.measurements.weight} kg" else ""
                val heightStr =
                    if (item.measurements.height > 0) "${item.measurements.height} cm" else ""

                listOf(dateStr, weightStr, heightStr, item.gender)
                    .any { it.contains(searchQuery, ignoreCase = true) }
            }
        }

        // Ordenar
        when (selectedSortOption) {
            "Más reciente" -> filtered.sortedByDescending { it.createdAt.seconds }
            "Más antiguo" -> filtered.sortedBy { it.createdAt.seconds }
            "Por peso" -> filtered.sortedByDescending { it.measurements.weight }
            "Por altura" -> filtered.sortedByDescending { it.measurements.height }
            else -> filtered
        }
    }

    LaunchedEffect(Unit) {
        onConfigureTopBar(
            {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = null,
                        tint = traiBlue
                    )
                }
            },
            {
                // sin acciones a la derecha
            },
            {
                Text(
                    text = "Historial de medidas",
                    color = traiBlue
                )
            }
        )
    }
    LaunchedEffect(Unit) {
        onConfigureFAB(null)
    }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                isLoading -> {
                    LoadingContent()
                }

                errorMessage != null -> {
                    ErrorContent(
                        message = errorMessage!!,
                        onRetry = {
                            isLoading = true
                            errorMessage = null
                            loadHistoryData(bodyStatsViewModel) { items, error ->
                                historyItems = items
                                errorMessage = error
                                isLoading = false
                            }
                        }
                    )
                }

                historyItems.isEmpty() -> {
                    EmptyContent()
                }

                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Barra de búsqueda


                        // Estadísticas rápidas
                        if (!showCompareMode && searchQuery.isEmpty()) {
                            QuickStatsCard(
                                historyItems = historyItems,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        // Lista de medidas
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (filteredAndSortedItems.isEmpty() && searchQuery.isNotEmpty()) {
                                item {
                                    NoResultsContent(searchQuery = searchQuery)
                                }
                            } else {
                                items(
                                    items = filteredAndSortedItems,
                                    key = { it.id }
                                ) { item ->
                                    MeasurementHistoryCard(
                                        item = item,
                                        isExpanded = expandedItemId == item.id,
                                        isCompareMode = showCompareMode,
                                        isSelectedForComparison = selectedForComparison.contains(item.id),
                                        onExpandToggle = {
                                            expandedItemId = if (expandedItemId == item.id) null else item.id
                                        },
                                        // ✅ CAMBIO: Pasar el item completo en lugar de solo ejecutar callback
                                        onEdit = {
                                            Log.d("HistoryScreen", "Editando item: ${item.id}")
                                            onEditMeasurement(item.id) // Pass just the document ID to the callback
                                        },
                                        onCompareToggle = { itemId ->
                                            selectedForComparison = if (selectedForComparison.contains(itemId)) {
                                                selectedForComparison - itemId
                                            } else if (selectedForComparison.size < 3) {
                                                selectedForComparison + itemId
                                            } else {
                                                selectedForComparison
                                            }
                                        },
                                        onDelete = {
                                            bodyStatsViewModel.deleteBodyStatsRecord(item.id) { success, error ->
                                                if (success) {
                                                    historyItems = historyItems.filter { it.id != item.id }
                                                    if (selectedForComparison.contains(item.id)) {
                                                        selectedForComparison = selectedForComparison - item.id
                                                    }
                                                } else {
                                                    errorMessage = error
                                                }
                                            }
                                        }


                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

}