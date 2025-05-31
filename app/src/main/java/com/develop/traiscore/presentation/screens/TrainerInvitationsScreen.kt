package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.develop.traiscore.presentation.components.invitations.CreateInvitationDialog
import com.develop.traiscore.presentation.components.invitations.EmptyInvitationsState
import com.develop.traiscore.presentation.components.invitations.InvitationCard
import com.develop.traiscore.presentation.components.invitations.copyToClipboard
import com.develop.traiscore.presentation.components.invitations.shareInvitation
import com.develop.traiscore.presentation.theme.navbarDay
import com.develop.traiscore.presentation.theme.traiBackgroundDay
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.viewmodels.InvitationViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerInvitationsScreen(
    onBack: () -> Unit,
    viewModel: InvitationViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val invitations by viewModel.invitations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser

    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadInvitations()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invitaciones", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = navbarDay
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = traiBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva invitación")
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

                invitations.isEmpty() -> {
                    EmptyInvitationsState(
                        onCreateClick = { showCreateDialog = true }
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(invitations) { invitation ->
                            InvitationCard(
                                invitation = invitation,
                                onShare = { code ->
                                    shareInvitation(context, code, currentUser?.displayName ?: "Tu entrenador")
                                },
                                onCopy = { code ->
                                    copyToClipboard(context, code)
                                },
                                onCancel = {
                                    viewModel.cancelInvitation(invitation.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo para crear invitación
    if (showCreateDialog) {
        CreateInvitationDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { expirationDays ->
                viewModel.createInvitation(
                    trainerName = currentUser?.displayName ?: "Entrenador",
                    trainerEmail = currentUser?.email ?: "",
                    expirationDays = expirationDays
                )
                showCreateDialog = false
            }
        )
    }
}