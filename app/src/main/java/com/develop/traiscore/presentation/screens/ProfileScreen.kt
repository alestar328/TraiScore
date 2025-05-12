package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.develop.traiscore.R
import com.develop.traiscore.presentation.MainActivity
import com.develop.traiscore.presentation.navigation.NavigationRoutes
import com.develop.traiscore.presentation.theme.TraiScoreTheme
import com.develop.traiscore.presentation.theme.traiBackgroundDay
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.theme.traiOrange
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController
) {
    var selectedTab by remember { mutableStateOf(2) /* 0:Explorar,1:Mapa,2:Perfil */ }
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val googleSignInClient = remember {
        val mainActivity = context as? MainActivity
        mainActivity?.googleSignInClient
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "alejandro ormeño",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* abrir drawer */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                    }
                },
                actions = {
                    IconButton(onClick = { /* abrir chat */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
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

            // Botones grandes
            ProfileButton(
                text = "Mis medidas",
                containerColor = traiBlue,
                contentColor = Color.Black,
                onClick = {
                    navController.navigate(NavigationRoutes.Measurements.route)

                })
            Spacer(Modifier.height(12.dp))
            ProfileButton(
                text = "Favoritos",
                containerColor = traiOrange,
                contentColor = Color.Black,
                onClick = { /* … */ })
            Spacer(Modifier.height(12.dp))
            ProfileButton(
                text = "Cerrar sesion",
                containerColor = Color.Red,
                contentColor = Color.White,
                onClick = {
                    scope.launch {
                        auth.signOut()
                        googleSignInClient?.signOut()?.addOnCompleteListener {
                            navController.navigate(NavigationRoutes.Login.route) {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        }
                    }
                })

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
        contentPadding = PaddingValues() // centers the text nicely
    ) {
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

@Preview(
    name = "ProfileScreen Preview",
    showBackground = true,
    backgroundColor = 0xFFF5F5F5, // mismo gris claro de fondo
    widthDp = 360,
    heightDp = 640
)
@Composable
fun ProfileScreenPreview() {
    TraiScoreTheme {
        val navController = rememberNavController()
        ProfileScreen(navController = navController)
    }
}