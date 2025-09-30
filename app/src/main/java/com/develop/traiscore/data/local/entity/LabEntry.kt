package com.develop.traiscore.data.local.entity

import android.graphics.RectF
import java.util.UUID

data class LabEntry(
    val id: String = UUID.randomUUID().toString(),
    var test: String,            // "Glucosa", "Urea", "Creatinina", etc.
    var value: Double?,          // 92.0
    var unit: String?,           // "mg/dL", "mmol/L", etc.
    var referenceRange: String? = null,   // opcional ("70–110 mg/dL")
    var confidence: Float? = null,        // opcional, del OCR
    var sourceBox: RectF? = null          // opcional, bbox en la imagen
)
