@file:Suppress("DEPRECATION")
// Suprime advertencias relacionadas con APIs marcadas como obsoletas.

package com.example.proyecto_movil_parcial

// Importaciones estándar de Android, Jetpack Compose, Firebase, Gson y navegación.
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.proyecto_movil_parcial.screens.InicioScreen
import com.example.proyecto_movil_parcial.navigation.BottomNavigationBar
import com.example.proyecto_movil_parcial.navigation.Screen
import com.example.proyecto_movil_parcial.screens.DesResultadosScreen
import com.example.proyecto_movil_parcial.screens.DesScreen
import com.example.proyecto_movil_parcial.screens.DicScreen
import com.example.proyecto_movil_parcial.screens.IntenScreen
import com.example.proyecto_movil_parcial.screens.NuevPaScreen
import com.example.proyecto_movil_parcial.screens.OracionDesScreen
import com.example.proyecto_movil_parcial.screens.PerfScreen
import com.example.proyecto_movil_parcial.screens.ResultaScreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// Actividad principal que sirve como punto de entrada de la aplicación.
class MainActivity : ComponentActivity() {

    // Cliente de autenticación de Google.
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    // Autenticación base de Firebase.
    private lateinit var mAuth: FirebaseAuth
    // Base de datos en la nube de Firebase (NoSQL).
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicialización de servicios de Firebase.
        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Configuración de inicio de sesión de Google con ID de cliente web.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        // Obtener usuario autenticado actual de Firebase.
        val auth = Firebase.auth
        val user = auth.currentUser
        val userName = user?.displayName ?: "Usuario"

        // Renderizado de la interfaz de usuario principal usando Jetpack Compose.
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Composable que gestiona toda la navegación de la app.
                    MainNavigationScreen(
                        userName = if (user != null) "Hola, $userName" else "Cargando..",
                        onSignOut = { signOutAndStartSignInActivity() }
                    )
                }
            }
        }
    }

    // Función que cierra sesión en Firebase y en Google, y redirige al login.
    private fun signOutAndStartSignInActivity() {
        mAuth.signOut()

        mGoogleSignInClient.signOut().addOnCompleteListener(this) {
            val intent = Intent(this@MainActivity, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}

@Composable
fun MainNavigationScreen(
    // Nombre del usuario a mostrar en pantalla.
    userName: String,
    // Callback para cerrar sesión.
    onSignOut: () -> Unit
) {
    // Controlador de navegación para gestionar rutas.
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Determina si se debe mostrar la barra de navegación inferior, según la ruta actual.
    val shouldShowBottomBar = when {
        currentRoute?.startsWith(Screen.NuevaPalabra.rout) == true ||
                currentRoute?.startsWith(Screen.Intentar.rout) == true ||
                currentRoute?.startsWith(Screen.CrearOracion.rout) == true ||
                currentRoute?.startsWith("resultado_screen") == true ||
                currentRoute?.startsWith(Screen.ResultadoOracion.rout) == true -> false
        else -> true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (shouldShowBottomBar) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Inico.rout, // Pantalla inicial al lanzar la app.
            modifier = Modifier.padding(innerPadding)
        ) {
            // Pantalla de inicio.
            composable(route = Screen.Inico.rout) {
                InicioScreen(onNavigateToNewWord = { navController.navigate(Screen.NuevaPalabra.rout) })
            }

            // Diccionario de palabras aprendidas.
            composable(route = Screen.Diccionario.rout) {
                DicScreen()
            }

            // Desafíos interactivos.
            composable(route = Screen.Desafios.rout) {
                DesScreen(navController = navController)
            }

            // Perfil de usuario.
            composable(route = Screen.Perfil.rout) {
                PerfScreen(userName = userName, onSignOut = onSignOut)
            }

            // Pantalla para añadir una nueva palabra.
            composable(route = Screen.NuevaPalabra.rout) {
                NuevPaScreen(
                    // Navegación a pantalla de intento tras agregar palabra.
                    onWordAdded = { palabra ->
                        navController.navigate("${Screen.Intentar.rout}/$palabra") {
                            popUpTo(Screen.NuevaPalabra.rout) { inclusive = true }
                        }
                    },
                    onCancel = { navController.popBackStack() }
                )
            }

            // Pantalla de intento, recibe palabra como argumento.
            composable(
                route = "${Screen.Intentar.rout}/{palabra}",
                arguments = listOf(navArgument("palabra") { type = NavType.StringType })
            ) { backStackEntry ->
                val palabra = backStackEntry.arguments?.getString("palabra") ?: ""
                IntenScreen(
                    palabra = palabra,
                    onResult = { esCorrecta, exercise ->
                        // Serializa y codifica el ejercicio para pasarlo por la URL.
                        val exerciseJson = Gson().toJson(exercise)
                        val encodedJson = URLEncoder.encode(exerciseJson, StandardCharsets.UTF_8.toString())
                        navController.navigate("resultado_screen/$palabra/$esCorrecta/$encodedJson") {
                            popUpTo("${Screen.Intentar.rout}/{palabra}") { inclusive = true }
                        }
                    }
                )
            }

            // Pantalla de resultado del intento.
            composable(
                route = "resultado_screen/{palabra}/{esCorrecta}/{exerciseJson}",
                arguments = listOf(
                    navArgument("palabra") { type = NavType.StringType },
                    navArgument("esCorrecta") { type = NavType.BoolType },
                    navArgument("exerciseJson") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val palabra = backStackEntry.arguments?.getString("palabra") ?: ""
                val esCorrecta = backStackEntry.arguments?.getBoolean("esCorrecta") ?: false
                val exerciseJson = backStackEntry.arguments?.getString("exerciseJson") ?: ""
                ResultaScreen(
                    palabra = palabra,
                    esCorrecta = esCorrecta,
                    exerciseJson = exerciseJson,
                    onAddToDictionary = {
                        navController.navigate(Screen.Diccionario.rout) {
                            popUpTo(Screen.Inico.rout) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    onBackToHome = {
                        navController.navigate(Screen.Inico.rout) {
                            popUpTo(Screen.Inico.rout) { inclusive = true
                            }
                        }
                    }
                )
            }

            // Pantalla para crear una oración basada en palabras previas.
            composable(route = Screen.CrearOracion.rout) {
                OracionDesScreen(
                    onNavigateToResult = { resultAsJson ->
                        navController.navigate("${Screen.ResultadoOracion.rout}/$resultAsJson") {
                            popUpTo(Screen.CrearOracion.rout) { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Resultado del desafío de oración.
            composable(
                route = "${Screen.ResultadoOracion.rout}/{resultJson}",
                arguments = listOf(navArgument("resultJson") { type = NavType.StringType })
            ) { backStackEntry ->
                val resultJson = backStackEntry.arguments?.getString("resultJson")
                DesResultadosScreen(
                    resultJson = resultJson,
                    onFinish = {
                        navController.navigate(Screen.Desafios.rout) {
                            popUpTo(Screen.Desafios.rout) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}

// Vista previa de la navegación principal, útil para inspección visual en diseño.
@Preview
@Composable
fun MainNavigationPreview() {
    MaterialTheme {
        MainNavigationScreen("VANESA") {}
    }
}