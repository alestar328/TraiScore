package com.develop.traiscore.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.develop.traiscore.R
import com.develop.traiscore.presentation.theme.TraiScoreTheme
import com.develop.traiscore.presentation.theme.traiBlue

// Data class para los colores de categoría
data class CategoryColor(
    val name: String,
    val value: Color,
    val displayName: String
)

// Data class para los iconos disponibles
data class CategoryIcon(
    val id: String,
    val name: String,
    val drawableRes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCategoryUI(
    onBack: () -> Unit,
    onSave: (name: String, iconId: String, color: Color) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf<CategoryIcon?>(null) } // Por defecto sin icono
    var selectedColor by remember { mutableStateOf(traiBlue) }
    var nameError by remember { mutableStateOf<String?>(null) }

    // Colores predefinidos simplificados
    val colorPalette = listOf(
        CategoryColor("cyan", Color(0xFF3AF2FF), "Cyan"),
        CategoryColor("coral", Color(0xFFFF6B6B), "Coral"),
        CategoryColor("mint", Color(0xFF4ECDC4), "Menta"),
        CategoryColor("ocean", Color(0xFF45B7D1), "Océano"),
        CategoryColor("jade", Color(0xFF96CEB4), "Jade"),
        CategoryColor("honey", Color(0xFFFECA57), "Miel"),
        CategoryColor("sakura", Color(0xFFFF9FF3), "Rosa"),
        CategoryColor("royal", Color(0xFF5F27CD), "Púrpura"),
        CategoryColor("sunset", Color(0xFFFF7675), "Naranja"),
        CategoryColor("forest", Color(0xFF00B894), "Verde"),
        CategoryColor("midnight", Color(0xFF2D3436), "Azul"),
        CategoryColor("gold", Color(0xFFFDCB6E), "Dorado")
    )

    // Iconos disponibles (usando drawables existentes)
    val availableIcons = listOf(
        CategoryIcon("dumbbell", "Mancuernas", R.drawable.chest_pic),
        CategoryIcon("cardio", "Cardio", R.drawable.core_pic),
        CategoryIcon("strength", "Fuerza", R.drawable.arms_pic),
        CategoryIcon("run", "Correr", R.drawable.legs_pic),
        CategoryIcon("bike", "Ciclismo", R.drawable.shoulders_pic),
        CategoryIcon("swim", "Natación", R.drawable.back_pic),
        CategoryIcon("yoga", "Yoga", R.drawable.chest_pic),
        CategoryIcon("boxing", "Boxeo", R.drawable.arms_pic),
        CategoryIcon("football", "Fútbol", R.drawable.legs_pic),
        CategoryIcon("basketball", "Basketball", R.drawable.shoulders_pic),
        CategoryIcon("tennis", "Tenis", R.drawable.back_pic),
        CategoryIcon("target", "Objetivo", R.drawable.core_pic)
    )

    // Validación del nombre
    fun validateName(name: String): String? {
        if (name.isEmpty()) return null
        if (name.length < 2) return "Mínimo 2 caracteres"
        if (name.length > 15) return "Máximo 15 caracteres"
        if (!name.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$"))) return "Solo letras y espacios"
        return null
    }

    fun handleNameChange(name: String) {
        categoryName = name
        nameError = validateName(name)
    }

    fun canSave(): Boolean {
        return categoryName.trim().isNotEmpty() && nameError == null
        // Ya no requiere selectedIcon - puede ser null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nueva Categoría",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.Yellow
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.DarkGray
                )
            )
        },
        containerColor = Color.DarkGray,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (canSave()) {
                        onSave(categoryName, selectedIcon?.id ?: "", selectedColor)
                    }
                },
                icon = {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Crear categoría"
                    )
                },
                text = { Text("Crear") },
                containerColor = if (canSave()) Color.Green else Color.Gray,
                contentColor = Color.Black
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                // Preview de la categoría
                CategoryPreview(
                    name = categoryName.ifEmpty { "Mi Categoría" },
                    icon = selectedIcon,
                    color = selectedColor
                )

                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                // Campo de nombre
                Text(
                    text = "Nombre de la categoría",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { handleNameChange(it) },
                    placeholder = { Text("Ej: Fullbody") },
                    singleLine = true,
                    isError = nameError != null,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    supportingText = {
                        nameError?.let { error ->
                            Text(
                                text = error,
                                color = Color.Red,
                                style = MaterialTheme.typography.bodySmall
                            )
                        } ?: run {
                            Text(
                                text = "${categoryName.length}/15 caracteres",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = traiBlue,
                        unfocusedBorderColor = traiBlue,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        cursorColor = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                // Selector de icono y color en una fila
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Sección de iconos
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Ícono (opcional)",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = "Puedes dejarlo sin ícono",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Botón "Sin ícono"
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (selectedIcon == null) traiBlue.copy(alpha = 0.3f)
                                    else Color.White.copy(alpha = 0.1f)
                                )
                                .then(
                                    if (selectedIcon == null) Modifier.border(
                                        2.dp,
                                        traiBlue,
                                        RoundedCornerShape(8.dp)
                                    ) else Modifier
                                )
                                .clickable { selectedIcon = null }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (selectedIcon == null) "✓ Sin ícono" else "Sin ícono",
                                color = if (selectedIcon == null) traiBlue else Color.Gray,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (selectedIcon == null) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.height(160.dp)
                        ) {
                            items(availableIcons) { icon ->
                                IconSelectionCard(
                                    icon = icon,
                                    isSelected = selectedIcon?.id == icon.id,
                                    onClick = { selectedIcon = icon }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Sección de colores
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Color",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.height(200.dp)
                        ) {
                            items(colorPalette) { colorItem ->
                                ColorSelectionCard(
                                    categoryColor = colorItem,
                                    isSelected = selectedColor == colorItem.value,
                                    onClick = { selectedColor = colorItem.value }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp)) // Espacio para el FAB
            }
        }
    }
}

@Composable
private fun CategoryPreview(
    name: String,
    icon: CategoryIcon?,
    color: Color
) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Image(
                    painter = painterResource(id = icon.drawableRes),
                    contentDescription = icon.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                // Placeholder cuando no hay icono seleccionado (estado por defecto)
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📁",
                        fontSize = 24.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                ),
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun IconSelectionCard(
    icon: CategoryIcon,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) Color.White.copy(alpha = 0.2f)
                else Color.White.copy(alpha = 0.05f)
            )
            .then(
                if (isSelected) Modifier.border(
                    2.dp,
                    traiBlue,
                    RoundedCornerShape(12.dp)
                ) else Modifier
            )
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = icon.drawableRes),
            contentDescription = icon.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(6.dp))
        )
    }
}

@Composable
private fun ColorSelectionCard(
    categoryColor: CategoryColor,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(categoryColor.value)
            .then(
                if (isSelected) Modifier.border(
                    3.dp,
                    Color.White,
                    CircleShape
                ) else Modifier
            )
            .clickable { onClick() }
    )
}

@Preview(showBackground = true)
@Composable
fun CreateCategoryUIPreview() {
    TraiScoreTheme {
        CreateCategoryUI(
            onBack = { /* Preview */ },
            onSave = { _, _, _ -> /* Preview */ }
        )
    }
}