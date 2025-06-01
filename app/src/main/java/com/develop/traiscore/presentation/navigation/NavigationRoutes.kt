package com.develop.traiscore.presentation.navigation

sealed class NavigationRoutes(val route: String) {
    object Login : NavigationRoutes("login")
    object Settings : NavigationRoutes("settings")
    object Main : NavigationRoutes("main")
    object Routines : NavigationRoutes("routines")
    object CreateRoutine : NavigationRoutes("createroutine")
    object Register : NavigationRoutes("register")
    object Measurements : NavigationRoutes("measurements")
    object MeasurementsHistory : NavigationRoutes("measurements_history")
    object TrainerInvitations : NavigationRoutes("trainer_invitations")
    object EnterInvitation : NavigationRoutes("enter_invitation")

}