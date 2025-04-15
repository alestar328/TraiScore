package com.develop.traiscore.presentation.navigation

sealed class NavigationRoutes(val route: String) {
    object Login : NavigationRoutes("login")
    object Main : NavigationRoutes("main")
    object Routines : NavigationRoutes("routines")
}