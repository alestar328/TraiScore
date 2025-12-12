package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.develop.traiscore.core.ColumnType
import com.develop.traiscore.data.firebaseData.SimpleExercise
import com.develop.traiscore.presentation.theme.traiBlue

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RoutineTable(
    exercises: List<SimpleExercise>,
    exerciseNames: List<String>,
    onDeleteExercise: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
    onSeriesChanged: (Int, String) -> Unit = { _, _ -> },
    onWeightChanged: (Int, String) -> Unit = { _, _ -> },
    onRepsChanged: (Int, String) -> Unit = { _, _ -> },
    onRirChanged: (Int, String) -> Unit = { _, _ -> },
    onFieldChanged: (exerciseIndex: Int, columnType: ColumnType, newValue: String) -> Unit,
    onDuplicateExercise: (index: Int) -> Unit = { _ -> }, // ✅ NUEVO: Callback para duplicar
    backgroundColor: Color = MaterialTheme.colorScheme.onBackground,
    headerTextColor: Color = Color.Yellow,
    bodyTextColor: Color = Color.White,
    dividerColor: Color = traiBlue,
    dividerThickness: Float = 2.5f,
    fontSize: Int = 12,
    fontSizeText: Int = 10,
    fontWeight: FontWeight = FontWeight.Normal,
    headerFontWeight: FontWeight = FontWeight.Bold,
    inputBorderColor: Color = Color.Transparent,
    inputFocusedBorderColor: Color = traiBlue,
    inputCursorColor: Color = Color.Yellow,
    enableSwipe: Boolean = true,
    validateInput: (String, ColumnType) -> String,
    bottomPadding: Dp = 16.dp
) {
    // ✅ Altura dinámica basada en el contenido
    val headerHeight = 56.dp
    val rowHeight = 58.dp // Altura de cada fila
    val totalHeight = headerHeight + (rowHeight * exercises.size) + bottomPadding

    Column(
        modifier = modifier
        .fillMaxWidth()
        .padding(5.dp)
        .background(MaterialTheme.colorScheme.background)
        .height(totalHeight) // ✅ Altura exacta según el número de ejercicios
        ) {
    // Header
    TableHeader(
        textColor = headerTextColor,
        fontWeight = headerFontWeight,
        fontSize = fontSize
    )

    // Filas de ejercicios - Sin scroll, altura natural
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        exercises.forEachIndexed { index, exercise ->
            var rowHasFocus by remember { mutableStateOf(false) }

            key(index)  {
                if (enableSwipe) {
                    val dismissState = rememberDismissState(
                        confirmStateChange = { dismissValue ->
                            when (dismissValue) {
                                DismissValue.DismissedToStart -> {
                                    // Swipe hacia la izquierda → Eliminar
                                    onDeleteExercise(index)
                                    true // ✅ Permitir dismiss para eliminar
                                }

                                DismissValue.DismissedToEnd -> {
                                    // Swipe hacia la derecha → Duplicar
                                    onDuplicateExercise(index)
                                    false // ✅ Mantener false para duplicar
                                }

                                else -> false
                            }
                        }
                    )
                    LaunchedEffect(dismissState.currentValue) {
                        when (dismissState.currentValue) {
                            DismissValue.DismissedToStart -> {
                                // Para eliminar: resetear inmediatamente ya que la fila desaparecerá
                                dismissState.reset()
                            }
                            DismissValue.DismissedToEnd -> {
                                // Para duplicar: esperar un poco y resetear
                                kotlinx.coroutines.delay(300)
                                dismissState.reset()
                            }
                            else -> { /* Default, no hacer nada */ }
                        }
                    }
                    SwipeToDismiss(
                        state = dismissState,
                        directions = setOf(
                            DismissDirection.EndToStart,    // ← Swipe izquierda (eliminar)
                            DismissDirection.StartToEnd     // → Swipe derecha (duplicar)
                        ),
                        background = {
                            when (dismissState.dismissDirection) {
                                DismissDirection.EndToStart -> {
                                    // Fondo rojo para eliminar (swipe hacia la izquierda)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Red)
                                            .padding(end = 20.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar",
                                            tint = Color.White
                                        )
                                    }
                                }

                                DismissDirection.StartToEnd -> {
                                    // Fondo verde para duplicar (swipe hacia la derecha)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Green)
                                            .padding(start = 20.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Duplicar",
                                            tint = Color.White
                                        )
                                    }
                                }

                                else -> {}
                            }
                        },
                        dismissContent = {
                            TableRow(
                                exercise = exercise,
                                exerciseNames = exerciseNames,
                                exerciseIndex = index,
                                onRepsChanged = onRepsChanged,
                                onSeriesChanged = onSeriesChanged,
                                onWeightChanged = onWeightChanged,
                                onRirChanged = onRirChanged,
                                textColor = bodyTextColor,
                                fontSize = fontSizeText,
                                fontWeight = fontWeight,
                                inputBorderColor = inputBorderColor,
                                inputFocusedBorderColor = inputFocusedBorderColor,
                                inputCursorColor = inputCursorColor,
                                onFocusChange = { rowHasFocus = it },
                                validateInput = validateInput,
                                onFieldChanged = onFieldChanged,
                            )
                        }
                    )
                } else {
                    TableRow(
                        exercise = exercise,
                        exerciseNames = exerciseNames,
                        exerciseIndex = index,
                        onRepsChanged = onRepsChanged,
                        onSeriesChanged = onSeriesChanged,
                        onWeightChanged = onWeightChanged,
                        onRirChanged = onRirChanged,
                        textColor = bodyTextColor,
                        fontSize = fontSize,
                        fontWeight = fontWeight,
                        inputBorderColor = inputBorderColor,
                        inputFocusedBorderColor = inputFocusedBorderColor,
                        inputCursorColor = inputCursorColor,
                        onFocusChange = { rowHasFocus = it },
                        validateInput = validateInput,
                        onFieldChanged = onFieldChanged,
                    )
                }
            }

            // Divider entre filas cuando hay foco
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
        listOf(
            "Ejercicio" to 1.7f,
            "Series" to 0.7f,
            "Peso" to 0.8f,
            "Reps" to 0.7f,
            "RIR" to 0.7f
        ).forEach {
            HeaderCell(it.first, it.second, textColor, fontWeight, fontSize)
        }
    }
}

@Composable
private fun TableRow(
    exercise: SimpleExercise,
    exerciseIndex: Int,
    exerciseNames: List<String>,
    onSeriesChanged: (Int, String) -> Unit,
    onWeightChanged: (Int, String) -> Unit,
    onRepsChanged: (Int, String) -> Unit,
    onRirChanged: (Int, String) -> Unit,
    onFieldChanged: (exerciseIndex: Int, columnType: ColumnType, newValue: String) -> Unit,   // 👈 AÑADIDO
    textColor: Color,
    fontSize: Int,
    fontWeight: FontWeight,
    inputBorderColor: Color,
    inputFocusedBorderColor: Color,
    inputCursorColor: Color,
    onFocusChange: (Boolean) -> Unit,
    validateInput: (String, ColumnType) -> String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BodyCellAutocomplete(
            value = exercise.name,
            onValueChangeFinal = { finalName ->
                onFieldChanged(exerciseIndex, ColumnType.EXERCISE_NAME, finalName)
            },
            exerciseNames = exerciseNames,
            weight = 1.7f,
            textColor = textColor,
            fontSize = fontSize,
            fontWeight = fontWeight,
            focusedBorderColor = inputFocusedBorderColor,
            cursorColor = inputCursorColor
        )
        val cells: List<Quadruple<String, Float, ColumnType, (String) -> Unit>> = listOf(
            Quadruple(exercise.series.toString(), 0.7f, ColumnType.SERIES) { v ->
                onSeriesChanged(exerciseIndex, v)
            },
            Quadruple(exercise.weight, 0.8f, ColumnType.WEIGHT) { v ->
                onWeightChanged(exerciseIndex, v)
            },
            Quadruple(exercise.reps, 0.7f, ColumnType.REPS) { v ->
                onRepsChanged(exerciseIndex, v)
            },
            Quadruple(exercise.rir.toString(), 0.7f, ColumnType.RIR) { v ->
                onRirChanged(exerciseIndex, v)
            }
        )
        cells.forEach { (value, weight, columnType, onChange) ->
            BodyCell(
                value = value,
                onValueChange = { raw ->
                    val clean = validateInput(raw, columnType)
                    onChange(clean)
                },
                weight = weight,
                textColor = textColor,
                fontSize = fontSize,
                fontWeight = fontWeight,
                borderColor = inputBorderColor,
                focusedBorderColor = inputFocusedBorderColor,
                cursorColor = inputCursorColor,
                columnType = columnType,
                onFocusChanged = { focused ->
                    onFocusChange(focused)
                },
                validateInput = validateInput
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RowScope.BodyCellAutocomplete(
    value: String,
    onValueChangeFinal: (String) -> Unit,
    exerciseNames: List<String>,
    weight: Float,
    textColor: Color,
    fontSize: Int,
    fontWeight: FontWeight,
    focusedBorderColor: Color,
    cursorColor: Color
) {
    var localValue by remember { mutableStateOf(value) }
    var expanded by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }

    LaunchedEffect(value) {
        if (!isFocused) {
            localValue = value
        }
    }

    val filtered = remember(localValue) {
        if (localValue.isBlank()) emptyList()
        else exerciseNames.filter { it.contains(localValue, ignoreCase = true) }.take(20)
    }

    // ✅ ExposedDropdownMenuBox - Componente nativo para autocompletar
    ExposedDropdownMenuBox(
        expanded = expanded && isFocused && filtered.isNotEmpty(),
        onExpandedChange = { /* No hacer nada, controlamos manualmente */ },
        modifier = Modifier.weight(weight)
    ) {
        OutlinedTextField(
            value = localValue,
            onValueChange = { newValue ->
                localValue = newValue
                expanded = newValue.isNotBlank() && filtered.isNotEmpty()
            },
            singleLine = true,
            textStyle = TextStyle(
                fontSize = fontSize.sp,
                color = textColor,
                textAlign = TextAlign.Start
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .menuAnchor() // ✅ CRÍTICO: Ancla el dropdown al TextField
                .onFocusChanged {
                    val nowFocused = it.isFocused
                    if (!nowFocused && isFocused) {
                        onValueChangeFinal(localValue)
                        expanded = false
                    }
                    isFocused = nowFocused

                    // Expandir automáticamente si tiene texto y hay resultados
                    if (nowFocused && localValue.isNotBlank() && filtered.isNotEmpty()) {
                        expanded = true
                    }
                },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = focusedBorderColor,
                cursorColor = cursorColor,
                focusedContainerColor = Color(0xFF1A1A1A),
                unfocusedContainerColor = Color.Transparent
            ),
            trailingIcon = null // ✅ Sin ícono de dropdown (no queremos el icono estándar)
        )

        // ✅ Menu anclado correctamente al TextField
        ExposedDropdownMenu(
            expanded = expanded && isFocused && filtered.isNotEmpty(),
            onDismissRequest = {
                expanded = false
            },
            modifier = Modifier
                .background(Color(0xFF222222))
                .heightIn(max = 200.dp) // ✅ Altura máxima para scroll automático
        ) {
            filtered.forEach { suggestion ->
                DropdownMenuItem(
                    text = {
                        Text(
                            suggestion,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    },
                    onClick = {
                        localValue = suggestion
                        onValueChangeFinal(suggestion)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
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
        modifier = Modifier
            .weight(weight)
            .padding(2.dp),
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
    columnType: ColumnType,
    onFocusChanged: (Boolean) -> Unit,
    validateInput: (String, ColumnType) -> String
) {
    var localValue by remember { mutableStateOf(value) }

    OutlinedTextField(
        value = localValue,
        onValueChange = { newValue ->
            val validatedValue = validateInput(newValue, columnType)
            localValue = validatedValue
            onValueChange(validatedValue)
        },
        singleLine = true,
        textStyle = TextStyle(
            fontSize = fontSize.sp,
            color = textColor,
            textAlign = TextAlign.Center
        ),
        modifier = Modifier
            .weight(weight)
            .height(50.dp)
            .onFocusChanged { onFocusChanged(it.isFocused) },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Number
        ),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = borderColor,
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
        SimpleExercise("Press inclinado", 3, "12", "20", 2),
        SimpleExercise("Press de hombros", 3, "12", "30", 1),
        SimpleExercise("Fondos", 3, "12", "100", 0)
    )

    val dummyExerciseNames = listOf(
        "Press de banca",
        "Press inclinado",
        "Press militar",
        "Fondos en paralelas",
        "Aperturas con mancuernas"
    )

    val fakeValidateInput: (String, ColumnType) -> String = { input, _ ->
        input.filter { it.isDigit() || it == '.' }
    }

    RoutineTable(
        exercises = dummyExercises,
        exerciseNames = dummyExerciseNames,   // ✅ USAMOS LA LISTA DUMMY
        onDeleteExercise = { /* nada */ },
        onFieldChanged = { row, column, value ->
            println("Fila $row, columna $column → $value")
        },
        validateInput = fakeValidateInput
    )
}