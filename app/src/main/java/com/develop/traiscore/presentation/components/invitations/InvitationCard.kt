package com.develop.traiscore.presentation.components.invitations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.develop.traiscore.data.local.entity.InvitationEntity
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.viewmodels.InvitationViewModel
import java.util.*

@Composable
fun InvitationCard(
    invitation: InvitationEntity,
    onShare: (String) -> Unit,
    onCopy: (String) -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Código: ${invitation.invitationCode}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = traiBlue
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Creada: ${formatDate(invitation.createdAt.toDate())}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    invitation.expiresAt?.let {
                        Text(
                            text = "Expira: ${formatDate(it.toDate())}",
                            fontSize = 14.sp,
                            color = if (invitation.hasExpired()) Color.Red else Color.Gray
                        )
                    }

                    if (invitation.usedBy != null) {
                        Text(
                            text = "Usada",
                            fontSize = 14.sp,
                            color = Color.Green,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (!invitation.isAvailable()) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (invitation.hasExpired()) Color.Red else Color.Green,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (invitation.hasExpired()) "Expirada" else "Usada",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (invitation.isAvailable()) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onShare(invitation.invitationCode) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = traiBlue)
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Compartir")
                    }

                    OutlinedButton(
                        onClick = { onCopy(invitation.invitationCode) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.MailOutline,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copiar")
                    }

                    IconButton(onClick = onCancel) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Cancelar",
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
}