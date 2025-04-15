package com.develop.traiscore.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.develop.traiscore.presentation.theme.traiBlue


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterableDropdown(
    items: List<String>,
    onItemSelected: (String) -> Unit,
    selectedValue: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    var filterText by remember { mutableStateOf(TextFieldValue(selectedValue)) }

    val filteredItems = items.filter {
        it.contains(filterText.text, ignoreCase = true)
    }

    ExposedDropdownMenuBox(
        expanded = expanded && filteredItems.isNotEmpty() && filterText.text.length > 0,
        modifier = modifier,
        onExpandedChange = { expanded = !expanded },
    ) {
        // Input field
        OutlinedTextField(
            value = filterText,
            onValueChange = {
                filterText = it
                expanded = it.text.isNotEmpty() // ✅ Solo abre si hay texto

            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = traiBlue,
                unfocusedBorderColor = traiBlue,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
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

