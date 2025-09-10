package com.develop.traiscore.presentation.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.develop.traiscore.R
import com.develop.traiscore.presentation.theme.TraiScoreTheme
import com.develop.traiscore.presentation.theme.tsColors

@Composable
fun TrainerBottomNavigationBar(
    selectedIndex: Int,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.height(100.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        // Mis Clientes
        NavigationBarItem(
            selected = selectedIndex == 0,
            onClick = { onItemClick(0) },
            icon = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy((-1).dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.clients_icon),
                        contentDescription = stringResource(R.string.nav_clients),
                        tint = if (selectedIndex == 0)
                            MaterialTheme.tsColors.ledCyan
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(TraiScoreTheme.dimens.iconSizeSmall)
                    )
                    // Solo mostrar texto si está seleccionado
                    if (selectedIndex == 0) {
                        Text(
                            text = stringResource(R.string.nav_clients), // ✅ UPDATED
                            color = MaterialTheme.tsColors.ledCyan,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            alwaysShowLabel = false,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.tsColors.primaryText,
                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                selectedTextColor = MaterialTheme.tsColors.primaryText,
                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                indicatorColor = Color.Transparent
            )
        )

        // Rutinas
        NavigationBarItem(
            selected = selectedIndex == 1,
            onClick = { onItemClick(1) },
            icon = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy((-1).dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.routines_icon),
                        contentDescription = stringResource(R.string.nav_routines),
                        tint = if (selectedIndex == 1)
                            MaterialTheme.tsColors.ledCyan
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(TraiScoreTheme.dimens.iconSizeSmall)
                    )
                    // Solo mostrar texto si está seleccionado
                    if (selectedIndex == 1) {
                        Text(
                            text = stringResource(R.string.nav_routines), // ✅ UPDATED
                            color = MaterialTheme.tsColors.ledCyan,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            alwaysShowLabel = false,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.tsColors.primaryText,
                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                selectedTextColor = MaterialTheme.tsColors.primaryText,
                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                indicatorColor = Color.Transparent
            )
        )

        // Profile
        NavigationBarItem(
            selected = selectedIndex == 2,
            onClick = { onItemClick(2) },
            icon = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy((-1).dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = stringResource(R.string.nav_profile),
                        tint = if (selectedIndex == 2)
                            MaterialTheme.tsColors.ledCyan
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(TraiScoreTheme.dimens.iconSizeSmall)
                    )
                    // Solo mostrar texto si está seleccionado
                    if (selectedIndex == 2) {
                        Text(
                            text = stringResource(R.string.nav_profile), // ✅ UPDATED
                            color = MaterialTheme.tsColors.ledCyan,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            alwaysShowLabel = false,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.tsColors.primaryText,
                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                selectedTextColor = MaterialTheme.tsColors.primaryText,
                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                indicatorColor = Color.Transparent
            )
        )
    }
}