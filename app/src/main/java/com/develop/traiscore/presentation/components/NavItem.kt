package com.develop.traiscore.presentation.components

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector

data class NavItem(
    val label: String,
    val imageVector: ImageVector? = null,
    val painter: Painter? = null,
    val badgeCount : Int,
)
