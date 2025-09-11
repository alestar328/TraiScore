package com.develop.traiscore.presentation.components

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.develop.traiscore.core.DefaultCategoryExer
import com.develop.traiscore.presentation.theme.traiBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExerciseDialogToDB(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit // nombre, categoría
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<DefaultCategoryExer?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var message = ""
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && selectedCategory != null) {
                        message = "Guardado!";
                        onSave(name, selectedCategory!!.name)
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color(0xFFA8E6CF), // Verde pastel
                    contentColor = Color.Black
                )
            )
            {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color(0xFFFF8A80), // Rojo pastel
                    contentColor = Color.Black
                )
            ) {
                Text("Cancelar")
            }
        },
        title = {
            Text("Nuevo Ejercicio", fontSize = 20.sp, color = Color.Cyan)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Ejercicio",
                    color = traiBlue,
                    fontSize = 18.sp
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = traiBlue,
                        unfocusedBorderColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        cursorColor = Color.Black
                    )
                )
                Text(
                    text = "Categoría",
                    color = traiBlue,
                    fontSize = 18.sp
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.let { stringResource(id = it.titleCat) } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = traiBlue,
                            unfocusedBorderColor = traiBlue,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DefaultCategoryExer.entries.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(id = category.titleCat),
                                        fontSize = 13.sp
                                    )
                                },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color.DarkGray,
        shape = RoundedCornerShape(16.dp)
    )
}