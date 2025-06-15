package com.example.proyecto_movil_parcial.Screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import com.example.proyecto_movil_parcial.services.OpenAIServiceProvider
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevPaScreen(
    onWordAdded: (String) -> Unit = {},
    onCancel: () -> Unit = {}
) {
    var palabraText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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

            // Fila para contener ambos botones
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón para cancelar/regresar
                TextButton(
                    onClick = onCancel,
                    enabled = !isLoading
                ) {
                    Text(text = "Cancelar", color = Color.Gray)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Botón Agregar
                Button(
                    onClick = {
                        if (palabraText.isNotBlank()) {
                            isLoading = true

                            scope.launch {
                                try {
                                    val exercise = OpenAIServiceProvider.service.generateQuickExercise(
                                        palabraText.trim()
                                    )

                                    if (exercise != null) {
                                        onWordAdded(palabraText.trim())
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Generando ejercicio básico...",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        onWordAdded(palabraText.trim())
                                    }

                                } catch (e: Exception) {
                                    android.util.Log.e("NuevPaScreen", "Error: ${e.message}")
                                    Toast.makeText(
                                        context,
                                        "Usando ejercicio básico",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    onWordAdded(palabraText.trim())
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.width(120.dp),
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
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Texto informativo durante la carga
            if (isLoading) {
                Text(
                    text = "🤖 Generando ejercicio con IA...",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
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
fun NuevPaScreenPreview() {
    MaterialTheme {
        NuevPaScreen()
    }
}