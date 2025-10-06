package com.develop.traiscore.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.develop.traiscore.data.local.dao.MedicalStatsDao
import com.develop.traiscore.data.local.entity.LabEntry
import com.develop.traiscore.data.mappers.toDto
import com.develop.traiscore.data.remote.dtos.MedicalReportDto
import com.develop.traiscore.data.remote.dtos.MedicalStatsExport
import com.develop.traiscore.utils.UnitRegistry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class LabResultsUiState(
    val entries: List<LabEntry> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class LabResultsViewModel @Inject constructor(
    private val repo: MedicalStatsDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(LabResultsUiState())
    val uiState: StateFlow<LabResultsUiState> = _uiState.asStateFlow()

    /** Sugerencias de unidades por prueba (puedes sustituir por datos remotos/config) */
    val unitSuggestionsByTest: Map<String, List<String>> = mapOf(
        "Glucosa" to UnitRegistry.labels(listOf("mg/dL", "mmol/L")),
        "Urea" to UnitRegistry.labels(listOf("mg/dL", "mmol/L")),
        "Creatinina" to UnitRegistry.labels(listOf("mg/dL", "µmol/L")),
        "Ácido úrico" to UnitRegistry.labels(listOf("mg/dL", "µmol/L")),
        "Colesterol total" to UnitRegistry.labels(listOf("mg/dL", "mmol/L")),
    )
    /** Inicializa/rehidrata la lista (p. ej. desde OCR o BD). */
    fun setEntries(entries: List<LabEntry>) {
        _uiState.update { it.copy(entries = entries, errorMessage = null) }
    }

    /** Añade una fila vacía. */
    fun addRow() {
        val newRow = LabEntry(
            id = UUID.randomUUID().toString(),
            test = "",
            value = null,
            unit = null
        )
        _uiState.update { it.copy(entries = it.entries + newRow) }
    }
    fun buildReportDto(userId: String, appVersion: String, source: String = "manual", tags: List<String> = emptyList()): MedicalReportDto {
        val now = System.currentTimeMillis()
        val reportId = java.util.UUID.randomUUID().toString()
        val entriesDto = uiState.value.entries.map { it.toDto() }
        return MedicalReportDto(
            id = reportId,
            userId = userId,
            createdAt = now,
            source = source,
            tags = tags,
            entriesCount = entriesDto.size,
            appVersion = appVersion,
            schemaVersion = 1,
            entries = entriesDto
        )
    }
    fun saveCurrentReport(userId: String, appVersion: String, onDone: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val dto = buildReportDto(userId, appVersion)
            val res = repo.saveReport(userId, dto)
            res.fold(
                onSuccess = { onDone(true, null) },
                onFailure = { e ->
                    _uiState.update { it.copy(errorMessage = e.message) }
                    onDone(false, e.message)
                }
            )
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    /** Actualiza una fila completa (copy desde la UI). */
    fun updateEntry(updated: LabEntry) {
        _uiState.update { state ->
            state.copy(entries = state.entries.map { if (it.id == updated.id) updated else it })
        }
    }

    /** Actualiza campos individuales (cómodo desde la UI). */
    fun updateTest(id: String, test: String) {
        _uiState.update { state ->
            state.copy(entries = state.entries.map { if (it.id == id) it.copy(test = test) else it })
        }
    }

    fun updateValue(id: String, raw: String) {
        val parsed = raw.replace(',', '.').toDoubleOrNull()
        _uiState.update { state ->
            state.copy(entries = state.entries.map { if (it.id == id) it.copy(value = parsed) else it })
        }
    }

    fun updateUnit(id: String, unit: String?) {
        _uiState.update { state ->
            state.copy(entries = state.entries.map { if (it.id == id) it.copy(unit = unit?.ifBlank { null }) else it })
        }
    }

    /** Borra una fila por id. */
    fun deleteRow(id: String) {
        _uiState.update { state -> state.copy(entries = state.entries.filterNot { it.id == id }) }
    }

    /** Ordena por nombre de prueba (asc/desc). */
    fun sortByTest(ascending: Boolean = true) {
        _uiState.update { state ->
            val sorted = if (ascending) {
                state.entries.sortedBy { it.test.lowercase() }
            } else {
                state.entries.sortedByDescending { it.test.lowercase() }
            }
            state.copy(entries = sorted)
        }
    }

    /** Limpia errores visibles en UI. */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /** Ejemplo de carga asíncrona (si luego quieres traer de BD). */
    fun loadFromRepositoryExample() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        try {
            // val data = repository.getLabEntries()
            // _uiState.update { it.copy(entries = data, isLoading = false) }
            _uiState.update { it.copy(isLoading = false) } // placeholder
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Error desconocido") }
        }
    }

    /** Reemplaza la lista completa desde OCR ya parseado a LabEntry. */
    fun replaceAllFromOcr(entries: List<LabEntry>) {
        _uiState.update { it.copy(entries = entries) }
    }
}