package com.develop.traiscore.domain.model

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

data class MeasurementDataPoint(
    val date: Timestamp,
    val value: Float,
    val measurementType: BodyMeasurementType
) {
    // Formato de fecha para etiquetas en gráficas (dd/MM o solo día)
    fun getFormattedDate(format: String = "dd/MM"): String {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        return sdf.format(date.toDate())
    }

    // Para LineChartView - convierte directamente a Pair<String, Float>
    fun toChartPoint(dateFormat: String = "dd/MM"): Pair<String, Float> {
        return getFormattedDate(dateFormat) to value
    }
}
