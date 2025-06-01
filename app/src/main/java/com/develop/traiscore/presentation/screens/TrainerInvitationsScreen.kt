package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.develop.traiscore.data.local.entity.InvitationEntity
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun TrainerInvitationsScreen(
    onBack: () -> Unit,
    viewModel: InvitationViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val invitations by viewModel.invitations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser

    var showDeleteDialog by remember { mutableStateOf(false) }
    var invitationToDelete by remember { mutableStateOf<InvitationEntity?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) } // ← AÑADIDO: Esta variable faltaba

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
                            val dismissState = rememberDismissState(
                                confirmStateChange = { dismissValue ->
                                    if (dismissValue == DismissValue.DismissedToStart) {
                                        invitationToDelete = invitation
                                        showDeleteDialog = true
                                    }
                                    false // Siempre retornar false para que vuelva a su posición
                                }
                            )

                            SwipeToDismiss(
                                state = dismissState,
                                directions = setOf(DismissDirection.EndToStart),
                                background = {
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
                                                contentDescription = "Eliminar",
                                                tint = Color.White
                                            )
                                        }
                                    }
                                },
                                dismissContent = {
                                    InvitationCard(
                                        invitation = invitation,
                                        onShare = { code ->
                                            shareInvitation(
                                                context,
                                                code,
                                                currentUser?.displayName ?: "Tu entrenador"
                                            )
                                        },
                                        onCopy = { code ->
                                            copyToClipboard(context, code)
                                        },
                                        onCancel = {
                                            invitationToDelete = invitation
                                            showDeleteDialog = true
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }

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

    // Diálogo de confirmación para eliminar - AÑADIDO: Este diálogo faltaba
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                invitationToDelete = null
            },
            title = { Text("Eliminar invitación") },
            text = { Text("¿Estás seguro de que quieres eliminar esta invitación?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        invitationToDelete?.let { invitation ->
                            viewModel.deleteInvitation(invitation.id)
                        }
                        showDeleteDialog = false
                        invitationToDelete = null
                    }
                ) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        invitationToDelete = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}