package com.develop.traiscore.presentation.navigation

import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.develop.traiscore.presentation.components.NavItem
import com.develop.traiscore.presentation.theme.navbarDay
import com.develop.traiscore.presentation.theme.primaryBlack
import com.develop.traiscore.presentation.theme.traiBlue
import androidx.compose.foundation.layout.size



@Composable
fun BottomNavigationBar(
    navItemList: List<NavItem>,
    selectedIndex: Int,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.height(100.dp),
        containerColor = navbarDay,
        contentColor = Color.Black
    ) {
        navItemList.forEachIndexed { index, navItem ->
            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = { onItemClick(index) },
                icon = {
                    when {
                        navItem.imageVector != null -> {
                            Icon(
                                imageVector = navItem.imageVector,
                                contentDescription = navItem.label,
                                tint = if (selectedIndex == index) traiBlue else primaryBlack,
                                modifier = Modifier.size(24.dp) // Tamaño consistente
                            )
                        }
                        navItem.painter != null -> {
                            Icon(
                                painter = navItem.painter,
                                contentDescription = navItem.label,
                                tint = if (selectedIndex == index) traiBlue else primaryBlack,
                                modifier = Modifier.size(24.dp) // Tamaño consistente
                            )
                        }
                    }
                },
                label = {
                    Text(
                        text = navItem.label,
                        color = if (selectedIndex == index) traiBlue else primaryBlack,
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
}