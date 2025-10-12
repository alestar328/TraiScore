package com.develop.traiscore.data.remote.dtos

@kotlinx.serialization.Serializable
data class LabEntryDto(
    val id: String = "",
    val testKey: String? = null,
    val testLabel: String = "",
    val value: Double? = null,
    val unitKey: String? = null,
    val unitLabel: String? = null,
    val valueSI: Double? = null,
    val notes: String? = null
) {
    // 👇 Necesario para Firestore
    constructor() : this(
        id = "",
        testKey = null,
        testLabel = "",
        value = null,
        unitKey = null,
        unitLabel = null,
        valueSI = null,
        notes = null
    )
}

@kotlinx.serialization.Serializable
data class MedicalReportDto(
    val id: String = "",
    val userId: String = "",
    val createdAt: Long = 0L,
    val source: String? = null,
    val tags: List<String> = emptyList(),
    val entriesCount: Int = 0,
    val appVersion: String = "",
    val schemaVersion: Int = 1,
    val entries: List<LabEntryDto> = emptyList()
) {
    // 👇 También necesario, por si Firestore lo crea directamente
    constructor() : this(
        id = "",
        userId = "",
        createdAt = 0L,
        source = null,
        tags = emptyList(),
        entriesCount = 0,
        appVersion = "",
        schemaVersion = 1,
        entries = emptyList()
    )
}
