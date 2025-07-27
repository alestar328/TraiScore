package com.develop.traiscore.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
        // Aquí puedes añadir más drawable resources cuando los tengas
        R.drawable.legs_pic,
        R.drawable.arms_pic,
        R.drawable.shoulders_pic,
        R.drawable.core_pic,
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
        Text(
            text = "Grupos Musculares",
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            contentPadding = PaddingValues(horizontal = 60.dp),
            pageSpacing = 20.dp
        ) { page ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
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
                        },
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (pagerState.currentPage == page) 8.dp else 4.dp
                    )
                ) {
                    Image(
                        painter = painterResource(id = muscleGroups[page]),
                        contentDescription = "Grupo muscular ${page + 1}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Indicadores de página
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(muscleGroups.size) { index ->
                Box(
                    modifier = Modifier
                        .size(
                            if (pagerState.currentPage == index) 12.dp else 8.dp
                        )
                        .background(
                            color = if (pagerState.currentPage == index)
                                Color.White
                            else
                                Color.Gray.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                        .animateContentSize()
                )
            }
        }
    }
}