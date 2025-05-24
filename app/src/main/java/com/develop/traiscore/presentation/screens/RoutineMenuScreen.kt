package com.develop.traiscore.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.develop.traiscore.core.DefaultCategoryExer
import com.develop.traiscore.data.firebaseData.RoutineTypeItem
import com.develop.traiscore.presentation.theme.traiBackgroundDay
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.viewmodels.RoutineViewModel
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun RoutineMenu(
    onRoutineClick: (String, String) -> Unit, // acepta docId y type
    onAddClick: () -> Unit,
    viewModel: RoutineViewModel
) {
    val routineTypes = remember { mutableStateListOf<RoutineTypeItem>() }
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var showEmptyDialog by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(/* aquí el clientId */)
            .collection("routines")
            .get()
            .addOnSuccessListener { result ->
                val uniqueTypes = mutableSetOf<Pair<String, String>>() // Pair<type, docId>
                for (document in result) {
                    val docId = document.id
                    val clientName = document.getString("clientName") ?: "Cliente"
                    val sections = document.get("sections") as? List<Map<String, Any>> ?: continue

                    for (section in sections) {
                        val type = section["type"] as? String ?: continue
                        if (uniqueTypes.add(Pair(type, docId))) {
                            routineTypes.add(RoutineTypeItem(type, docId, clientName))
                        }
                    }
                }
                isLoading = false
                if (routineTypes.isEmpty()) {
                    showEmptyDialog = true
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al cargar rutinas", Toast.LENGTH_SHORT).show()
                isLoading = false
                showEmptyDialog = true
            }
    }
    // Si no hay rutinas, mostramos el diálogo y salimos
    if (showEmptyDialog && !isLoading) {
        AlertDialog(
            onDismissRequest = { showEmptyDialog = false },
            title = { Text("Sin rutinas") },
            text = { Text("No tienes rutinas guardadas") },
            confirmButton = {
                TextButton(onClick = { showEmptyDialog = false }) {
                    Text("Aceptar")
                }
            }
        )
        return
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mis Rutinas",
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = traiBlue
                )
            )
        },
        containerColor = Color.DarkGray,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nueva rutina") },
                containerColor = traiBlue,
                contentColor = Color.Black,
                modifier = Modifier.navigationBarsPadding() // evita solapamiento con nav‐bar
            )
        }
    ) { innerPadding ->
            LazyColumn (
                modifier = Modifier
                    .fillMaxSize()
                    .background(traiBackgroundDay)
                    .padding(innerPadding)
                    .navigationBarsPadding(),          // evita que el contenido quede tras la nav‐bar
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(routineTypes, key = { _, r -> "${r.documentId}-${r.type}" }) { index, routine ->
                    // Creamos el estado de swipe
                    val dismissState = rememberDismissState(
                        confirmStateChange = { newValue ->
                            if (newValue == DismissValue.DismissedToEnd || newValue == DismissValue.DismissedToStart) {
                                viewModel.deleteRoutineType(
                                    documentId = routine.documentId,
                                    type = routine.type
                                ) { success ->
                                    if (success) {
                                        Toast.makeText(context, "Tipo de rutina eliminado", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Error al eliminar tipo de rutina", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                routineTypes.removeAt(index)
                                true
                            } else false
                        }
                    )
                    SwipeToDismiss(
                        state = dismissState,
                        directions = setOf(DismissDirection.EndToStart),
                        background = {
                            // Fondo rojo con icono de basura
                            if (dismissState.targetValue != DismissValue.Default) {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(Color.Red)
                                        .padding(end = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar rutina",
                                        tint = Color.White
                                    )
                                }
                            }
                        },
                        dismissContent = {
                            RoutineItem(
                                name = routine.type,
                                imageResId = try {
                                    DefaultCategoryExer.valueOf(routine.type.uppercase()).imageCat
                                } catch (_: Exception) {
                                    DefaultCategoryExer.BACK.imageCat  // fallback si no coincide
                                },
                                onClick = { onRoutineClick(routine.documentId, routine.type) }
                            )
                        }
                    )
                }

// Y al final, el espacio para el FAB:
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
    }
}
@Composable
fun RoutineItem(name: String, imageResId: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.LightGray) // Puedes eliminar o mantener este color según la imagen
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = name, fontSize = 30.sp, color = Color.Black)
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = "Imagen de rutina",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .height(80.dp)
                .width(80.dp)
                .clip(RoundedCornerShape(12.dp))
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RoutineMenuPreview() {
    RoutineMenu(
        onRoutineClick = { docId, type ->
            println("Clicked routine with ID: $docId and Type: $type")
        },
        onAddClick = { println("Add new routine") },
        viewModel = RoutineViewModel()
    )
}