package com.example.proyecto_movil_parcial.Screens

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
import com.example.proyecto_movil_parcial.services.QuickExerciseResponse
import com.example.proyecto_movil_parcial.services.OpenAIServiceProvider
import com.example.proyecto_movil_parcial.services.FirebaseWordServiceProvider
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun ResultaScreen(
    palabra: String,
    esCorrecta: Boolean,
    exerciseJson: String,
    onAddToDictionary: () -> Unit,
    onBackToHome: () -> Unit
) {
    var isProcessing by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val exercise = remember {
        try {
            val decodedJson = URLDecoder.decode(exerciseJson, StandardCharsets.UTF_8.toString())
            Gson().fromJson(decodedJson, QuickExerciseResponse::class.java)
        } catch (e: Exception) {
            null
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        HearderInicio(title = "Nueva Palabra")
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = if (esCorrecta) "✅" else "💪", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (esCorrecta) "¡Bien hecho!" else "¡Sigue intentando!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD3BCA0)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = palabra.replaceFirstChar { it.uppercase() },
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = exercise?.definition ?: "Definición no disponible",
                        fontSize = 16.sp,
                        color = Color.Black,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Start
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    isProcessing = true
                    scope.launch {
                        try {
                            val yaExiste = FirebaseWordServiceProvider.service.palabraYaExiste(palabra)
                            if (yaExiste) {
                                Toast.makeText(context, "Esta palabra ya está en tu diccionario", Toast.LENGTH_SHORT).show()
                                onAddToDictionary()
                                return@launch
                            }
                            val completeContent = OpenAIServiceProvider.service.generateCompleteWordContent(palabra)
                            if (completeContent != null) {
                                val result = FirebaseWordServiceProvider.service.savePalabraAgregada(palabra, completeContent)
                                if (result.isSuccess) {
                                    Toast.makeText(context, "¡Palabra agregada al diccionario!", Toast.LENGTH_SHORT).show()
                                    onAddToDictionary()
                                } else {
                                    throw result.exceptionOrNull() ?: Exception("Error desconocido")
                                }
                            } else {
                                throw Exception("No se pudo generar contenido completo")
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isProcessing = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCEA3D9)),
                shape = RoundedCornerShape(28.dp),
                enabled = !isProcessing
            ) {
                if (isProcessing) {
                    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Generando contenido...", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                } else {
                    Text(text = "Agregar al diccionario", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }
            TextButton(onClick = onBackToHome, modifier = Modifier.padding(top = 8.dp)) {
                Text(text = "Volver al inicio", color = Color.Gray, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ResultaScreenCorrectaPreview() {
    val sampleExercise = QuickExerciseResponse("Definition here", "", "", emptyList())
    val json = Gson().toJson(sampleExercise)
    ResultaScreen(
        palabra = "Ephemeral",
        esCorrecta = true,
        exerciseJson = json,
        onAddToDictionary = {},
        onBackToHome = {}
    )
}