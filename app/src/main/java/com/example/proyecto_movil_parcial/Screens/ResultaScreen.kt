package com.example.proyecto_movil_parcial.Screens


import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyecto_movil_parcial.components.HearderInicio
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ResultaScreen(
    palabra: String = "",
    esCorrecta: Boolean = false,
    onAddToDictionary: () -> Unit = {},
    onBackToHome: () -> Unit = {}
) {
    var isProcessing by remember { mutableStateOf(false) }
    var definicionGenerada by remember { mutableStateOf("") }
    var ejemplosGenerados by remember { mutableStateOf<List<String>>(emptyList()) }
    val context = LocalContext.current

    // Generar definición y ejemplos cuando se carga la pantalla
    LaunchedEffect(palabra) {
        generateWordDefinition(palabra) { definicion, ejemplos ->
            definicionGenerada = definicion
            ejemplosGenerados = ejemplos
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        HearderInicio(
            title = "Resultado"
        )

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Ícono y mensaje de resultado
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (esCorrecta)
                        Color(0xFFE8F5E8)
                    else
                        Color(0xFFFFE8E8)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (esCorrecta) Icons.Default.CheckCircle else Icons.Default.Delete,
                        contentDescription = if (esCorrecta) "Correcto" else "Incorrecto",
                        modifier = Modifier.size(64.dp),
                        tint = if (esCorrecta) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (esCorrecta) "¡Bien hecho!" else "¡Sigue intentando!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (esCorrecta) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )

                    if (esCorrecta) {
                        Text(
                            text = "Has usado la palabra correctamente",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            // Información de la palabra
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Palabra
                    Text(
                        text = palabra.replaceFirstChar { it.uppercase() },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Definición
                    if (definicionGenerada.isNotEmpty()) {
                        Text(
                            text = definicionGenerada,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    // Ejemplos
                    if (ejemplosGenerados.isNotEmpty()) {
                        Text(
                            text = "Ejemplos:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        ejemplosGenerados.forEach { ejemplo ->
                            Text(
                                text = "• $ejemplo",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Mensaje de confirmación
            if (esCorrecta) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFFE8F5E8),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = "🎉 Palabra agregada 🎉",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4CAF50),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Botón de acción
            Button(
                onClick = {
                    isProcessing = true
                    if (esCorrecta) {
                        // Actualizar progreso en Firebase y ir al diccionario
                        updateWordProgress(
                            palabra = palabra,
                            esCorrecta = true,
                            definicion = definicionGenerada,
                            ejemplos = ejemplosGenerados,
                            onSuccess = {
                                isProcessing = false
                                onAddToDictionary()
                            },
                            onError = { error ->
                                isProcessing = false
                                Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        // Solo actualizar progreso y volver al inicio
                        updateWordProgress(
                            palabra = palabra,
                            esCorrecta = false,
                            definicion = definicionGenerada,
                            ejemplos = ejemplosGenerados,
                            onSuccess = {
                                isProcessing = false
                                onBackToHome()
                            },
                            onError = { error ->
                                isProcessing = false
                                Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFCEA3D9)
                ),
                shape = RoundedCornerShape(24.dp),
                enabled = !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (esCorrecta) "Agregar al diccionario" else "Volver al inicio",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

// Función para generar definición y ejemplos (simulada)
private fun generateWordDefinition(
    palabra: String,
    onComplete: (String, List<String>) -> Unit
) {
    // En una implementación real, esto haría una llamada a OpenAI API
    when (palabra.lowercase()) {
        "ephemeral" -> {
            onComplete(
                "Ephemeral means something that lasts for a very short time, like a gust of wind or a digital notification that disappears once read. Its beauty or impact often comes from its briefness.",
                listOf(
                    "The ephemeral beauty of cherry blossoms attracts thousands of visitors each spring.",
                    "They enjoyed a moment of ephemeral joy during the celebration, knowing it would soon end.",
                    "The artist is known for his ephemeral installations that change with each new trend.",
                    "Teenage fashions can be incredibly ephemeral, changing with each new trend."
                )
            )
        }
        else -> {
            onComplete(
                "Esta palabra significa... (definición generada por IA)",
                listOf(
                    "Ejemplo 1 con la palabra $palabra en contexto.",
                    "Otro ejemplo mostrando el uso de $palabra.",
                    "Un tercer ejemplo de cómo usar $palabra correctamente."
                )
            )
        }
    }
}

// Función para actualizar progreso en Firebase
private fun updateWordProgress(
    palabra: String,
    esCorrecta: Boolean,
    definicion: String,
    ejemplos: List<String>,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    if (currentUser == null) {
        onError("Usuario no encontrado")
        return
    }

    // Buscar la palabra en Firebase
    firestore.collection("palabras")
        .whereEqualTo("userId", currentUser.uid)
        .whereEqualTo("palabra", palabra.lowercase())
        .get()
        .addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                val document = querySnapshot.documents[0]
                val currentCorrect = document.getLong("intentosCorrectos")?.toInt() ?: 0
                val currentTotal = document.getLong("intentosTotales")?.toInt() ?: 0

                val newCorrect = if (esCorrecta) currentCorrect + 1 else currentCorrect
                val newTotal = currentTotal + 1
                val isAprendida = newCorrect >= 3 // Considerar aprendida después de 3 aciertos

                val updates = hashMapOf<String, Any>(
                    "intentosCorrectos" to newCorrect,
                    "intentosTotales" to newTotal,
                    "isAprendida" to isAprendida,
                    "definicion" to definicion,
                    "ejemplos" to ejemplos,
                    "status" to "completada"
                )

                document.reference.update(updates)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onError("Error al actualizar: ${it.message}") }
            } else {
                onError("Palabra no encontrada")
            }
        }
        .addOnFailureListener { onError("Error al buscar palabra: ${it.message}") }
}

@Preview(
    showBackground = true,
    widthDp = 360,
    heightDp = 640
)
@Composable
fun ResultaScreenCorrectaPreview() {
    MaterialTheme {
        ResultaScreen(
            palabra = "ephemeral",
            esCorrecta = true
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 360,
    heightDp = 640
)
@Composable
fun ResultaScreenIncorrectaPreview() {
    MaterialTheme {
        ResultaScreen(
            palabra = "ephemeral",
            esCorrecta = false
        )
    }
}