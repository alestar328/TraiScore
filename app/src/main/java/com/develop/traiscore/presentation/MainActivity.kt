package com.develop.traiscore.presentation

import android.content.Context
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
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import com.develop.traiscore.R
import com.develop.traiscore.presentation.navigation.NavigationRoutes
import com.develop.traiscore.presentation.screens.BodyMeasurementsScreen
import com.develop.traiscore.presentation.screens.CreateRoutineScreen
import com.develop.traiscore.presentation.theme.TraiScoreTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.security.MessageDigest
import java.util.UUID



@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    lateinit var googleSignInClient: GoogleSignInClient
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        FirebaseApp.initializeApp(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            //Creamos esta variable
            val windowSize = calculateWindowSizeClass(this)
            //Insertamos la propiedad aqui
            TraiScoreTheme(
                windowSize = windowSize.widthSizeClass
            ) {
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
fun AppNavigation(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val startRoute = if (auth.currentUser != null) {
        NavigationRoutes.Main.route
    } else {
        NavigationRoutes.Login.route
    }
    NavHost(
        navController = navController,
        startDestination = startRoute
    ) {
        composable(NavigationRoutes.Login.route) {
            LoginScreenRoute(
                onLoginSuccess = {
                    navController.navigate(NavigationRoutes.Main.route) {
                        popUpTo(NavigationRoutes.Login.route) { inclusive = true }
                    }
                },
                onRegisterClick = {
                    // si tienes una ruta para crear cuenta, navega aquí
                    navController.navigate(NavigationRoutes.Register.route)
                }
            )
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
        composable(NavigationRoutes.Measurements.route) {
            BodyMeasurementsScreen(onBack = { navController.popBackStack() },
                navController = navController)
        }

    }
}
