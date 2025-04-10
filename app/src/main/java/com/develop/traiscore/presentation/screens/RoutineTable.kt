package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class SimpleExercise(
    val name: String,
    val series: Int,
    val reps: String,
    val weight: String,
    val rir: Int
)

@Composable
fun RoutineTable(routinesByType: Map<String, List<SimpleExercise>>) {
    routineType: String,
    exercises: List<SimpleExercise>,
    modifier: Modifier = Modifier
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Título de la sección (p. ej. "Empuje")
            Text(
                text = routineType,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Cabecera de la tabla
            TableHeader()

            // Filas: por cada ejercicio, una fila
            exercises.forEachIndexed { index, exercise ->
                TableRow(exercise)

                // Línea divisoria entre filas (opcional)
                if (index < exercises.size - 1) {
                    Divider(
                        color = Color.LightGray,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }

    @Composable
    private fun TableHeader() {
        // Fila de encabezados
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            HeaderCell("Ejercicio", weight = 1.6f)
            HeaderCell("Series", weight = 0.6f)
            HeaderCell("Reps Estimadas", weight = 1f)
            HeaderCell("Peso", weight = 0.6f)
            HeaderCell("RIR", weight = 0.6f)
        }
    }

    @Composable
    private fun TableRow(exercise: SimpleExercise) {
        // Fila de datos de un ejercicio
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BodyCell(exercise.name, weight = 1.6f)
            BodyCell(exercise.series.toString(), weight = 0.6f)

            // "Reps Estimadas" con estilo de input
            BodyCell(
                text = exercise.reps,
                weight = 1f,
                isRepsCell = true
            )

            BodyCell(exercise.weight, weight = 0.6f)
            BodyCell(exercise.rir.toString(), weight = 0.6f)
        }
    }

    /**
     * Cabecera: texto en negrita, ancho proporcional (weight).
     */
    @Composable
    private fun RowScope.HeaderCell(
        text: String,
        weight: Float
    ) {
        Box(
            modifier = Modifier
                .weight(weight)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }

    /**
     * Celdas del cuerpo: si es la columna de Reps, se dibuja un borde simulando input.
     */
    @Composable
    private fun RowScope.BodyCell(
        text: String,
        weight: Float,
        isRepsCell: Boolean = false
    ) {
        val baseModifier = Modifier
            .weight(weight)
            .padding(4.dp)

        // Emula un campo input si es la columna "Reps Estimadas"
        val cellModifier =
            if (isRepsCell) {
                baseModifier
                    .border(
                        width = 1.dp,
                        color = Color.Gray,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            } else {
                baseModifier
            }

        Box(
            modifier = cellModifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }

    // Vista previa estática
    @Composable
    fun RoutineTablePreview() {
        val dummyExercises = listOf(
            SimpleExercise("Press de banca", 3, "12", "50", 2),
            SimpleExercise("Press inclinado con mancu", 3, "12", "20", 2),
            SimpleExercise("Press de hombros", 3, "12", "30", 1),
            SimpleExercise("Fondos", 3, "Peso", "-", 0)
        )

        RoutineTable(routineType = "Empuje", exercises = dummyExercises)
    }