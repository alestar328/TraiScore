package com.develop.traiscore.presentation.components.bodyMeasurements

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.develop.traiscore.R
import com.develop.traiscore.presentation.screens.MeasurementHistoryItem
import com.develop.traiscore.presentation.theme.*
import java.util.*

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterialApi::class)
@Composable
fun MeasurementHistoryCard(
    item: MeasurementHistoryItem,
    isExpanded: Boolean,
    isCompareMode: Boolean,
    isSelectedForComparison: Boolean,
    onExpandToggle: () -> Unit,
    onEdit: () -> Unit,
    onCompareToggle: (String) -> Unit,
    onDelete: () -> Unit = {} // ✅ AÑADIR parámetro
) {
    var showDeleteDialog by remember { mutableStateOf(false) } // ✅ AÑADIR estado del diálogo

    val dismissState = rememberDismissState(
        confirmStateChange = { dismissValue ->
            if (dismissValue == DismissValue.DismissedToStart) {
                showDeleteDialog = true // ✅ CAMBIAR: Mostrar diálogo en lugar de eliminar directamente
                false // No permitir dismiss hasta confirmar
            } else {
                false
            }
        }
    )

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar registro") },
            text = { Text("¿Estás seguro de que quieres eliminar este registro de medidas? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.EndToStart),
        background = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Red,
                        shape = RoundedCornerShape(12.dp) // ✅ AÑADIR: Mismo radio que la Card
                    )
                    .padding(end = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        dismissContent = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (isCompareMode) {
                            onCompareToggle(item.id)
                        } else {
                            onExpandToggle()
                        }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelectedForComparison && isCompareMode)
                        traiBlue.copy(alpha = 0.2f) else Color.DarkGray
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = if (item.isLatest) 4.dp else 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    formatDate(item.createdAt.toDate()),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                if (item.isLatest) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = traiBlue,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            stringResource(R.string.measures_actual),
                                            modifier = Modifier.padding(
                                                horizontal = 8.dp,
                                                vertical = 2.dp
                                            ),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Text(
                                formatTime(item.createdAt.toDate()),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isCompareMode) {
                                Checkbox(
                                    checked = isSelectedForComparison,
                                    onCheckedChange = { onCompareToggle(item.id) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = traiBlue,
                                        uncheckedColor = Color.Gray
                                    )
                                )
                            } else {
                                IconButton(onClick = onEdit) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Editar",
                                        tint = traiBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Icon(
                                    if (isExpanded) Icons.Default.ThumbUp else Icons.Default.ThumbUp,
                                    contentDescription = if (isExpanded) "Contraer" else "Expandir",
                                    tint = Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Vista resumida - siempre visible
                    QuickMeasurementSummary(measurements = item.measurements, gender = item.gender)

                    // Vista expandida
                    AnimatedVisibility(
                        visible = isExpanded && !isCompareMode,
                        enter = expandVertically(tween(300)) + fadeIn(tween(300)),
                        exit = shrinkVertically(tween(200)) + fadeOut(tween(200))
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = Color.Gray.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(12.dp))

                            DetailedMeasurementsView(measurements = item.measurements)
                        }
                    }
                }
            }
        }
    )
}