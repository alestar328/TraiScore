package com.develop.traiscore.presentation.navigation

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.develop.traiscore.R
import com.develop.traiscore.presentation.theme.navbarDay
import com.develop.traiscore.presentation.theme.primaryBlack
import com.develop.traiscore.presentation.theme.traiBlue

@Composable
fun TrainerBottomNavigationBar(
    selectedIndex: Int,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.height(100.dp),
        containerColor = navbarDay,
        contentColor = Color.Black
    ) {
        // Mis Clientes
        NavigationBarItem(
            selected = selectedIndex == 0,
            onClick = { onItemClick(0) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.clients_icon),
                    contentDescription = "Mis Clientes",
                    tint = if (selectedIndex == 0) traiBlue else primaryBlack,
                    modifier = Modifier.size(24.dp) // Tamaño consistente
                )
            },
            label = {
                Text(
                    text = "Mis Clientes",
                    color = if (selectedIndex == 0) traiBlue else primaryBlack,
                )
            },
            alwaysShowLabel = false,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = traiBlue,
                unselectedIconColor = primaryBlack,
                selectedTextColor = traiBlue,
                unselectedTextColor = primaryBlack,
                indicatorColor = Color.Transparent
            )
        )

        // Rutinas
        NavigationBarItem(
            selected = selectedIndex == 1,
            onClick = { onItemClick(1) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.routines_icon),
                    contentDescription = "Rutinas",
                    tint = if (selectedIndex == 1) traiBlue else primaryBlack,
                    modifier = Modifier.size(24.dp) // Tamaño consistente
                )
            },
            label = {
                Text(
                    text = "Rutinas",
                    color = if (selectedIndex == 1) traiBlue else primaryBlack,
                )
            },
            alwaysShowLabel = false,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = traiBlue,
                unselectedIconColor = primaryBlack,
                selectedTextColor = traiBlue,
                unselectedTextColor = primaryBlack,
                indicatorColor = Color.Transparent
            )
        )

        // Profile
        NavigationBarItem(
            selected = selectedIndex == 2,
            onClick = { onItemClick(2) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = if (selectedIndex == 2) traiBlue else primaryBlack,
                    modifier = Modifier.size(24.dp) // Tamaño consistente
                )
            },
            label = {
                Text(
                    text = "Profile",
                    color = if (selectedIndex == 2) traiBlue else primaryBlack,
                )
            },
            alwaysShowLabel = false,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = traiBlue,
                unselectedIconColor = primaryBlack,
                selectedTextColor = traiBlue,
                unselectedTextColor = primaryBlack,
                indicatorColor = Color.Transparent
            )
        )
    }
}