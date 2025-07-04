package com.example.proyecto_movil_parcial.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.proyecto_movil_parcial.services.OpenAIServiceProvider
import com.example.proyecto_movil_parcial.ui.theme.Proyecto_Movil_parcialTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevPaScreen(
    // Callback que se dispara cuando se agrega una nueva palabra.
    onWordAdded: (String) -> Unit = {},
    onCancel: () -> Unit = {} // Callback ejecutado al cancelar la acción.
) {
    // Estado reactivo que almacena el texto ingresado por el usuario.
    var palabraText by remember { mutableStateOf("") }

    // Estado booleano que indica si hay una operación de red en curso.
    var isLoading by remember { mutableStateOf(false) }

    // Contexto actual necesario para mostrar toasts u otras operaciones contextuales.
    val context = LocalContext.current

    // Alcance de corrutinas ligado al ciclo de vida del Composable.
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Encabezado personalizado con el título de la pantalla.
        HearderInicio(title = "Nueva Palabra")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center, // Centrado vertical del contenido.
            horizontalAlignment = Alignment.CenterHorizontally // Alineación horizontal al centro.
        ) {
            Spacer(modifier = Modifier.weight(1f)) // Empuja el contenido hacia el centro.

            // Texto descriptivo con estilo.
            Text(
                text = "¿Cuál es tu nueva palabra?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Campo de texto para ingresar la palabra.
            OutlinedTextField(
                value = palabraText,
                onValueChange = { newValue ->
                    // Validación local que permite solo letras, espacios y apóstrofes.
                    if (newValue.all { it.isLetter() || it.isWhitespace() || it == '\'' }) {
                        palabraText = newValue
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                placeholder = { Text("", color = Color.Gray) }, // Placeholder opcional.
                shape = RoundedCornerShape(24.dp),
                singleLine = true, // Evita saltos de línea.
                enabled = !isLoading, // Desactiva el input si se está procesando.
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.weight(1f)) // Espaciado inferior dinámico.

            // Contenedor horizontal para botones de acción (cancelar y buscar).
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.End, // Alineación al final.
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón para cancelar la acción.
                TextButton(onClick = onCancel, enabled = !isLoading) {
                    Text(text = "Cancelar", color = Color.Gray)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Botón principal que dispara la generación de un ejercicio.
                Button(
                    onClick = {
                        if (palabraText.isNotBlank()) {
                            isLoading = true // Habilita estado de carga.

                            // Lanzamiento de una corrutina para generar el ejercicio asincrónicamente.
                            scope.launch {
                                // Llamada al servicio de OpenAI para generar contenido educativo.
                                val exercise = OpenAIServiceProvider.service.generateQuickExercise(palabraText.trim())

                                // Validación del resultado: si es exitoso, se comunica la nueva palabra.
                                if (exercise != null) {
                                    onWordAdded(palabraText.trim())
                                } else {
                                    // Feedback al usuario si ocurre un error de red o procesamiento.
                                    Toast.makeText(context, "Error al generar ejercicio. Inténtalo de nuevo.", Toast.LENGTH_LONG).show()
                                }

                                isLoading = false // Finaliza estado de carga.
                            }
                        }
                    },
                    modifier = Modifier.width(120.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(24.dp),
                    enabled = !isLoading && palabraText.isNotBlank() // Desactiva si está vacío o cargando.
                ) {
                    // Renderizado condicional: si está cargando, muestra spinner.
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Buscar",
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// Composable de vista previa útil para pruebas de UI en tiempo de diseño.
@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun NuevPaScreenPreview() {

    Proyecto_Movil_parcialTheme {
           NuevPaScreen()
       }
}
