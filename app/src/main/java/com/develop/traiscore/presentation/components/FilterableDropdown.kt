package com.develop.traiscore.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.develop.traiscore.presentation.theme.traiBlue
import java.text.Normalizer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterableDropdown(
    items: List<String>,
    onItemSelected: (String) -> Unit,
    selectedValue: String,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    maxDisplayedItems: Int = 6
) {
    var expanded by remember { mutableStateOf(false) }
    var filterText by remember { mutableStateOf(TextFieldValue("")) }
    var userIsTyping by remember { mutableStateOf(false) } // **SIMPLIFICADO**: Solo controlar si está escribiendo

    LaunchedEffect(selectedValue) {
        filterText = if (selectedValue.isNotBlank())
            TextFieldValue(selectedValue)
        else
            TextFieldValue("")

        // **SIMPLIFICADO**: Si viene preseleccionado, el usuario NO está escribiendo
        userIsTyping = false
    }

    val filteredItems = remember(items, filterText.text) {
        if (filterText.text.isBlank()) {
            items.take(maxDisplayedItems)
        } else {
            val query = filterText.text.lowercase().trim()
            val queryNormalized = normalizeText(query)

            items
                .map { item ->
                    val itemNormalized = normalizeText(item.lowercase())
                    val score = calculateRelevanceScore(itemNormalized, queryNormalized, item.lowercase(), query)
                    item to score
                }
                .filter { it.second > 0 } // Solo items con coincidencia
                .sortedByDescending { it.second } // Ordenar por relevancia
                .take(maxDisplayedItems)
                .map { it.first }
        }
    }

    // **SIMPLIFICADO**: Solo mostrar si está escribiendo Y hay resultados
    val shouldShowDropdown = userIsTyping && filterText.text.isNotEmpty() && filteredItems.isNotEmpty()

    ExposedDropdownMenuBox(
        expanded = shouldShowDropdown, // Usar directamente la condición
        modifier = modifier,
        onExpandedChange = { newExpanded ->
            expanded = newExpanded
            // **SIMPLIFICADO**: Si el usuario expande manualmente, está "escribiendo"
            if (newExpanded) {
                userIsTyping = true
            }
        },
    ) {
        // Input field
        OutlinedTextField(
            value = filterText,
            onValueChange = { newValue ->
                filterText = newValue
                // **CAMBIO 3**: Expandir solo cuando hay texto y resultados
                expanded = newValue.text.isNotEmpty() &&
                        items.any { item ->
                            calculateRelevanceScore(
                                normalizeText(item.lowercase()),
                                normalizeText(newValue.text.lowercase()),
                                item.lowercase(),
                                newValue.text.lowercase()
                            ) > 0
                        }
            },
            placeholder = {
                if (filterText.text.isEmpty()) Text(placeholder)
            },
            trailingIcon = {
                // **CAMBIO**: Simplificar - siempre mostrar el icono
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = shouldShowDropdown)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = traiBlue,
                unfocusedBorderColor = traiBlue,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = Color.Black
            ),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        // **SOLUCIÓN**: Usar ExposedDropdownMenu que maneja mejor el teclado
        ExposedDropdownMenu(
            expanded = shouldShowDropdown,
            onDismissRequest = {
                expanded = false
            },
            modifier = Modifier.heightIn(max = 240.dp)
        ) {
            filteredItems.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = highlightMatches(item, filterText.text),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    onClick = {
                        filterText = TextFieldValue(item)
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }

            // Mostrar mensaje si hay más resultados
            if (items.size > maxDisplayedItems && filterText.text.isNotBlank()) {
                val totalMatches = items.count { item ->
                    calculateRelevanceScore(
                        normalizeText(item.lowercase()),
                        normalizeText(filterText.text.lowercase()),
                        item.lowercase(),
                        filterText.text.lowercase()
                    ) > 0
                }
                if (totalMatches > filteredItems.size) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "... y ${totalMatches - filteredItems.size} más",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        onClick = { /* No hacer nada */ },
                        enabled = false
                    )
                }
            }
        }
    }
}

/**
 * Normaliza texto removiendo acentos y caracteres especiales
 */
private fun normalizeText(text: String): String {
    return Normalizer.normalize(text, Normalizer.Form.NFD)
        .replace(Regex("[\\p{InCombiningDiacriticalMarks}]"), "")
        .lowercase()
}

/**
 * Calcula puntuación de relevancia para ordenar resultados
 */
private fun calculateRelevanceScore(
    normalizedItem: String,
    normalizedQuery: String,
    originalItem: String,
    originalQuery: String
): Int {
    if (normalizedQuery.isBlank()) return 0

    var score = 0

    // 🎯 Coincidencia exacta (máxima prioridad)
    if (normalizedItem == normalizedQuery) score += 1000

    // 🎯 Inicia con la búsqueda (alta prioridad)
    if (normalizedItem.startsWith(normalizedQuery)) score += 500

    // 🎯 Palabra completa coincide (alta prioridad)
    val itemWords = normalizedItem.split(" ", "-", "_")
    val queryWords = normalizedQuery.split(" ", "-", "_")

    queryWords.forEach { queryWord ->
        itemWords.forEach { itemWord ->
            when {
                itemWord == queryWord -> score += 300
                itemWord.startsWith(queryWord) -> score += 200
                itemWord.contains(queryWord) -> score += 100
            }
        }
    }

    // 🎯 Contiene la búsqueda (prioridad media)
    if (normalizedItem.contains(normalizedQuery)) score += 50

    // 🎯 Búsqueda fuzzy - caracteres en orden
    if (containsCharactersInOrder(normalizedItem, normalizedQuery)) score += 25

    return score
}

/**
 * Verifica si todos los caracteres de query aparecen en orden en item
 */
private fun containsCharactersInOrder(item: String, query: String): Boolean {
    var queryIndex = 0
    for (char in item) {
        if (queryIndex < query.length && char == query[queryIndex]) {
            queryIndex++
        }
    }
    return queryIndex == query.length
}

/**
 * Resalta las coincidencias en el texto
 */
@Composable
private fun highlightMatches(text: String, query: String): androidx.compose.ui.text.AnnotatedString {
    if (query.isBlank()) return buildAnnotatedString { append(text) }

    return buildAnnotatedString {
        val normalizedText = normalizeText(text)
        val normalizedQuery = normalizeText(query)

        var lastIndex = 0
        var searchIndex = 0

        while (searchIndex < normalizedText.length) {
            val matchIndex = normalizedText.indexOf(normalizedQuery, searchIndex)
            if (matchIndex == -1) break

            // Texto antes de la coincidencia
            append(text.substring(lastIndex, matchIndex))

            // Coincidencia resaltada
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    color = traiBlue
                )
            ) {
                append(text.substring(matchIndex, matchIndex + normalizedQuery.length))
            }

            lastIndex = matchIndex + normalizedQuery.length
            searchIndex = lastIndex
        }

        // Resto del texto
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
}

@Composable
@Preview
fun FilterableDropdownPreview() {
    val lista = listOf(
        "Press banca",
        "Press banca inclinado",
        "Press militar",
        "Curl mancuerna",
        "Curl barra",
        "Sentadilla",
        "Sentadilla búlgara",
        "Dominadas",
        "Peso muerto",
        "Fondos"
    )

    FilterableDropdown(
        items = lista,
        onItemSelected = { selected ->
            println("Seleccionaste: $selected")
        },
        selectedValue = "",
        placeholder = "Buscar ejercicio..."
    )
}