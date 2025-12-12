package com.develop.traiscore.presentation.screens

import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.develop.traiscore.BuildConfig
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.develop.traiscore.R
import com.develop.traiscore.core.DefaultCategoryExer
import com.develop.traiscore.data.firebaseData.RoutineDocument
import com.develop.traiscore.exports.ImportRoutineViewModel
import com.develop.traiscore.presentation.components.trainers.TopBarTrainersRoutines
import com.develop.traiscore.presentation.theme.TraiScoreTheme
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.theme.tsColors
import com.develop.traiscore.presentation.viewmodels.RoutineViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun RoutineMenuScreen(
    onRoutineClick: (String, String) -> Unit,
    onAddClick: () -> Unit,
    viewModel: RoutineViewModel,
    importViewModel: ImportRoutineViewModel = hiltViewModel(),
    screenTitle: String = "Mis Rutinas",
    clientName: String? = null,
    onConfigureTopBar: (left: @Composable () -> Unit, right: @Composable () -> Unit) -> Unit = { _, _ -> },
    onConfigureFAB: (fab: (@Composable () -> Unit)?) -> Unit = {}
) {
    val context = LocalContext.current
    var showEmptyDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var routineToDelete by remember { mutableStateOf<Pair<Int, RoutineDocument>?>(null) }

    val currentTargetUser = if (viewModel.isClientMode()) {
        viewModel.getTargetUserId()
    } else {
        FirebaseAuth.getInstance().currentUser?.uid
    }

    LaunchedEffect(currentTargetUser) {
        Log.d("RoutineMenuScreen", "LaunchedEffect triggered for user: $currentTargetUser")

        if (currentTargetUser != null) {
            viewModel.loadRoutines(context) { hasRoutines ->
                if (!hasRoutines && !viewModel.hasShownEmptyDialog) {
                    showEmptyDialog = true
                }
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            viewModel.handleFileImport(
                context = context,
                uri = selectedUri,
                importViewModel = importViewModel,
                onSuccess = { message ->
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                },
                onError = { error ->
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                }
            )
        }
    }


    // Diálogo de confirmación para eliminar rutina
    if (showDeleteDialog && routineToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                routineToDelete = null
            },
            title = {
                Text(
                    text = "Confirmar eliminación",
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que quieres eliminar la rutina '${routineToDelete?.second?.clientName}'?",
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        routineToDelete?.let { (index, routine) ->
                            viewModel.deleteRoutineType(routine.documentId) { success ->
                                if (success) {
                                    // ✅ Actualizar la lista del viewModel en lugar de la lista local
                                    viewModel.routineTypes.removeAt(index)
                                    Toast.makeText(context, "Rutina eliminada", LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Error al eliminar rutina",
                                        LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                        showDeleteDialog = false
                        routineToDelete = null
                    }
                ) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        routineToDelete = null
                    }
                ) {
                    Text("Cancelar", color = traiBlue)
                }
            }
        )
    }

    // Si no hay rutinas, mostramos el diálogo y salimos
    if (showEmptyDialog && !viewModel.isLoading) {
        AlertDialog(
            onDismissRequest = { showEmptyDialog = false },
            title = { Text("Sin rutinas") },
            text = {
                Text(
                    if (viewModel.isClientMode()) {
                        "Este cliente no tiene rutinas guardadas"
                    } else {
                        "No tienes rutinas guardadas"
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showEmptyDialog = false
                        viewModel.markEmptyDialogShown()
                    })
                {
                    Text("Aceptar")
                }
            }
        )
        return
    }
    LaunchedEffect(Unit) {
        when (BuildConfig.FLAVOR) {

            "trainer" -> {
                onConfigureTopBar(
                    {
                        /* Left icon vacío para entrenador */
                    },
                    {
                        TopBarTrainersRoutines(
                            title = screenTitle,
                            onShareClick = { println("Compartir rutinas") }
                        )
                    }
                )
            }

            "production", "lite" -> {
                onConfigureTopBar(
                    {
                     /*   FloatingActionButton(
                         /*   onClick = { filePickerLauncher.launch("*/*") },
                            modifier = Modifier.size(30.dp),
                            containerColor = MaterialTheme.tsColors.ledCyan
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.import_icon),
                                contentDescription = "Importar",
                                tint = Color.Black
                            )
                        }*/
                    },
                    {

                        FloatingActionButton(
                            onClick = { println("⏱️ Cronómetro") },
                            modifier = Modifier.size(30.dp),
                            containerColor = MaterialTheme.tsColors.ledCyan
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.timer_icon),
                                contentDescription = "Temporizador",
                                tint = Color.Black
                            )
                        }
                    }
                )
            }
        }

        onConfigureFAB {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nueva rutina") },
                containerColor = traiBlue
            )
        }
    }


    LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = TraiScoreTheme.dimens.paddingMedium),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(
                    viewModel.routineTypes,
                    key = { index, r -> "${r.documentId}-${r.type}-$index" }
                ) { index, routine ->
                    val dismissState = rememberDismissState(
                        confirmStateChange = { newValue ->
                            if (newValue == DismissValue.DismissedToStart) {
                                routineToDelete = Pair(index, routine)
                                showDeleteDialog = true
                            }
                            false
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
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onRoutineClick(routine.documentId, routine.type) }
                            ) {
                                RoutineItem(
                                    name = routine.clientName,
                                    imageResId = try {
                                        DefaultCategoryExer.valueOf(routine.type.uppercase()).imageCat
                                    } catch (_: Exception) {
                                        DefaultCategoryExer.BACK.imageCat  // fallback si no coincide
                                    },
                                    onClick = { onRoutineClick(routine.documentId, routine.type) }
                                )
                            }

                        }
                    )
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
fun RoutineItemPreview() {
    // Preview solo del item individual
    RoutineItem(
        name = "Rutina de Pecho",
        imageResId = R.drawable.chest_pic, // Ajustar según tu drawable
        onClick = {}
    )
}