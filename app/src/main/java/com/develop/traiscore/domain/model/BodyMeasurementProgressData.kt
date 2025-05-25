package com.develop.traiscore.domain.model

data class BodyMeasurementProgressData(
    val userId: String,
    val measurementSeries: Map<BodyMeasurementType, BodyMeasurementTimeSeries>
) {
    // Obtiene datos de una medida específica para graficar
    fun getTimeSeriesFor(type: BodyMeasurementType): BodyMeasurementTimeSeries? {
        return measurementSeries[type]
    }

    // Obtiene datos listos para LineChartView
    fun getChartDataFor(type: BodyMeasurementType, dateFormat: String = "dd/MM"): List<Pair<String, Float>> {
        return measurementSeries[type]?.toLineChartData(dateFormat) ?: emptyList()
    }

    // Lista de todas las medidas que tienen datos suficientes para graficar
    fun getAvailableMetricsForChart(): List<BodyMeasurementType> {
        return measurementSeries.filter { (_, series) ->
            series.hasEnoughDataForChart()
        }.keys.toList()
    }

    // Obtiene resumen de progreso para mostrar en cards
    fun getProgressSummary(): Map<BodyMeasurementType, MeasurementSummary> {
        return measurementSeries.mapValues { (type, series) ->
            MeasurementSummary(
                type = type,
                latestValue = series.getLatestValue(),
                totalChange = series.getChangeFromFirst(),
                percentageChange = series.getPercentageChange(),
                totalRecords = series.dataPoints.size
            )
        }
    }

    // Verifica si hay algún dato disponible
    fun hasAnyData(): Boolean {
        return measurementSeries.values.any { it.dataPoints.isNotEmpty() }
    }

    // Obtiene el total de registros
    fun getTotalRecords(): Int {
        return measurementSeries.values.firstOrNull()?.dataPoints?.size ?: 0
    }
}