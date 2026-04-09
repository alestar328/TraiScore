package com.develop.traiscore.exports

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.develop.traiscore.data.firebaseData.RoutineDocument
import com.develop.traiscore.data.firebaseData.RoutineSection
import com.develop.traiscore.data.firebaseData.SimpleExercise
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class ImportExcelState {
    object Idle : ImportExcelState()
    object Parsing : ImportExcelState()
    data class Preview(val divisions: List<ParsedDivision>) : ImportExcelState()
    object Saving : ImportExcelState()
    data class Done(val count: Int) : ImportExcelState()
    data class Error(val message: String) : ImportExcelState()
}

@HiltViewModel
class ImportExcelViewModel @Inject constructor() : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _state = MutableStateFlow<ImportExcelState>(ImportExcelState.Idle)
    val state: StateFlow<ImportExcelState> = _state.asStateFlow()

    private var editableDivisions = mutableListOf<ParsedDivision>()

    fun parseFile(context: Context, uri: Uri) {
        editableDivisions = mutableListOf()
        viewModelScope.launch {
            _state.value = ImportExcelState.Parsing
            try {
                val divisions = withContext(Dispatchers.IO) {
                    ExcelHeuristicParser.parse(context, uri)
                }
                if (divisions.isEmpty()) {
                    _state.value = ImportExcelState.Error(
                        "No se detectaron divisiones en el archivo.\n" +
                        "Asegúrate de que el Excel tenga nombres de días o grupos musculares."
                    )
                } else {
                    editableDivisions = divisions.map {
                        ParsedDivision(name = it.name, exercises = it.exercises)
                    }.toMutableList()
                    _state.value = ImportExcelState.Preview(editableDivisions.toList())
                }
            } catch (e: Exception) {
                _state.value = ImportExcelState.Error("Error al leer el archivo: ${e.message}")
            }
        }
    }

    fun updateDivisionName(index: Int, newName: String) {
        val div = editableDivisions.getOrNull(index) ?: return
        editableDivisions[index] = ParsedDivision(name = newName, exercises = div.exercises)
        _state.value = ImportExcelState.Preview(editableDivisions.toList())
    }

    fun removeDivision(index: Int) {
        if (index < 0 || index >= editableDivisions.size) return
        editableDivisions.removeAt(index)
        _state.value = ImportExcelState.Preview(editableDivisions.toList())
    }

    fun reset() {
        editableDivisions = mutableListOf()
        _state.value = ImportExcelState.Idle
    }

    fun saveAll(onDone: () -> Unit) {
        val userId = auth.currentUser?.uid ?: run {
            _state.value = ImportExcelState.Error("Usuario no autenticado")
            return
        }
        viewModelScope.launch {
            _state.value = ImportExcelState.Saving
            try {
                var saved = 0
                editableDivisions.forEach { division ->
                    val type = ExcelHeuristicParser.inferType(division.name)
                    val doc = RoutineDocument(
                        userId = userId,
                        trainerId = null,
                        documentId = "",
                        type = type,
                        createdAt = Timestamp.now(),
                        clientId = null,
                        clientEmail = null,
                        clientName = division.name,
                        routineName = division.name,
                        sections = listOf(
                            RoutineSection(
                                type = type,
                                exercises = division.exercises.map {
                                    SimpleExercise(
                                        name = it.name,
                                        series = it.series,
                                        reps = it.reps,
                                        weight = it.weight,
                                        rir = it.rir
                                    )
                                }
                            )
                        )
                    )
                    saveToFirestore(doc, userId)
                    saved++
                }
                _state.value = ImportExcelState.Done(saved)
                onDone()
            } catch (e: Exception) {
                _state.value = ImportExcelState.Error("Error al guardar: ${e.message}")
            }
        }
    }

    private suspend fun saveToFirestore(doc: RoutineDocument, userId: String) =
        withContext(Dispatchers.IO) {
            val data = hashMapOf(
                "clientName" to doc.clientName,
                "routineName" to doc.routineName,
                "type" to doc.type,
                "createdAt" to doc.createdAt,
                "trainerId" to doc.trainerId,
                "sections" to doc.sections.map { section ->
                    hashMapOf(
                        "type" to section.type,
                        "exercises" to section.exercises.map { ex ->
                            hashMapOf(
                                "name" to ex.name,
                                "series" to ex.series,
                                "reps" to ex.reps,
                                "weight" to ex.weight,
                                "rir" to ex.rir
                            )
                        }
                    )
                }
            )
            firestore.collection("users").document(userId)
                .collection("routines").add(data).await()
        }
}
