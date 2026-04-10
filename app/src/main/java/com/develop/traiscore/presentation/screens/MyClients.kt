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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.develop.traiscore.R
import com.develop.traiscore.data.local.entity.UserEntity
import com.develop.traiscore.presentation.components.ClientCard
import com.develop.traiscore.presentation.theme.TraiScoreTheme
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.viewmodels.MyClientsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyClients(
    onClientClick: (UserEntity) -> Unit,
    onAddClientClick: () -> Unit,
    onInvitationsClick: () -> Unit,
    onConfigureTopBar: (
        @Composable () -> Unit,
        @Composable () -> Unit,
        (@Composable () -> Unit)?
    ) -> Unit = { _, _, _ -> },
    onConfigureFAB: ((@Composable () -> Unit)?) -> Unit = {},
    viewModel: MyClientsViewModel = hiltViewModel()
) {
    val clients by viewModel.clients.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Configurar TopBar y FAB del Scaffold externo (MainScreen)
    LaunchedEffect(Unit) {
        onConfigureTopBar({ }, { }, null) // TopBar por defecto: logo "TraiScore" centrado
        onConfigureFAB {
            FloatingActionButton(
                onClick = onInvitationsClick,
                containerColor = traiBlue,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar cliente"
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = TraiScoreTheme.dimens.paddingMedium)
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
                        onClick = { viewModel.refreshClients() },
                        colors = ButtonDefaults.buttonColors(containerColor = traiBlue)
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
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.person_add),
                        contentDescription = null,
                        tint = Color.Gray.copy(alpha = 0.5f),
                        modifier = Modifier.size(72.dp)
                    )
                    Text(
                        text = "Sin clientes aún",
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Invita a tus primeros clientes pulsando el botón\u00A0+",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = onInvitationsClick,
                        colors = ButtonDefaults.buttonColors(containerColor = traiBlue)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.person_add),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Invitar cliente")
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(clients) { client ->
                        ClientCard(
                            client = client,
                            onClick = { onClientClick(client) }
                        )
                    }
                }
            }
        }
    }
}