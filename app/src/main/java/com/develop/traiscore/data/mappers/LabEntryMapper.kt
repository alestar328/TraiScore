package com.develop.traiscore.data.mappers

import com.develop.traiscore.data.remote.dtos.LabEntryDto

private val TEST_KEY_MAP = mapOf(
    "glucosa" to "GLUCOSE",
    "urea" to "UREA",
    "creatinina" to "CREATININE",
    "ácido úrico" to "URIC_ACID",
    "colesterol total" to "CHOLESTEROL_TOTAL",
    "hdl" to "HDL",
    "ldl" to "LDL",
    "triglicéridos" to "TRIGLYCERIDES"
)
fun normalizeTestKey(label: String): String? =
    TEST_KEY_MAP[label.trim().lowercase()]

// Opcional: conversión a SI por dimensión/prueba (placeholder)
object UnitConverter {
    fun toSI(value: Double?, unitKey: String?, testKey: String?): Double? {
        if (value == null || unitKey == null) return null
        // Aquí tu lógica por test (p.ej. glucosa mg/dL → mmol/L * 0.0555)
        return null
    }
}

// Mappers UI -> DTO
fun com.develop.traiscore.data.local.entity.LabEntry.toDto(): LabEntryDto {
    val unitDef = com.develop.traiscore.utils.UnitRegistry.findByKey(unit ?: "")
    val unitKey = unitDef?.key ?: unit
    val unitLabel = unitDef?.label ?: unit
    val tKey = normalizeTestKey(test)
    return LabEntryDto(
        id = id,
        testKey = tKey,
        testLabel = test,
        value = value,
        unitKey = unitKey,
        unitLabel = unitLabel,
        valueSI = UnitConverter.toSI(value, unitKey, tKey),
        notes = referenceRange // o mueve a otro campo si prefieres
    )
}