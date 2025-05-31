package com.develop.traiscore.presentation.components.invitations

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.viewmodels.InvitationViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvitationDialog(
    onDismiss: () -> Unit,
    onCreate: (Int?) -> Unit
) {
    var selectedExpiration by remember { mutableStateOf(7) } // 7 días por defecto

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva invitación") },
        text = {
            Column {
                Text("Selecciona la duración de la invitación:")

                Spacer(modifier = Modifier.height(16.dp))

                // Opciones de expiración
                listOf(
                    1 to "1 día",
                    7 to "7 días",
                    30 to "30 días",
                    -1 to "Sin expiración"
                ).forEach { (days, label) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedExpiration == days,
                            onClick = { selectedExpiration = days }
                        )
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onCreate(if (selectedExpiration == -1) null else selectedExpiration)
                }
            ) {
                Text("Crear", color = traiBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(date)
}

fun shareInvitation(context: Context, code: String, trainerName: String) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT,
            "¡Únete a TraiScore!\n\n" +
                    "$trainerName te invita a entrenar con TraiScore.\n\n" +
                    "Usa este código de invitación: $code\n\n" +
                    "Descarga la app y regístrate con este código para comenzar."
        )
    }
    context.startActivity(Intent.createChooser(shareIntent, "Compartir invitación"))
}

fun copyToClipboard(context: Context, code: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Código de invitación", code)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Código copiado", Toast.LENGTH_SHORT).show()
}