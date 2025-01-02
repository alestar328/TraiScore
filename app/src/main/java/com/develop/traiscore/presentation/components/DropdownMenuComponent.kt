package com.develop.traiscore.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.develop.traiscore.presentation.theme.TraiScoreTheme
import com.develop.traiscore.presentation.theme.traiBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuComponent(
    items: List<String>,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Seleccionar"
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf(placeholder) }
    val shape = RoundedCornerShape(12.dp)

    // Botón desplegable
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selectedItem,
            onValueChange = { },
            readOnly = true,
            modifier = modifier
                .menuAnchor()
                .fillMaxWidth()
                .border(
                    width = 5.dp, // Grosor del borde
                    color = traiBlue, // Color del borde
                    shape = shape, // Esquinas redondeadas
                )
                .clip(shape) // Recorta el contenido al borde redondeado
                .background(Color.White), // Aplica el color de fondo dentro del borde
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(
                focusedContainerColor = Color.White, // Fondo al enfocarse
                unfocusedContainerColor = Color.White, // Fondo sin enfocarse
                unfocusedIndicatorColor = Color.Transparent, // Sin línea inferior
                focusedIndicatorColor = Color.Transparent // Sin línea inferior al enfocarse
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        selectedItem = item
                        expanded = false
                        onItemSelected(item)

                    }
                )

            }
        }
    }

}


@Preview(
    name = "DropdownMenuComponentPreview",
    showBackground = true
)
@Composable
fun DropdownMenuComponentPreview() {

    val lista = listOf("Opcion 1", "Opcion 2", "Opcion 3")
    TraiScoreTheme {
        DropdownMenuComponent(
            items = lista,
            onItemSelected = { selected ->
                println("Seleccionaste: $selected")
            },
            placeholder = "Selecciona una opcion"

        )
    }
}