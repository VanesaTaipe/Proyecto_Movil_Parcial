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
import com.example.proyecto_movil_parcial.components.HearderInicio
import com.example.proyecto_movil_parcial.services.*
import com.example.proyecto_movil_parcial.ui.theme.Proyecto_Movil_parcialTheme
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OracionDesScreen(
    onNavigateToResult: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var palabraAgregada by remember { mutableStateOf<PalabraAgregada?>(null) }
    var userSentence by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isVerifying by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true
        val randomWord = FirebaseWordServiceProvider.service.getRandomPalabraAgregada()
        if (randomWord == null) {
            Toast.makeText(context, "No tienes palabras en tu diccionario. Agrega una para empezar.", Toast.LENGTH_LONG).show()
            onNavigateBack()
        } else {
            palabraAgregada = randomWord
        }
        isLoading = false
    }

    Column(modifier = Modifier.fillMaxSize()) {
        HearderInicio(title = "Crea Oraciones")
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (palabraAgregada != null) {
            val word = palabraAgregada!!
            val randomExample by remember(word) {
                mutableStateOf(word.examples.randomOrNull() ?: "no hay ejemplo.")
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                Text(
                    text = word.palabra.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "AI Example: $randomExample",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = userSentence,
                    onValueChange = { userSentence = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        if (userSentence.isBlank()) {
                            Toast.makeText(context, "escribe una oración.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isVerifying = true
                        scope.launch {
                            try {
                                val evaluation = OpenAIServiceProvider.service.evaluateSentence(word.palabra, userSentence)
                                if (evaluation != null) {
                                    val result = OracionDesResult(
                                        palabra = word.palabra,
                                        oracionUsuario = userSentence,
                                        revisionAI = evaluation.revisionAI,
                                        pequenosAjustes = evaluation.pequenosAjustes,
                                        oracionAjustada = evaluation.oracionAjustada
                                    )
                                    FirebaseWordServiceProvider.service.saveChallengeResult(result)
                                    val resultJson = Gson().toJson(result)
                                    val encodedJson = URLEncoder.encode(resultJson, StandardCharsets.UTF_8.toString())
                                    onNavigateToResult(encodedJson)
                                } else {
                                    Toast.makeText(context, "no se pudo obtener la revisión", Toast.LENGTH_LONG).show()
                                }
                            } catch(e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            } finally {
                                isVerifying = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !isVerifying
                ) {
                    if (isVerifying) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Verificar", color = Color.Black)
                    }
                }
            }
        }
    }
}
@Preview
@Composable
fun OracionDesScreenPreview() {
    Proyecto_Movil_parcialTheme() {
        OracionDesScreen(
            onNavigateToResult = { result ->
            },
            onNavigateBack = {
            }
        )
    }
}