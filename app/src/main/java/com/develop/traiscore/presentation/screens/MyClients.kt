package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.develop.traiscore.data.local.entity.UserEntity
import com.develop.traiscore.presentation.components.ClientCard
import com.develop.traiscore.presentation.components.trainers.TopBarTrainers
import com.develop.traiscore.presentation.components.trainers.TopBarTrainersClients
import com.develop.traiscore.presentation.theme.TraiScoreTheme
import com.develop.traiscore.presentation.theme.navbarDay
import com.develop.traiscore.presentation.theme.traiBackgroundDay
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.viewmodels.MyClientsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyClients(
    onClientClick: (UserEntity) -> Unit,
    onAddClientClick: () -> Unit,
    onInvitationsClick: () -> Unit,
    viewModel: MyClientsViewModel = hiltViewModel()
) {
    val clients by viewModel.clients.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Ya no necesitamos LaunchedEffect porque el ViewModel configura
    // automáticamente el listener en tiempo real en su init

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = TraiScoreTheme.dimens.paddingMedium),
        topBar = {
            TopBarTrainersClients(
                title = "Clientes",
                clientCount = clients.size,
                onRefreshClick = { viewModel.refreshClients() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onInvitationsClick,
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
                .background(MaterialTheme.colorScheme.background)
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
                            onClick = { viewModel.refreshClients() }, // Usar refreshClients en lugar de retry manual
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