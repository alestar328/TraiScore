package com.develop.traiscore.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.develop.traiscore.R
import kotlin.math.absoluteValue

@Composable
fun MuscleGroupCarousel(
    modifier: Modifier = Modifier,
    onImageSelected: (Int) -> Unit = {} // Callback para cuando se selecciona una imagen
) {
    // Lista de imágenes de los grupos musculares
    val muscleGroups = listOf(
        R.drawable.chest_pic,
        R.drawable.glutes_pic,
        R.drawable.back_pic,
        R.drawable.legs_pic,
        R.drawable.arms_pic,
        R.drawable.shoulders_pic,
        R.drawable.core_pic,
    )

    // Lista de nombres para mostrar
    val muscleGroupNames = listOf(
        "Pecho",
        "Glúteos",
        "Espalda",
        "Piernas",
        "Brazos",
        "Hombros",
        "Core"
    )

    val pagerState = rememberPagerState(pageCount = { muscleGroups.size })

    // Detectar cambios en la página actual y llamar al callback
    LaunchedEffect(pagerState.currentPage) {
        onImageSelected(muscleGroups[pagerState.currentPage])
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp), // Aumentamos la altura para el texto
            contentPadding = PaddingValues(horizontal = 60.dp),
            pageSpacing = 20.dp
        ) { page ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Card con la imagen
                Card(
                    modifier = Modifier
                        .size(120.dp) // Contenedor cuadrado
                        .graphicsLayer {
                            // Efecto de escala para la página actual
                            val pageOffset = (
                                    (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                                    ).absoluteValue

                            alpha = lerp(
                                start = 0.6f,
                                stop = 1.0f,
                                fraction = 1f - pageOffset.coerceIn(0f, 1f)
                            )
                            scaleY = lerp(
                                start = 0.85f,
                                stop = 1.0f,
                                fraction = 1f - pageOffset.coerceIn(0f, 1f)
                            )
                            scaleX = lerp(
                                start = 0.85f,
                                stop = 1.0f,
                                fraction = 1f - pageOffset.coerceIn(0f, 1f)
                            )
                        }
                        // ✅ Agregar borde cuando es la página actual
                        .let { mod ->
                            if (pagerState.currentPage == page) {
                                mod.border(
                                    width = 3.dp,
                                    color = Color.Green,
                                    shape = RoundedCornerShape(16.dp)
                                )
                            } else mod
                        },
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (pagerState.currentPage == page) 12.dp else 4.dp
                    )
                ) {
                    Image(
                        painter = painterResource(id = muscleGroups[page]),
                        contentDescription = "Grupo muscular ${muscleGroupNames[page]}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // ✅ Texto del nombre del grupo muscular
                Text(
                    text = muscleGroupNames[page],
                    style = if (pagerState.currentPage == page) {
                        MaterialTheme.typography.labelLarge
                    } else {
                        MaterialTheme.typography.labelMedium
                    },
                    color = if (pagerState.currentPage == page) {
                        Color.Green
                    } else {
                        Color.White.copy(alpha = 0.7f)
                    },
                    modifier = Modifier.animateContentSize(
                        animationSpec = tween(300)
                    )
                )
            }
        }
    }
}