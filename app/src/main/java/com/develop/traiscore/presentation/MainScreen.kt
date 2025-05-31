package com.develop.traiscore.presentation

import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.develop.traiscore.core.UserRole
import com.develop.traiscore.data.Authentication.UserRoleManager
import com.develop.traiscore.presentation.components.NavItem
import com.develop.traiscore.presentation.navigation.BottomNavigationBar
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
    var isBottomSheetVisible by remember { mutableStateOf(false) }
    var currentUserRole by remember { mutableStateOf<UserRole?>(null) }

    val showNavBar = when (currentUserRole) {
        UserRole.CLIENT -> !(selectedIndex == 4 &&
                (routineScreenState is ScreenState.BODY_MEASUREMENTS_SCREEN ||
                        routineScreenState is ScreenState.MEASUREMENTS_HISTORY_SCREEN))
        UserRole.TRAINER -> true // Siempre mostrar navbar para trainers
        null -> false // No mostrar mientras se carga el rol
    }

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(Unit) {
        UserRoleManager.getCurrentUserRole { role ->
            currentUserRole = role
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showNavBar) {
                when (currentUserRole) {
                    UserRole.TRAINER -> {
                        TrainerBottomNavigationBar(
                            selectedIndex = selectedIndex,
                            onItemClick = { index ->
                                selectedIndex = index
                            }
                        )
                    }
                    UserRole.CLIENT -> {
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
                    null -> {
                        // Mientras se carga el rol, no mostramos navbar
                    }
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
            routineViewModel = routineViewModel,
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
    routineViewModel: RoutineViewModel,
    currentUserRole: UserRole?
) {
    // Para TRAINER
    if (currentUserRole == UserRole.TRAINER) {
        when (selectedIndex) {
            0 -> MyClients(
                onClientClick = { client ->
                    // Aquí puedes navegar al perfil del cliente o rutinas del cliente
                    println("Cliente seleccionado: ${client.getFullName()}")
                },
                onAddClientClick = {
                    // Navegar a pantalla de agregar cliente
                    println("Agregar nuevo cliente")
                },
                onInvitationsClick = {
                    navController.navigate("trainer_invitations")
                }
            )
            1 -> {
                when (routineScreenState) {
                    is ScreenState.MAIN_ROUTINE_MENU -> RoutineMenuScreen(
                        onRoutineClick = { docId, type ->
                            onRoutineSelected(docId, type)
                        },
                        onAddClick = {
                            onCreateRoutine()
                        },
                        viewModel = routineViewModel
                    )

                    is ScreenState.FIREBASE_ROUTINE_SCREEN -> {
                        RoutineScreen(
                            documentId = (routineScreenState as ScreenState.FIREBASE_ROUTINE_SCREEN).documentId,
                            selectedType = (routineScreenState as ScreenState.FIREBASE_ROUTINE_SCREEN).selectedType,
                            onBack = onBackToRoutineMenu,
                            currentUserRole = currentUserRole
                        )
                    }

                    is ScreenState.CREATE_ROUTINE_SCREEN -> {
                        CreateRoutineScreen(
                            onBack = onBackToRoutineMenu,
                            navController = navController,
                            currentUserRole = currentUserRole
                        )
                    }

                    else -> Unit
                }
            }
            2 -> ProfileScreen(
                navController = navController,
                onMeasurementsClick = onMeasurementsClick
            )
        }
        return
    }

    // Para CLIENT (código existente)
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
                    viewModel = routineViewModel
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
                    currentUserRole?.let { role ->
                        CreateRoutineScreen(
                            onBack = onBackToRoutineMenu,
                            navController = navController,
                            currentUserRole = role
                        )
                    } ?: run {
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