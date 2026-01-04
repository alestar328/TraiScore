package com.develop.traiscore.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.develop.traiscore.R
import com.develop.traiscore.core.Gender
import com.develop.traiscore.core.UserRole
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.zIndex
import com.develop.traiscore.presentation.components.DatePickerSection
import com.develop.traiscore.presentation.components.FullScreenDatePicker
import com.develop.traiscore.presentation.components.ToggleButtonRowUser
import com.develop.traiscore.presentation.theme.Black
import com.develop.traiscore.presentation.theme.Roboto
import com.develop.traiscore.presentation.theme.TraiScoreTheme
import com.develop.traiscore.presentation.theme.traiBlue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
@Composable
fun LoginScreen(
    onGoogleClick: () -> Unit,
) {
    var showOnboarding by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        if (showOnboarding) {
            OnboardingSection(
                onFinish = { showOnboarding = false },
                onGoogleClick = onGoogleClick
            )
        } else {
            // Solo el botón de Google
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.tslogo),
                    contentDescription = "Logo TraiScore",
                    modifier = Modifier.size(120.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Bienvenido a TraiScore",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(48.dp))
                ElevatedButton(
                    onClick = onGoogleClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.elevatedButtonElevation(6.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.google),
                        contentDescription = "Google sign-in",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Accede con Google",
                        color = Color.Black,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingSection(onFinish: () -> Unit, onGoogleClick: () -> Unit = {}) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    val pages = listOf(
        Triple(R.drawable.onboarding1, "Registra tus entrenamientos", "Guarda tus sesiones y mejora cada día."),
        Triple(R.drawable.onboarding2, "Analiza tu progreso", "Visualiza tu rendimiento en gráficos inteligentes."),
        Triple(R.drawable.onboarding3, "Alcanza tus metas", "Organiza tus rutinas y logra tus objetivos.")
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // 🔹 Horizontal Pager con las 3 pantallas
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                val current = pages[page]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = current.first),
                        contentDescription = null,
                        modifier = Modifier
                            .size(450.dp),
                        contentScale = ContentScale.Fit
                    )
                    Text(
                        text = current.second,
                        color = Color.Cyan,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = current.third,
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }


            // 🔹 Indicadores
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(pages.size) { index ->
                    val color = if (index == pagerState.currentPage) traiBlue else Color.Gray
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(10.dp)
                            .background(color, shape = RoundedCornerShape(50))
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 🔹 Botones inferiores: Siguiente / Comenzar + Google
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 🔹 Botón de Google visible desde la primera onboarding
                ElevatedButton(
                    onClick = onGoogleClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.elevatedButtonElevation(6.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.google),
                        contentDescription = "Google sign-in",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Accede con Google",
                        color = Color.Black,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(
    name = "LoginScreenPreview",
    showBackground = true
)
@Composable
fun LoginScreenPreview() {
    TraiScoreTheme {
        LoginScreen(
            onGoogleClick = {}
        )
    }
}