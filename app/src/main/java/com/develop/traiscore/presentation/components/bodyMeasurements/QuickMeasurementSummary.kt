package com.develop.traiscore.presentation.components.bodyMeasurements

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.develop.traiscore.data.local.entity.UserMeasurements
import com.develop.traiscore.presentation.theme.*
import java.util.*

@Composable
fun QuickMeasurementSummary(
    measurements: UserMeasurements,
    gender: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (measurements.weight > 0) {
            MeasurementChip(
                label = "Peso",
                value = "${measurements.weight} kg",
                icon = Icons.Default.ThumbUp
            )
        }

        if (measurements.height > 0) {
            MeasurementChip(
                label = "Altura",
                value = "${measurements.height} cm",
                icon = Icons.Default.ThumbUp
            )
        }

        MeasurementChip(
            label = "Género",
            value = gender,
            icon = Icons.Default.Person
        )
    }
}
