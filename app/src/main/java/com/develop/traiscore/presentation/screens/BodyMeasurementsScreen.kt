package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.develop.traiscore.presentation.theme.navbarDay
import com.develop.traiscore.presentation.theme.traiBlue


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyMeasurementsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    initialData: Map<String, String> = emptyMap(),
    onSave: (gender: String, data: Map<String, String>) -> Unit,


    ) {
    var selectedGender by remember { mutableStateOf(initialData["Gender"] ?: "Male") }
    val genders = listOf("Male", "Female", "Other")

    // Estado de cada campo
    val measurements = remember {
        mutableStateMapOf(
            "Height" to (initialData["Height"] ?: ""),
            "Weight" to (initialData["Weight"] ?: ""),
            "Neck"   to (initialData["Neck"]   ?: ""),
            "Chest"  to (initialData["Chest"]  ?: ""),
            "Arms"   to (initialData["Arms"]   ?: ""),
            "Waist"  to (initialData["Waist"]  ?: ""),
            "Thigh"  to (initialData["Thigh"]  ?: ""),
            "Calf"   to (initialData["Calf"]   ?: "")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Body Measurements", color = traiBlue) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = navbarDay
                )
            )
        },
        bottomBar = {
            ExtendedFloatingActionButton(
                onClick = { onSave(selectedGender, measurements) },
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
                text = { Text("Save") },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save"
                    )
                },
                containerColor = traiBlue,
                contentColor = Color.White
            )
        },
        content = { inner ->
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .background(Color.LightGray)
                    .navigationBarsPadding()
                    .padding(inner),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                // 1) Selector de género
                item {
                    Text("Gender", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        genders.forEach { gender ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = (selectedGender == gender),
                                    onClick = { selectedGender = gender },
                                    colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                        selectedColor = traiBlue
                                    )
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(gender)
                            }
                        }
                    }
                }

                // 2) Campos de medida en dos columnas
                val entries = measurements.entries.toList()
                items(entries.chunked(2)) { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        pair.forEach { (label, value) ->
                            Column(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = value,
                                    onValueChange = { measurements[label] = it },
                                    singleLine = true,
                                    label = { Text(label) },
                                    trailingIcon = { Text(if (label == "Weight") "kg" else "cm") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = traiBlue,
                                        focusedLabelColor = traiBlue,
                                        cursorColor = traiBlue
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                )
                            }
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                // Espacio extra abajo para que no tape la bottom bar
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    )
}