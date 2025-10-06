package com.develop.traiscore.data.remote.dtos

@kotlinx.serialization.Serializable
data class LabEntryDto(
    val id: String,
    val testKey: String? = null,      // clave normalizada (p.ej. "GLUCOSE")
    val testLabel: String,            // cómo lo ve el usuario ("Glucosa")
    val value: Double? = null,
    val unitKey: String? = null,      // clave estable ("mg/dL")
    val unitLabel: String? = null,    // etiqueta mostrada
    val valueSI: Double? = null,      // opcional: a sistema base
    val notes: String? = null
)

@kotlinx.serialization.Serializable
data class MedicalReportDto(
    val id: String,
    val userId: String,
    val createdAt: Long,
    val source: String? = null,       // "OCR", "manual", etc.
    val tags: List<String> = emptyList(),
    val entriesCount: Int,
    val appVersion: String,
    val schemaVersion: Int = 1,
    val entries: List<LabEntryDto>    // ← embebidas para export fácil
)

@kotlinx.serialization.Serializable
data class MedicalStatsExport(
    val fileType: String = "TraiScore_MedicalStats",
    val schemaVersion: Int = 1,
    val appVersion: String,
    val userId: String,
    val exportedAt: Long,
    val reports: List<MedicalReportDto>
)
