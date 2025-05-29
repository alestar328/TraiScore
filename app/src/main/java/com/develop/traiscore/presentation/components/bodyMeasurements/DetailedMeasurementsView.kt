package com.develop.traiscore.presentation.components.bodyMeasurements


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.develop.traiscore.data.local.entity.UserMeasurements
import com.develop.traiscore.presentation.theme.*

@Composable
fun DetailedMeasurementsView(
    measurements: UserMeasurements
) {
    val measurementsList = listOf(
        "Cuello" to measurements.neck,
        "Pecho" to measurements.chest,
        "Brazos" to measurements.arms,
        "Cintura" to measurements.waist,
        "Muslo" to measurements.thigh,
        "Pantorrilla" to measurements.calf
    ).filter { it.second > 0 }

    if (measurementsList.isNotEmpty()) {
        Text(
            "Medidas detalladas",
            style = MaterialTheme.typography.titleSmall,
            color = traiBlue,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        measurementsList.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                pair.forEach { (name, value) ->
                    DetailMeasurementItem(
                        name = name,
                        value = "$value cm",
                        modifier = Modifier.weight(1f)
                    )
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}