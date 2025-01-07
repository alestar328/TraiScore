package com.develop.traiscore.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.develop.traiscore.R
import com.develop.traiscore.presentation.theme.primaryBlack
import com.develop.traiscore.presentation.theme.primaryWhite
import com.develop.traiscore.presentation.theme.traiBlue

@Composable
fun CustomNavigationBar(
    navItemList: List<NavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(primaryBlack)
    ) {
        // NavigationBar for regular items
        NavigationBar(
            containerColor = primaryBlack,
            contentColor = primaryWhite,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            navItemList.forEachIndexed { index, navItem ->
                if (index != 2) { // Exclude the center item (Add)
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { onItemSelected(index) },
                        icon = {
                            if (navItem.imageVector != null) {
                                Icon(
                                    imageVector = navItem.imageVector,
                                    contentDescription = "Icon",
                                    tint = if (selectedIndex == index) traiBlue
                                    else primaryWhite
                                )
                            } else if (navItem.painter != null) {
                                Icon(
                                    painter = navItem.painter,
                                    contentDescription = "Icon",
                                    tint = if (selectedIndex == index) traiBlue
                                    else primaryWhite
                                )
                            }
                        },
                        label = {
                            Text(
                                text = navItem.label,
                                color = if (selectedIndex == index) traiBlue
                                else primaryWhite
                            )
                        },
                        alwaysShowLabel = false,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = traiBlue,
                            unselectedIconColor = primaryWhite,
                            selectedTextColor = traiBlue,
                            unselectedTextColor = primaryWhite,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }

        // Floating Add Button
        FloatingActionButton(
            onClick = { onItemSelected(2) },
            containerColor = traiBlue,
            contentColor = primaryWhite,
            modifier = Modifier
                .size(72.dp) // Size of the button
                .offset(y = (-36).dp) // Elevate it above the navigation bar
                .align(Alignment.BottomCenter)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.plus_icon),
                contentDescription = "Add ExerciseEntity",
                tint = primaryWhite,
                modifier = Modifier.size(36.dp) // Icon size inside the button
            )
        }
    }
}