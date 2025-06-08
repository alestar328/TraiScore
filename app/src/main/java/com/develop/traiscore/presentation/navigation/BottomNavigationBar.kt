package com.develop.traiscore.presentation.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import com.develop.traiscore.presentation.theme.TraiScoreTheme
import com.develop.traiscore.presentation.theme.tsColors


@Composable
fun BottomNavigationBar(
    navItemList: List<NavItem>,
    selectedIndex: Int,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.height(100.dp),
        containerColor = MaterialTheme.tsColors.backgroundBottomColor,
        contentColor = MaterialTheme.tsColors.primaryText
    ) {
        navItemList.forEachIndexed { index, navItem ->
            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = { onItemClick(index) },
                icon = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy((-1).dp) // Ajusta este valor
                    ) {
                        when {
                            navItem.imageVector != null -> {
                                Icon(
                                    imageVector = navItem.imageVector,
                                    contentDescription = navItem.label,
                                    tint = if (selectedIndex == index)
                                        MaterialTheme.tsColors.ledCyan
                                    else
                                        MaterialTheme.tsColors.secondaryText,
                                    modifier = Modifier.size(TraiScoreTheme.dimens.iconSizeSmall)
                                )
                            }
                            navItem.painter != null -> {
                                Icon(
                                    painter = navItem.painter,
                                    contentDescription = navItem.label,
                                    tint = if (selectedIndex == index)
                                        MaterialTheme.tsColors.ledCyan
                                    else
                                        MaterialTheme.tsColors.secondaryText,
                                    modifier = Modifier.size(TraiScoreTheme.dimens.iconSizeSmall)
                                )
                            }
                        }
                        // Solo mostrar texto si está seleccionado
                        if (selectedIndex == index) {
                            Text(
                                text = navItem.label,
                                color = MaterialTheme.tsColors.ledCyan,
                                fontSize = 12.sp
                            )
                        }
                    }
                },

                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.tsColors.primaryText,
                    unselectedIconColor = MaterialTheme.tsColors.secondaryText,
                    selectedTextColor = MaterialTheme.tsColors.primaryText,
                    unselectedTextColor = MaterialTheme.tsColors.secondaryText,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}