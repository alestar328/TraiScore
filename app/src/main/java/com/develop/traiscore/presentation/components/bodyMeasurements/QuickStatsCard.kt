package com.develop.traiscore.presentation.components.bodyMeasurements

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.develop.traiscore.presentation.screens.MeasurementHistoryItem
import com.develop.traiscore.presentation.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp

@Composable
fun QuickStatsCard(
    historyItems: List<MeasurementHistoryItem>,
    modifier: Modifier = Modifier
) {
    if (historyItems.size < 2) return

    val latest = historyItems.maxByOrNull { it.createdAt.seconds }
    val previous = historyItems.sortedByDescending { it.createdAt.seconds }.getOrNull(1)

    if (latest == null || previous == null) return

    val weightChange = latest.measurements.weight - previous.measurements.weight
    val daysBetween = ((latest.createdAt.seconds - previous.createdAt.seconds) / 86400).toInt()

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Progreso Reciente",
                style = MaterialTheme.typography.titleMedium,
                color = traiBlue,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Cambio de peso",
                    value = "${if (weightChange >= 0) "+" else ""}${"%.1f".format(weightChange)} kg",
                    icon = if (weightChange >= 0) Icons.Default.ThumbUp else Icons.Default.ThumbUp,
                    color = if (weightChange >= 0) Color.Green else Color.Red
                )

                StatItem(
                    label = "Hace",
                    value = "$daysBetween días",
                    icon = Icons.Default.ThumbUp,
                    color = Color.Gray
                )

                StatItem(
                    label = "Total registros",
                    value = "${historyItems.size}",
                    icon = Icons.Default.ThumbUp,
                    color = traiBlue
                )
            }
        }
    }
}