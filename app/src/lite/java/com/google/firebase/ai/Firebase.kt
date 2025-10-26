package com.google.firebase.ai

import com.google.firebase.Firebase
import com.google.firebase.ai.type.GenerativeBackend

// ✅ Esta es la función de extensión sobre la clase real Firebase
fun Firebase.ai(backend: GenerativeBackend): FirebaseAIClient = FirebaseAIClient()

class FirebaseAIClient {
    fun generativeModel(model: String): DummyModel = DummyModel()
}

class DummyModel {
    fun generateContent(prompt: String): DummyResponse =
        DummyResponse("IA no disponible en Lite.")
}

data class DummyResponse(val text: String?)