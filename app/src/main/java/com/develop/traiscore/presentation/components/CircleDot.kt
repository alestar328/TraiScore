package com.develop.traiscore.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.develop.traiscore.presentation.theme.TraiScoreTheme

@Composable
fun CircleDot(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit = {} // Contenido opcional

){
    Box(
        modifier = modifier
            .size(TraiScoreTheme.dimens.iconSizeSmall)
            .clip(shape = CircleShape)
            .background(color)
    ){
        content() // Muestra el contenido
    }
}

@Preview(
    name = "CircleDorPreview",
    showBackground = true,
)
@Composable
fun CircleDorPreview(){
    TraiScoreTheme{
        CircleDot(color = Color.Red) {
            // Ejemplo de ícono en el centro del círculo
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.White
            )
        }
    }
}