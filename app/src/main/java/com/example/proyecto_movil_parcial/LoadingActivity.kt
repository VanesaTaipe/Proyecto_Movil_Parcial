package com.example.proyecto_movil_parcial

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoadingActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoadingScreen()
                }
            }
        }

        // Verificar usuario al iniciar
        android.util.Log.d("LoadingActivity", "Iniciando verificación de usuario")
        checkUserStatus()
    }

    private fun checkUserStatus() {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            // Si no hay usuario, regresar al login
            redirectToLogin()
            return
        }

        val userId = currentUser.uid
        android.util.Log.d("LoadingActivity", "Usuario ID: $userId")
        android.util.Log.d("LoadingActivity", "Usuario email: ${currentUser.email}")

        // Verificar si el usuario existe en Firestore
        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                try {
                    if (document.exists()) {
                        // Usuario existe, verificar si es primera vez o usuario recurrente
                        val maxPalabrasDia = document.getLong("maxPalabrasDia")
                        val fechaRegistro = document.getTimestamp("fechaRegistro")
                        val isFirstTime = intent.getBooleanExtra("isFirstTime", false)

                        android.util.Log.d("LoadingActivity", "maxPalabrasDia: $maxPalabrasDia")
                        android.util.Log.d("LoadingActivity", "fechaRegistro: $fechaRegistro")
                        android.util.Log.d("LoadingActivity", "isFirstTime: $isFirstTime")

                        when {
                            // Si tiene maxPalabrasDia configurado, es usuario completo
                            maxPalabrasDia != null && maxPalabrasDia > 0 -> {
                                // Actualizar último acceso y ir a MainActivity
                                updateLastAccessAndRedirectToMain(userId)
                            }
                            // Si no tiene maxPalabrasDia pero tiene fechaRegistro, necesita completar configuración
                            fechaRegistro != null -> {
                                redirectToPresentation()
                            }
                            // Caso extraño: documento existe pero sin datos básicos
                            else -> {
                                android.util.Log.w("LoadingActivity", "Usuario existe pero sin datos básicos")
                                recreateUserAndRedirectToPresentation(currentUser)
                            }
                        }
                    } else {
                        // Usuario no existe -> Crear y ir a PresentationActivity
                        createUserAndRedirectToPresentation(currentUser)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("LoadingActivity", "Error procesando datos: ${e.message}", e)
                    Toast.makeText(this, "Error procesando datos: ${e.message}",
                        Toast.LENGTH_SHORT).show()
                    redirectToLogin()
                }
            }
            .addOnFailureListener { exception ->
                // Error al consultar Firestore
                android.util.Log.e("LoadingActivity", "Error de conexión: ${exception.message}", exception)
                Toast.makeText(this, "Error de conexión: ${exception.message}",
                    Toast.LENGTH_LONG).show()

                // Intentar de nuevo después de 3 segundos
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    checkUserStatus()
                }, 3000)
            }
    }

    private fun updateLastAccessAndRedirectToMain(userId: String) {
        firestore.collection("users")
            .document(userId)
            .update("ultimoAcceso", com.google.firebase.Timestamp.now())
            .addOnSuccessListener {
                redirectToMain()
            }
            .addOnFailureListener { exception ->
                android.util.Log.w("LoadingActivity", "Error actualizando último acceso: ${exception.message}")
                // Aunque falle la actualización, igual redirigir a Main
                redirectToMain()
            }
    }

    private fun createUserAndRedirectToPresentation(user: com.google.firebase.auth.FirebaseUser) {
        val userData = hashMapOf(
            "email" to user.email,
            "displayName" to user.displayName,
            "fechaRegistro" to com.google.firebase.Timestamp.now(),
            "ultimoAcceso" to com.google.firebase.Timestamp.now(),
            "isNewUser" to true
        )

        android.util.Log.d("LoadingActivity", "Creando nuevo usuario")

        firestore.collection("users")
            .document(user.uid)
            .set(userData)
            .addOnSuccessListener {
                android.util.Log.d("LoadingActivity", "Usuario creado exitosamente")
                redirectToPresentation()
            }
            .addOnFailureListener { exception ->
                android.util.Log.e("LoadingActivity", "Error al crear usuario: ${exception.message}", exception)
                Toast.makeText(this, "Error al crear usuario: ${exception.message}",
                    Toast.LENGTH_SHORT).show()
                redirectToLogin()
            }
    }

    private fun recreateUserAndRedirectToPresentation(user: com.google.firebase.auth.FirebaseUser) {
        val userData = hashMapOf(
            "email" to user.email,
            "displayName" to user.displayName,
            "fechaRegistro" to com.google.firebase.Timestamp.now(),
            "ultimoAcceso" to com.google.firebase.Timestamp.now(),
            "isRecreated" to true
        )

        android.util.Log.d("LoadingActivity", "Recreando datos de usuario")

        firestore.collection("users")
            .document(user.uid)
            .update(userData as Map<String, Any>)
            .addOnSuccessListener {
                redirectToPresentation()
            }
            .addOnFailureListener { exception ->
                android.util.Log.e("LoadingActivity", "Error recreando usuario: ${exception.message}", exception)
                Toast.makeText(this, "Error actualizando datos: ${exception.message}",
                    Toast.LENGTH_SHORT).show()
                redirectToLogin()
            }
    }

    private fun redirectToLogin() {
        android.util.Log.d("LoadingActivity", "Redirigiendo a login")
        val intent = Intent(this, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun redirectToPresentation() {
        android.util.Log.d("LoadingActivity", "Redirigiendo a presentación")
        val intent = Intent(this, PresentationActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun redirectToMain() {
        android.util.Log.d("LoadingActivity", "Redirigiendo a main")
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

@Composable
fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Spinner de carga
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "AiWordFlow",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Estamos configurando tu experiencia :3...",
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