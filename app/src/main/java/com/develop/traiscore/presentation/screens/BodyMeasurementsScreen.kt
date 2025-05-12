package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import com.develop.traiscore.presentation.theme.traiBlue


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyMeasurementsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    var selectedGender by remember { mutableStateOf("Male") }
    val genders = listOf("Male", "Female", "Other")

    // Map of field label → (current value, onValueChange)
    val measurements = remember {
        mutableStateMapOf(
            "Height" to "180",
            "Weight" to "75",
            "Neck"   to "40",
            "Chest"  to "100",
            "Arms"   to "35",
            "Waist"  to "80",
            "Thigh"  to "60",
            "Calf"   to "40"
        )
    }

    Column( // Usamos Column en lugar de Scaffold
        modifier = modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Body Measurements", fontSize = 20.sp , color = traiBlue)},
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack,
                        contentDescription = "Back",)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Gray
            )
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentPadding = PaddingValues(vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1) Gender selector
            item {
                Text("Gender",
                    color = Color.Black,
                    style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    genders.forEach { gender ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = (selectedGender == gender),
                                onClick = { selectedGender = gender }
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(gender,
                                color = Color.Black)
                        }
                    }
                }
            }

            // 2) Measurement fields
            items(measurements.entries.toList()) { (label, value) ->
                val unit = if (label == "Weight") "kg" else "cm"
                OutlinedTextField(
                    value = value,
                    onValueChange = { new -> measurements[label] = new },
                    label = { Text(label) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = { Text(unit) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}