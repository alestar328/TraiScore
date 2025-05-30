package com.develop.traiscore.presentation.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.develop.traiscore.presentation.theme.navbarDay
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.viewmodels.BodyStatsViewModel
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import com.develop.traiscore.data.local.entity.SubscriptionLimits
import com.develop.traiscore.presentation.components.SubscriptionInfoCard
import com.develop.traiscore.presentation.components.UpgradeDialog
import com.develop.traiscore.presentation.viewmodels.SubscriptionViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyMeasurementsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    initialData: Map<String, String> = emptyMap(),
    onSave: (gender: String, data: Map<String, String>) -> Unit,
    bodyStatsViewModel: BodyStatsViewModel = hiltViewModel(),
    subscriptionViewModel: SubscriptionViewModel = hiltViewModel(),
    onMeasurementsClick: () -> Unit,
    onMeasurementsHistoryClick: () -> Unit // ← NUEVO PARÁMETRO


) {
    val context = LocalContext.current

    // Estados locales para la UI
    var selectedGender by remember { mutableStateOf("Male") }
    val genders = listOf("Male", "Female", "Other")
    var showUpgradeDialog by remember { mutableStateOf(false) }
    var subscriptionLimits by remember { mutableStateOf<SubscriptionLimits?>(null) }

    // Estados de medidas - inicializar con datos del ViewModel o initialData
    val measurements = remember {
        mutableStateMapOf<String, String>().apply {
            // Usar datos del ViewModel si están disponibles, sino usar initialData
            val defaultMeasurements = mapOf(
                "Height" to "",
                "Weight" to "",
                "Neck" to "",
                "Chest" to "",
                "Arms" to "",
                "Waist" to "",
                "Thigh" to "",
                "Calf" to ""
            )

            defaultMeasurements.forEach { (key, defaultValue) ->
                put(key, initialData[key] ?: defaultValue)
            }
        }
    }

    // Cargar datos existentes al inicializar
    LaunchedEffect(Unit) {
        bodyStatsViewModel.loadLatestBodyStats()
        subscriptionViewModel.loadUserSubscription { subscription ->
            // USAR LA NUEVA FUNCIÓN que cuenta primero
            subscriptionViewModel.checkBodyStatsLimitsWithCount { limits ->
                subscriptionLimits = limits
                Log.d("BodyMeasurementsScreen", "Límites cargados: $limits")
            }
        }
    }

    // Actualizar límites cuando cambie la suscripción
    LaunchedEffect(subscriptionViewModel.userSubscription, subscriptionViewModel.actualDocumentsCount) {
        if (subscriptionViewModel.userSubscription != null) {
            subscriptionLimits = subscriptionViewModel.checkBodyStatsLimits()
            Log.d("BodyMeasurementsScreen", "Límites actualizados: $subscriptionLimits")
        }
    }

    // Sincronizar datos del ViewModel con la UI cuando se carguen
    LaunchedEffect(bodyStatsViewModel.bodyMeasurements, bodyStatsViewModel.selectedGender) {
        if (bodyStatsViewModel.bodyMeasurements.isNotEmpty()) {
            bodyStatsViewModel.bodyMeasurements.forEach { (key, value) ->
                measurements[key] = value
            }
        }
        bodyStatsViewModel.selectedGender?.let { gender ->
            selectedGender = gender
        }
    }

    // Mostrar mensajes de error
    LaunchedEffect(bodyStatsViewModel.errorMessage) {
        bodyStatsViewModel.errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            bodyStatsViewModel.clearError()
        }
    }

    // Función para guardar datos
    fun saveData() {
        val currentMeasurements = measurements.toMap()

        // Validar datos antes de guardar
        val validationError = bodyStatsViewModel.validateMeasurements(currentMeasurements)
        if (validationError != null) {
            Toast.makeText(context, validationError, Toast.LENGTH_SHORT).show()
            return
        }

        // Verificar límites de suscripción
        val limits = subscriptionViewModel.checkBodyStatsLimits()
        if (!limits.canCreateBodyStats) {
            showUpgradeDialog = true
            return
        }

        // Guardar en Firebase con límites
        bodyStatsViewModel.saveBodyStatsWithLimits(
            gender = selectedGender,
            measurements = currentMeasurements,
            subscriptionViewModel = subscriptionViewModel
        ) { success, error, requiresUpgrade ->
            when {
                success -> {
                    Toast.makeText(context, "Medidas guardadas exitosamente", Toast.LENGTH_SHORT).show()
                    // Actualizar límites después de guardar
                    subscriptionLimits = subscriptionViewModel.checkBodyStatsLimits()
                    onSave(selectedGender, currentMeasurements)
                }
                requiresUpgrade -> showUpgradeDialog = true
                else -> Toast.makeText(context, error ?: "Error al guardar", Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun validateMeasurementInput(input: String): String {
        if (input.isEmpty()) return input

        // No permitir números que empiecen con 0 (excepto 0 solo o 0.x)
        if (input.startsWith("0") && input.length > 1 && input[1] != '.') {
            return input.drop(1)
        }

        // No permitir negativos
        if (input.startsWith("-")) return input.drop(1)

        // Filterar solo números y punto decimal
        val filtered = input.filter { it.isDigit() || it == '.' }

        // Manejar punto decimal
        val parts = filtered.split(".")

        return when {
            // Más de un punto decimal
            parts.size > 2 -> filtered.dropLast(1)

            // Solo parte entera
            parts.size == 1 -> {
                val intPart = parts[0].take(3) // Máximo 3 dígitos enteros
                intPart
            }

            // Parte entera + decimal
            else -> {
                val intPart = parts[0].take(3)  // Máximo 3 dígitos enteros
                val decPart = parts[1].take(2)  // Máximo 2 dígitos decimales

                if (intPart.isEmpty() && decPart.isEmpty()) {
                    ""
                } else if (decPart.isEmpty()) {
                    "$intPart."
                } else {
                    "$intPart.$decPart"
                }
            }
        }
    }

    // Diálogo de actualización de plan
    if (showUpgradeDialog) {
        UpgradeDialog(
            limits = subscriptionLimits,
            onDismiss = { showUpgradeDialog = false },
            onUpgrade = {
                // Simular upgrade (aquí se integrará el sistema de pagos)
                subscriptionViewModel.upgradeToPremium { success ->
                    if (success) {
                        Toast.makeText(context, "¡Upgrade exitoso! Ahora tienes medidas ilimitadas", Toast.LENGTH_LONG).show()
                        showUpgradeDialog = false
                    } else {
                        Toast.makeText(context, "Error en el upgrade", Toast.LENGTH_SHORT).show()
                    }
                }
            }
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
            Column {
                // Fila de botones superiores (solo mostrar si las funciones no son vacías)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    // Botón de historial
                    ExtendedFloatingActionButton(
                        onClick = onMeasurementsHistoryClick,
                        modifier = Modifier.weight(1f),
                        containerColor = Color.Yellow,
                        contentColor = Color.Black
                    ) {
                        Icon(
                            Icons.Default.ThumbUp,
                            contentDescription = "Historial",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ver Historial")
                    }
                }

                // Botón principal de guardar
                ExtendedFloatingActionButton(
                    onClick = {
                        if (!bodyStatsViewModel.isLoading) {
                            saveData()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    text = {
                        if (bodyStatsViewModel.isLoading) {
                            Text("Guardando...")
                        } else {
                            Text("Save")
                        }
                    },
                    icon = {
                        if (bodyStatsViewModel.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.width(24.dp).height(24.dp),
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save"
                            )
                        }
                    },
                    containerColor = traiBlue,
                    contentColor = Color.White
                )
            }
        },
        content = { inner ->
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                        .background(Color.LightGray)
                        .padding(inner),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    // Mostrar información de suscripción
                    subscriptionLimits?.let { limits ->
                        item {
                            SubscriptionInfoCard(limits = limits)
                        }
                    }
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
                                        ),
                                        enabled = !bodyStatsViewModel.isLoading
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = gender,
                                        color = if (bodyStatsViewModel.isLoading) Color.Gray else Color.Black
                                    )
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
                                        onValueChange = { newValue ->
                                            val validatedValue = validateMeasurementInput(newValue)
                                            measurements[label] = validatedValue
                                        },
                                        singleLine = true,
                                        label = { Text(label) },
                                        trailingIcon = {
                                            Text(
                                                text = if (label == "Weight") "kg" else "cm",
                                                color = Color.Gray
                                            )
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = traiBlue,
                                            focusedLabelColor = traiBlue,
                                            cursorColor = traiBlue
                                        ),
                                        enabled = !bodyStatsViewModel.isLoading,
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

                // Indicador de carga inicial
                if ((bodyStatsViewModel.isLoading && bodyStatsViewModel.bodyMeasurements.isEmpty()) ||
                    subscriptionViewModel.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = traiBlue)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Cargando...", color = Color.Gray)
                        }
                    }
                }
            }
        }
    )
}