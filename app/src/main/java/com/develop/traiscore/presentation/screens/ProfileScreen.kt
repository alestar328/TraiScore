package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.develop.traiscore.R
import com.develop.traiscore.core.UserRole
import com.develop.traiscore.data.Authentication.UserRoleManager
import com.develop.traiscore.presentation.MainActivity
import com.develop.traiscore.presentation.navigation.NavigationRoutes
import com.develop.traiscore.presentation.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class TrainerInfo(
    val name: String,
    val email: String,
    val photoUrl: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    onMeasurementsClick: () -> Unit,
    // Removemos el parámetro onEnterInvitationClick ya que usaremos navController directamente
) {
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

    LaunchedEffect(navController.currentBackStackEntry) { // <- Cambiar esto
        // Obtener rol del usuario
        UserRoleManager.getCurrentUserRole { role ->
            currentUserRole = role
        }

        // Si es cliente, buscar información del trainer
        val currentUser = auth.currentUser
        if (currentUser != null) {
            scope.launch {
                try {
                    isLoading = true // <- Agregar esto
                    trainerInfo = null // <- Resetear antes de buscar

                    val userDoc = FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(currentUser.uid)
                        .get()
                        .await()

                    val userRole = userDoc.getString("userRole")
                    if (userRole == "CLIENT") {
                        val linkedTrainerUid = userDoc.getString("linkedTrainerUid")
                        android.util.Log.d("ProfileScreen", "linkedTrainerUid: $linkedTrainerUid") // <- Debug

                        if (linkedTrainerUid != null) {
                            // Obtener información del trainer
                            val trainerDoc = FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(linkedTrainerUid)
                                .get()
                                .await()

                            android.util.Log.d("ProfileScreen", "Datos del trainer: ${trainerDoc.data}") // <- AGREGAR

                            val trainerName = "${trainerDoc.getString("firstName") ?: ""} ${trainerDoc.getString("lastName") ?: ""}".trim()
                            val trainerEmail = trainerDoc.getString("email") ?: ""

                            android.util.Log.d("ProfileScreen", "firstName: ${trainerDoc.getString("firstName")}") // <- AGREGAR
                            android.util.Log.d("ProfileScreen", "lastName: ${trainerDoc.getString("lastName")}") // <- AGREGAR
                            android.util.Log.d("ProfileScreen", "email: ${trainerDoc.getString("email")}") // <- AGREGAR
                            val trainerPhoto = trainerDoc.getString("photoURL")

                            android.util.Log.d("ProfileScreen", "Trainer encontrado: $trainerName") // <- Debug

                            if (trainerName.isNotEmpty()) {
                                trainerInfo = TrainerInfo(trainerName, trainerEmail, trainerPhoto)
                            }
                        }
                    }
                    isLoading = false
                } catch (e: Exception) {
                    android.util.Log.e("ProfileScreen", "Error cargando trainer info", e) // <- Debug
                    isLoading = false
                }
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.trailogoup),
                        contentDescription = "Logo cabecera",
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(NavigationRoutes.Settings.route)
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = navbarDay
                )
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(traiBackgroundDay),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // Avatar con badge en hexágono
            Box(contentAlignment = Alignment.BottomEnd) {
                Image(
                    painter = painterResource(R.drawable.user_png),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(48.dp))
                )
                HexagonBadge(
                    number = "1",
                    modifier = Modifier
                        .offset(x = 4.dp, y = 4.dp)
                        .size(24.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Estadísticas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ProfileStat(count = "0", label = "Piques")
                Divider(
                    modifier = Modifier
                        .height(32.dp)
                        .width(1.dp)
                        .background(Color.LightGray)
                )
                ProfileStat(count = "0", label = "Amigos")
                Divider(
                    modifier = Modifier
                        .height(32.dp)
                        .width(1.dp)
                        .background(Color.LightGray)
                )
                ProfileStat(count = "0", label = "SEGUIDOS")
            }

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
            if (currentUserRole == UserRole.TRAINER) {
                ProfileButton(
                    text = "Gestionar Invitaciones",
                    containerColor = traiBlue,
                    contentColor = Color.White,
                    icon = Icons.Default.AddCircle,
                    onClick = {
                        navController.navigate(NavigationRoutes.TrainerInvitations.route)
                    }
                )
                Spacer(Modifier.height(12.dp))
            }

            // Botones principales
            if (currentUserRole == UserRole.CLIENT) {
                ProfileButton(
                    text = "Mis medidas",
                    containerColor = traiBlue,
                    contentColor = Color.Black,
                    icon = Icons.Default.Home,
                    onClick = onMeasurementsClick
                )
                Spacer(Modifier.height(12.dp))
            }

            ProfileButton(
                text = "Cerrar sesión",
                containerColor = Color.Red,
                contentColor = Color.White,
                icon = Icons.Default.Clear,
                onClick = {
                    scope.launch {
                        auth.signOut()
                        googleSignInClient?.signOut()?.addOnCompleteListener {
                            navController.navigate(NavigationRoutes.Login.route) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun TrainerSection(
    trainerInfo: TrainerInfo?,
    isLoading: Boolean,
    onAddTrainer: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
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
                        .fillMaxWidth()
                        .padding(24.dp),
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
private fun ProfileStat(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(text = label, fontSize = 12.sp, color = Color.DarkGray)
    }
}

@Composable
fun ProfileButton(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0xFFE0E0E0),
    contentColor: Color = Color.Black,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth(0.9f)
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
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