package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.develop.traiscore.data.local.entity.LabEntry
import com.develop.traiscore.presentation.components.TraiScoreTopBar
import com.develop.traiscore.presentation.components.general.LabResultsTableUI
import com.develop.traiscore.presentation.theme.tsColors

@Composable
fun LabResultsScreen(
    entries: List<LabEntry>,
    onEntriesChange: (List<LabEntry>) -> Unit,
    onBack: () -> Unit,              // ← Volver a tomar foto
    onConfirm: (() -> Unit)? = null, // ← Opcional: continuar/guardar
    unitSuggestionsByTest: Map<String, List<String>> = emptyMap()
) {
    Scaffold(
        topBar = {
            TraiScoreTopBar(
                leftIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.tsColors.ledCyan
                        )
                    }
                },
                rightIcon = {
                    if (onConfirm != null) {
                        TextButton(onClick = onConfirm) {
                            Text("Confirmar")
                        }
                    } else {
                        // espacio para equilibrar el TopBar cuando no hay acción derecha
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(24.dp))
                    }
                }
            )
        }
    ) { padding ->
        LabResultsTableUI(
            modifier = Modifier
                .padding(padding)
                .navigationBarsPadding()
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            entries = entries,
            onEntriesChange = onEntriesChange,
            unitSuggestionsByTest = unitSuggestionsByTest
        )
    }
}