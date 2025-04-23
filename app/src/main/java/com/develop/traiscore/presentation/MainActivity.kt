package com.develop.traiscore.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import com.develop.traiscore.presentation.navigation.NavigationRoutes
import com.develop.traiscore.presentation.screens.CreateRoutineScreen
import com.develop.traiscore.presentation.screens.LoginScreen
import com.develop.traiscore.presentation.theme.TraiScoreTheme
import com.google.firebase.FirebaseApp
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        installSplashScreen()

        FirebaseApp.initializeApp(this)

        setContent {
            //Creamos esta variable
            val windowSize = calculateWindowSizeClass(this)
            //Insertamos la propiedad aqui
            TraiScoreTheme (
                windowSize = windowSize.widthSizeClass
            ){
                val navController = rememberNavController()
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    AppNavigation(navController)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController){
    NavHost(
        navController = navController,
        startDestination = NavigationRoutes.Login.route
    ){
        composable(NavigationRoutes.Login.route){
            LoginScreen(onLoginClicked = {
                navController.navigate(NavigationRoutes.Main.route){
                    popUpTo(NavigationRoutes.Login.route){ inclusive = true}
                }
            })
        }
        composable(NavigationRoutes.Main.route) {
            MainScreen(navController = navController)
        }
        composable(NavigationRoutes.CreateRoutine.route) {
            CreateRoutineScreen(
                onBack = { navController.popBackStack() },
                navController = navController
            )
        }

    }
}
