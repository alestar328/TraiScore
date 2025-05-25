package com.develop.traiscore.domain.model

data class BodyMeasurementTimeSeries(
    val measurementType: BodyMeasurementType,
    val dataPoints: List<MeasurementDataPoint>
) {
    // Convierte directamente al formato que espera LineChartView
    fun toLineChartData(dateFormat: String = "dd/MM"): List<Pair<String, Float>> {
        return dataPoints
            .sortedBy { it.date.seconds } // Ordenar por fecha
            .map { it.toChartPoint(dateFormat) }
    }

    // Obtiene el último valor registrado
    fun getLatestValue(): Float? =
        dataPoints.maxByOrNull { it.date.seconds }?.value

    // Calcula el cambio desde el primer al último registro
    fun getChangeFromFirst(): Float? {
        if (dataPoints.size < 2) return null
        val sortedPoints = dataPoints.sortedBy { it.date.seconds }
        val first = sortedPoints.first().value
        val last = sortedPoints.last().value
        return last - first
    }

    // Calcula el porcentaje de cambio
    fun getPercentageChange(): Float? {
        if (dataPoints.size < 2) return null
        val sortedPoints = dataPoints.sortedBy { it.date.seconds }
        val first = sortedPoints.first().value
        val last = sortedPoints.last().value
        return if (first != 0f) ((last - first) / first) * 100 else 0f
    }

    // Verifica si hay suficientes datos para mostrar gráfica
    fun hasEnoughDataForChart(): Boolean = dataPoints.size >= 2
}