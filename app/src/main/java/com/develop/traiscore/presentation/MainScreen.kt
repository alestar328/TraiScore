package com.develop.traiscore.presentation

import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.develop.traiscore.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog

import com.develop.traiscore.presentation.components.NavItem
import com.develop.traiscore.presentation.screens.AddExerciseDialogContent
import com.develop.traiscore.presentation.screens.ExercisesScreen
import com.develop.traiscore.presentation.screens.LoginScreen
import com.develop.traiscore.presentation.screens.RoutineScreen
import com.develop.traiscore.presentation.screens.SettingsScreen
import com.develop.traiscore.presentation.screens.StatScreen
import com.develop.traiscore.presentation.theme.TraiScoreTheme
import com.develop.traiscore.presentation.theme.primaryBlack
import com.develop.traiscore.presentation.theme.primaryWhite
import com.develop.traiscore.presentation.theme.traiBlue


@Composable
fun MainScreen(modifier: Modifier = Modifier){

    val navItemList = listOf(
        NavItem("Ejercicio",painter = painterResource(id = R.drawable.routine_icon),badgeCount = 0),
        NavItem("Stats",painter = painterResource(id = R.drawable.stats_icon),badgeCount = 0),
        NavItem("Add",painter = painterResource(id = R.drawable.plus_icon),badgeCount = 0),
        NavItem("Rutina",painter = painterResource(id = R.drawable.pesa_icon),badgeCount = 0),
        NavItem("Settings", imageVector =  Icons.Default.Settings,badgeCount = 0)

    )

    var selectedIndex by remember{
        mutableIntStateOf(0)
    }
    var isDialogVisible by remember {
        mutableStateOf(false)
    }

    Scaffold (modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = primaryBlack, // Fondo de la barra de navegación
                contentColor = primaryWhite // Color por defecto de los íconos
            ) {
                navItemList.forEachIndexed{index, navItem ->

                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            if(index == 2){
                                isDialogVisible = true
                            }else{
                                selectedIndex = index
                            }
                        },
                        icon = {
                            if(navItem.imageVector != null) {
                                Icon(imageVector = navItem.imageVector,
                                    contentDescription = "Icon",
                                    tint = if(selectedIndex == index) traiBlue
                                    else primaryWhite)
                            }else if(navItem.painter != null){
                                Icon(painter = navItem.painter,
                                    contentDescription = "Icon",
                                    tint = if (selectedIndex == index) traiBlue
                                    else primaryWhite)
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
                            selectedIconColor = traiBlue, // Color del ícono seleccionado
                        unselectedIconColor = primaryWhite, // Color del ícono no seleccionado
                        selectedTextColor = traiBlue, // Color del texto seleccionado
                        unselectedTextColor = primaryWhite, // Color del texto no seleccionado
                        indicatorColor = Color.Transparent // Elimina el halo de selección
                        )
                    )
                }

            }
        }



    ){ innerPadding ->
        ContenteScreen(
            modifier = Modifier.padding(innerPadding),
            selectedIndex = selectedIndex
        )

    }
    // Muestra el diálogo si está activo
    if (isDialogVisible) {
        Dialog (
            onDismissRequest = {isDialogVisible = false}
        ){
            AddExerciseDialogContent(
                onSave = {name, reps, weight, rir ->
                    println("Guardado: $name, $reps, $weight, $rir")
                    isDialogVisible = false
                },
                onCancel = {
                    isDialogVisible = false
                }
            )
         }
    }
}

@Composable
fun ContenteScreen(modifier: Modifier = Modifier, selectedIndex : Int){
    when(selectedIndex){
            0-> ExercisesScreen()
            1-> StatScreen()
            2->Text("Pantalla Add (opcional)")
            3-> RoutineScreen()
            4-> SettingsScreen()
        }

}

@Preview(
    name = "MainScreenPreview",
    showBackground = true
)
@Composable
fun MainScreenPreview() {
    TraiScoreTheme {
        MainScreen()
    }
}