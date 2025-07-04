package com.example.proyecto_movil_parcial

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Activity encargada de realizar validaciones iniciales al arrancar la app:
// Verifica si el usuario está autenticado y redirige a la vista correspondiente.
class LoadingActivity : ComponentActivity() {

    // Instancia de FirebaseAuth utilizada para obtener información de autenticación del usuario.
    private lateinit var auth: FirebaseAuth

    // Instancia de Firestore para realizar operaciones CRUD sobre documentos de usuario.
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicialización de Firebase Authentication y Firestore.
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Renderización de UI utilizando Jetpack Compose con el tema Material Design 3.
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), // Establece la superficie para cubrir toda la pantalla.
                    color = MaterialTheme.colorScheme.background // Utiliza el color de fondo del tema definido.
                ) {
                    LoadingScreen() // Composable que representa una pantalla de carga.
                }
            }
        }

        // Inicia la validación del estado del usuario.
        checkUserStatus()
    }

    // Función que determina si hay un usuario autenticado y realiza el flujo correspondiente.
    private fun checkUserStatus() {
        val currentUser = auth.currentUser // Obtiene el usuario autenticado actualmente.

        if (currentUser == null) {
            // Si no hay usuario autenticado, redirige a la pantalla de inicio de sesión.
            redirectToLogin()
            return
        }

        val userId = currentUser.uid // Obtiene el identificador único del usuario.

        // Realiza una consulta al documento del usuario en la colección "users".
        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                try {
                    if (document.exists()) {
                        // Si el documento existe, se intenta leer la propiedad "maxPalabrasDia".
                        val maxPalabrasDia = document.getLong("maxPalabrasDia")

                        when {
                            // Verifica si "maxPalabrasDia" está definido y es mayor a 0.
                            // Esto puede interpretarse como una configuración ya realizada por el usuario.
                            maxPalabrasDia != null && maxPalabrasDia > 0 -> {
                                updateLastAccessAndRedirectToMain(userId)
                            }
                            else -> {
                                // Si no tiene configurado "maxPalabrasDia", se considera un usuario sin onboarding completo.
                                redirectToPresentation()
                            }
                        }
                    } else {
                        // Si no existe el documento, se asume que es un usuario nuevo.
                        // Se crea el documento en la colección y se redirige a onboarding/presentación.
                        createUserAndRedirectToPresentation(currentUser)
                    }
                } catch (e: Exception) {
                    // Captura errores inesperados durante la lectura del documento.
                    Toast.makeText(this, "Error procesando datos", Toast.LENGTH_SHORT).show()
                    redirectToLogin()
                }
            }
            .addOnFailureListener { exception ->
                // En caso de fallo en la conexión o acceso a Firestore, se notifica al usuario.
                Toast.makeText(this, "Error de conexión", Toast.LENGTH_LONG).show()

                // Se realiza un reintento después de 3 segundos.
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    checkUserStatus()
                }, 3000)
            }
    }

    // Actualiza el campo "ultimoAcceso" con el timestamp actual y redirige al MainActivity.
    private fun updateLastAccessAndRedirectToMain(userId: String) {
        firestore.collection("users")
            .document(userId)
            .update("ultimoAcceso", com.google.firebase.Timestamp.now()) // Actualiza el último acceso del usuario.
            .addOnSuccessListener {
                redirectToMain() // Redirige a la pantalla principal si la actualización es exitosa.
            }
            .addOnFailureListener {
                // Incluso si falla, continúa con la redirección para no bloquear al usuario.
                redirectToMain()
            }
    }

    // Crea un nuevo documento en la colección "users" para un usuario autenticado que aún no tiene datos registrados.
    private fun createUserAndRedirectToPresentation(user: com.google.firebase.auth.FirebaseUser) {
        val userData = hashMapOf(
            "email" to user.email,
            "displayName" to user.displayName,
            "fechaRegistro" to com.google.firebase.Timestamp.now(), // Fecha de creación del usuario.
            "ultimoAcceso" to com.google.firebase.Timestamp.now(),   // Fecha de último acceso (inicialmente igual).
            "isNewUser" to true // Flag que puede ser usado para lógica condicional de bienvenida o tutorial.
        )

        firestore.collection("users")
            .document(user.uid)
            .set(userData)
            .addOnSuccessListener {
                redirectToPresentation() // Si el registro se completa correctamente, se muestra la presentación.
            }
            .addOnFailureListener { exception ->
                // Si ocurre un error al crear el documento, se notifica al usuario.
                Toast.makeText(this, "Error al crear usuario", Toast.LENGTH_SHORT).show()
                redirectToLogin()
            }
    }

    // Redirige al usuario a la pantalla de inicio de sesión, limpiando la pila de actividades.
    private fun redirectToLogin() {
        val intent = Intent(this, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // Redirige al usuario a la presentación u onboarding, limpiando la pila de actividades.
    private fun redirectToPresentation() {
        val intent = Intent(this, PresentationActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // Redirige al usuario a la actividad principal de la aplicación.
    private fun redirectToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

/**
 * Composable que representa la interfaz visual de carga mientras se verifica
 * el estado del usuario en segundo plano.
 *
 * Muestra:
 * - Un indicador de progreso circular.
 * - Logo de la aplicación.
 * - Un texto informativo para el usuario.
 */
@Composable
fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(64.dp)
                .testTag("cargando"),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "logo"
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Configurando tu experiencia...",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingScreenPreview() {
    MaterialTheme {
        LoadingScreen()
    }
}