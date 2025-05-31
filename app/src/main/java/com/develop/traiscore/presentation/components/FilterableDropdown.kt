package com.develop.traiscore.presentation.components


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.develop.traiscore.presentation.theme.traiBlue


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterableDropdown(
    items: List<String>,
    onItemSelected: (String) -> Unit,
    selectedValue: String,
    placeholder: String = "",
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    var filterText by remember { mutableStateOf(TextFieldValue("")) }

    LaunchedEffect(selectedValue) {
        filterText = if (selectedValue.isNotBlank())
            TextFieldValue(selectedValue)
        else
            TextFieldValue("")

    }
    val filteredItems = items.filter {
        it.lowercase().startsWith(filterText.text.lowercase())
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        modifier = modifier,
        onExpandedChange = { expanded = !expanded },
    ) {
        // Input field
        OutlinedTextField(
            value = filterText,
            onValueChange = {
                filterText = it
                expanded = true

            },
            placeholder = {
                if (filterText.text.isEmpty()) Text(placeholder)
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy( // ✅ AÑADIR: Configuración del teclado
                capitalization = KeyboardCapitalization.Sentences,
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

        ExposedDropdownMenu(
            expanded = expanded && filterText.text.isNotEmpty(),
            onDismissRequest = { expanded = false }
        ) {
            filteredItems.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        filterText = TextFieldValue(item)
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
@Preview
fun FilterableDropdownPreview() {
    val lista = listOf("Press banca", "Sentadilla", "Dominadas", "Peso muerto", "Fondos")
    FilterableDropdown(
        items = lista,
        onItemSelected = { selected ->
            println("Seleccionaste: $selected")
        },
        selectedValue = "Selecciona"
    )
}

