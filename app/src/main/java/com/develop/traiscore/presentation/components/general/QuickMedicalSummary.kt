package com.develop.traiscore.presentation.components.general


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.theme.traiBlueLabel

data class MedicalMeasurementsSummary(
    val weight: Double,
    val height: Double,
    val fatIndex: Double,
    val spO2: Int
)

@Composable
fun QuickMedicalSummary(summary: MedicalMeasurementsSummary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SummaryChip(
            label = "Peso",
            value = "${summary.weight} kg",
            painter = painterResource(id = com.develop.traiscore.R.drawable.peso_icon) // 👈 ejemplo con drawable personalizado
        )
        SummaryChip(
            label = "Altura",
            value = "${summary.height} cm",
            painter = painterResource(id = com.develop.traiscore.R.drawable.height_icon) // 👈 ejemplo con drawable personalizado
        )
        SummaryChip(
            label = "SpO₂",
            value = "${summary.spO2} %",
            painter = painterResource(id = com.develop.traiscore.R.drawable.heart_icon) // 👈 ejemplo con drawable personalizado
        )
    }
}

@Composable
fun SummaryChip(
    label: String,
    value: String,
    icon: ImageVector? = null,
    painter: Painter? = null,
    tint: Color = traiBlueLabel
) {
    Card( // 👈 usa Card en lugar de ElevatedCard
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = traiBlue.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            when {
                icon != null -> Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
                painter != null -> Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(bottom = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(color = tint)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}
