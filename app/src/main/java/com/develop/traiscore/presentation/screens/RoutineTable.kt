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
import androidx.compose.ui.focus.onFocusChanged
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
    onRepsChanged: (exerciseIndex: Int, newRep: String) -> Unit,
    backgroundColor: Color = Color.DarkGray,
    headerTextColor: Color = Color.Yellow,
    bodyTextColor: Color = Color.White,
    dividerColor: Color = traiBlue,
    dividerThickness: Float = 2.5f,
    fontSize: Int = 14,
    fontWeight: FontWeight = FontWeight.Normal,
    headerFontWeight: FontWeight = FontWeight.Bold,
    inputBorderColor: Color = Color.Transparent,
    inputFocusedBorderColor: Color = traiBlue,
    inputCursorColor: Color = Color.Yellow
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(5.dp)
            .background(backgroundColor)
    ) {
        TableHeader(
            textColor = headerTextColor,
            fontWeight = headerFontWeight,
            fontSize = fontSize
        )

        exercises.forEachIndexed { index, exercise ->
            var rowHasFocus by remember { mutableStateOf(false) }

            TableRow(
                exercise = exercise,
                exerciseIndex = index,
                onRepsChanged = onRepsChanged,
                textColor = bodyTextColor,
                fontSize = fontSize,
                fontWeight = fontWeight,
                inputBorderColor = inputBorderColor,
                inputFocusedBorderColor = inputFocusedBorderColor,
                inputCursorColor = inputCursorColor,
                onFocusChange = { focused -> rowHasFocus = focused }
            )

            if (index < exercises.size - 1 && rowHasFocus) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    thickness = dividerThickness.dp,
                    color = dividerColor
                )
            }
        }
    }
}

@Composable
private fun TableHeader(
    textColor: Color,
    fontWeight: FontWeight,
    fontSize: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf("Ejercicio" to 1.5f, "Series" to 0.7f, "Peso" to 0.8f,"Reps" to 0.7f, "RIR" to 0.7f).forEach {
            HeaderCell(it.first, it.second, textColor, fontWeight, fontSize)
        }
    }
}

@Composable
private fun TableRow(
    exercise: SimpleExercise,
    exerciseIndex: Int,
    onRepsChanged: (Int, String) -> Unit,
    textColor: Color,
    fontSize: Int,
    fontWeight: FontWeight,
    inputBorderColor: Color,
    inputFocusedBorderColor: Color,
    inputCursorColor: Color,
    onFocusChange: (Boolean) -> Unit
) {
    var isAnyFieldFocused by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nombre del ejercicio (no editable)
        Box(
            modifier = Modifier
                .weight(1.5f)
                .height(45.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = exercise.name,
                fontSize = fontSize.sp,
                fontWeight = fontWeight,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }

        // Campos editables con control de entrada
        listOf(
            Triple(exercise.series.toString(), 0.7f) { input: String ->
                if (input.matches(Regex("^\\d{0,3}$"))) {
                    // TODO: actualizar series si se desea
                }
            },
            Triple(exercise.weight, 0.8f) { input: String ->
                // Peso puede tener coma o punto, por lo que se permite libremente
            },
            Triple(exercise.reps, 0.7f) { input: String ->
                if (input.matches(Regex("^\\d{0,3}$"))) {
                    onRepsChanged(exerciseIndex, input)
                }
            },
            Triple(exercise.rir.toString(), 0.7f) { input: String ->
                if (input.matches(Regex("^\\d{0,3}$"))) {
                    // TODO: actualizar rir si se desea
                }
            }
        ).forEach { (value, weight, onChange) ->
            BodyCell(
                value = value,
                onValueChange = onChange,
                weight = weight,
                textColor = textColor,
                fontSize = fontSize,
                fontWeight = fontWeight,
                borderColor = inputBorderColor,
                focusedBorderColor = inputFocusedBorderColor,
                cursorColor = inputCursorColor,
                onFocusChanged = { focused ->
                    isAnyFieldFocused = focused
                    onFocusChange(focused)
                }
            )
        }
    }
}

@Composable
private fun RowScope.HeaderCell(
    text: String,
    weight: Float,
    color: Color,
    fontWeight: FontWeight,
    fontSize: Int
) {
    Box(
        modifier = Modifier.weight(weight).padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = fontSize.sp,
            fontWeight = fontWeight,
            color = color,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun RowScope.BodyCell(
    value: String,
    onValueChange: (String) -> Unit,
    weight: Float,
    textColor: Color,
    fontSize: Int,
    fontWeight: FontWeight,
    borderColor: Color,
    focusedBorderColor: Color,
    cursorColor: Color,
    onFocusChanged: (Boolean) -> Unit
) {
    var localValue by remember { mutableStateOf(value) }

    val width = when (weight) {
        1.5f -> 120.dp   // Ejercicio
        0.8f -> 60.dp    // Series / Reps
        0.7f -> 60.dp    // Peso / RIR
        else -> 60.dp
    }

    OutlinedTextField(
        value = localValue,
        onValueChange = {
            localValue = it
            onValueChange(it)
        },
        singleLine = true,
        textStyle = TextStyle(
            fontSize = fontSize.sp,
            color = textColor,
            textAlign = TextAlign.Center
        ),
        modifier = Modifier
            .width(width)
            .height(50.dp)
            .onFocusChanged { onFocusChanged(it.isFocused) },
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = focusedBorderColor,
            cursorColor = cursorColor,
            focusedContainerColor = Color(0xFF1A1A1A),
            unfocusedContainerColor = Color.Transparent
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
            println("Ejercicio $exerciseIndex, nuevas reps: $newRep")
        }
    )
}
