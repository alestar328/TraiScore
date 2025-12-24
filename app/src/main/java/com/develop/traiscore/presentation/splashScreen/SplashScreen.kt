package com.develop.traiscore.presentation.splashScreen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
    import com.develop.traiscore.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigate: () -> Unit) {
    // Estados para las animaciones
    var startAnimation by remember { mutableStateOf(false) }
    var typewriterText by remember { mutableStateOf("") }
    val fullText = "Track your trainings"

    // Animación de aparición del logo (fade in + scale)
    val logoAlpha = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1500,
            easing = FastOutSlowInEasing
        )
    )

    val logoScale = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    // Animación del texto (aparición después del logo)
    val textAlpha = animateFloatAsState(
        targetValue = if (typewriterText.isNotEmpty()) 1f else 0f,
        animationSpec = tween(durationMillis = 300)
    )

    LaunchedEffect(key1 = true) {
        // Iniciar animación del logo
        startAnimation = true

        // Esperar a que termine la animación del logo
        delay(1000)

        // Efecto máquina de escribir
        for (i in 1..fullText.length) {
            typewriterText = fullText.substring(0, i)
            delay(100) // Velocidad de escritura
        }

        // Esperar un poco más antes de navegar
        delay(1500)
        onNavigate()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo con animación de aparición gradual
            Image(
                painter = painterResource(id = R.drawable.tslogo), // Cambia por tu logo
                contentDescription = "TraiScore Logo",
                modifier = Modifier
                    .size(120.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
            )


            // Texto con efecto máquina de escribir
            Text(
                text = typewriterText,
                modifier = Modifier.alpha(textAlpha.value),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}