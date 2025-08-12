package com.develop.traiscore.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.develop.traiscore.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ActiveSessionCard(
    sessionName: String,
    sessionColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = sessionColor.copy(alpha = 1f)  // ⭐ Color de fondo con transparencia
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ⭐ Icono de pesa en color negro
            Icon(
                painter = painterResource(id = R.drawable.pesa_icon),
                contentDescription = "Sesión activa",
                tint = Color.Black,  // ⭐ Color negro como pediste
                modifier = Modifier.size(32.dp)
            )

            Column {
                // ⭐ Nombre de la sesión en lugar de "Entrenamientos de Hoy"
                Text(
                    text = sessionName,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )

                // Fecha actual (esto se mantiene igual)
                Text(
                    text = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
                        .format(Calendar.getInstance().time),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
            }
        }
    }
}