package com.develop.traiscore.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor() : ViewModel() {

    private val _currentLanguage = MutableStateFlow("es") // ✅ CORREGIDO: _ en lugar de *
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow() // ✅ CORREGIDO

    init {
        // Set current language from system locale
        _currentLanguage.value = Locale.getDefault().language // ✅ CORREGIDO
        Log.d("LanguageViewModel", "Initial language: ${_currentLanguage.value}") // ✅ CORREGIDO
    }


    fun setLanguage(languageCode: String) {
        Log.d("LanguageViewModel", "Setting language to: $languageCode")
        _currentLanguage.value = languageCode // ✅ CORREGIDO
    }
}