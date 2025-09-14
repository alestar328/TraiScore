package com.develop.traiscore.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.develop.traiscore.core.UserRole
import com.develop.traiscore.presentation.components.NavItem
import com.develop.traiscore.presentation.navigation.BottomNavigationBar
import com.develop.traiscore.presentation.navigation.NavigationRoutes
import com.develop.traiscore.presentation.navigation.TrainerBottomNavigationBar
import com.develop.traiscore.presentation.screens.AddExerciseBottomSheet
import com.develop.traiscore.presentation.screens.BodyMeasurementsHistoryScreen
import com.develop.traiscore.presentation.screens.BodyMeasurementsScreen
import com.develop.traiscore.presentation.screens.CreateRoutineScreen
import com.develop.traiscore.presentation.screens.ExercisesScreen
import com.develop.traiscore.presentation.screens.MyClients
import com.develop.traiscore.presentation.screens.ProfileScreen
import com.develop.traiscore.presentation.screens.RoutineMenuScreen
import com.develop.traiscore.presentation.screens.RoutineScreen
import com.develop.traiscore.presentation.screens.StatScreen
import com.develop.traiscore.presentation.theme.TraiScoreTheme
import com.develop.traiscore.presentation.viewmodels.ExercisesScreenViewModel
import com.develop.traiscore.presentation.viewmodels.RoutineViewModel
import com.develop.traiscore.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    exeScreenViewModel: ExercisesScreenViewModel = hiltViewModel(),
    routineViewModel: RoutineViewModel = hiltViewModel(),
    initialSelectedIndex: Int = 0 // ✅ NUEVO PARÁMETRO

) {
    val navItemList = listOf(
        NavItem(
            stringResource(R.string.nav_exercises), // Instead of "Ejercicio"
            painter = painterResource(id = R.drawable.routine_icon),
            badgeCount = 0
        ),
        NavItem(
            stringResource(R.string.nav_stats), // Instead of "Stats"
            painter = painterResource(id = R.drawable.stats_icon),
            badgeCount = 0
        ),
        NavItem(
            stringResource(R.string.nav_add), // Instead of "Add"
            painter = painterResource(id = R.drawable.plus_icon),
            badgeCount = 0
        ),
        NavItem(
            stringResource(R.string.nav_routines), // Instead of "Rutina"
            painter = painterResource(id = R.drawable.pesa_icon),
            badgeCount = 0
        ),
        NavItem(
            stringResource(R.string.nav_profile), // Instead of "Profile"
            imageVector = Icons.Default.Person,
            badgeCount = 0
        )
    )

    var routineScreenState by remember { mutableStateOf<ScreenState>(ScreenState.MAIN_ROUTINE_MENU) }
    var selectedIndex by remember { mutableIntStateOf(initialSelectedIndex) } // ✅ USAR parámetro
    var isBottomSheetVisible by remember { mutableStateOf(false) }
    var currentUserRole by remember { mutableStateOf<UserRole?>(null) }

    val shouldShowNavBar = currentUserRole != null || true // Siempre mostrar



    LaunchedEffect(Unit) {
        // ✅ ELIMINADO: Ya no necesitamos getUserRole
        routineViewModel.clearTargetClient()
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // ✅ NUEVO: UI basada únicamente en BuildConfig.FLAVOR
            when (BuildConfig.FLAVOR) {
                "trainer" -> {
                    TrainerBottomNavigationBar(
                        selectedIndex = selectedIndex,
                        onItemClick = { index ->
                            selectedIndex = index
                        }
                    )
                }
                "athlete" -> {
                    BottomNavigationBar(
                        navItemList = navItemList,
                        selectedIndex = selectedIndex,
                        onItemClick = { index ->
                            if (index == 2) {
                                isBottomSheetVisible = true
                            } else {
                                selectedIndex = index
                            }
                        }
                    )
                }
                else -> {
                    // Default para debug o main
                    BottomNavigationBar(
                        navItemList = navItemList,
                        selectedIndex = selectedIndex,
                        onItemClick = { index ->
                            if (index == 2) {
                                isBottomSheetVisible = true
                            } else {
                                selectedIndex = index
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        ContentScreen(
            modifier = Modifier.padding(innerPadding),
            selectedIndex = selectedIndex,
            navController = navController,
            exeScreenViewModel = exeScreenViewModel,
            routineScreenState = routineScreenState,
            onRoutineSelected = { docId, type ->
                routineScreenState = ScreenState.FIREBASE_ROUTINE_SCREEN(docId, type)
            },
            onBackToRoutineMenu = {
                routineScreenState = ScreenState.MAIN_ROUTINE_MENU
            },
            onCreateRoutine = {
                routineScreenState = ScreenState.CREATE_ROUTINE_SCREEN
            },
            onMeasurementsClick = {
                routineScreenState = ScreenState.BODY_MEASUREMENTS_SCREEN
            },
            onMeasurementsHistoryClick = {
                routineScreenState = ScreenState.MEASUREMENTS_HISTORY_SCREEN
            },
            onEditMeasurementFromHistory = {
                routineScreenState = ScreenState.BODY_MEASUREMENTS_SCREEN
            },
            onBackToMeasurements = {  // ✅ AGREGAR ESTA LÍNEA
                routineScreenState = ScreenState.BODY_MEASUREMENTS_SCREEN
            },
            routineViewModel = routineViewModel
        )
    }

    // ✅ NUEVO: Solo mostrar AddExerciseBottomSheet en la versión athlete
    if (BuildConfig.FLAVOR == "athlete") {
        AddExerciseBottomSheet(
            isVisible = isBottomSheetVisible,
            onDismiss = { isBottomSheetVisible = false },
            onSave = { updated ->
                println("🔧 Datos actualizados: $updated")
                isBottomSheetVisible = false
            }
        )
    }
}
sealed class ScreenState {
    object MAIN_ROUTINE_MENU : ScreenState()
    data class FIREBASE_ROUTINE_SCREEN(val documentId: String, val selectedType: String) :
        ScreenState()

    object CREATE_ROUTINE_SCREEN : ScreenState()
    object BODY_MEASUREMENTS_SCREEN : ScreenState()
    object MEASUREMENTS_HISTORY_SCREEN : ScreenState()
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
    onEditMeasurementFromHistory: () -> Unit,
    onBackToMeasurements: () -> Unit,
    routineViewModel: RoutineViewModel

    // ✅ ELIMINADO: currentUserRole: UserRole?
) {
    // ✅ NUEVO: UI completamente basada en flavor
    when (BuildConfig.FLAVOR) {
        "trainer" -> {
            TrainerContent(
                selectedIndex = selectedIndex,
                navController = navController,
                routineScreenState = routineScreenState,
                onRoutineSelected = onRoutineSelected,
                onBackToRoutineMenu = onBackToRoutineMenu,
                onCreateRoutine = onCreateRoutine,
                onMeasurementsClick = onMeasurementsClick,
                onMeasurementsHistoryClick = onMeasurementsHistoryClick,
                onEditMeasurementFromHistory = onEditMeasurementFromHistory,
                onBackToMeasurements = onBackToMeasurements,
                routineViewModel = routineViewModel
            )
        }
        "athlete" -> {
            AthleteContent(
                selectedIndex = selectedIndex,
                navController = navController,
                exeScreenViewModel = exeScreenViewModel,
                routineScreenState = routineScreenState,
                onRoutineSelected = onRoutineSelected,
                onBackToRoutineMenu = onBackToRoutineMenu,
                onCreateRoutine = onCreateRoutine,
                onMeasurementsClick = onMeasurementsClick,
                onMeasurementsHistoryClick = onMeasurementsHistoryClick,
                onEditMeasurementFromHistory = onEditMeasurementFromHistory,
                onBackToMeasurements = onBackToMeasurements,
                routineViewModel = routineViewModel
            )
        }
        else -> {
            // Default para debug - mostrar UI de athlete
            AthleteContent(
                selectedIndex = selectedIndex,
                navController = navController,
                exeScreenViewModel = exeScreenViewModel,
                routineScreenState = routineScreenState,
                onRoutineSelected = onRoutineSelected,
                onBackToRoutineMenu = onBackToRoutineMenu,
                onCreateRoutine = onCreateRoutine,
                onMeasurementsClick = onMeasurementsClick,
                onMeasurementsHistoryClick = onMeasurementsHistoryClick,
                onEditMeasurementFromHistory = onEditMeasurementFromHistory,
                onBackToMeasurements = onBackToMeasurements,
                routineViewModel = routineViewModel
            )
        }
    }
}

@Composable
private fun TrainerContent(
    selectedIndex: Int,
    navController: NavHostController,
    routineScreenState: ScreenState,
    onRoutineSelected: (String, String) -> Unit,
    onBackToRoutineMenu: () -> Unit,
    onCreateRoutine: () -> Unit,
    onMeasurementsClick: () -> Unit,
    onMeasurementsHistoryClick: () -> Unit,
    onEditMeasurementFromHistory: () -> Unit,
    onBackToMeasurements: () -> Unit,
    routineViewModel: RoutineViewModel
) {
    when (selectedIndex) {
        0 -> MyClients(
            onClientClick = { client ->
                navController.navigate("client_profile/${client.uid}")
            },
            onAddClientClick = {
                println("Agregar nuevo cliente")
            },
            onInvitationsClick = {
                navController.navigate(NavigationRoutes.TrainerInvitations.route)
            }
        )
        1 -> {
            when (routineScreenState) {
                is ScreenState.MAIN_ROUTINE_MENU ->
                    RoutineMenuScreen(
                        onRoutineClick = { docId, type ->
                            onRoutineSelected(docId, type)
                        },
                        onAddClick = {
                            onCreateRoutine()
                        },
                        viewModel = routineViewModel,
                        screenTitle = "Mis Rutinas de Entrenador",
                        clientName = null
                        // ✅ ELIMINADO: userRole = UserRole.TRAINER
                    )
                is ScreenState.FIREBASE_ROUTINE_SCREEN -> {
                    RoutineScreen(
                        documentId = routineScreenState.documentId,
                        selectedType = routineScreenState.selectedType,
                        onBack = onBackToRoutineMenu
                    )
                }
                is ScreenState.CREATE_ROUTINE_SCREEN -> {
                    CreateRoutineScreen(
                        onBack = onBackToRoutineMenu,
                        navController = navController
                        // ✅ ELIMINADO: currentUserRole = UserRole.TRAINER
                    )
                }
                else -> Unit
            }
        }
        2 -> {
            // ✅ AGREGAR manejo de ScreenState para medidas también en Trainer
            when (routineScreenState) {
                is ScreenState.BODY_MEASUREMENTS_SCREEN -> BodyMeasurementsScreen(
                    onBack = onBackToRoutineMenu,
                    onSave = { gender, data ->
                        onBackToRoutineMenu()
                    },
                    onMeasurementsClick = onMeasurementsClick,
                    onMeasurementsHistoryClick = onMeasurementsHistoryClick
                )
                is ScreenState.MEASUREMENTS_HISTORY_SCREEN -> BodyMeasurementsHistoryScreen(
                    onBack = onBackToMeasurements,
                    onEditMeasurement = { historyItem ->
                        onEditMeasurementFromHistory()
                    }
                )
                else -> ProfileScreen(
                    navController = navController,
                    onMeasurementsClick = onMeasurementsClick // ✅ USAR callback
                )
            }
        }
    }
}

@Composable
private fun AthleteContent(
    selectedIndex: Int,
    navController: NavHostController,
    exeScreenViewModel: ExercisesScreenViewModel,
    routineScreenState: ScreenState,
    onRoutineSelected: (String, String) -> Unit,
    onBackToRoutineMenu: () -> Unit,
    onCreateRoutine: () -> Unit,
    onMeasurementsClick: () -> Unit,
    onMeasurementsHistoryClick: () -> Unit,
    onEditMeasurementFromHistory: () -> Unit,
    onBackToMeasurements: () -> Unit,
    routineViewModel: RoutineViewModel
) {
    when (selectedIndex) {
        0 -> ExercisesScreen(navController = navController)
        1 -> StatScreen(navController = navController)
        2 -> Text("Pantalla Add (opcional)")
        3 -> {
            when (routineScreenState) {
                is ScreenState.MAIN_ROUTINE_MENU ->
                    RoutineMenuScreen(
                        onRoutineClick = { docId, type ->
                            onRoutineSelected(docId, type)
                        },
                        onAddClick = {
                            onCreateRoutine()
                        },
                        viewModel = routineViewModel,
                        screenTitle = "Mis Rutinas",
                        clientName = null
                        // ✅ ELIMINADO: userRole = UserRole.CLIENT
                    )
                is ScreenState.FIREBASE_ROUTINE_SCREEN -> {
                    RoutineScreen(
                        documentId = routineScreenState.documentId,
                        selectedType = routineScreenState.selectedType,
                        onBack = onBackToRoutineMenu
                        // ✅ ELIMINADO: currentUserRole = UserRole.CLIENT
                    )
                }
                is ScreenState.CREATE_ROUTINE_SCREEN -> {
                    CreateRoutineScreen(
                        onBack = onBackToRoutineMenu,
                        navController = navController
                        // ✅ ELIMINADO: currentUserRole = UserRole.CLIENT
                    )
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
                    onMeasurementsClick = onMeasurementsClick,
                    onMeasurementsHistoryClick = onMeasurementsHistoryClick
                )
                is ScreenState.MEASUREMENTS_HISTORY_SCREEN -> BodyMeasurementsHistoryScreen(
                    onBack = onBackToMeasurements,
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