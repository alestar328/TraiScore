package com.develop.traiscore.presentation.components

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.develop.traiscore.core.DefaultCategoryExer
import com.develop.traiscore.presentation.theme.traiBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExeRoutineDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
    exerciseNames: List<String>,
    onExerciseSelected: (String) -> Unit,
    selectedCategory: DefaultCategoryExer?
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && selectedCategory != null) {
                        onSave(name, selectedCategory.name)
                        Toast.makeText(context, "Añadido!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color(0xFFA8E6CF),
                    contentColor = Color.Black
                )
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color(0xFFFF8A80),
                    contentColor = Color.Black
                )
            ) {
                Text("Cancelar")
            }
        },
        title = {
            Text("Agregar Ejercicio", fontSize = 20.sp, color = Color.Cyan)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Ejercicio", color = traiBlue, fontSize = 18.sp)

                FilterableDropdown(
                    items = exerciseNames,
                    selectedValue = name,
                    onItemSelected = { selected ->
                        name = selected
                        onExerciseSelected(selected)
                    },
                    placeholder = "Buscar ejercicio...",
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Categoría", color = traiBlue, fontSize = 18.sp)

                OutlinedTextField(
                    value = selectedCategory?.let { stringResource(it.titleCat) } ?: "Sin categoría",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = traiBlue,
                        unfocusedBorderColor = traiBlue,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }
        },
        containerColor = Color.DarkGray,
        shape = RoundedCornerShape(16.dp)
    )
}