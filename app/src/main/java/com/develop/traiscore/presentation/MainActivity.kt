package com.develop.traiscore.presentation

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import com.develop.traiscore.R
import com.develop.traiscore.core.UserRole
import com.develop.traiscore.data.Authentication.UserRoleManager
import com.develop.traiscore.exports.ImportRoutineViewModel
import com.develop.traiscore.presentation.navigation.NavigationRoutes
import com.develop.traiscore.presentation.screens.BodyMeasurementsScreen
import com.develop.traiscore.presentation.screens.CreateRoutineScreen
import com.develop.traiscore.presentation.theme.TraiScoreTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import com.develop.traiscore.presentation.screens.LoginScreenRoute
import java.io.File


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    lateinit var googleSignInClient: GoogleSignInClient
    private val importViewModel: ImportRoutineViewModel by viewModels()

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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    AppNavigation(navController)
                }
            }
        }
        handleIncomingIntent(intent)
    }

    // OBLIGATORIO: Manejar nuevos intents cuando la app ya está abierta
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    // OBLIGATORIO: Detectar intents del sistema (VIEW/SEND)
    private fun handleIncomingIntent(intent: Intent?) {
        Log.d("MainActivity", "Handling intent: ${intent?.action}, data: ${intent?.data}")

        when (intent?.action) {
            Intent.ACTION_VIEW -> {
                // Archivo .traiscore abierto directamente
                val uri = intent.data
                if (uri != null) {
                    Log.d("MainActivity", "Received VIEW intent with URI: $uri")
                    if (isTraiScoreFile(uri)) {
                        Log.d("MainActivity", "Confirmed TraiScore file via VIEW: $uri")
                        showImportDialog(uri)
                    } else {
                        Log.d("MainActivity", "Not a TraiScore file: $uri")
                        showNotTraiScoreFileDialog()
                    }
                }
            }

            Intent.ACTION_SEND -> {
                // Archivo .traiscore compartido
                val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                if (uri != null) {
                    Log.d("MainActivity", "Received SEND intent with URI: $uri")
                    if (isTraiScoreFile(uri)) {
                        Log.d("MainActivity", "Confirmed TraiScore file via SEND: $uri")
                        showImportDialog(uri)
                    } else {
                        Log.d("MainActivity", "Not a TraiScore file: $uri")
                        showNotTraiScoreFileDialog()
                    }
                }
            }
        }
    }

    // MEJORADO: Detectar archivos .traiscore con mejor validación
    private fun isTraiScoreFile(uri: Uri): Boolean {
        val fileName = getFileName(uri)
        val isTraiScoreExtension = fileName?.endsWith(".traiscore", ignoreCase = true) == true

        Log.d("MainActivity", "Checking file: $fileName, isTraiScore: $isTraiScoreExtension")

        // Validación adicional: intentar leer el contenido para verificar estructura
        if (isTraiScoreExtension) {
            return validateTraiScoreContent(uri)
        }

        return false
    }
    // NUEVO: Validar contenido del archivo
    private fun validateTraiScoreContent(uri: Uri): Boolean {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val content = inputStream?.bufferedReader()?.use { it.readText() }

            // Verificar que contenga las claves esperadas de un archivo TraiScore
            content?.contains("\"appVersion\"") == true &&
                    content.contains("\"routineName\"") &&
                    content.contains("\"sections\"")

        } catch (e: Exception) {
            Log.e("MainActivity", "Error validating TraiScore content", e)
            false
        }
    }
    private fun showImportDialog(uri: Uri) {
        val fileName = getFileName(uri) ?: "archivo.traiscore"

        val builder = AlertDialog.Builder(this)
        builder.setTitle("📱 TraiScore - Importar Rutina")
        builder.setMessage("¿Deseas importar la rutina desde el archivo '$fileName'?\n\n💪 La rutina se agregará a tu biblioteca personal.")

        builder.setPositiveButton("✅ Sí, importar") { _, _ ->
            // Delegar la lógica al ViewModel
            importViewModel.importRoutineFromUri(
                context = this,
                uri = uri,
                onSuccess = { routineName, routineId ->
                    Toast.makeText(
                        this,
                        "🎉 ¡Rutina '$routineName' importada exitosamente!\n\nYa puedes encontrarla en tu biblioteca.",
                        Toast.LENGTH_LONG
                    ).show()
                },
                onError = { error ->
                    Toast.makeText(
                        this,
                        "❌ Error al importar: $error",
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }

        builder.setNegativeButton("❌ Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        builder.setCancelable(true)
        builder.show()
    }
    private fun showNotTraiScoreFileDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("⚠️ Archivo no compatible")
        builder.setMessage("El archivo seleccionado no es una rutina válida de TraiScore (.traiscore).\n\n📋 Solo puedes importar rutinas exportadas desde TraiScore.")
        builder.setPositiveButton("Entendido") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }
    // OBLIGATORIO: Acceso a ContentResolver
    private fun getFileName(uri: Uri): String? {
        return try {
            when (uri.scheme) {
                "file" -> File(uri.path ?: "").name
                "content" -> {
                    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (nameIndex >= 0) cursor.getString(nameIndex) else null
                        } else null
                    }
                }
                else -> null
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error getting file name", e)
            null
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
            var currentUserRole by remember { mutableStateOf<UserRole?>(null) }

            LaunchedEffect(Unit) {
                UserRoleManager.getCurrentUserRole { role ->
                    currentUserRole = role
                }
            }
            currentUserRole?.let { role ->
                CreateRoutineScreen(
                    onBack = { navController.popBackStack() },
                    navController = navController,
                    currentUserRole = role // Agregar este parámetro
                )
            } ?: run {
                Text("Cargando...")
            }
        }




        composable(NavigationRoutes.Measurements.route) {
            BodyMeasurementsScreen(
                onBack = { navController.popBackStack() },
                onSave = { gender, data ->
                    navController.popBackStack()
                },
                initialData = emptyMap() // o tus valores precargados


            )
        }

    }
}
