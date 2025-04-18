package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.develop.traiscore.presentation.theme.traiBlue

data class SimpleExercise(
    val name: String,
    val series: Int,
    val reps: String,
    val weight: String,
    val rir: Int
)

@Composable
fun RoutineTable(
    exercises: List<SimpleExercise>,
    modifier: Modifier = Modifier,
    onRepsChanged: (exerciseIndex: Int, newRep: String) -> Unit

    ) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(5.dp)
            .background(Color.DarkGray)

    ) {
        TableHeader()

        // Filas: por cada ejercicio, una fila
        exercises.forEachIndexed { index, exercise ->
            TableRow(
                exercise = exercise,
                exerciseIndex = index,
                onRepsChanged = onRepsChanged
            )

            if (index < exercises.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    thickness = 1.5.dp,
                    color = traiBlue
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
        HeaderCell("Ejercicio", weight = 1.5f)
        HeaderCell("Series", weight = 0.8f)
        HeaderCell("Reps", weight = 0.8f)
        HeaderCell("Peso", weight = 0.7f)
        HeaderCell("RIR", weight = 0.7f)
    }
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 4.dp),
        thickness = 2.5.dp,
        color = traiBlue
    )
}

@Composable
private fun TableRow(
    exercise: SimpleExercise,
    exerciseIndex: Int,
    onRepsChanged: (exerciseIndex: Int, newRep: String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BodyCell(exercise.name, weight = 1.2f)
        BodyCell(exercise.series.toString(), weight = 0.7f)
        RepsCell(
            text = exercise.reps,
            onValueChange = { newRep -> onRepsChanged(exerciseIndex, newRep) },
            modifier = Modifier
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
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Yellow,
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
    val cellModifier = Modifier
        .weight(weight)


    if (isRepsCell) {
        // La celda editable para Reps
        var repValue by remember { mutableStateOf(text) }
        OutlinedTextField(
            value = repValue,
            onValueChange = { repValue = it },
            modifier = cellModifier,
            singleLine = true,
            textStyle = TextStyle(fontSize = 14.sp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Gray,
                focusedBorderColor = Color.Blue, // O usa tu variable traiblue si corresponde
                cursorColor = Color.Blue
            )
        )
    } else {
        // Para las demás columnas simplemente mostramos un Text
        Box(
            modifier = cellModifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}
@Composable
private fun RowScope.RepsCell(
    text: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var repValue by remember(key1 = text) { mutableStateOf(text) }
    // Eliminamos el weight y aplicamos width fija
    OutlinedTextField(
        value = repValue,
        onValueChange = { newText ->
            repValue = newText
            onValueChange(newText)
        },
        modifier = Modifier
            .width(60.dp) // Ajusta a tu gusto
            .height(45.dp),
        singleLine = true,
        textStyle = TextStyle(
            fontSize = 14.sp,
            color = Color.White,
            textAlign = TextAlign.Center
        ),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = traiBlue,
            focusedBorderColor = Color.Yellow,
            cursorColor = Color.Yellow
        )
    )
}
@Preview(showBackground = true)
@Composable
fun RoutineTablePreview() {
    val dummyExercises = listOf(
        SimpleExercise("Press de banca", 3, "12", "50", 2),
        SimpleExercise("Press inclinado con mancu", 3, "12", "20", 2),
        SimpleExercise("Press de hombros", 3, "12", "30", 1),
        SimpleExercise("Fondos", 3, "12", "100", 0)
    )

    RoutineTable(
        exercises = dummyExercises,
        onRepsChanged = { exerciseIndex, newRep ->
            // En el preview, puedes dejarlo vacío o imprimir algún log:
            println("Ejercicio $exerciseIndex, nuevas reps: $newRep")
        }
    )
}