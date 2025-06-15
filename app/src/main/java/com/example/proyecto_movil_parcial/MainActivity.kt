package com.example.proyecto_movil_parcial

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
import com.example.proyecto_movil_parcial.Screens.DesScreen
import com.example.proyecto_movil_parcial.Screens.DicScreen
import com.example.proyecto_movil_parcial.Screens.InicioScreen
import com.example.proyecto_movil_parcial.Screens.IntenScreen
import com.example.proyecto_movil_parcial.Screens.NuevPaScreen
import com.example.proyecto_movil_parcial.Screens.PerfScreen
import com.example.proyecto_movil_parcial.Screens.ResultaScreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.example.proyecto_movil_parcial.navigation.BottomNavigationBar
import com.example.proyecto_movil_parcial.navigation.Screen
import com.example.proyecto_movil_parcial.services.QuickExerciseResponse
import com.example.proyecto_movil_parcial.Screens.PalabraDetalleScreen

class MainActivity : ComponentActivity() {

    companion object {
        var currentExercise: com.example.proyecto_movil_parcial.services.QuickExerciseResponse? = null
    }
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Configurar Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        // Obtener el usuario actual
        val auth = Firebase.auth
        val user = auth.currentUser
        val userName = user?.displayName ?: "Usuario"

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigationScreen(
                        userName = if (user != null) "Hola, $userName" else "Loading..",
                        onSignOut = { signOutAndStartSignInActivity() }
                    )
                }
            }
        }
    }

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
    userName: String,
    onSignOut: () -> Unit
) {
    val navController = rememberNavController()
    val currentRoute by navController.currentBackStackEntryAsState()

    val shouldShowBottomBar = when (currentRoute?.destination?.route) {
        Screen.NuevaPalabra.rout,
        Screen.Intentar.rout,
        "resultado_screen/{palabra}/{esCorrecta}" -> false
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
            startDestination = Screen.Inico.rout,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Pantallas principales con bottom navigation
            composable(route = Screen.Inico.rout) {
                InicioScreen(
                    onNavigateToNewWord = {
                        navController.navigate(Screen.NuevaPalabra.rout)
                    }
                )
            }

            composable(route = Screen.Diccionario.rout) {
                DicScreen()
            }

            composable(route = Screen.Desafíos.rout) {
                DesScreen()
            }

            composable(route = Screen.Perfil.rout) {
                PerfScreen(
                    userName = userName,
                    onSignOut = onSignOut
                )
            }

            // Flujo de aprendizaje sin bottom navigation
            composable(route = Screen.NuevaPalabra.rout) {
                NuevPaScreen(
                    onWordAdded = { palabra ->
                        // Navegar a "Intenta adivinar" con la palabra
                        navController.navigate("intenta_adivinar/$palabra") {
                            popUpTo(Screen.NuevaPalabra.rout) { inclusive = true }
                        }
                    },
                    onCancel = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.Intentar.rout + "/{palabra}",
                arguments = listOf(navArgument("palabra") { type = NavType.StringType })
            ) { backStackEntry ->
                val palabra = backStackEntry.arguments?.getString("palabra") ?: ""
                IntenScreen(
                    palabra = palabra,
                    onResult = { esCorrecta, exercise ->
                        MainActivity.currentExercise = exercise

                        navController.navigate("resultado_screen/$palabra/$esCorrecta") {
                            popUpTo(Screen.Intentar.rout + "/{palabra}") { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = "resultado_screen/{palabra}/{esCorrecta}",
                arguments = listOf(
                    navArgument("palabra") { type = NavType.StringType },
                    navArgument("esCorrecta") { type = NavType.BoolType }
                )
            ) { backStackEntry ->
                val palabra = backStackEntry.arguments?.getString("palabra") ?: ""
                val esCorrecta = backStackEntry.arguments?.getBoolean("esCorrecta") ?: false

                ResultaScreen(
                    palabra = palabra,
                    esCorrecta = esCorrecta,
                    exercise = MainActivity.currentExercise, // 🎯 PASAR ejercicio
                    onAddToDictionary = {
                        // Navegar al diccionario y limpiar back stack
                        navController.navigate(Screen.Diccionario.rout) {
                            popUpTo(Screen.Inico.rout) {
                                inclusive = false
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onBackToHome = {
                        // Volver al inicio y limpiar back stack
                        navController.navigate(Screen.Inico.rout) {
                            popUpTo(Screen.Inico.rout) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun MainNavigationPreview(){
    MaterialTheme {
        MainNavigationScreen("VANESA") { }
    }
}