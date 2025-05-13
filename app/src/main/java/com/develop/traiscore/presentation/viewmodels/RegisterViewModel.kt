package com.develop.traiscore.presentation.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel  : ViewModel() {
    // Campos básicos
    val email = MutableStateFlow("")
    val name = MutableStateFlow("")
    val lastName = MutableStateFlow("")
    val birthDate = MutableStateFlow("")

    // Rol de usuario
    val isTrainer = MutableStateFlow(false)
    val isAtleta = MutableStateFlow(false)

    // Sexo (solo para atleta)
    private val _gender = MutableStateFlow<String?>(null)
    val gender: StateFlow<String?> = _gender.asStateFlow()
    // Evento de registro exitoso
    private val _registrationSuccess = MutableSharedFlow<Unit>()
    val registrationSuccess = _registrationSuccess.asSharedFlow()

    // Medidas (solo atleta)
    val height = MutableStateFlow("")
    val weight = MutableStateFlow("")
    val neck = MutableStateFlow("")
    val chest = MutableStateFlow("")
    val arms = MutableStateFlow("")
    val waist = MutableStateFlow("")
    val thigh = MutableStateFlow("")
    val calf = MutableStateFlow("")

    // Métodos para actualizar
    fun onRoleTrainer() {
        isTrainer.value = true
        isAtleta.value = false
    }
    fun onRoleAtleta() {
        isAtleta.value = true
        isTrainer.value = false
    }

    fun onGenderSelect(g: String) {
        _gender.value = g
    }
    // Validación y registro
    fun register() {
        val e = email.value.trim()
        val n = name.value.trim()
        val l = lastName.value.trim()
        val b = birthDate.value.trim()
        // Solo campos obligatorios
        if (e.isNotEmpty() && n.isNotEmpty() && l.isNotEmpty() && b.isNotEmpty()) {
            // Aquí iría lógica para guardar en Firestore o backend
            viewModelScope.launch {
                _registrationSuccess.emit(Unit)
            }
        } else {
            // opcional: exponer errores de validación
        }
    }
    // Aquí podrías exponer un evento o Flow para "registro completado"
}