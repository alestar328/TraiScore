package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.develop.traiscore.data.local.entity.UserEntity
import com.develop.traiscore.presentation.components.ClientCard
import com.develop.traiscore.presentation.theme.navbarDay
import com.develop.traiscore.presentation.theme.traiBackgroundDay
import com.develop.traiscore.presentation.theme.traiBlue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyClients(
    onClientClick: (UserEntity) -> Unit,
    onAddClientClick: () -> Unit
) {
    var clients by remember { mutableStateOf<List<UserEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Cargar clientes del entrenador actual
    LaunchedEffect(Unit) {
        val currentTrainerId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentTrainerId != null) {
            try {
                val db = FirebaseFirestore.getInstance()
                val snapshot = db.collection("users")
                    .whereEqualTo("linkedTrainerUid", currentTrainerId)
                    .whereEqualTo("userRole", "CLIENT")
                    .whereEqualTo("isActive", true)
                    .get()
                    .await()

                clients = snapshot.documents.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        UserEntity.fromFirestore(data, doc.id)
                    } catch (e: Exception) {
                        null
                    }
                }.sortedBy { it.getFullName() }

                isLoading = false
            } catch (e: Exception) {
                error = "Error al cargar clientes: ${e.message}"
                isLoading = false
            }
        } else {
            error = "No se pudo identificar al entrenador"
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mis Clientes",
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = navbarDay
                ),
                actions = {
                    // Contador de clientes
                    Text(
                        text = "${clients.size} clientes",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClientClick,
                containerColor = traiBlue,
                contentColor = Color.White,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar cliente"
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(traiBackgroundDay)
                .padding(innerPadding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = traiBlue
                    )
                }

                error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = error!!,
                            color = Color.Red,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { /* Retry logic */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = traiBlue
                            )
                        ) {
                            Text("Reintentar")
                        }
                    }
                }

                clients.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No tienes clientes registrados",
                            fontSize = 18.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onAddClientClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = traiBlue
                            )
                        ) {
                            Text("Agregar primer cliente")
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(clients) { client ->
                            ClientCard(
                                client = client,
                                onClick = { onClientClick(client) }
                            )
                        }

                        // Espaciado inferior para el FAB
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
}