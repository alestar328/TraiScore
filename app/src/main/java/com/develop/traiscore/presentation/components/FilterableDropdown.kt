package com.develop.traiscore.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
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
    maxDisplayedItems: Int = 6,
    textFieldHeight: Dp? = null,
    textSize: TextUnit? = null,
    contentPadding: PaddingValues? = null
) {
    var expanded by remember { mutableStateOf(false) }
    var filterText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    // Sincronizar el valor externo (selectedValue) con el campo de texto
    LaunchedEffect(selectedValue) {
        filterText = selectedValue
    }

    val filteredItems = remember(items, filterText) {
        if (filterText.isBlank()) {
            items.take(maxDisplayedItems)
        } else {
            val query = filterText.lowercase().trim()
            val queryNormalized = normalizeText(query)

            items
                .map { item ->
                    val itemNormalized = normalizeText(item.lowercase())
                    val score = calculateRelevanceScore(
                        itemNormalized,
                        queryNormalized,
                        item.lowercase(),
                        query
                    )
                    item to score
                }
                .filter { it.second > 0 }
                .sortedByDescending { it.second }
                .take(maxDisplayedItems)
                .map { it.first }
        }
    }

    val hasText = filterText.isNotEmpty()
    val shouldShowDropdown = expanded && filteredItems.isNotEmpty()

    ExposedDropdownMenuBox(
        expanded = shouldShowDropdown,
        onExpandedChange = { newExpanded ->
            expanded = newExpanded
        },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = filterText,
            onValueChange = { newValue ->
                filterText = newValue
                expanded = true // al escribir, abrimos el dropdown
            },
            singleLine = true,
            maxLines = 1,
            textStyle = textSize?.let { LocalTextStyle.current.copy(fontSize = it) }
                ?: LocalTextStyle.current,
            placeholder = {
                if (filterText.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = textSize?.let { LocalTextStyle.current.copy(fontSize = it) }
                            ?: LocalTextStyle.current
                    )
                }
            },
            trailingIcon = {
                if (hasText) {
                    IconButton(
                        onClick = {
                            filterText = ""
                            onItemSelected("")
                            expanded = false
                            focusManager.clearFocus()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Limpiar",
                            tint = Color.Red
                        )
                    }
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = shouldShowDropdown)
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Words,
                imeAction = androidx.compose.ui.text.input.ImeAction.Done
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
                .heightIn(min = textFieldHeight ?: TextFieldDefaults.MinHeight)
                .then(if (contentPadding != null) Modifier.padding(contentPadding) else Modifier)
        )

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
                            text = highlightMatches(item, filterText),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    onClick = {
                        filterText = item           // 👈 mostrar el seleccionado en el campo
                        onItemSelected(item)        // 👈 notificar al padre
                        expanded = false
                        focusManager.clearFocus()
                    }
                )
            }

            // Mensaje de "y más..." si hay más resultados
            if (items.size > maxDisplayedItems && filterText.isNotBlank()) {
                val totalMatches = items.count { item ->
                    calculateRelevanceScore(
                        normalizeText(item.lowercase()),
                        normalizeText(filterText.lowercase()),
                        item.lowercase(),
                        filterText.lowercase()
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
                        onClick = { },
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
private fun highlightMatches(text: String, query: String) =
    if (query.isBlank()) {
        buildAnnotatedString { append(text) }
    } else {
        buildAnnotatedString {
            val normalizedText = normalizeText(text)
            val normalizedQuery = normalizeText(query)

            var lastIndex = 0
            var searchIndex = 0

            while (searchIndex < normalizedText.length) {
                val matchIndex = normalizedText.indexOf(normalizedQuery, searchIndex)
                if (matchIndex == -1) break

                append(text.substring(lastIndex, matchIndex))

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

            if (lastIndex < text.length) {
                append(text.substring(lastIndex))
            }
        }
    }
