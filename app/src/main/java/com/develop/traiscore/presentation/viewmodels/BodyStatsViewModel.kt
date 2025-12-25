package com.develop.traiscore.presentation.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.develop.traiscore.data.local.entity.BodyStatsEntity
import com.develop.traiscore.data.local.entity.UserMeasurements
import com.develop.traiscore.data.repository.BodyStatsRepository
import com.develop.traiscore.domain.model.BodyMeasurementProgressBuilder
import com.develop.traiscore.domain.model.BodyMeasurementProgressData
import com.develop.traiscore.domain.model.BodyMeasurementType
import com.develop.traiscore.domain.model.MeasurementSummary
import com.develop.traiscore.domain.model.MeasurementType
import com.develop.traiscore.presentation.screens.MeasurementHistoryItem
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class BodyStatsViewModel @Inject constructor(
    private val repository: BodyStatsRepository  // ← INYECTAR REPOSITORY
) : ViewModel() {

    companion object {
        private const val TAG = "BodyStatsVM"
    }

    private var targetUserId: String? = null

    // Estados del ViewModel - mantener compatibilidad con UI existente
    var isLoading by mutableStateOf(false)
        private set
    var isEditMode by mutableStateOf(false)
        private set
    var editingDocumentId by mutableStateOf<String?>(null)
        private set
    var editingLocalId by mutableStateOf<Long?>(null)  // ← NUEVO: ID local para edición
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var bodyMeasurements by mutableStateOf<Map<String, String>>(emptyMap())
        private set
    var selectedGender by mutableStateOf<String?>(null)
        private set

    // Lista de medidas para el historial
    private var bodyStatsList by mutableStateOf<List<BodyStatsEntity>>(emptyList())

    private fun getCurrentUserId(): String {
        val userId = targetUserId ?: FirebaseAuth.getInstance().currentUser?.uid ?: ""
        Log.d(TAG, "🔧 getCurrentUserId() retorna: $userId (targetUserId=$targetUserId)")
        return userId
    }

    fun setTargetUser(userId: String) {
        Log.d(TAG, "🔧 setTargetUser llamado con userId: $userId")
        targetUserId = userId
        loadBodyStatsForUser(userId)
    }

    /**
     * Cargar medidas del usuario y observar cambios (local-first)
     */
    private fun loadBodyStatsForUser(userId: String) {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null

                // Primero importar desde Firebase si es necesario
                repository.importBodyStatsFromFirebase()

                // Observar cambios en Room (reactivo)
                repository.getBodyStatsFlow(userId).collectLatest { statsList ->
                    bodyStatsList = statsList

                    // Si hay datos, cargar el más reciente
                    if (statsList.isNotEmpty()) {
                        val latest = statsList.first()
                        selectedGender = latest.gender
                        bodyMeasurements = latest.toMeasurementsMap()
                        Log.d(TAG, "✅ Medidas cargadas para usuario: $userId (${statsList.size} registros)")
                    } else {
                        // No hay datos previos
                        bodyMeasurements = getDefaultMeasurements()
                        selectedGender = "Male"
                        Log.d(TAG, "No hay medidas previas para usuario: $userId")
                    }
                    isLoading = false
                }
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Error al cargar datos: ${e.message}"
                Log.e(TAG, "Error en loadBodyStatsForUser", e)
            }
        }
    }

    /**
     * Cargar las medidas más recientes (para compatibilidad)
     */
    fun loadLatestBodyStats() {
        val userId = getCurrentUserId()
        if (userId.isEmpty()) {
            errorMessage = "Usuario no autenticado"
            return
        }
        loadBodyStatsForUser(userId)
    }

    /**
     * Cargar medida para editar desde el historial
     */
    fun loadMeasurementForEdit(item: MeasurementHistoryItem) {
        isEditMode = true
        editingDocumentId = item.id

        // Buscar el ID local correspondiente
        val localEntity = bodyStatsList.find {
            it.firebaseId == item.id ||
                    // Fallback: si no tiene firebaseId, buscar por timestamp aproximado
                    (it.firebaseId.isEmpty() &&
                            Math.abs(it.createdAt - item.createdAt.toDate().time) < 1000)
        }

        editingLocalId = localEntity?.id
        selectedGender = item.gender
        bodyMeasurements = item.measurements.toMeasurementsMap()

        Log.d(TAG, "✅ Datos cargados para editar - Firebase: ${item.id}, Local: $editingLocalId")
    }

    /**
     * Cargar medida para editar por ID de Firebase
     */
    fun loadMeasurementForEditById(documentId: String) {
        viewModelScope.launch {
            isLoading = true
            isEditMode = true
            editingDocumentId = documentId

            // Buscar en la lista local
            val entity = bodyStatsList.find { it.firebaseId == documentId }

            if (entity != null) {
                editingLocalId = entity.id
                selectedGender = entity.gender
                bodyMeasurements = entity.toMeasurementsMap()
                Log.d(TAG, "✅ Medida cargada para editar: $documentId")
            } else {
                errorMessage = "Registro no encontrado"
                clearEditMode()
            }

            isLoading = false
        }
    }

    fun clearEditMode() {
        isEditMode = false
        editingDocumentId = null
        editingLocalId = null
        Log.d(TAG, "✅ Modo edición limpiado")
    }

    /**
     * Función principal para guardar o actualizar (decide automáticamente)
     */
    fun saveOrUpdateBodyStats(
        gender: String,
        measurements: Map<String, String>,
        subscriptionViewModel: SubscriptionViewModel,
        onComplete: (success: Boolean, error: String?, requiresUpgrade: Boolean) -> Unit
    ) {
        Log.d(TAG, "Iniciando guardado/actualización. EditMode: $isEditMode, LocalId: $editingLocalId")

        if (isEditMode && editingLocalId != null) {
            // Modo edición: actualizar registro existente
            updateBodyStats(editingLocalId!!, gender, measurements) { success, error ->
                if (success) {
                    clearEditMode()
                }
                onComplete(success, error, false)
            }
        } else {
            // Modo creación: verificar límites y crear nuevo
            saveBodyStatsWithLimits(gender, measurements, subscriptionViewModel, onComplete)
        }
    }

    /**
     * Guardar nuevas medidas con verificación de límites
     */
    private fun saveBodyStatsWithLimits(
        gender: String,
        measurements: Map<String, String>,
        subscriptionViewModel: SubscriptionViewModel,
        onComplete: (success: Boolean, error: String?, requiresUpgrade: Boolean) -> Unit
    ) {
        viewModelScope.launch {
            Log.d(TAG, "Verificando límites de suscripción...")

            // Verificar límites usando el conteo local
            val currentCount = repository.getBodyStatsCount(getCurrentUserId())
            subscriptionViewModel.checkCanCreateNewDocumentWithCount(currentCount) { canCreate, message ->
                if (!canCreate) {
                    Log.w(TAG, "Límite alcanzado: $message")
                    onComplete(false, message, true)
                    return@checkCanCreateNewDocumentWithCount
                }

                // Guardar en Repository (local + sync)
                viewModelScope.launch {
                    isLoading = true
                    val localId = repository.createBodyStats(gender, measurements)

                    if (localId > 0) {
                        Log.d(TAG, "✅ Medidas guardadas con ID local: $localId")

                        // Actualizar estado local
                        selectedGender = gender
                        bodyMeasurements = measurements

                        // Actualizar conteo en subscription
                        subscriptionViewModel.updateBodyStatsCount(currentCount + 1)

                        isLoading = false
                        onComplete(true, null, false)
                    } else {
                        isLoading = false
                        onComplete(false, "Error al guardar medidas", false)
                    }
                }
            }
        }
    }

    /**
     * Actualizar medidas existentes
     */
    private fun updateBodyStats(
        localId: Long,
        gender: String,
        measurements: Map<String, String>,
        onComplete: (success: Boolean, error: String?) -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true

            val success = repository.updateBodyStats(localId, gender, measurements)

            if (success) {
                selectedGender = gender
                bodyMeasurements = measurements
                Log.d(TAG, "✅ Medidas actualizadas - ID local: $localId")
                onComplete(true, null)
            } else {
                onComplete(false, "Error al actualizar medidas")
            }

            isLoading = false
        }
    }

    /**
     * Eliminar registro de medidas
     */
    fun deleteBodyStatsRecord(
        documentId: String,
        onComplete: (success: Boolean, error: String?) -> Unit
    ) {
        viewModelScope.launch {
            // Buscar el ID local correspondiente
            val entity = bodyStatsList.find { it.firebaseId == documentId }

            if (entity != null) {
                val success = repository.deleteBodyStats(entity.id)
                if (success) {
                    Log.d(TAG, "✅ Registro eliminado: $documentId")
                    onComplete(true, null)
                } else {
                    onComplete(false, "Error al eliminar registro")
                }
            } else {
                // Si no tiene ID local, intentar eliminar por Firebase ID antiguo
                Log.w(TAG, "Registro no encontrado localmente: $documentId")
                onComplete(false, "Registro no encontrado")
            }
        }
    }

    /**
     * Obtener historial de medidas (para compatibilidad con UI existente)
     */
    fun getBodyStatsHistory(
        onComplete: (success: Boolean, data: List<Map<String, Any>>?, error: String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val userId = getCurrentUserId()
                if (userId.isEmpty()) {
                    onComplete(false, null, "Usuario no autenticado")
                    return@launch
                }

                // Usar datos locales
                val historyList = bodyStatsList.map { entity ->
                    mapOf(
                        "documentId" to (entity.firebaseId.ifEmpty { entity.id.toString() }),
                        "gender" to entity.gender,
                        "measurements" to entity.toMeasurementsMap(),
                        "createdAt" to Timestamp(Date(entity.createdAt)),
                        "updatedAt" to Timestamp(Date(entity.updatedAt))
                    )
                }

                onComplete(true, historyList, null)
            } catch (e: Exception) {
                Log.e(TAG, "Error obteniendo historial", e)
                onComplete(false, null, "Error al obtener historial: ${e.message}")
            }
        }
    }

    /**
     * Cargar datos del historial (función auxiliar para la UI)
     */
    fun loadHistoryData(
        viewModel: BodyStatsViewModel,
        onComplete: (List<MeasurementHistoryItem>, String?) -> Unit
    ) {
        viewModel.getBodyStatsHistory { success, data, error ->
            if (success && data != null) {
                val items = data.mapIndexedNotNull { index, firebaseData ->
                    try {
                        val measurements = (firebaseData["measurements"] as? Map<String, Any>)?.let { measMap ->
                            UserMeasurements(
                                height = (measMap["Height"] as? String)?.toDoubleOrNull() ?: 0.0,
                                weight = (measMap["Weight"] as? String)?.toDoubleOrNull() ?: 0.0,
                                neck = (measMap["Neck"] as? String)?.toDoubleOrNull() ?: 0.0,
                                chest = (measMap["Chest"] as? String)?.toDoubleOrNull() ?: 0.0,
                                arms = (measMap["Arms"] as? String)?.toDoubleOrNull() ?: 0.0,
                                waist = (measMap["Waist"] as? String)?.toDoubleOrNull() ?: 0.0,
                                thigh = (measMap["Thigh"] as? String)?.toDoubleOrNull() ?: 0.0,
                                calf = (measMap["Calf"] as? String)?.toDoubleOrNull() ?: 0.0,
                                lastUpdated = firebaseData["createdAt"] as? Timestamp
                            )
                        } ?: UserMeasurements()

                        MeasurementHistoryItem(
                            id = firebaseData["documentId"] as? String ?: "item_$index",
                            measurements = measurements,
                            gender = firebaseData["gender"] as? String ?: "Male",
                            createdAt = firebaseData["createdAt"] as? Timestamp ?: Timestamp.now(),
                            isLatest = index == 0
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                onComplete(items, null)
            } else {
                onComplete(emptyList(), error)
            }
        }
    }

    /**
     * Obtener datos de progreso para gráficas
     */
    fun getBodyMeasurementProgressData(
        onComplete: (success: Boolean, data: BodyMeasurementProgressData?, error: String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val userId = getCurrentUserId()
                if (userId.isEmpty()) {
                    onComplete(false, null, "Usuario no autenticado")
                    return@launch
                }

                // Usar datos locales ordenados por fecha
                val sortedStats = bodyStatsList.sortedBy { it.createdAt }

                val builder = BodyMeasurementProgressBuilder()
                sortedStats.forEach { entity ->
                    val firebaseData = mapOf(
                        "gender" to entity.gender,
                        "measurements" to entity.toMeasurementsMap(),
                        "createdAt" to Timestamp(Date(entity.createdAt))
                    )
                    builder.addFirebaseDocument(firebaseData)
                }

                val progressData = builder.build(userId)
                Log.d(TAG, "✅ Datos de progreso generados: ${progressData.getTotalRecords()} registros")
                onComplete(true, progressData, null)

            } catch (e: Exception) {
                Log.e(TAG, "Error procesando datos de progreso", e)
                onComplete(false, null, "Error procesando datos: ${e.message}")
            }
        }
    }

    /**
     * Sincronizar datos pendientes manualmente
     */
    fun syncPendingData() {
        viewModelScope.launch {
            repository.syncPendingBodyStats()
        }
    }

    // ========== FUNCIONES AUXILIARES (sin cambios) ==========

    fun formatDate(date: Date): String {
        return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
    }

    fun clearError() {
        errorMessage = null
    }

    fun validateMeasurements(measurements: Map<String, String>): String? {
        return when {
            measurements.values.all { it.isBlank() } ->
                "Al menos una medida debe tener valor"
            measurements.any { (_, value) ->
                value.isNotBlank() && (value.toDoubleOrNull() == null || value.toDouble() <= 0)
            } ->
                "Las medidas deben ser números positivos"
            else -> null
        }
    }

    private fun getDefaultMeasurements(): Map<String, String> =
        MeasurementType.getDefaultMeasurements()

    fun resetState() {
        bodyMeasurements = getDefaultMeasurements()
        selectedGender = "Male"
        errorMessage = null
        isLoading = false
    }

    fun getProgressDataForMeasurement(
        measurementType: BodyMeasurementType,
        onComplete: (success: Boolean, chartData: List<Pair<String, Float>>?, summary: MeasurementSummary?, error: String?) -> Unit
    ) {
        getBodyMeasurementProgressData { success, progressData, error ->
            if (success && progressData != null) {
                val chartData = progressData.getChartDataFor(measurementType)
                val summary = progressData.getProgressSummary()[measurementType]
                onComplete(true, chartData, summary, null)
            } else {
                onComplete(false, null, null, error)
            }
        }
    }

    fun getAvailableMetricsForCharts(
        onComplete: (success: Boolean, metrics: List<BodyMeasurementType>?, error: String?) -> Unit
    ) {
        getBodyMeasurementProgressData { success, progressData, error ->
            if (success && progressData != null) {
                val availableMetrics = progressData.getAvailableMetricsForChart()
                onComplete(true, availableMetrics, null)
            } else {
                onComplete(false, null, error)
            }
        }
    }

    fun hasEnoughDataForCharts(
        onComplete: (hasData: Boolean, totalRecords: Int) -> Unit
    ) {
        getBodyMeasurementProgressData { success, progressData, _ ->
            if (success && progressData != null) {
                val hasData = progressData.hasAnyData()
                val totalRecords = progressData.getTotalRecords()
                onComplete(hasData, totalRecords)
            } else {
                onComplete(false, 0)
            }
        }
    }

    // Para compatibilidad con funciones antiguas que aún se llamen
    @Deprecated("Usar createBodyStats del repository")
    fun saveBodyStats(
        gender: String,
        measurements: Map<String, String>,
        onComplete: (success: Boolean, error: String?) -> Unit
    ) {
        viewModelScope.launch {
            val localId = repository.createBodyStats(gender, measurements)
            onComplete(localId > 0, if (localId > 0) null else "Error al guardar")
        }
    }

    @Deprecated("Usar updateBodyStats con ID local")
    fun updateBodyStatsById(
        documentId: String,
        gender: String,
        measurements: Map<String, String>,
        onComplete: (success: Boolean, error: String?) -> Unit
    ) {
        val entity = bodyStatsList.find { it.firebaseId == documentId }
        if (entity != null) {
            updateBodyStats(entity.id, gender, measurements, onComplete)
        } else {
            onComplete(false, "Registro no encontrado")
        }
    }
}