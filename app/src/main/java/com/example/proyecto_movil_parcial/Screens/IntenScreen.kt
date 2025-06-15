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
import com.example.proyecto_movil_parcial.services.ExerciseOption
import com.example.proyecto_movil_parcial.services.OpenAIServiceProvider
import com.example.proyecto_movil_parcial.services.QuickExerciseResponse
import kotlinx.coroutines.launch

@Composable
fun IntenScreen(
    palabra: String = "",
    onResult: (Boolean, QuickExerciseResponse?) -> Unit = { _, _ -> }
) {
    var selectedOption by remember { mutableStateOf(-1) }
    var isLoading by remember { mutableStateOf(false) }
    var exercise by remember { mutableStateOf<QuickExerciseResponse?>(null) }
    var isLoadingExercise by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    // Cargar ejercicio desde OpenAI cuando se inicia la pantalla
    LaunchedEffect(palabra) {
        scope.launch {
            isLoadingExercise = true
            errorMessage = null

            try {
                val exerciseResponse = OpenAIServiceProvider.service.generateQuickExercise(palabra)

                if (exerciseResponse != null) {
                    exercise = exerciseResponse
                    android.util.Log.d("IntenScreen", "Exercise loaded: ${exerciseResponse.options.size} options")
                } else {
                    errorMessage = "Error generando ejercicio, usando versión básica"
                    // Crear ejercicio fallback
                    exercise = createFallbackExercise(palabra)
                }
            } catch (e: Exception) {
                android.util.Log.e("IntenScreen", "Error loading exercise: ${e.message}")
                errorMessage = "Sin conexión a IA, usando ejercicio básico"
                exercise = createFallbackExercise(palabra)
            } finally {
                isLoadingExercise = false
            }
        }
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

            // Mostrar mensaje de error si existe
            errorMessage?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3CD)
                    )
                ) {
                    Text(
                        text = "⚠️ $message",
                        modifier = Modifier.padding(12.dp),
                        fontSize = 12.sp,
                        color = Color(0xFF856404)
                    )
                }
            }

            // Loading o opciones
            if (isLoadingExercise) {
                // Estado de carga
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "🤖 Generando ejercicio con IA...",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                // 🎯 Mostrar 2 opciones
                val currentExercise = exercise
                if (currentExercise != null) {
                    currentExercise.options.forEachIndexed { index, opcion ->
                        OpcionItem(
                            texto = opcion.text,
                            isSelected = selectedOption == index,
                            onClick = { selectedOption = index }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón Verificar
            Button(
                onClick = {
                    val currentExercise = exercise
                    if (selectedOption != -1 && currentExercise != null) {
                        isLoading = true
                        val esCorrecta = currentExercise.options[selectedOption].isCorrect

                        //
                        onResult(esCorrecta, currentExercise)
                    }
                },
                modifier = Modifier
                    .width(120.dp)
                    .align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFCEA3D9)
                ),
                shape = RoundedCornerShape(24.dp),
                enabled = !isLoading && !isLoadingExercise && selectedOption != -1
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

// Función fallback para casos donde no funciona la API
private fun createFallbackExercise(palabra: String): QuickExerciseResponse {
    val options = when (palabra.lowercase()) {
        "ephemeral" -> listOf(
            ExerciseOption("The ephemeral beauty of cherry blossoms attracts many visitors.", true),
            ExerciseOption("The ephemeral building was designed to last for centuries.", false)
        )
        else -> listOf(
            ExerciseOption("The meaning of $palabra is very important to understand.", true),
            ExerciseOption("She decided to $palabra the difficult situation quickly.", false)
        )
    }.shuffled()

    return QuickExerciseResponse(
        definition = "A word that needs to be learned and practiced.",
        correctSentence = options.find { it.isCorrect }?.text ?: "",
        incorrectSentence = options.find { !it.isCorrect }?.text ?: "",
        options = options
    )
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