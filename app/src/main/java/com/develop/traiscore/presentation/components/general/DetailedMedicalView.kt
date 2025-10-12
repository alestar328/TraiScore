package com.develop.traiscore.presentation.components.general

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.theme.traiBlueLabel

data class MedicalMeasurementsDetails(
    val fatMass: Double,
    val leanMass: Double,
    val glucose: Double,
    val hemoglobin: Double,
    val cholesterolTotal: Double,
    val cholesterolLDL: Double,
    val cholesterolHDL: Double,
    val triglycerides: Double,
    val liverFunctionALT: Double,
    val liverFunctionAST: Double,
    val renalUrea: Double,
    val renalCreatinine: Double,
    val thyroidFunction: Double,
    val albumin: Double,
    val prealbumin: Double
)

@Composable
fun DetailedMedicalView(details: MedicalMeasurementsDetails) {
    Column {
        Text(
            "Detalles Médicos",
            style = MaterialTheme.typography.titleSmall.copy(
                color = traiBlueLabel,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        val pairs = listOf(
            "Masa de Grasa" to "${details.fatMass} kg",
            "Masa sin Grasa" to "${details.leanMass} kg",
            "Glucosa" to "${details.glucose} mg/dL",
            "Hemoglobina" to "${details.hemoglobin} g/dL",
            "Colesterol Total" to "${details.cholesterolTotal} mg/dL",
            "Colesterol LDL" to "${details.cholesterolLDL} mg/dL",
            "Colesterol HDL" to "${details.cholesterolHDL} mg/dL",
            "Triglicéridos" to "${details.triglycerides} mg/dL",
            "Función Hepática (ALT, AST)" to "${details.liverFunctionALT} / ${details.liverFunctionAST} U/L",
            "Función Renal (Urea, Creatinina)" to "${details.renalUrea} / ${details.renalCreatinine} mg/dL",
            "Función Tiroidea" to "${details.thyroidFunction} mg/dL",
            "Proteínas Séricas (Albúmina, Prealbúmina)" to "${details.albumin} / ${details.prealbumin} g/dL"
        )

        pairs.chunked(2).forEach { chunk ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                chunk.forEach { (name, value) ->
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = traiBlueLabel,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}