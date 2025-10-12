package com.develop.traiscore.presentation.navigation

sealed class NavigationRoutes(val route: String) {
    object Login : NavigationRoutes("login")
    object Settings : NavigationRoutes("settings")
    object Main : NavigationRoutes("main")
    object Exercises : NavigationRoutes("exercises")
    object Stats : NavigationRoutes("stats")
    object Routines : NavigationRoutes("routines")
    object Profile : NavigationRoutes("profile")
    object CreateRoutine : NavigationRoutes("createroutine")
    object Register : NavigationRoutes("register")
    object Measurements : NavigationRoutes("measurements")
    object MeasurementsHistory : NavigationRoutes("measurements_history")
    object TrainerInvitations : NavigationRoutes("trainer_invitations")
    object EnterInvitation : NavigationRoutes("enter_invitation")
    object Language : NavigationRoutes("language")
    object Pricing : NavigationRoutes("pricing")
    object MyExercises : NavigationRoutes("my_exercises")
    object MyHealthWithIA : NavigationRoutes("my_health_ia")
    object CameraScan : NavigationRoutes("camera_scan")
    object PhotoPreviewTemp : NavigationRoutes("photo_preview_temp?uri={uri}") {
        fun createRoute(uri: String) = "photo_preview_temp?uri=$uri"
    }
    object LabResults : NavigationRoutes("lab_results")

    object ClientProfile : NavigationRoutes("client_profile/{clientId}") {
        fun createRoute(clientId: String) = "client_profile/$clientId"
    }
    object MeasurementsEdit : NavigationRoutes("measurements_edit/{documentId}") {
        fun createRoute(documentId: String) = "measurements_edit/$documentId"
    }

    object ClientStats : NavigationRoutes("client_stats/{clientId}") {
        fun createRoute(clientId: String) = "client_stats/$clientId"
    }

    object ClientMeasurementsHistory : NavigationRoutes("client_measurements_history/{clientId}") {
        fun createRoute(clientId: String) = "client_measurements_history/$clientId"
    }
    object ClientRoutines : NavigationRoutes("client_routines/{clientId}") {
        fun createRoute(clientId: String) = "client_routines/$clientId"
    }
    object MedicalHistory : NavigationRoutes("medical_history")


}