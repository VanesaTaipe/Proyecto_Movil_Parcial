package com.example.proyecto_movil_parcial.Screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyecto_movil_parcial.components.HearderInicio
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevPaScreen(
    onWordAdded: (String) -> Unit = {},
    onCancel: () -> Unit = {}
) {
    var palabraText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        HearderInicio(
            title = "Nueva Palabra"
        )

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Pregunta principal
            Text(
                text = "¿Cuál es tu nueva palabra?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Campo de entrada
            OutlinedTextField(
                value = palabraText,
                onValueChange = { newValue ->
                    // Solo permitir letras y espacios (para phrasal verbs)
                    if (newValue.all { it.isLetter() || it.isWhitespace() || it == '\'' }) {
                        palabraText = newValue
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                placeholder = {
                    Text(
                        "",
                        color = Color.Gray
                    )
                },
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            // Botón Agregar
            Button(
                onClick = {
                    if (palabraText.isNotBlank()) {
                        isLoading = true
                        saveWordToFirebase(
                            palabra = palabraText.trim(),
                            context = context,
                            onSuccess = { palabraGuardada ->
                                isLoading = false
                                onWordAdded(palabraGuardada)
                            },
                            onError = { error ->
                                isLoading = false
                                Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                },
                modifier = Modifier
                    .width(120.dp)
                    .align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFCEA3D9)
                ),
                shape = RoundedCornerShape(24.dp),
                enabled = !isLoading && palabraText.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Agregar",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Función para guardar palabra en Firebase
private fun saveWordToFirebase(
    palabra: String,
    context: android.content.Context,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    if (currentUser == null) {
        onError("Usuario no encontrado")
        return
    }

    // Datos de la palabra a guardar
    val palabraData = hashMapOf(
        "palabra" to palabra.lowercase(),
        "userId" to currentUser.uid,
        "fechaAgregada" to com.google.firebase.Timestamp.now(),
        "definicion" to "", // Se llenará después con IA
        "ejemplos" to emptyList<String>(),
        "intentosCorrectos" to 0,
        "intentosTotales" to 0,
        "isAprendida" to false,
        "status" to "pendiente" // pendiente, procesada, completada
    )

    firestore.collection("palabras")
        .add(palabraData)
        .addOnSuccessListener { documentReference ->
            // Actualizar con el ID del documento
            documentReference.update("id", documentReference.id)
                .addOnSuccessListener {
                    onSuccess(palabra)
                }
                .addOnFailureListener {
                    onError("Error al actualizar ID: ${it.message}")
                }
        }
        .addOnFailureListener { exception ->
            onError("Error al guardar: ${exception.message}")
        }
}

@Preview(
    showBackground = true,
    widthDp = 360,
    heightDp = 640
)
@Composable
fun NuevPaScreenPreview() {
    MaterialTheme {
        NuevPaScreen()
    }
}