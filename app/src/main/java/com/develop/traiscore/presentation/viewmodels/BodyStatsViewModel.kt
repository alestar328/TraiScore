package com.develop.traiscore.presentation.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BodyStatsViewModel @Inject constructor() : ViewModel() {

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

    // Referencia a la colección del usuario actual
    private val userStatsRef
        get() = auth.currentUser?.uid?.let { userId ->
            db.collection("users")
                .document(userId)
                .collection("bodyStats")
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

        // Crear documento con estructura consistente
        val bodyStatsDocument = mapOf(
            "userId" to userId,
            "gender" to gender,
            "measurements" to measurements,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        // Guardar en subcolección del usuario
        userStatsRef?.add(bodyStatsDocument)
            ?.addOnSuccessListener { documentReference ->
                isLoading = false
                // Actualizar el estado local
                selectedGender = gender
                bodyMeasurements = measurements

                Log.d("BodyStatsVM", "✅ Medidas guardadas con ID: ${documentReference.id}")
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

    /**
     * Obtiene el historial completo de medidas del usuario
     */
    fun getBodyStatsHistory(
        onComplete: (success: Boolean, data: List<Map<String, Any>>?, error: String?) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
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
    private fun getDefaultMeasurements(): Map<String, String> = mapOf(
        "Height" to "",
        "Weight" to "",
        "Neck" to "",
        "Chest" to "",
        "Arms" to "",
        "Waist" to "",
        "Thigh" to "",
        "Calf" to ""
    )

    /**
     * Resetea el estado del ViewModel
     */
    fun resetState() {
        bodyMeasurements = getDefaultMeasurements()
        selectedGender = "Male"
        errorMessage = null
        isLoading = false
    }
}