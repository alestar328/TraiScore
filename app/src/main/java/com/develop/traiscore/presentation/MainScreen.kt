package com.develop.traiscore.presentation

import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.develop.traiscore.core.UserRole
import com.develop.traiscore.data.Authentication.UserRoleManager
import com.develop.traiscore.presentation.components.NavItem
import com.develop.traiscore.presentation.screens.AddExerciseBottomSheet
import com.develop.traiscore.presentation.screens.BodyMeasurementsHistoryScreen
import com.develop.traiscore.presentation.screens.BodyMeasurementsScreen
import com.develop.traiscore.presentation.screens.CreateRoutineScreen
import com.develop.traiscore.presentation.screens.ExercisesScreen
import com.develop.traiscore.presentation.screens.ProfileScreen
import com.develop.traiscore.presentation.screens.RoutineMenuScreen
import com.develop.traiscore.presentation.screens.RoutineScreen
import com.develop.traiscore.presentation.screens.StatScreen
import com.develop.traiscore.presentation.theme.TraiScoreTheme
import com.develop.traiscore.presentation.theme.navbarDay
import com.develop.traiscore.presentation.theme.primaryBlack
import com.develop.traiscore.presentation.theme.traiBlue
import com.develop.traiscore.presentation.viewmodels.ExercisesScreenViewModel
import com.develop.traiscore.presentation.viewmodels.RoutineViewModel
import com.google.firebase.auth.FirebaseAuth


@OptIn(ExperimentalMaterial3Api::class)
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
    var selectedIndex by remember { mutableIntStateOf(0) }
    var isBottomSheetVisible by remember { mutableStateOf(false) } // ✅ CAMBIO: de isDialogVisible a isBottomSheetVisible

    val showNavBar = !(selectedIndex == 4 &&
            (routineScreenState is ScreenState.BODY_MEASUREMENTS_SCREEN ||
                    routineScreenState is ScreenState.MEASUREMENTS_HISTORY_SCREEN))

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var currentUserRole by remember { mutableStateOf<UserRole?>(null) }

    LaunchedEffect(Unit) {
        UserRoleManager.getCurrentUserRole { role ->
            currentUserRole = role
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showNavBar) {
                NavigationBar(
                    modifier = Modifier.height(100.dp),
                    containerColor = navbarDay,
                    contentColor = Color.Black
                ) {
                    navItemList.forEachIndexed { index, navItem ->
                        NavigationBarItem(
                            selected = selectedIndex == index,
                            onClick = {
                                if (index == 2) {
                                    isBottomSheetVisible = true // ✅ CAMBIO: Mostrar bottom sheet
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
            onMeasurementsHistoryClick = { // ← NUEVA FUNCIÓN
                routineScreenState = ScreenState.MEASUREMENTS_HISTORY_SCREEN
            },
            onEditMeasurementFromHistory = {
                routineScreenState = ScreenState.BODY_MEASUREMENTS_SCREEN
            },
            routineViewModel = routineViewModel, // Pass the viewModel here
            currentUserRole = currentUserRole

        )

    }
    AddExerciseBottomSheet(
        isVisible = isBottomSheetVisible,
        onDismiss = { isBottomSheetVisible = false },
        onSave = { updated ->
            println("🔧 Datos actualizados: $updated")
            isBottomSheetVisible = false
        }
    )
}

sealed class ScreenState {
    object MAIN_ROUTINE_MENU : ScreenState()
    data class FIREBASE_ROUTINE_SCREEN(val documentId: String, val selectedType: String) :
        ScreenState()

    object CREATE_ROUTINE_SCREEN : ScreenState()
    object BODY_MEASUREMENTS_SCREEN : ScreenState()
    object MEASUREMENTS_HISTORY_SCREEN : ScreenState() // ← NUEVO ESTADO
}

@Composable
fun ContentScreen(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    navController: NavHostController,
    exeScreenViewModel: ExercisesScreenViewModel,
    routineScreenState: ScreenState,
    onRoutineSelected: (String, String) -> Unit,
    onBackToRoutineMenu: () -> Unit,
    onCreateRoutine: () -> Unit,
    onMeasurementsClick: () -> Unit,
    onMeasurementsHistoryClick: () -> Unit,
    onEditMeasurementFromHistory: () -> Unit, // ← NUEVO PARÁMETRO
    routineViewModel: RoutineViewModel,
    currentUserRole: UserRole?
) {
    when (selectedIndex) {
        0 -> ExercisesScreen()
        1 -> StatScreen()
        2 -> Text("Pantalla Add (opcional)")
        3 -> {
            when (routineScreenState) {
                is ScreenState.MAIN_ROUTINE_MENU -> RoutineMenuScreen(
                    onRoutineClick = { docId, type ->
                        onRoutineSelected(docId, type)
                    },
                    onAddClick = {
                        onCreateRoutine()
                    },
                    viewModel = routineViewModel // Pass the viewModel to RoutineMenu
                )

                is ScreenState.FIREBASE_ROUTINE_SCREEN -> {
                    currentUserRole?.let { role ->
                        RoutineScreen(
                            documentId = (routineScreenState as ScreenState.FIREBASE_ROUTINE_SCREEN).documentId,
                            selectedType = (routineScreenState as ScreenState.FIREBASE_ROUTINE_SCREEN).selectedType,
                            onBack = onBackToRoutineMenu,
                            currentUserRole = role
                        )
                    } ?: run {
                        Text("Cargando…")
                    }
                }

                is ScreenState.CREATE_ROUTINE_SCREEN -> {
                    // Solo mostrar si tenemos el rol del usuario
                    currentUserRole?.let { role ->
                        CreateRoutineScreen(
                            onBack = onBackToRoutineMenu,
                            navController = navController,
                            currentUserRole = role // Pasar el rol aquí
                        )
                    } ?: run {
                        // Mostrar loading mientras se obtiene el rol
                        Text("Cargando...")
                    }
                }

                else -> Unit
            }
        }

        4 -> {
            when (routineScreenState) {
                is ScreenState.BODY_MEASUREMENTS_SCREEN -> BodyMeasurementsScreen(
                    onBack = onBackToRoutineMenu,
                    onSave = { gender, data ->
                        println("Guardar gender=$gender, medidas=$data")
                        onBackToRoutineMenu()
                    },
                    // ✅ AÑADIR PARÁMETROS FALTANTES
                    onMeasurementsClick = onMeasurementsClick,
                    onMeasurementsHistoryClick = onMeasurementsHistoryClick
                )

                is ScreenState.MEASUREMENTS_HISTORY_SCREEN -> BodyMeasurementsHistoryScreen(
                    onBack = onBackToRoutineMenu,
                    onEditMeasurement = { historyItem ->
                        onEditMeasurementFromHistory()
                    }
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