package com.develop.traiscore.utils

import com.develop.traiscore.domain.model.UnitDef
import com.develop.traiscore.statics.UnitDimension

object UnitRegistry {
    // Ejemplo para química clínica
    private val units = listOf(
        UnitDef(
            key = "mg/dL",
            label = "mg/dL",
            dimension = UnitDimension.CONCENTRATION,
            aliases = listOf("mgdl", "mg por dL")
        ),
        UnitDef(
            key = "mmol/L",
            label = "mmol/L",
            dimension = UnitDimension.CONCENTRATION,
            aliases = listOf("mmol l", "mmol·L⁻¹")
        ),
        UnitDef(
            key = "µmol/L",
            label = "µmol/L",
            dimension = UnitDimension.CONCENTRATION
        ),
        UnitDef(
            key = "g/L",
            label = "g/L",
            dimension = UnitDimension.CONCENTRATION
        ),
        UnitDef(
            key = "%",
            label = "%",
            dimension = UnitDimension.PERCENT
        ),
        // Ejemplos generales (si luego quieres usar en otras pantallas)
        UnitDef("kg", "kg", UnitDimension.MASS, aliases = listOf("Kg")),
        UnitDef("mg", "mg", UnitDimension.MASS),
        UnitDef("ft", "ft", UnitDimension.LENGTH),
    )
    fun allLabels(): List<String> = all().map { it.label }.distinct()

    fun all(): List<UnitDef> = units

    fun byDimension(dim: UnitDimension): List<UnitDef> =
        units.filter { it.dimension == dim }

    fun labels(keys: List<String>): List<String> =
        units.filter { it.key in keys }.map { it.label }

    fun findByKey(key: String): UnitDef? =
        units.firstOrNull { it.key.equals(key, ignoreCase = true) || it.aliases.any { a -> a.equals(key, true) } }
}