package com.develop.traiscore.presentation.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.develop.traiscore.data.local.entity.UserMeasurements
import com.develop.traiscore.domain.model.BodyMeasurementProgressBuilder
import com.develop.traiscore.domain.model.BodyMeasurementProgressData
import com.develop.traiscore.domain.model.BodyMeasurementType
import com.develop.traiscore.domain.model.MeasurementSummary
import com.develop.traiscore.domain.model.MeasurementType
import com.develop.traiscore.presentation.screens.MeasurementHistoryItem
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class BodyStatsViewModel @Inject constructor() : ViewModel() {
    private var targetUserId: String? = null

    // Estados del ViewModel
    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var bodyMeasurements by mutableStateOf<Map<String, String>>(emptyMap())
        private set

    var selectedGender by mutableStateOf<String?>(null)
        private set

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val userStatsRef
        get() = run {
            val userId = getCurrentUserId()
            Log.d("BodyStatsVM", "🔧 userStatsRef para userId: $userId")
            if (userId.isNotEmpty()) {
                db.collection("users")
                    .document(userId)
                    .collection("bodyStats")
            } else {
                null
            }
        }


    fun setTargetUser(userId: String) {
        Log.d("BodyStatsVM", "🔧 setTargetUser llamado con userId: $userId")
        targetUserId = userId
        // Recargar datos para el nuevo usuario
        loadBodyStatsForUser(userId)
    }


    private fun getCurrentUserId(): String {
        val userId = targetUserId ?: FirebaseAuth.getInstance().currentUser?.uid ?: ""
        Log.d("BodyStatsVM", "🔧 getCurrentUserId() retorna: $userId (targetUserId=$targetUserId)")
        return userId
    }

    private fun loadBodyStatsForUser(userId: String) {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null

                // Obtener los datos más recientes del usuario específico
                val targetUserStatsRef = db
                    .collection("users")
                    .document(userId)
                    .collection("bodyStats")

                targetUserStatsRef
                    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        isLoading = false
                        if (!querySnapshot.isEmpty) {
                            val document = querySnapshot.documents.first()
                            val data = document.data ?: return@addOnSuccessListener

                            // Cargar género
                            selectedGender = data["gender"] as? String

                            // Cargar medidas
                            val measurements = data["measurements"] as? Map<String, Any> ?: emptyMap()
                            bodyMeasurements = measurements.mapValues { (_, value) ->
                                value.toString()
                            }

                            Log.d("BodyStatsVM", "✅ Medidas cargadas para usuario: $userId")
                        } else {
                            // No hay datos previos
                            bodyMeasurements = getDefaultMeasurements()
                            selectedGender = "Male"
                            Log.d("BodyStatsVM", "No hay medidas previas para usuario: $userId")
                        }
                    }
                    .addOnFailureListener { exception ->
                        isLoading = false
                        errorMessage = "Error al cargar las medidas: ${exception.message}"
                        Log.e("BodyStatsVM", "Error cargando medidas para usuario $userId", exception)
                    }
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Error al cargar datos: ${e.message}"
                Log.e("BodyStatsVM", "Error en loadBodyStatsForUser", e)
            }
        }
    }



    fun deleteBodyStatsRecord(
        documentId: String,
        onComplete: (success: Boolean, error: String?) -> Unit
    ) {
        // ✅ CAMBIO: Usar getCurrentUserId() en lugar de auth.currentUser?.uid directamente
        val userId = getCurrentUserId()
        if (userId.isEmpty()) {
            onComplete(false, "Usuario no autenticado")
            return
        }

        // ✅ CORRECCIÓN: Usar db (que es Firebase.firestore) en lugar de firestore
        val targetUserStatsRef = db
            .collection("users")
            .document(userId)
            .collection("bodyStats")

        targetUserStatsRef.document(documentId)
            .delete()
            .addOnSuccessListener {
                Log.d("BodyStatsVM", "✅ Registro eliminado correctamente: $documentId para usuario: $userId")
                onComplete(true, null)
            }
            .addOnFailureListener { exception ->
                val errorMsg = "Error al eliminar registro: ${exception.message}"
                Log.e("BodyStatsVM", "Error eliminando registro para usuario $userId", exception)
                onComplete(false, errorMsg)
            }
    }
    /**
     * Carga las medidas corporales más recientes del usuario
     */
    fun saveBodyStatsWithLimits(
        gender: String,
        measurements: Map<String, String>,
        subscriptionViewModel: SubscriptionViewModel,
        onComplete: (success: Boolean, error: String?, requiresUpgrade: Boolean) -> Unit
    ) {
        Log.d("BodyStatsVM", "Iniciando guardado con verificación de límites")

        // Verificar límites contando documentos reales
        subscriptionViewModel.checkCanCreateNewDocument { canCreate, message ->
            Log.d("BodyStatsVM", "¿Puede crear documento?: $canCreate, mensaje: $message")

            if (!canCreate) {
                Log.w("BodyStatsVM", "No puede crear documento: $message")
                onComplete(false, message, true)
                return@checkCanCreateNewDocument
            }

            Log.d("BodyStatsVM", "Procediendo a guardar documento")

            // Guardar normalmente
            saveBodyStats(gender, measurements) { success, error ->
                if (success) {
                    Log.d("BodyStatsVM", "Documento guardado exitosamente, recontando...")
                    // Recontar documentos después de guardar exitosamente
                    subscriptionViewModel.countActualBodyStatsDocuments { newCount ->
                        Log.d("BodyStatsVM", "Nuevo conteo después de guardar: $newCount")
                        onComplete(true, null, false)
                    }
                } else {
                    Log.e("BodyStatsVM", "Error guardando documento: $error")
                    onComplete(false, error, false)
                }
            }
        }
    }
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

    // Función para formatear fechas
    fun formatDate(date: Date): String {
        return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
    }
    fun loadLatestBodyStats() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            errorMessage = "Usuario no autenticado"
            return
        }

        isLoading = true
        errorMessage = null

        userStatsRef?.orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            ?.limit(1)
            ?.get()
            ?.addOnSuccessListener { querySnapshot ->
                isLoading = false
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents.first()
                    val data = document.data ?: return@addOnSuccessListener

                    // Cargar género
                    selectedGender = data["gender"] as? String

                    // Cargar medidas
                    val measurements = data["measurements"] as? Map<String, Any> ?: emptyMap()
                    bodyMeasurements = measurements.mapValues { (_, value) ->
                        value.toString()
                    }

                    Log.d("BodyStatsVM", "✅ Medidas cargadas correctamente")
                } else {
                    // No hay datos previos, usar valores por defecto
                    bodyMeasurements = getDefaultMeasurements()
                    selectedGender = "Male"
                    Log.d("BodyStatsVM", "No hay medidas previas, usando valores por defecto")
                }
            }
            ?.addOnFailureListener { exception ->
                isLoading = false
                errorMessage = "Error al cargar las medidas: ${exception.message}"
                Log.e("BodyStatsVM", "Error cargando medidas", exception)
            }
    }

    /**
     * Guarda las medidas corporales y género en Firebase
     */
    fun saveBodyStats(
        gender: String,
        measurements: Map<String, String>,
        onComplete: (success: Boolean, error: String?) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onComplete(false, "Usuario no autenticado")
            return
        }

        isLoading = true
        errorMessage = null

        // Generar ID único para el documento
        val documentId = generateUniqueId()

        // Crear documento con estructura consistente
        val bodyStatsDocument = mapOf(
            "id" to documentId,  // ✅ ID único agregado
            "userId" to userId,
            "gender" to gender,
            "measurements" to measurements,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        // Guardar en subcolección del usuario con ID específico
        userStatsRef?.document(documentId)?.set(bodyStatsDocument)
            ?.addOnSuccessListener {
                isLoading = false
                // Actualizar el estado local
                selectedGender = gender
                bodyMeasurements = measurements

                Log.d("BodyStatsVM", "✅ Medidas guardadas con ID: $documentId")
                onComplete(true, null)
            }
            ?.addOnFailureListener { exception ->
                isLoading = false
                val errorMsg = "Error al guardar las medidas: ${exception.message}"
                errorMessage = errorMsg
                Log.e("BodyStatsVM", "Error guardando medidas", exception)
                onComplete(false, errorMsg)
            }
    }
    private fun generateUniqueId(): String {
        val timestamp = System.currentTimeMillis()
        val random = (1000..9999).random()
        return "measurement_${timestamp}_$random"
    }
    fun updateBodyStatsById(
        documentId: String,
        gender: String,
        measurements: Map<String, String>,
        onComplete: (success: Boolean, error: String?) -> Unit
    ) {
        val userId = getCurrentUserId()
        if (userId.isEmpty()) {
            onComplete(false, "Usuario no autenticado")
            return
        }

        isLoading = true
        errorMessage = null

        val updateData = mapOf(
            "gender" to gender,
            "measurements" to measurements,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        val targetUserStatsRef = db
            .collection("users")
            .document(userId)
            .collection("bodyStats")

        targetUserStatsRef.document(documentId)
            .update(updateData)
            .addOnSuccessListener {
                isLoading = false
                selectedGender = gender
                bodyMeasurements = measurements
                Log.d("BodyStatsVM", "✅ Medidas actualizadas para ID: $documentId")
                onComplete(true, null)
            }
            .addOnFailureListener { exception ->
                isLoading = false
                val errorMsg = "Error al actualizar: ${exception.message}"
                errorMessage = errorMsg
                Log.e("BodyStatsVM", "Error actualizando medidas", exception)
                onComplete(false, errorMsg)
            }
    }
    /**
     * Actualiza las medidas existentes más recientes
     */
    fun updateLatestBodyStats(
        gender: String,
        measurements: Map<String, String>,
        onComplete: (success: Boolean, error: String?) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onComplete(false, "Usuario no autenticado")
            return
        }

        isLoading = true
        errorMessage = null

        // Buscar el documento más reciente
        userStatsRef?.orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            ?.limit(1)
            ?.get()
            ?.addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Actualizar documento existente
                    val document = querySnapshot.documents.first()
                    val updateData = mapOf(
                        "gender" to gender,
                        "measurements" to measurements,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )

                    document.reference.update(updateData)
                        .addOnSuccessListener {
                            isLoading = false
                            selectedGender = gender
                            bodyMeasurements = measurements
                            Log.d("BodyStatsVM", "✅ Medidas actualizadas correctamente")
                            onComplete(true, null)
                        }
                        .addOnFailureListener { exception ->
                            isLoading = false
                            val errorMsg = "Error al actualizar: ${exception.message}"
                            errorMessage = errorMsg
                            Log.e("BodyStatsVM", "Error actualizando medidas", exception)
                            onComplete(false, errorMsg)
                        }
                } else {
                    // No existe documento previo, crear uno nuevo
                    saveBodyStats(gender, measurements, onComplete)
                }
            }
            ?.addOnFailureListener { exception ->
                isLoading = false
                val errorMsg = "Error al buscar medidas existentes: ${exception.message}"
                errorMessage = errorMsg
                Log.e("BodyStatsVM", "Error buscando medidas", exception)
                onComplete(false, errorMsg)
            }
    }


    fun getBodyStatsHistory(
        onComplete: (success: Boolean, data: List<Map<String, Any>>?, error: String?) -> Unit
    ) {
        val userId = getCurrentUserId() // ✅ CAMBIO: usar getCurrentUserId en lugar de auth.currentUser?.uid
        if (userId.isEmpty()) {
            onComplete(false, null, "Usuario no autenticado")
            return
        }

        userStatsRef?.orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            ?.get()
            ?.addOnSuccessListener { querySnapshot ->
                val historyList = querySnapshot.documents.mapNotNull { document ->
                    document.data?.let { data ->
                        data + ("documentId" to document.id)
                    }
                }
                onComplete(true, historyList, null)
            }
            ?.addOnFailureListener { exception ->
                val errorMsg = "Error al obtener historial: ${exception.message}"
                Log.e("BodyStatsVM", "Error obteniendo historial", exception)
                onComplete(false, null, errorMsg)
            }
    }

    /**
     * Limpia los mensajes de error
     */
    fun clearError() {
        errorMessage = null
    }

    /**
     * Valida que los campos de medidas sean correctos
     */
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

    /**
     * Obtiene las medidas por defecto
     */
    private fun getDefaultMeasurements(): Map<String, String> =
        MeasurementType.getDefaultMeasurements()

    /**
     * Resetea el estado del ViewModel
     */
    fun resetState() {
        bodyMeasurements = getDefaultMeasurements()
        selectedGender = "Male"
        errorMessage = null
        isLoading = false
    }

    fun getBodyMeasurementProgressData(
        onComplete: (success: Boolean, data: BodyMeasurementProgressData?, error: String?) -> Unit
    ) {
        // ✅ CAMBIO: Usar getCurrentUserId() en lugar de auth.currentUser?.uid
        val userId = getCurrentUserId()
        if (userId.isEmpty()) {
            onComplete(false, null, "Usuario no autenticado")
            return
        }

        userStatsRef?.orderBy("createdAt", com.google.firebase.firestore.Query.Direction.ASCENDING)
            ?.get()
            ?.addOnSuccessListener { querySnapshot ->
                try {
                    val builder = BodyMeasurementProgressBuilder()

                    querySnapshot.documents.forEach { document ->
                        document.data?.let { data ->
                            builder.addFirebaseDocument(data)
                        }
                    }

                    val progressData = builder.build(userId)
                    Log.d("BodyStatsVM", "✅ Datos de progreso generados para usuario $userId: ${progressData.getTotalRecords()} registros")
                    onComplete(true, progressData, null)

                } catch (exception: Exception) {
                    Log.e("BodyStatsVM", "Error procesando datos de progreso para usuario $userId", exception)
                    onComplete(false, null, "Error procesando datos: ${exception.message}")
                }
            }
            ?.addOnFailureListener { exception ->
                val errorMsg = "Error al obtener datos de progreso: ${exception.message}"
                Log.e("BodyStatsVM", "Error obteniendo progreso para usuario $userId", exception)
                onComplete(false, null, errorMsg)
            }
    }

    /**
     * Obtiene datos de progreso para una medida específica
     */
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

    /**
     * Obtiene lista de medidas disponibles para graficar
     */
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

    /**
     * Verifica si hay suficientes datos para mostrar gráficas
     */
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
}