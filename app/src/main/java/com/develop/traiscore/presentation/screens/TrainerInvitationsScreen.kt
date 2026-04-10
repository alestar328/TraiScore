package com.develop.traiscore.presentation.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.develop.traiscore.data.local.entity.InvitationEntity
import com.develop.traiscore.presentation.components.invitations.CreateInvitationDialog
import com.develop.traiscore.presentation.components.invitations.EmptyInvitationsState
import com.develop.traiscore.presentation.components.invitations.InvitationCard
import com.develop.traiscore.presentation.components.invitations.copyToClipboard
import com.develop.traiscore.presentation.components.invitations.shareViaWhatsApp
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
    val newInvitation by viewModel.newlyCreatedInvitation.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser

    val snackbarHostState = remember { SnackbarHostState() }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var invitationToDelete by remember { mutableStateOf<InvitationEntity?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    // Mostrar errores como Snackbar
    LaunchedEffect(error) {
        error?.let { snackbarHostState.showSnackbar(it) }
    }

    // Cuando se crea una invitación, mostrar el diálogo de compartir
    var invitationToShare by remember { mutableStateOf<InvitationEntity?>(null) }
    LaunchedEffect(newInvitation) {
        if (newInvitation != null) {
            invitationToShare = newInvitation
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadInvitations()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Invitaciones") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
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
                                    false
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
                                            shareViaWhatsApp(
                                                context,
                                                code,
                                                currentUser?.displayName ?: "Tu entrenador"
                                            )
                                        },
                                        onCopy = { code -> copyToClipboard(context, code) },
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

    // Diálogo de creación (selección de expiración)
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

    // Diálogo de compartir tras crear invitación
    invitationToShare?.let { invitation ->
        ShareInvitationDialog(
            invitation = invitation,
            trainerName = currentUser?.displayName ?: "Tu entrenador",
            onDismiss = {
                invitationToShare = null
                viewModel.clearNewInvitation()
            },
            onShareWhatsApp = {
                shareViaWhatsApp(context, invitation.invitationCode, currentUser?.displayName ?: "Tu entrenador")
            },
            onCopy = { copyToClipboard(context, invitation.invitationCode) }
        )
    }

    // Diálogo de confirmación para eliminar
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
                        invitationToDelete?.let { viewModel.deleteInvitation(it.id) }
                        showDeleteDialog = false
                        invitationToDelete = null
                    }
                ) { Text("Eliminar", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    invitationToDelete = null
                }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ShareInvitationDialog(
    invitation: InvitationEntity,
    trainerName: String,
    onDismiss: () -> Unit,
    onShareWhatsApp: () -> Unit,
    onCopy: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "¡Invitación creada!",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Comparte este código con tu cliente:",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                // Código prominente
                Surface(
                    color = traiBlue.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = invitation.invitationCode,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = traiBlue,
                        letterSpacing = 8.sp,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                }

                // Botón WhatsApp
                Button(
                    onClick = onShareWhatsApp,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Enviar por WhatsApp", fontWeight = FontWeight.SemiBold)
                }

                // Botón copiar
                OutlinedButton(
                    onClick = onCopy,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Copiar código")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Listo", color = traiBlue, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}
