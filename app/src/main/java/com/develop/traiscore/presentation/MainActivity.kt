package com.develop.traiscore.presentation

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.develop.traiscore.R
import com.develop.traiscore.core.UserRole
import com.develop.traiscore.data.Authentication.UserRoleManager
import com.develop.traiscore.exports.ImportRoutineViewModel
import com.develop.traiscore.presentation.navigation.NavigationRoutes
import com.develop.traiscore.presentation.screens.BodyMeasurementsHistoryScreen
import com.develop.traiscore.presentation.screens.BodyMeasurementsScreen
import com.develop.traiscore.presentation.screens.CameraGalleryScreen
import com.develop.traiscore.presentation.screens.ClientProfileScreen
import com.develop.traiscore.presentation.screens.CreateCategoryUI
import com.develop.traiscore.presentation.screens.CreateRoutineScreen
import com.develop.traiscore.presentation.screens.LanguageScreenUI
import com.develop.traiscore.presentation.theme.TraiScoreTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import com.develop.traiscore.presentation.screens.LoginScreenRoute
import com.develop.traiscore.presentation.screens.RoutineMenuScreen
import com.develop.traiscore.presentation.screens.RoutineScreen
import com.develop.traiscore.presentation.screens.ScreenModeUI
import com.develop.traiscore.presentation.screens.SettingsScreen
import com.develop.traiscore.presentation.screens.StatScreen
import com.develop.traiscore.presentation.viewmodels.BodyStatsViewModel
import com.develop.traiscore.presentation.viewmodels.MyClientsViewModel
import com.develop.traiscore.presentation.viewmodels.RoutineViewModel
import com.develop.traiscore.presentation.viewmodels.StatScreenViewModel
import com.develop.traiscore.presentation.viewmodels.ThemeViewModel
import com.develop.traiscore.utils.LocaleManager
import java.io.File
import java.util.Locale


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    lateinit var googleSignInClient: GoogleSignInClient
    private val importViewModel: ImportRoutineViewModel by viewModels()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val savedLanguage = LocaleManager.getLanguage(this)
        LocaleManager.applyLanguage(this, savedLanguage)

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

            val themeViewModel: ThemeViewModel = hiltViewModel()
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

            //Insertamos la propiedad aqui
            TraiScoreTheme(
                darkTheme = isDarkTheme,
                dynamicColor = false,
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
    override fun attachBaseContext(newBase: Context?) {
        val savedLanguage = newBase?.let { LocaleManager.getLanguage(it) } ?: "es"
        val locale = Locale(savedLanguage)
        Locale.setDefault(locale)

        val config = Configuration(newBase?.resources?.configuration)
        config.setLocale(locale)

        super.attachBaseContext(newBase?.createConfigurationContext(config))
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }


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

        Log.d("MainActivity", "=== FILE VALIDATION DEBUG ===")
        Log.d("MainActivity", "URI: $uri")
        Log.d("MainActivity", "File name: $fileName")

        // NO confiar solo en la extensión - WhatsApp puede cambiar nombres de archivo
        // En su lugar, validar SIEMPRE el contenido
        return validateTraiScoreContent(uri)
    }

    // NUEVO: Validar contenido del archivo
    private fun validateTraiScoreContent(uri: Uri): Boolean {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val content = inputStream?.bufferedReader()?.use { it.readText() }

            Log.d("MainActivity", "=== CONTENT VALIDATION DEBUG ===")
            Log.d("MainActivity", "Content length: ${content?.length}")
            Log.d("MainActivity", "Content preview: ${content?.take(300)}...")

            if (content.isNullOrBlank()) {
                Log.w("MainActivity", "Content is null or blank")
                return false
            }

            // Primero verificar si es JSON válido
            val looksLikeJson = content.trim().startsWith("{") && content.trim().endsWith("}")
            Log.d("MainActivity", "Looks like JSON: $looksLikeJson")

            if (!looksLikeJson) {
                Log.w("MainActivity", "Content doesn't look like JSON")
                return false
            }

            // Verificar campos específicos de TraiScore usando Gson para más precisión
            return try {
                val gson = com.google.gson.Gson()
                val jsonObject = gson.fromJson(content, com.google.gson.JsonObject::class.java)

                val hasFileType = jsonObject?.has("fileType") == true &&
                        jsonObject.get("fileType")?.asString == "TraiScore_Routine"
                val hasRoutineName = jsonObject?.has("routineName") == true
                val hasSections = jsonObject?.has("sections") == true
                val hasAppVersion = jsonObject?.has("appVersion") == true

                Log.d("MainActivity", "GSON Validation checks:")
                Log.d("MainActivity", "- fileType = TraiScore_Routine: $hasFileType")
                Log.d("MainActivity", "- has routineName: $hasRoutineName")
                Log.d("MainActivity", "- has sections: $hasSections")
                Log.d("MainActivity", "- has appVersion: $hasAppVersion")

                val isValid = hasFileType && hasRoutineName && hasSections && hasAppVersion
                Log.d("MainActivity", "Final validation result: $isValid")

                isValid

            } catch (jsonException: Exception) {
                Log.e("MainActivity", "JSON parsing failed", jsonException)
                false
            }

        } catch (e: Exception) {
            Log.e("MainActivity", "Error validating content", e)
            false
        }
    }

    private fun showImportDialog(uri: Uri) {
        val fileName = getFileName(uri) ?: "rutina_traiscore.json"

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
        builder.setMessage("El archivo seleccionado no es una rutina válida de TraiScore.\n\n📋 Solo puedes importar rutinas en formato JSON exportadas desde TraiScore.\n\n💡 Los archivos válidos tienen nombres como 'TraiScore_[nombre]_[fecha].json'")
        builder.setPositiveButton("Entendido") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

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
    val context = LocalContext.current

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
                }

            )
        }
        composable(
            "social_media_camera?exercise={exercise}&exerciseMaxReps={exerciseMaxReps}&oneRepMax={oneRepMax}&maxReps={maxReps}&totalWeight={totalWeight}&trainingDays={trainingDays}"
        ) { backStackEntry ->
            CameraGalleryScreen(
                exerciseName = backStackEntry.arguments?.getString("exercise") ?: "",
                exerciseNameMaxReps = backStackEntry.arguments?.getString("exerciseMaxReps") ?: "", // NUEVO
                oneRepMax = backStackEntry.arguments?.getString("oneRepMax")?.toFloatOrNull() ?: 0f,
                maxReps = backStackEntry.arguments?.getString("maxReps")?.toIntOrNull() ?: 0,
                totalWeight = backStackEntry.arguments?.getString("totalWeight")?.toDoubleOrNull() ?: 0.0,
                trainingDays = backStackEntry.arguments?.getString("trainingDays")?.toIntOrNull() ?: 0,
                navController = navController
            )
        }
        composable("screen_mode") {
            ScreenModeUI(
                onBack = { navController.popBackStack() }
            )
        }
        composable("createCategory") {
            CreateCategoryUI(
                onBack = { navController.popBackStack() },
                onSave = { name, iconId, color ->
                    navController.popBackStack()
                }
            )
        }
        composable(NavigationRoutes.Language.route) {
            LanguageScreenUI(
                onBack = {
                    navController.popBackStack()
                },
                onLanguageChanged = { languageCode ->
                    // Guardar idioma y reiniciar app
                    LocaleManager.setLanguage(context, languageCode)

                    // Reiniciar la app completamente
                    val intent = Intent(context, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    (context as? ComponentActivity)?.finish()
                }
            )
        }
        composable(NavigationRoutes.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToScreenMode = { navController.navigate("screen_mode") },
                onNavigateToCreateCategory = {
                    navController.navigate("createCategory") // ✅ O tu ruta
                },
                onNavigateToLanguage = {
                    navController.navigate(NavigationRoutes.Language.route)
                }

            )
        }
        composable(NavigationRoutes.Main.route) {
            MainScreen(navController = navController)
        }

        composable(NavigationRoutes.MeasurementsHistory.route) {
            BodyMeasurementsHistoryScreen(
                onBack = { navController.popBackStack() },
                onEditMeasurement = { historyItem ->
                    navController.navigate(NavigationRoutes.Measurements.route)
                }
            )
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
                    navController = navController
                )
            } ?: run {
                Text("Cargando...")
            }
        }

        composable(
            route = NavigationRoutes.ClientProfile.route,
            arguments = listOf(navArgument("clientId") { type = NavType.StringType })
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getString("clientId") ?: return@composable

            // Aquí necesitarías obtener los datos del cliente
            // Por simplicidad, puedes usar un ViewModel o pasarlos como argumentos serializados
            LaunchedEffect(clientId) {
                // Cargar datos del cliente
            }

            // Por ahora, crear un UserEntity dummy o cargarlo del ViewModel
            // ClientProfileScreen(
            //     client = client,
            //     onBack = { navController.popBackStack() },
            //     onStatsClick = { clientId ->
            //         navController.navigate(NavigationRoutes.ClientStats.createRoute(clientId))
            //     }
            // )
        }

        composable(
            route = NavigationRoutes.ClientStats.route,
            arguments = listOf(navArgument("clientId") { type = NavType.StringType })
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getString("clientId") ?: return@composable

            // Usar StatScreen pero configurado para mostrar datos del cliente
            StatScreen(
                modifier = Modifier,
                navController = navController,
                viewModel = hiltViewModel<StatScreenViewModel>().apply {
                    setTargetUser(clientId) // Configurar para mostrar datos del cliente específico
                }
            )
        }

        composable(NavigationRoutes.Measurements.route) {
            BodyMeasurementsScreen(
                onBack = { navController.popBackStack() },
                onSave = { gender, data ->
                    navController.popBackStack()
                },
                initialData = emptyMap(),
                onMeasurementsClick = { },
                onMeasurementsHistoryClick = {
                    navController.navigate(NavigationRoutes.MeasurementsHistory.route)
                }
            )
        }
        composable(NavigationRoutes.TrainerInvitations.route) {
            com.develop.traiscore.presentation.screens.TrainerInvitationsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(NavigationRoutes.EnterInvitation.route) {
            com.develop.traiscore.presentation.screens.EnterInvitationScreen(
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.navigate(NavigationRoutes.Main.route) {
                        popUpTo(NavigationRoutes.EnterInvitation.route) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = "client_profile/{clientId}",
            arguments = listOf(navArgument("clientId") { type = NavType.StringType })
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getString("clientId") ?: return@composable

            val myClientsViewModel: MyClientsViewModel = hiltViewModel()
            val clients by myClientsViewModel.clients.collectAsState()
            val context = LocalContext.current // ✅ AGREGAR para mostrar Toast

            val client = clients.find { it.uid == clientId }

            if (client != null) {
                ClientProfileScreen(
                    client = client,
                    onBack = { navController.popBackStack() },
                    onStatsClick = { clientUid ->
                        navController.navigate("client_stats/$clientUid")
                    },
                    onMeasurementsClick = { clientUid ->
                        navController.navigate("client_measurements_history/$clientUid")
                    },
                    onRoutinesClick = { clientUid ->
                        navController.navigate("client_routines/$clientUid")
                    },
                    onRemoveClient = { clientUid, onSuccess -> // ✅ NUEVO CALLBACK
                        myClientsViewModel.removeClient(clientUid) { success, error ->
                            if (success) {
                                Toast.makeText(
                                    context,
                                    "Cliente dado de baja exitosamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onSuccess() // Ejecutar callback de éxito (navegar atrás)
                            } else {
                                Toast.makeText(
                                    context,
                                    error ?: "Error al dar de baja cliente",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        composable(
            route = "client_routines/{clientId}",
            arguments = listOf(navArgument("clientId") { type = NavType.StringType })
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getString("clientId") ?: return@composable

            val routineViewModel: RoutineViewModel = hiltViewModel()
            val myClientsViewModel: MyClientsViewModel = hiltViewModel()
            val clients by myClientsViewModel.clients.collectAsState()
            val context = LocalContext.current // ✅ MOVER FUERA del LaunchedEffect

            // Buscar el nombre del cliente
            val client = clients.find { it.uid == clientId }
            val clientName = client?.getFullName() ?: "Cliente"

            // Configurar el ViewModel para cargar rutinas del cliente específico
            LaunchedEffect(clientId) {
                routineViewModel.setTargetClient(clientId)
                // ✅ USAR context que está fuera del LaunchedEffect
                routineViewModel.loadRoutines(context = context) { }
            }

            // Limpiar cuando se sale de la pantalla
            DisposableEffect(Unit) {
                onDispose {
                    routineViewModel.clearTargetClient()
                }
            }

            // Usar RoutineMenuScreen con título personalizado
            RoutineMenuScreen(
                onRoutineClick = { docId, type ->
                    navController.navigate("client_routine_detail/$clientId/$docId/$type")
                },
                onAddClick = {
                    navController.navigate("create_routine_for_client/$clientId")
                },
                viewModel = routineViewModel,
                screenTitle = "Rutinas del Cliente",
                clientName = clientName
            )
        }


        composable(
            route = "client_routine_detail/{clientId}/{docId}/{type}",
            arguments = listOf(
                navArgument("clientId") { type = NavType.StringType },
                navArgument("docId") { type = NavType.StringType },
                navArgument("type") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getString("clientId") ?: return@composable
            val docId = backStackEntry.arguments?.getString("docId") ?: return@composable
            val type = backStackEntry.arguments?.getString("type") ?: return@composable

            // ✅ CORRECCIÓN: Usar interpolación de strings correctamente
            val routineViewModel: RoutineViewModel = hiltViewModel(
                key = "client_routine_${clientId}_$docId" // ✅ Usar ${} para interpolación
            )

            // Configurar el ViewModel para el cliente específico
            LaunchedEffect(clientId) {
                routineViewModel.setTargetClient(clientId)
            }

            // ✅ LIMPIAR cuando se sale de la pantalla
            DisposableEffect(Unit) {
                onDispose {
                    routineViewModel.clearTargetClient()
                }
            }

            RoutineScreen(
                documentId = docId,
                selectedType = type,
                onBack = { navController.popBackStack() },
                routineViewModel = routineViewModel
            )
        }
        composable(
            route = "create_routine_for_client/{clientId}",
            arguments = listOf(navArgument("clientId") { type = NavType.StringType })
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getString("clientId") ?: return@composable

            val myClientsViewModel: MyClientsViewModel = hiltViewModel()
            val clients by myClientsViewModel.clients.collectAsState()

            // Buscar el nombre del cliente
            val client = clients.find { it.uid == clientId }
            val clientName = client?.getFullName() ?: "Cliente"

            CreateRoutineScreen(
                onBack = { navController.popBackStack() },
                navController = navController,
                targetClientId = clientId, // ✅ PASAR el ID del cliente
                clientName = clientName // ✅ PASAR el nombre del cliente
            )
        }
        composable(
            route = "client_measurements_history/{clientId}",
            arguments = listOf(navArgument("clientId") { type = NavType.StringType })
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getString("clientId") ?: return@composable

            // Configurar BodyMeasurementsHistoryScreen para mostrar datos del cliente específico
            BodyMeasurementsHistoryScreen(
                onBack = { navController.popBackStack() },
                onEditMeasurement = { historyItem ->
                    // Optionalmente navegar a editar medidas del cliente
                    navController.navigate(NavigationRoutes.Measurements.route)
                },
                bodyStatsViewModel = hiltViewModel<BodyStatsViewModel>().apply {
                    // ✅ IMPORTANTE: Configurar el ViewModel para cargar datos del cliente específico
                    // Necesitarás agregar un método setTargetUser en BodyStatsViewModel
                    setTargetUser(clientId)
                }
            )
        }
        composable(
            route = "client_stats/{clientId}",
            arguments = listOf(navArgument("clientId") { type = NavType.StringType })
        ) { backStackEntry ->
            val clientId = backStackEntry.arguments?.getString("clientId") ?: return@composable

            val statViewModel: StatScreenViewModel = hiltViewModel()
            val bodyStatsViewModel: BodyStatsViewModel = hiltViewModel() // ✅ AGREGAR este ViewModel

            // Configurar ambos ViewModels para mostrar datos del cliente
            LaunchedEffect(clientId) {
                statViewModel.setTargetUser(clientId)
                bodyStatsViewModel.setTargetUser(clientId) // ✅ AGREGAR configuración
            }

            // ✅ PASAR ambos ViewModels y el clientId
            StatScreen(
                viewModel = statViewModel,
                bodyStatsViewModel = bodyStatsViewModel,
                clientId = clientId,
                navController = navController // ← AGREGAR ESTO
            )
        }


    }
}
