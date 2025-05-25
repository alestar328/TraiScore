package com.develop.traiscore.domain.model

data class MeasurementSummary(
    val type: BodyMeasurementType,
    val latestValue: Float?,
    val totalChange: Float?,
    val percentageChange: Float?,
    val totalRecords: Int
) {
    fun getFormattedLatestValue(): String {
        return latestValue?.let { "${it.toInt()} ${type.unit}" } ?: "N/A"
    }

    fun getFormattedChange(): String {
        return totalChange?.let {
            val sign = if (it >= 0) "+" else ""
            "$sign${it.toInt()} ${type.unit}"
        } ?: "N/A"
    }

    fun getFormattedPercentageChange(): String {
        return percentageChange?.let {
            val sign = if (it >= 0) "+" else ""
            "$sign${"%.1f".format(it)}%"
        } ?: "N/A"
    }
}
