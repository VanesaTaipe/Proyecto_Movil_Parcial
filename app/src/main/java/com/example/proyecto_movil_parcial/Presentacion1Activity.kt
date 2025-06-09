package com.example.proyecto_movil_parcial

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyecto_movil_parcial.components.HeaderSection
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PresentationActivity : ComponentActivity() {

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
                    PresentationScreen(
                        onFinish = { maxPalabras ->
                            saveMaxPalabrasAndNavigate(maxPalabras)
                        }
                    )
                }
            }
        }
    }

    private fun saveMaxPalabrasAndNavigate(maxPalabras: Int) {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(this, "Error: Usuario no encontrado", Toast.LENGTH_SHORT).show()
            redirectToLogin()
            return
        }

        // Actualizar el documento del usuario con maxPalabrasDia
        firestore.collection("users")
            .document(currentUser.uid)
            .update(
                mapOf(
                    "maxPalabrasDia" to maxPalabras,
                    "ultimoAcceso" to com.google.firebase.Timestamp.now()
                )
            )
            .addOnSuccessListener {
                // Navegar a MainActivity
                val intent = Intent(this@PresentationActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al guardar configuración: ${exception.message}",
                    Toast.LENGTH_SHORT).show()
            }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

@Composable
fun PresentationScreen(onFinish: (Int) -> Unit) {
    FirstPresentationScreen(onNext = onFinish)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirstPresentationScreen(onNext: (Int) -> Unit) {
    var palabrasText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        // Usar el componente HeaderSection para la cabecera
        HeaderSection(
            title = "Bienvenido a AiWordFlow"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Contenedor con fondo beige para la instrucción
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFEBDABF),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = "Define cuantas palabras al día te gustaría aprender",
                    fontSize = 16.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Campo de entrada para el número de palabras
            OutlinedTextField(
                value = palabrasText,
                onValueChange = { newValue ->
                    // Solo permitir números
                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                        palabrasText = newValue
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp),
                placeholder = { Text("Escribe un número") },
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.weight(1f))

            // Botón para continuar
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Button(
                    onClick = {
                        val maxPalabras = palabrasText.toIntOrNull()
                        if (maxPalabras != null && maxPalabras > 0) {
                            isLoading = true
                            onNext(maxPalabras)
                        }
                    },
                    modifier = Modifier.width(120.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFCEA3D9)  // Color morado claro
                    ),
                    shape = RoundedCornerShape(24.dp),
                    enabled = !isLoading && palabrasText.isNotEmpty() && palabrasText.toIntOrNull() != null && palabrasText.toInt() > 0
                ) {
                    if (isLoading) {
                        Text(
                            text = "...",
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "Siguiente",
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    widthDp = 360,
    heightDp = 640
)
@Composable
fun FirstPresentationScreenPreview() {
    MaterialTheme {
        FirstPresentationScreen(onNext = {})
    }
}