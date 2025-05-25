package com.develop.traiscore.domain.model

import com.google.firebase.Timestamp

class BodyMeasurementProgressBuilder {
    private val seriesMap = mutableMapOf<BodyMeasurementType, MutableList<MeasurementDataPoint>>()

    /**
     * Añade un documento de medidas corporales del formato que usas en Firebase
     * @param measurements Map con las medidas (ej: "Height" -> "175", "Weight" -> "70")
     * @param timestamp Timestamp del documento
     */
    fun addMeasurementDocument(
        measurements: Map<String, String>, // el formato de tu defaultMeasurements
        timestamp: Timestamp
    ) {
        measurements.forEach { (key, value) ->
            val type = BodyMeasurementType.fromKey(key)
            val numericValue = value.toFloatOrNull()

            if (type != null && numericValue != null && numericValue > 0) {
                seriesMap.getOrPut(type) { mutableListOf() }
                    .add(MeasurementDataPoint(timestamp, numericValue, type))
            }
        }
    }

    /**
     * Añade un documento completo desde Firebase (con estructura completa)
     * @param documentData Datos del documento de Firebase tal como los guardas
     */
    fun addFirebaseDocument(documentData: Map<String, Any>) {
        val measurements = documentData["measurements"] as? Map<String, Any> ?: return
        val timestamp = documentData["createdAt"] as? Timestamp ?: Timestamp.now()

        val measurementsStringMap = measurements.mapValues { it.value.toString() }
        addMeasurementDocument(measurementsStringMap, timestamp)
    }

    /**
     * Construye el objeto final BodyMeasurementProgressData
     */
    fun build(userId: String): BodyMeasurementProgressData {
        val timeSeries = seriesMap.mapValues { (type, points) ->
            BodyMeasurementTimeSeries(type, points.toList())
        }
        return BodyMeasurementProgressData(userId, timeSeries)
    }

    /**
     * Verifica si se han añadido datos
     */
    fun hasData(): Boolean = seriesMap.isNotEmpty()

    /**
     * Limpia los datos del builder
     */
    fun clear() {
        seriesMap.clear()
    }
}