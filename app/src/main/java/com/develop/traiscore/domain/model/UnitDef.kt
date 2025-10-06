package com.develop.traiscore.domain.model

import com.develop.traiscore.statics.UnitDimension

data class UnitDef(
    val key: String,                 // id estable (p.ej. "mg/dL")
    val label: String,               // como se muestra (localizable)
    val dimension: UnitDimension,
    val aliases: List<String> = emptyList(),
    val toSi: ((Double) -> Double)? = null,     // opcional: a “SI” de su dimensión
    val fromSi: ((Double) -> Double)? = null    // opcional: desde “SI”
)