package com.example.proyecto_movil_parcial.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyecto_movil_parcial.components.HearderInicio

@Composable
fun IntenScreen(
    palabra: String = "",
    onResult: (Boolean) -> Unit = {}
) {
    var selectedOption by remember { mutableStateOf(-1) }
    var isLoading by remember { mutableStateOf(false) }

    // Opciones de ejemplo - en una implementación real, estas vendrían de una API de IA
    val opciones = remember {
        generateOptionsForWord(palabra)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        HearderInicio(
            title = "Intenta adivinar"
        )

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Palabra a adivinar
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = palabra.replaceFirstChar { it.uppercase() },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Instrucción
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
                    text = "Elige la oración donde \"$palabra\" se usa correctamente",
                    fontSize = 16.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Opciones de respuesta
            opciones.forEachIndexed { index, opcion ->
                OpcionItem(
                    texto = opcion.texto,
                    isSelected = selectedOption == index,
                    onClick = { selectedOption = index }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón Verificar
            Button(
                onClick = {
                    if (selectedOption != -1) {
                        isLoading = true
                        val esCorrecta = opciones[selectedOption].esCorrecta
                        onResult(esCorrecta)
                    }
                },
                modifier = Modifier
                    .width(120.dp)
                    .align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFCEA3D9)
                ),
                shape = RoundedCornerShape(24.dp),
                enabled = !isLoading && selectedOption != -1
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Verificar",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun OpcionItem(
    texto: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = texto,
                fontSize = 14.sp,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )
        }
    }
}

// Datos para las opciones
data class OpcionRespuesta(
    val texto: String,
    val esCorrecta: Boolean
)

// Función para generar opciones (en una implementación real, esto vendría de una API de IA)
private fun generateOptionsForWord(palabra: String): List<OpcionRespuesta> {
    return when (palabra.lowercase()) {
        "ephemeral" -> listOf(
            OpcionRespuesta(
                texto = "Cherry blossoms are so ephemeral that they only last for a few days in spring.",
                esCorrecta = true
            ),
            OpcionRespuesta(
                texto = "The ephemeral building was constructed to last for centuries.",
                esCorrecta = false
            ),
            OpcionRespuesta(
                texto = "Social media fame tends to be ephemeral, intense but short-lived.",
                esCorrecta = true
            )
        )
        else -> listOf(
            OpcionRespuesta(
                texto = "The $palabra was very important in the story.",
                esCorrecta = true
            ),
            OpcionRespuesta(
                texto = "She decided to $palabra the difficult situation.",
                esCorrecta = false
            ),
            OpcionRespuesta(
                texto = "The weather was $palabra and pleasant today.",
                esCorrecta = false
            )
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 360,
    heightDp = 640
)
@Composable
fun IntenScreenPreview() {
    MaterialTheme {
        IntenScreen(palabra = "ephemeral")
    }
}