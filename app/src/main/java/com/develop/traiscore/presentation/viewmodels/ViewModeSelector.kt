package com.develop.traiscore.presentation.viewmodels

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.develop.traiscore.R
import com.develop.traiscore.presentation.theme.tsColors

enum class ViewMode {
    YEAR, MONTH, TODAY
}

@Composable
fun ViewModeSelector(
    selectedMode: ViewMode,
    onModeSelected: (ViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ViewModeItem(
                title = stringResource(id = R.string.calendar_year),
                icon = R.drawable.year_icon,
                isSelected = selectedMode == ViewMode.YEAR,
                onClick = { onModeSelected(ViewMode.YEAR) },
                modifier = Modifier.weight(1f)
            )

            ViewModeItem(
                title = stringResource(id = R.string.calendar_month),
                icon = R.drawable.month_icon,
                isSelected = selectedMode == ViewMode.MONTH,
                onClick = { onModeSelected(ViewMode.MONTH) },
                modifier = Modifier.weight(1f)
            )

            ViewModeItem(
                title = stringResource(id = R.string.calendar_today),
                icon = R.drawable.today_icon,
                isSelected = selectedMode == ViewMode.TODAY,
                onClick = { onModeSelected(ViewMode.TODAY) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ViewModeItem(
    title: String,
    icon: Int? = null,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.tsColors.ledCyan.copy(alpha = 0.2f)
    } else {
        Color.Transparent
    }

    val textColor = if (isSelected) {
        MaterialTheme.tsColors.ledCyan
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = modifier
            .clickable { onClick() }
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (icon != null) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = title,
                tint = textColor,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = title,
                tint = textColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall, // Más pequeño para integración
            color = textColor,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1
        )
    }
}
@Preview(
    showBackground = true,
    name = "ViewModeSelector - Year Selected",
    apiLevel = 33
)
@Composable
fun ViewModeSelectorYearPreview() {
    PreviewTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Gray.copy(alpha = 0.3f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            ViewModeSelector(
                selectedMode = ViewMode.YEAR,
                onModeSelected = { /* Preview - no action */ },
            )
        }
    }
}




@Preview(
    showBackground = true,
    name = "ViewModeSelector - Dark Theme",
    apiLevel = 33
)
@Composable
fun ViewModeSelectorDarkPreview() {
    MaterialTheme(
        colorScheme = darkColorScheme(
            surface = Color.DarkGray,
            onSurface = Color.White,
            background = Color.Black
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            ViewModeSelector(
                selectedMode = ViewMode.MONTH,
                onModeSelected = { /* Preview - no action */ },
            )
        }
    }
}

@Preview(
    showBackground = true,
    name = "ViewModeSelector - All States",
    apiLevel = 33,
    heightDp = 600
)
@Composable
fun ViewModeSelectorAllStatesPreview() {
    PreviewTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Gray.copy(alpha = 0.2f))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Estados del ViewModeSelector",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Año seleccionado
            ViewModeSelector(
                selectedMode = ViewMode.YEAR,
                onModeSelected = { },
            )

        }
    }
}

@Preview(
    showBackground = true,
    name = "ViewModeSelector - Interactive Demo",
    apiLevel = 33
)
@Composable
fun ViewModeSelectorInteractivePreview() {
    var selectedMode by remember { mutableStateOf(ViewMode.MONTH) }

    PreviewTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray.copy(alpha = 0.3f))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Modo seleccionado: ${selectedMode.name}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            ViewModeSelector(
                selectedMode = selectedMode,
                onModeSelected = { newMode ->
                    selectedMode = newMode
                },
            )

            Text(
                text = "Toca una opción para cambiar el estado",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Preview(
    showBackground = true,
    name = "ViewModeSelector - Compact",
    apiLevel = 33,
    widthDp = 200
)
@Composable
fun ViewModeSelectorCompactPreview() {
    PreviewTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            ViewModeSelector(
                selectedMode = ViewMode.TODAY,
                onModeSelected = { },
                modifier = Modifier.wrapContentSize()
            )
        }
    }
}

// Función auxiliar para crear un tema de preview sin dependencias complejas
@Composable
private fun PreviewTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            surface = Color.White,
            onSurface = Color.Black,
            background = Color.White,
            primary = Color.Blue
        ),
        content = content
    )
}

// Mock del tema personalizado para evitar dependencias
private object MockTsColors {
    val ledCyan = Color.Cyan
}

// Extension mock para MaterialTheme
private val MaterialTheme.mockTsColors: MockTsColors
    get() = MockTsColors

// Versión simplificada del componente para preview sin dependencias externas
@Composable
fun ViewModeSelectorSimplified(
    selectedMode: ViewMode,
    onModeSelected: (ViewMode) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .wrapContentSize()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ViewModeItemSimplified(
                title = "Año",
                isSelected = selectedMode == ViewMode.YEAR,
                onClick = {
                    onModeSelected(ViewMode.YEAR)
                    onDismiss()
                }
            )

            ViewModeItemSimplified(
                title = "Mes",
                isSelected = selectedMode == ViewMode.MONTH,
                onClick = {
                    onModeSelected(ViewMode.MONTH)
                    onDismiss()
                }
            )

            ViewModeItemSimplified(
                title = "Hoy",
                isSelected = selectedMode == ViewMode.TODAY,
                onClick = {
                    onModeSelected(ViewMode.TODAY)
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun ViewModeItemSimplified(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        Color.Cyan.copy(alpha = 0.2f)
    } else {
        Color.Transparent
    }

    val textColor = if (isSelected) {
        Color.Cyan
    } else {
        Color.Black
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = title,
            tint = textColor,
            modifier = Modifier.size(20.dp)
        )

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Preview(
    showBackground = true,
    name = "ViewModeSelector - Simplified Version",
    apiLevel = 33
)
@Composable
fun ViewModeSelectorSimplifiedPreview() {
    var selectedMode by remember { mutableStateOf(ViewMode.MONTH) }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray.copy(alpha = 0.2f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            ViewModeSelectorSimplified(
                selectedMode = selectedMode,
                onModeSelected = { newMode ->
                    selectedMode = newMode
                },
                onDismiss = { }
            )
        }
    }
}