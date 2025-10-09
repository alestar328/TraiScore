package com.develop.traiscore.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.develop.traiscore.R
import com.develop.traiscore.data.firebaseData.RoutineDocument
import com.develop.traiscore.data.firebaseData.RoutineSection
import com.develop.traiscore.data.firebaseData.SimpleExercise
import com.develop.traiscore.data.local.entity.LabEntry
import com.develop.traiscore.exports.RoutineExportManager
import com.develop.traiscore.presentation.components.TraiScoreTopBar
import com.develop.traiscore.presentation.components.general.LabResultsTableUI
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.theme.tsColors
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun LabResultsScreen(
    entries: List<LabEntry>,
    onEntriesChange: (List<LabEntry>) -> Unit,
    onBack: () -> Unit,
    onConfirm: (() -> Unit)? = null,
    unitSuggestionsByTest: Map<String, List<String>> = emptyMap()

) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showAIDialog by remember { mutableStateOf(false) }
    var isGenerating by remember { mutableStateOf(false) }
    val model = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel("gemini-2.5-flash")

    Scaffold(
        topBar = {
            TraiScoreTopBar(
                leftIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.tsColors.ledCyan
                        )
                    }
                },
                rightIcon = {
                    if (onConfirm != null) {
                        TextButton(onClick = onConfirm) {
                            Text("Confirmar")
                        }
                    } else {
                        // espacio para equilibrar el TopBar cuando no hay acción derecha
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(24.dp))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAIDialog = true },
                containerColor = Color.White, // blanco si usas light mode
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(start = 16.dp, bottom = 16.dp)
                    .navigationBarsPadding(),
                shape = MaterialTheme.shapes.large
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.brain_ia),
                    contentDescription = "IA",
                    tint = traiBlue // puedes dejarlo sin tint si quieres el logo original
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Start // 👈 esquina inferior izquierda
    )
    { padding ->
        LabResultsTableUI(
            modifier = Modifier
                .padding(padding)
                .navigationBarsPadding()
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            entries = entries,
            onEntriesChange = onEntriesChange,
            unitSuggestionsByTest = unitSuggestionsByTest
        )
    }
    if (showAIDialog) {
        AIDialogUI(
            biometricsSummary = entries.joinToString("\n") { entry ->
                "- ${entry.test}: ${entry.value ?: "--"} ${entry.unit ?: ""}"
            },
            onDismiss = { showAIDialog = false },
            onConfirm = { userInput ->
                showAIDialog = false
                isGenerating = true

                scope.launch {
                    try {
                        val bioText = entries.joinToString("\n") {
                            "${it.test}: ${it.value ?: "--"} ${it.unit ?: ""}"
                        }

                        val prompt = """
                            Eres un entrenador personal virtual. 
                            Con los siguientes datos biométricos:
                            $bioText
                            
                            El usuario ha reportado las siguientes molestias: $userInput
                            
                            Genera una rutina de gimnasio segura y efectiva en formato JSON estructurado como este:
                            {
                              "routineName": "Rutina Personalizada IA",
                              "sections": [
                                {
                                  "type": "Pecho y Tríceps",
                                  "exercises": [
                                    {"name": "Press de banca", "series": 4, "reps": "10-12", "weight": "Moderado", "rir": 2},
                                    {"name": "Fondos en paralelas", "series": 3, "reps": "8-10", "weight": "Corporal", "rir": 1}
                                  ]
                                }
                              ]
                            }
                            NO agregues texto adicional fuera del JSON.
                        """.trimIndent()

                        val response = model.generateContent(prompt)
                        val text = response.text ?: error("Respuesta vacía del modelo")

                        val jsonText = text.substringAfter("{").substringBeforeLast("}")
                        val json = JSONObject("{$jsonText}")

                        val routineName = json.optString("routineName", "Rutina IA Generada")
                        val sectionsArray = json.optJSONArray("sections") ?: JSONArray()

                        val sections = mutableListOf<RoutineSection>()
                        for (i in 0 until sectionsArray.length()) {
                            val sectionObj = sectionsArray.getJSONObject(i)
                            val exercisesArray = sectionObj.optJSONArray("exercises") ?: JSONArray()

                            val exercises = mutableListOf<SimpleExercise>()
                            for (j in 0 until exercisesArray.length()) {
                                val e = exercisesArray.getJSONObject(j)
                                exercises.add(
                                    SimpleExercise(
                                        name = e.optString("name"),
                                        series = e.optInt("series"),
                                        reps = e.optString("reps"),
                                        weight = e.optString("weight"),
                                        rir = e.optInt("rir")
                                    )
                                )
                            }

                            sections.add(
                                RoutineSection(
                                    type = sectionObj.optString("type"),
                                    exercises = exercises
                                )
                            )
                        }

                        val routine = RoutineDocument(
                            userId = "user123", // reemplazar con el ID real del usuario logueado
                            type = "IA_GENERATED",
                            routineName = routineName,
                            clientName = "Usuario IA",
                            sections = sections
                        )

                        RoutineExportManager.exportRoutine(
                            context,
                            routine,
                            onSuccess = { uri ->
                                Toast.makeText(
                                    context,
                                    "Rutina generada y exportada con éxito ✅",
                                    Toast.LENGTH_LONG
                                ).show()
                                RoutineExportManager.shareRoutineFile(context, uri, routineName)
                            },
                            onError = { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        )
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error generando rutina: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        isGenerating = false
                    }
                }
            }
        )
    }

    // Mostrar progreso de generación
    if (isGenerating) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Generando rutina...") },
            text = {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = traiBlue)
                }
            },
            confirmButton = {}
        )
    }
}