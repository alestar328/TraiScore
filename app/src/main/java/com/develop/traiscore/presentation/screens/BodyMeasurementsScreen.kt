package com.develop.traiscore.presentation.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.viewmodels.BodyStatsViewModel
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.develop.traiscore.R
import com.develop.traiscore.data.local.entity.SubscriptionLimits
import com.develop.traiscore.domain.model.MeasurementType
import com.develop.traiscore.presentation.components.SubscriptionInfoCard
import com.develop.traiscore.presentation.components.UpgradeDialog
import com.develop.traiscore.presentation.theme.traiOrange
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
    onMeasurementsHistoryClick: () -> Unit,
    onConfigureTopBar: (
        @Composable () -> Unit,
        @Composable () -> Unit,
        (@Composable () -> Unit)?
    ) -> Unit,
    onConfigureFAB: (fab: (@Composable () -> Unit)?) -> Unit,
) {
    val context = LocalContext.current

    // Estados locales para la UI
    var selectedGender by remember { mutableStateOf("Male") }
    val genders = listOf(
        "Male" to stringResource(R.string.gender_male),
        "Female" to stringResource(R.string.gender_female),
        "Other" to stringResource(R.string.gender_other)
    )
    var showUpgradeDialog by remember { mutableStateOf(false) }
    var subscriptionLimits by remember { mutableStateOf<SubscriptionLimits?>(null) }

    // Estados de medidas - inicializar con datos del ViewModel o initialData
    val measurements = remember {
        mutableStateMapOf<String, String>().apply {
            // Usar enum para obtener medidas por defecto
            MeasurementType.getAllMeasurements().forEach { measurementType ->
                put(measurementType.key, initialData[measurementType.key] ?: "")
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (bodyStatsViewModel.isEditMode) {
                bodyStatsViewModel.clearEditMode()
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

        // ✅ CAMBIO: Usar la nueva función que detecta si es edición o creación
        bodyStatsViewModel.saveOrUpdateBodyStats(
            gender = selectedGender,
            measurements = currentMeasurements,
            subscriptionViewModel = subscriptionViewModel
        ) { success, error, requiresUpgrade ->
            when {
                success -> {
                    val message = if (bodyStatsViewModel.isEditMode) {
                        "Medidas actualizadas exitosamente"
                    } else {
                        "Medidas guardadas exitosamente"
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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

    LaunchedEffect(bodyStatsViewModel.isEditMode) {
        onConfigureTopBar(
            {
                IconButton(onClick = {
                    if (bodyStatsViewModel.isEditMode) {
                        bodyStatsViewModel.clearEditMode()
                    }
                    onBack()
                }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = traiBlue
                    )
                }
            },
            { /* right icon vacío */ },
            {
                Text(
                    text = if (bodyStatsViewModel.isEditMode)
                        "Editar medidas"
                    else
                        stringResource(R.string.profile_my_sizes),
                    color = traiBlue
                )
            }
        )
    }

    LaunchedEffect(bodyStatsViewModel.isEditMode, bodyStatsViewModel.isLoading) {
        onConfigureFAB {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                if (!bodyStatsViewModel.isEditMode) {
                    ExtendedFloatingActionButton(
                        onClick = onMeasurementsHistoryClick,
                        containerColor = traiOrange,
                        contentColor = Color.Black
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.history_logo),
                            contentDescription = "Historial",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.view_history))
                    }
                }

                ExtendedFloatingActionButton(
                    onClick = {
                        if (!bodyStatsViewModel.isLoading) {
                            saveData()
                        }
                    },
                    containerColor = traiBlue,
                    contentColor = Color.White
                ) {
                    if (bodyStatsViewModel.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White
                        )
                    } else {
                        Icon(Icons.Default.Check, contentDescription = "Guardar")
                    }

                    Spacer(Modifier.width(8.dp))

                    Text(
                        if (bodyStatsViewModel.isEditMode) "Actualizar"
                        else stringResource(R.string.save_measurements)
                    )
                }
            }
        }
    }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ){
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Mostrar información de suscripción
                    if (!bodyStatsViewModel.isEditMode) {
                        subscriptionLimits?.let { limits ->
                            SubscriptionInfoCard(limits = limits)
                        }
                    }

                    // 1) Selector de género
             /*       Text("Género", style = MaterialTheme.typography.titleLarge)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(1.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        genders.forEach { (genderKey, genderDisplayName) ->
                            // Cada opción ocupa un tercio del ancho
                            Row(
                                modifier = Modifier.weight(0.5f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center // centra Radio + texto dentro de su celda
                            ) {
                                RadioButton(
                                    selected = (selectedGender == genderKey),
                                    onClick = { selectedGender = genderKey },
                                    colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                        selectedColor = traiBlue
                                    ),
                                    enabled = !bodyStatsViewModel.isLoading
                                )
                                Text(
                                    text = genderDisplayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (bodyStatsViewModel.isLoading) Color.Gray else Color.White
                                )
                            }
                        }
                    }*/


                    // ✅ CAMBIO: Campos de medida usando enum y strings resources
                    val measurementTypes = MeasurementType.getAllMeasurements()
                    measurementTypes.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            pair.forEach { measurementType ->
                                Column(modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(
                                        value = measurements[measurementType.key] ?: "",
                                        onValueChange = { newValue ->
                                            val validatedValue = validateMeasurementInput(newValue)
                                            measurements[measurementType.key] = validatedValue
                                        },
                                        singleLine = true,
                                        label = {
                                            Text(stringResource(measurementType.displayNameRes))
                                        },
                                        trailingIcon = {
                                            Text(
                                                text = stringResource(measurementType.unitRes),
                                                color = Color.White
                                            )
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = traiBlue,
                                            focusedLabelColor = traiBlue,
                                            cursorColor = traiBlue,
                                            unfocusedBorderColor = Color.White,
                                            unfocusedLabelColor = Color.White
                                        ),
                                        enabled = !bodyStatsViewModel.isLoading,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    // Espacio extra abajo para que no tape la bottom bar
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