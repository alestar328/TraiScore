package com.develop.traiscore.presentation

import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.develop.traiscore.presentation.components.NavItem
import com.develop.traiscore.presentation.navigation.NavigationRoutes
import com.develop.traiscore.presentation.screens.AddExerciseDialogContent
import com.develop.traiscore.presentation.screens.BodyMeasurementsScreen
import com.develop.traiscore.presentation.screens.CreateRoutineScreen
import com.develop.traiscore.presentation.screens.ExercisesScreen
import com.develop.traiscore.presentation.screens.FirebaseRoutineScreen
import com.develop.traiscore.presentation.screens.ProfileScreen
import com.develop.traiscore.presentation.screens.RoutineMenu
import com.develop.traiscore.presentation.screens.SettingsScreen
import com.develop.traiscore.presentation.screens.StatScreen
import com.develop.traiscore.presentation.theme.TraiScoreTheme
import com.develop.traiscore.presentation.theme.navbarDay
import com.develop.traiscore.presentation.theme.primaryBlack
import com.develop.traiscore.presentation.theme.primaryWhite
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.viewmodels.ExercisesScreenViewModel
import com.develop.traiscore.presentation.viewmodels.RoutineViewModel


@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    exeScreenViewModel: ExercisesScreenViewModel = hiltViewModel(),
    routineViewModel: RoutineViewModel = hiltViewModel()
) {
    val navItemList = listOf(
        NavItem(
            "Ejercicio",
            painter = painterResource(id = R.drawable.routine_icon),
            badgeCount = 0
        ),
        NavItem("Stats", painter = painterResource(id = R.drawable.stats_icon), badgeCount = 0),
        NavItem("Add", painter = painterResource(id = R.drawable.plus_icon), badgeCount = 0),
        NavItem("Rutina", painter = painterResource(id = R.drawable.pesa_icon), badgeCount = 0),
        NavItem("Profile", imageVector = Icons.Default.Person, badgeCount = 0)

    )
    var routineScreenState by remember { mutableStateOf<ScreenState>(ScreenState.MAIN_ROUTINE_MENU) }

    var selectedIndex by remember {
        mutableIntStateOf(0)
    }
    var isDialogVisible by remember {
        mutableStateOf(false)
    }

    Scaffold(modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.height(100.dp),
                containerColor = navbarDay, // Fondo de la barra de navegación
                contentColor = Color.Black // Color por defecto de los íconos
            ) {
                navItemList.forEachIndexed { index, navItem ->

                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            if (index == 2) {
                                isDialogVisible = true
                            } else {
                                selectedIndex = index
                            }
                        },
                        icon = {
                            if (navItem.imageVector != null) {
                                Icon(
                                    imageVector = navItem.imageVector,
                                    contentDescription = "Icon",
                                    tint = if (selectedIndex == index) traiBlue
                                    else primaryBlack
                                )
                            } else if (navItem.painter != null) {
                                Icon(
                                    painter = navItem.painter,
                                    contentDescription = "Icon",
                                    tint = if (selectedIndex == index) traiBlue
                                    else primaryBlack
                                )
                            }
                        },
                        label = {
                            Text(
                                text = navItem.label,
                                color = if (selectedIndex == index) traiBlue
                                else primaryBlack,
                            )
                        },
                        alwaysShowLabel = false,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = traiBlue, // Color del ícono seleccionado
                            unselectedIconColor = primaryBlack, // Color del ícono no seleccionado
                            selectedTextColor = traiBlue, // Color del texto seleccionado
                            unselectedTextColor = primaryBlack, // Color del texto no seleccionado
                            indicatorColor = Color.Transparent // Elimina el halo de selección
                        )
                    )
                }

            }
        }
    ) { innerPadding ->
        ContentScreen(
            modifier = Modifier.padding(innerPadding),
            selectedIndex = selectedIndex,
            navController = navController, // 👈 aquí estaba el error
            exeScreenViewModel = exeScreenViewModel,
            routineScreenState = routineScreenState,
            onRoutineSelected = { docId, type ->
                routineScreenState = ScreenState.FIREBASE_ROUTINE_SCREEN(docId, type)
            },
            onBackToRoutineMenu = {
                routineScreenState = ScreenState.MAIN_ROUTINE_MENU
            },
            onCreateRoutine = {
                routineScreenState = ScreenState.CREATE_ROUTINE_SCREEN // ✅ estado se cambia aquí
            },
            onMeasurementsClick = {
                routineScreenState = ScreenState.BODY_MEASUREMENTS_SCREEN
            },
            routineViewModel = routineViewModel // Pass the viewModel here

        )

    }
    // Muestra el diálogo si está activo
    if (isDialogVisible) {
        Dialog(
            onDismissRequest = { isDialogVisible = false }
        ) {
            AddExerciseDialogContent(onDismiss =
            { isDialogVisible = false },
                onSave = { updated ->
                    println("🔧 Datos actualizados: $updated")
                }
            )
        }
    }
}
sealed class ScreenState {
    object MAIN_ROUTINE_MENU : ScreenState()
    data class FIREBASE_ROUTINE_SCREEN(val documentId: String, val selectedType: String) :
        ScreenState()
    object CREATE_ROUTINE_SCREEN : ScreenState() // 👈 nuevo
    object BODY_MEASUREMENTS_SCREEN : ScreenState()

}
@Composable
fun ContentScreen(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    navController: NavHostController,
    exeScreenViewModel: ExercisesScreenViewModel,
    routineScreenState: ScreenState,
    onRoutineSelected: (String, String) -> Unit,  // <- ahora acepta docId y type
    onBackToRoutineMenu: () -> Unit,
    onCreateRoutine: () -> Unit,
    onMeasurementsClick: () -> Unit,
    routineViewModel: RoutineViewModel
) {
    when (selectedIndex) {
        0 -> ExercisesScreen()
        1 -> StatScreen()
        2 -> Text("Pantalla Add (opcional)")
        3 -> {
            when (routineScreenState) {
                is ScreenState.MAIN_ROUTINE_MENU -> RoutineMenu(
                    onRoutineClick = { docId, type ->
                        onRoutineSelected(docId, type)
                    },
                    onAddClick = {
                        onCreateRoutine()
                    },
                    viewModel = routineViewModel // Pass the viewModel to RoutineMenu
                )
                is ScreenState.FIREBASE_ROUTINE_SCREEN -> FirebaseRoutineScreen(
                    documentId = (routineScreenState as ScreenState.FIREBASE_ROUTINE_SCREEN).documentId,
                    selectedType = (routineScreenState as ScreenState.FIREBASE_ROUTINE_SCREEN).selectedType,
                    onBack = onBackToRoutineMenu
                )
                is ScreenState.CREATE_ROUTINE_SCREEN -> CreateRoutineScreen(
                    onBack = onBackToRoutineMenu,
                    navController = navController
                )
                else -> Unit
            }
        }
        4 -> {
            when (routineScreenState) {
                is ScreenState.BODY_MEASUREMENTS_SCREEN -> BodyMeasurementsScreen(
                    onBack = onBackToRoutineMenu,
                    navController = navController
                )
                else -> ProfileScreen(
                    navController = navController,
                    onMeasurementsClick = onMeasurementsClick
                )
            }
        }
    }
}

@Preview(
    name = "MainScreenPreview",
    showBackground = true
)
@Composable
fun MainScreenPreview() {
    TraiScoreTheme {
        val fakeNavController = rememberNavController()
        MainScreen(navController = fakeNavController)
    }
}