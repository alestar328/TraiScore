package com.develop.traiscore.presentation.components.general


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.develop.traiscore.R
import com.develop.traiscore.data.local.entity.LabEntry
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.theme.traiOrange
import java.util.UUID



@Composable
fun LabResultsTableUI(
    modifier: Modifier = Modifier,
    entries: List<LabEntry>,
    onEntriesChange: (List<LabEntry>) -> Unit,
    // Sugerencias de unidades por prueba (ej: "Glucosa" -> ["mg/dL","mmol/L"])
    unitSuggestionsByTest: Map<String, List<String>> = emptyMap()
) {
    var local by remember(entries) { mutableStateOf(entries) }

    // Visibilidad de columnas (para “quitar columna” sin romper datos)
    var showColTest by remember { mutableStateOf(true) }
    var showColValue by remember { mutableStateOf(true) }
    var showColUnit by remember { mutableStateOf(true) }

    // Orden por nombre de prueba
    var sortAsc by remember { mutableStateOf(true) }

    fun push() = onEntriesChange(local)

    Surface(modifier = modifier) {
        Column(Modifier.fillMaxWidth()) {

            // ===== Toolbar =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalIconButton(
                    onClick = {
                        local = local + LabEntry(
                            id = UUID.randomUUID().toString(),
                            test = "",
                            value = null,
                            unit = null
                        )
                        push()
                    },
                    shape = androidx.compose.foundation.shape.CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir fila")
                }

                FilledTonalIconButton(
                    onClick = {
                        sortAsc = !sortAsc
                        local = if (sortAsc) {
                            local.sortedBy { it.test.lowercase() }
                        } else {
                            local.sortedByDescending { it.test.lowercase() }
                        }
                        push()
                    },
                    shape = androidx.compose.foundation.shape.CircleShape
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.sort_by_alpha),
                        contentDescription = "Ordenar"
                    )
                }

                // Restaurar (orden y visibilidad)
                FilledTonalIconButton(
                    onClick = {
                        sortAsc = true
                        showColTest = true; showColValue = true; showColUnit = true
                        local = local.sortedBy { it.test.lowercase() }
                        push()
                    },
                    shape = androidx.compose.foundation.shape.CircleShape
                ) {
                    Icon(painter = painterResource(id = R.drawable.history_logo), contentDescription = "Restablecer")
                }

                Spacer(Modifier.weight(1f))

                // Toggle columnas
                AssistChip(
                    onClick = { showColTest = !showColTest },
                    label = { Text("Prueba") },
                    leadingIcon = { Icon(painter = painterResource(id = R.drawable.grid_view), contentDescription = null) },
                    colors = AssistChipDefaults.assistChipColors(
                        labelColor = if (showColTest) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    )
                )
                AssistChip(
                    onClick = { showColValue = !showColValue },
                    label = { Text("Valor") },
                    leadingIcon = { Icon(painter = painterResource(id = R.drawable.grid_view), contentDescription = null) },
                    colors = AssistChipDefaults.assistChipColors(
                        labelColor = if (showColValue) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    )
                )
                AssistChip(
                    onClick = { showColUnit = !showColUnit },
                    label = { Text("Unidad") },
                    leadingIcon = { Icon(painter = painterResource(id = R.drawable.grid_view), contentDescription = null) },
                    colors = AssistChipDefaults.assistChipColors(
                        labelColor = if (showColUnit) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    )
                )
            }

            Divider()

            // ===== Cabecera =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showColTest) HeaderCell(text = "Prueba", modifier = Modifier.weight(0.5f))
                if (showColValue) HeaderCell(text = "Valor", modifier = Modifier.weight(0.25f))
                if (showColUnit) HeaderCell(text = "Unidad", modifier = Modifier.weight(0.25f))
                Spacer(Modifier.width(40.dp)) // espacio para icono eliminar
            }

            Divider(thickness = 1.dp)

            // ===== Filas =====
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 84.dp)
            ) {
                items(local, key = { it.id }) { item ->
                    LabRow(
                        entry = item,
                        showColTest = showColTest,
                        showColValue = showColValue,
                        showColUnit = showColUnit,
                        unitSuggestions = unitSuggestionsByTest[item.test].orEmpty(),
                        onChange = { updated ->
                            local = local.map { if (it.id == updated.id) updated else it }
                            push()
                        },
                        onDelete = {
                            local = local.filterNot { it.id == item.id }
                            push()
                        }
                    )
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun HeaderCell(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        modifier = modifier.padding(horizontal = 8.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LabRow(
    entry: LabEntry,
    showColTest: Boolean,
    showColValue: Boolean,
    showColUnit: Boolean,
    unitSuggestions: List<String>,
    onChange: (LabEntry) -> Unit,
    onDelete: () -> Unit
) {
    // Estados de edición como String para TextField
    var testText by remember(entry.id) { mutableStateOf(entry.test) }
    var valueText by remember(entry.id) { mutableStateOf(entry.value?.toString() ?: "") }
    var unitText by remember(entry.id) { mutableStateOf(entry.unit ?: "") }

    var unitExpanded by remember { mutableStateOf(false) }
    val allUnits = remember {
        com.develop.traiscore.utils.UnitRegistry.allLabels().sorted()
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showColTest) {
            //CELDA PRUEBA
            OutlinedTextField(
                value = testText,
                onValueChange = {
                    testText = it
                    onChange(entry.copy(test = it))
                },
                modifier = Modifier
                    .weight(0.5f)
                    .padding(horizontal = 6.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { /* foco a valor */ })
            )
        }

        if (showColValue) {
            // CELDA VALOR
            OutlinedTextField(
                value = valueText,
                onValueChange = {
                    valueText = it
                    val parsed = it.replace(',', '.').toDoubleOrNull()
                    onChange(entry.copy(value = parsed))
                },
                modifier = Modifier
                    .weight(0.25f)
                    .padding(horizontal = 6.dp),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = traiOrange, // 👈 aquí tu color
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline, // normal
                    cursorColor = traiOrange, // opcional, para que combine
                    focusedLabelColor = traiOrange // si tuviera label
                )
            )
        }

        if (showColUnit) {
            Column(
                modifier = Modifier
                    .weight(0.27f)
            ) {
                ExposedDropdownMenuBox(
                    expanded = unitExpanded,
                    onExpandedChange = { unitExpanded = it }
                ) {
                    // CELDA UNIDADES
                    OutlinedTextField(
                        value = unitText,
                        onValueChange = {
                            unitText = it
                            onChange(entry.copy(unit = it.ifBlank { null }))
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        singleLine = true,
                        readOnly = true,
                        textStyle = MaterialTheme.typography.bodyMedium,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = unitExpanded,
                        onDismissRequest = { unitExpanded = false }
                    ) {
                        allUnits.forEach { suggestion ->
                            DropdownMenuItem(
                                text = { Text(suggestion) },
                                onClick = {
                                    unitText = suggestion
                                    unitExpanded = false
                                    onChange(entry.copy(unit = suggestion))
                                }
                            )
                        }
                    }
                }
            }
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .size(40.dp)
                .padding(start = 4.dp)
        ) {
            Icon(Icons.Default.Delete, contentDescription = "Eliminar fila", tint = traiBlue)
        }
    }
}