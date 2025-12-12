package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.develop.traiscore.BuildConfig
import com.develop.traiscore.R
import com.develop.traiscore.core.UserRole
import com.develop.traiscore.presentation.MainActivity
import com.develop.traiscore.presentation.ScreenState
import com.develop.traiscore.presentation.components.TraiScoreTopBar
import com.develop.traiscore.presentation.components.general.ProfilePhotoComponent
import com.develop.traiscore.presentation.navigation.NavigationRoutes
import com.develop.traiscore.presentation.theme.*
import com.develop.traiscore.presentation.viewmodels.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class TrainerInfo(
    val name: String,
    val email: String,
    val photoUrl: String? = null
)

@Composable
fun ProfileScreen(
    navController: NavHostController,
    clientId: String? = null,
    onSettingsClick: () -> Unit,
    onMeasurementsClick: () -> Unit,
    onConfigureTopBar: (left: @Composable () -> Unit, right: @Composable () -> Unit) -> Unit = { _, _ -> },
    onConfigureFAB: (fab: (@Composable () -> Unit)?) -> Unit = {},
    profileViewModel: ProfileViewModel = hiltViewModel(),
    setRoutineScreenState: (ScreenState) -> Unit,


    ) {
    val profileUiState by profileViewModel.uiState.collectAsStateWithLifecycle()
    var showAchievements by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val googleSignInClient = remember {
        val mainActivity = context as? MainActivity
        mainActivity?.googleSignInClient
    }

    var currentUserRole by remember { mutableStateOf<UserRole?>(null) }
    var trainerInfo by remember { mutableStateOf<TrainerInfo?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Estado para controlar el listener de Firestore
    var firestoreListener by remember { mutableStateOf<ListenerRegistration?>(null) }
    val isAthlete = BuildConfig.FLAVOR == "athlete"
    val isLite = BuildConfig.FLAVOR == "lite"
    val isProduction = BuildConfig.FLAVOR == "production"
    val isTrainer = BuildConfig.FLAVOR == "trainer"
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Función para limpiar el listener
    fun cleanupListener() {
        firestoreListener?.remove()
        firestoreListener = null
    }

    // Función para obtener información del trainer
    suspend fun fetchTrainerInfo(trainerId: String): TrainerInfo? {
        return try {
            val trainerDoc = FirebaseFirestore.getInstance()
                .collection("users")
                .document(trainerId)
                .get()
                .await()

            android.util.Log.d("ProfileScreen", "Datos del trainer: ${trainerDoc.data}")

            val trainerName =
                "${trainerDoc.getString("firstName") ?: ""} ${trainerDoc.getString("lastName") ?: ""}".trim()
            val trainerEmail = trainerDoc.getString("email") ?: ""
            val trainerPhoto = trainerDoc.getString("photoURL")

            android.util.Log.d("ProfileScreen", "Trainer encontrado: $trainerName")

            if (trainerName.isNotEmpty()) {
                TrainerInfo(trainerName, trainerEmail, trainerPhoto)
            } else null
        } catch (e: Exception) {
            android.util.Log.e("ProfileScreen", "Error cargando trainer info", e)
            null
        }
    }

    LaunchedEffect(profileUiState.error) {
        profileUiState.error?.let { error ->
            // Aquí puedes mostrar un Toast o Snackbar con el error
            android.util.Log.e("ProfileScreen", "Error: $error")
            profileViewModel.clearError()
        }
    }


    DisposableEffect(Unit) {
        onDispose {
            cleanupListener()
        }
    }
    LaunchedEffect(Unit) {

        // 1️⃣ Cargar foto del usuario
        profileViewModel.loadCurrentUserPhoto()

        // 2️⃣ Configuración del TopBar según flavor
        when (BuildConfig.FLAVOR) {

            "trainer" -> {
                onConfigureTopBar(
                    { /* left trainer */ },
                    {
                        IconButton(onClick = { navController.navigate(NavigationRoutes.Settings.route) }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.tsColors.ledCyan
                            )
                        }
                    }
                )
            }

            "production", "athlete", "lite" -> {
                onConfigureTopBar(
                    {
                     /*   Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clickable { showAchievements = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.trophy_icon),
                                contentDescription = "Logros",
                                tint = MaterialTheme.tsColors.ledCyan
                            )
                        }*/
                    },
                    {
                        IconButton(onClick = { onSettingsClick() }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.tsColors.ledCyan
                            )
                        }
                    }
                )
            }
        }

        // 3️⃣ Configurar FAB global
        onConfigureFAB(null)

        // 4️⃣ Listener Firestore SOLO para athletes
        if (isAthlete || isProduction || isLite) {
            val currentUser = auth.currentUser ?: return@LaunchedEffect
            isLoading = true
            trainerInfo = null

            try {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()

                cleanupListener()
                firestoreListener = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUser.uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            android.util.Log.e("ProfileScreen", "Error listener", error)
                            isLoading = false
                            return@addSnapshotListener
                        }

                        val trainerId = snapshot?.getString("linkedTrainerUid")

                        if (trainerId != null) {
                            scope.launch {
                                trainerInfo = fetchTrainerInfo(trainerId)
                                isLoading = false
                            }
                        } else {
                            trainerInfo = null
                            isLoading = false
                        }
                    }

            } catch (e: Exception) {
                android.util.Log.e("ProfileScreen", "Error inicial", e)
                isLoading = false
            }
        }
    }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = TraiScoreTheme.dimens.paddingMedium),
                        horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))

                // Avatar con badge en hexágono
                Box(contentAlignment = Alignment.BottomEnd) {
                    ProfilePhotoComponent(
                        currentPhotoUrl = profileUiState.photoUrl,
                        isUploading = profileUiState.isUploadingPhoto,
                        onPhotoSelected = { uri ->
                            profileViewModel.uploadProfilePhoto(uri)
                        }
                    )
                  /*  HexagonBadge(
                        number = "1",
                        modifier = Modifier
                            .offset(x = 4.dp, y = 4.dp)
                            .size(24.dp)
                    )*/
                }

                Spacer(Modifier.height(16.dp))

                // Estadísticas
               /* Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    ProfileStat(count = "0", label = stringResource(R.string.profile_friends_count))

                }*/

                Spacer(Modifier.height(24.dp))

                // Sección de Trainer para CLIENTES
                if (currentUserRole == UserRole.CLIENT) {
                    TrainerSection(
                        trainerInfo = trainerInfo,
                        isLoading = isLoading,
                        onAddTrainer = {
                            // Navegamos directamente a la pantalla de invitación
                            navController.navigate(NavigationRoutes.EnterInvitation.route)
                        }
                    )
                    Spacer(Modifier.height(20.dp))
                }

                // Botón de invitaciones para TRAINERS
                if (isTrainer) {
                    ProfileButton(
                        text = "Gestionar Invitaciones",
                        containerColor = traiBlue,
                        contentColor = Color.White,
                        painter = rememberVectorPainter(image = Icons.Default.AddCircle),
                        onClick = { navController.navigate(NavigationRoutes.TrainerInvitations.route) }
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // Botones principales
                if (isAthlete || isProduction || isLite) {
                  /*  TrainerSection(
                        trainerInfo = trainerInfo,
                        isLoading = isLoading,
                        onAddTrainer = {
                            // Navegamos directamente a la pantalla de invitación
                            navController.navigate(NavigationRoutes.EnterInvitation.route)
                        }
                    )
                    Spacer(Modifier.height(20.dp))*/

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileButton(
                            text = stringResource(R.string.profile_my_sizes),
                            modifier = Modifier.weight(1f),
                            containerColor = traiBlue,
                            contentColor = Color.Black,
                            painter = painterResource(id = R.drawable.body_size),
                            onClick = onMeasurementsClick
                        )
                        ProfileButton(
                            text = stringResource(R.string.profile_my_exercises),
                            modifier = Modifier.weight(1f),
                            containerColor = traiOrange,
                            contentColor = Color.Black,
                            painter = painterResource(id = R.drawable.exercises_icon),
                            onClick = { setRoutineScreenState(ScreenState.MY_EXERCISES_SCREEN) }
                        )
                    }
                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileButton(
                            text = stringResource(R.string.profile_my_health_with_ia),
                            modifier = Modifier.weight(1f),
                            containerColor = Color.White,
                            contentColor = Color.Black,
                            painter = painterResource(id = R.drawable.brain_ia),
                            onClick = { navController.navigate(NavigationRoutes.MyHealthWithIA.route) }
                        )

                    }
                }
                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = stringResource(R.string.profile_close_session),
                    color = Color.Red, // 👈 Texto en rojo
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 30.dp) // Espaciado vertical
                        .clickable { showLogoutDialog = true },
                    textAlign = TextAlign.Center // 👈 Centrado en la fila
                )
            }

        if (showLogoutDialog) {
            LogoutConfirmDialog(
                onConfirm = {
                    showLogoutDialog = false
                    scope.launch {
                        // 🔁 lo mismo que hacías en el onClick original
                        cleanupListener()
                        auth.signOut()
                        googleSignInClient?.signOut()?.addOnCompleteListener {
                            navController.navigate(NavigationRoutes.Login.route) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        }
                    }
                },
                onDismiss = { showLogoutDialog = false }
            )
        }



        AchivementsUI(
            isVisible = showAchievements,
            onDismiss = { showAchievements = false },
            clientId = clientId // Pasar el clientId para mostrar logros del cliente correcto
        )
    }


@Composable
private fun TrainerSection(
    trainerInfo: TrainerInfo?,
    isLoading: Boolean,
    onAddTrainer: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = traiBlue
                    )
                }
            }
            trainerInfo != null -> {
                // Tiene trainer asignado
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            traiBlue.copy(alpha = 0.05f)
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Foto del trainer o icono por defecto
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(traiBlue.copy(alpha = 0.1f))
                                .border(2.dp, traiBlue, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.pesa_icon),
                                contentDescription = "Entrenador",
                                modifier = Modifier.size(32.dp),
                                tint = traiBlue
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mi Entrenador",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = trainerInfo.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = trainerInfo.email,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }

                        // Badge de verificado
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Verificado",
                            modifier = Modifier.size(24.dp),
                            tint = Color(0xFF4CAF50)
                        )
                    }
                }
            }

            else -> {
                // No tiene trainer
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAddTrainer() }
                        .background(
                            Color(0xFFFFF3E0) // Fondo amarillo claro
                        )
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Sin entrenador",
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFFFF9800) // Naranja
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Sin Entrenador Asignado",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Únete a un entrenador para obtener rutinas personalizadas",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onAddTrainer,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF9800)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Agregar Entrenador")
                    }
                }
            }
        }
    }
}
@Composable
private fun LogoutConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "¿Cerrar la sesión de tu cuenta?",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Divider()

                // Opción: Salir (fila completa)
                Text(
                    text = "Salir",
                    color = Color.Red,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onConfirm() }
                        .padding(vertical = 12.dp),
                    textAlign = TextAlign.Center
                )

                // Opción: Cancelar (fila completa)
                Text(
                    text = "Cancelar",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDismiss() }
                        .padding(vertical = 12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
@Composable
private fun ProfileStat(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(text = label, fontSize = 12.sp, color = Color.DarkGray)
    }
}

@Composable
fun ProfileButton(
    text: String,
    iconSize: Dp = 28.dp,
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0xFFE0E0E0),
    contentColor: Color = Color.Black,
    painter: Painter? = null, // Cambio de ImageVector a Painter
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .widthIn(min = 80.dp)
            .height(80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        if (painter != null) {
            Icon(
                painter = painter, // Usamos el Painter aquí
                contentDescription = null,
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private val hexagonShape: Shape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    moveTo(w * 0.5f, 0f)
    lineTo(w, h * 0.25f)
    lineTo(w, h * 0.75f)
    lineTo(w * 0.5f, h)
    lineTo(0f, h * 0.75f)
    lineTo(0f, h * 0.25f)
    close()
}

@Composable
private fun HexagonBadge(number: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(hexagonShape)
            .background(Color(0xFFB38F33)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}