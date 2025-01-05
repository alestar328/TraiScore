package com.develop.traiscore.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.develop.traiscore.presentation.theme.traiBlue



@Composable
fun FilterableDropdown(
    items: List<String>,
    onItemSelected: (String) -> Unit,
    selectedValue: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var filterText by remember { mutableStateOf(selectedValue) }
    val filteredItems = items.filter { it.contains(filterText, ignoreCase = true) }

    var textFieldSize by remember { mutableStateOf(IntSize.Zero) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Input field
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(2.dp, traiBlue, RoundedCornerShape(12.dp))
                .clickable { expanded = !expanded }
                .padding(8.dp)
                .onGloballyPositioned { coordinates ->
                    textFieldSize = coordinates.size
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                BasicTextField(
                    value = filterText,
                    onValueChange = {
                        filterText = it
                        expanded = true
                    },
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = Color.Black
                    ),
                    decorationBox = { innerTextField ->
                        Box(modifier = Modifier.weight(1f)) {
                            if (filterText.isEmpty()) {
                                Text(
                                    text = "Selecciona",
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        color = Color.Gray
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown Icon",
                    tint = traiBlue,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                )
            }
        }

        // Dropdown menu
        if (expanded) {
            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(
                    x = 0,
                    y = with(LocalDensity.current) { textFieldSize.height.toDp().roundToPx() +40 } // Adding 4dp for spacing
                )
            ) {
                Column(
                    modifier = Modifier
                        .width(with(LocalDensity.current) { textFieldSize.width.toDp() })
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(2.dp, traiBlue, RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    filteredItems.forEach { item ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    filterText = item
                                    onItemSelected(item)
                                    expanded = false
                                }
                                .padding(8.dp)
                        ) {
                            Text(text = item, fontSize = 16.sp, color = Color.Black)
                        }
                    }
                }
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

