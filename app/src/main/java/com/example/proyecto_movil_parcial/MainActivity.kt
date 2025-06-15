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
import com.example.proyecto_movil_parcial.Screens.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.example.proyecto_movil_parcial.navigation.BottomNavigationBar
import com.example.proyecto_movil_parcial.navigation.Screen
import com.google.gson.Gson
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

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
                        userName = if (user != null) "Hola, $userName" else "Cargando..",
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
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

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
            startDestination = Screen.Inico.rout,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = Screen.Inico.rout) {
                InicioScreen(onNavigateToNewWord = { navController.navigate(Screen.NuevaPalabra.rout) })
            }
            composable(route = Screen.Diccionario.rout) {
                DicScreen()
            }
            composable(route = Screen.Desafíos.rout) {
                DesScreen(navController = navController)
            }
            composable(route = Screen.Perfil.rout) {
                PerfScreen(userName = userName, onSignOut = onSignOut)
            }
            composable(route = Screen.NuevaPalabra.rout) {
                NuevPaScreen(
                    onWordAdded = { palabra ->
                        navController.navigate("intenta_adivinar/$palabra") {
                            popUpTo(Screen.NuevaPalabra.rout) { inclusive = true }
                        }
                    },
                    onCancel = { navController.popBackStack() }
                )
            }
            composable(
                route = "${Screen.Intentar.rout}/{palabra}",
                arguments = listOf(navArgument("palabra") { type = NavType.StringType })
            ) { backStackEntry ->
                val palabra = backStackEntry.arguments?.getString("palabra") ?: ""
                IntenScreen(
                    palabra = palabra,
                    onResult = { esCorrecta, exercise ->
                        val exerciseJson = Gson().toJson(exercise)
                        val encodedJson = URLEncoder.encode(exerciseJson, StandardCharsets.UTF_8.toString())
                        navController.navigate("resultado_screen/$palabra/$esCorrecta/$encodedJson") {
                            popUpTo("${Screen.Intentar.rout}/{palabra}") { inclusive = true }
                        }
                    }
                )
            }
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
                            popUpTo(Screen.Inico.rout) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onBackToHome = {
                        navController.navigate(Screen.Inico.rout) {
                            popUpTo(Screen.Inico.rout) { inclusive = true }
                        }
                    }
                )
            }
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
            composable(
                route = "${Screen.ResultadoOracion.rout}/{resultJson}",
                arguments = listOf(navArgument("resultJson") { type = NavType.StringType })
            ) { backStackEntry ->
                val resultJson = backStackEntry.arguments?.getString("resultJson")
                DesResultadosScreen(
                    resultJson = resultJson,
                    onFinish = {
                        navController.navigate(Screen.Desafíos.rout) {
                            popUpTo(Screen.Desafíos.rout) { inclusive = true }
                            launchSingleTop = true
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