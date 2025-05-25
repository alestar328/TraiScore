package com.develop.traiscore.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.develop.traiscore.data.local.entity.SubscriptionLimits

@Composable
fun SubscriptionInfoCard(limits: SubscriptionLimits) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                limits.requiresUpgrade -> Color(0xFFFFEBEE) // Rojo claro
                limits.remainingDocuments in 1..2 -> Color(0xFFFFF3E0) // Naranja claro
                else -> Color(0xFFE8F5E8) // Verde claro
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📊 Plan ${limits.currentPlan.name}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = when {
                    limits.currentPlan.bodyStatsDocumentsLimit == -1 -> "Registros  ilimitadas"
                    limits.requiresUpgrade -> "Límite alcanzado"
                    else -> "Te quedan ${limits.remainingDocuments} registros"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    limits.requiresUpgrade -> Color.Red
                    limits.remainingDocuments in 1..2 -> Color(0xFFFF6F00)
                    else -> Color(0xFF2E7D32)
                }
            )
        }
    }
}