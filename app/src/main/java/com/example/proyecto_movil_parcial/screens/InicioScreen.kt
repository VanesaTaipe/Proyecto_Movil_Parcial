package com.example.proyecto_movil_parcial.Screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.proyecto_movil_parcial.components.AddDocumentIcon
import com.example.proyecto_movil_parcial.components.HearderInicio
import com.example.proyecto_movil_parcial.services.FirebaseWordServiceProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun InicioScreen(
    onNavigateToNewWord: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var maxPalabras by remember { mutableStateOf<Int?>(null) }
    var palabrasActuales by remember { mutableStateOf<List<String>>(emptyList()) }
    var ejemploAleatorio by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var userName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            userName = currentUser.displayName ?: "Usuario"

            firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        maxPalabras = document.getLong("maxPalabrasDia")?.toInt()
                    }

                    scope.launch {
                        try {
                            val palabras = FirebaseWordServiceProvider.service.getPalabrasAgregadas()
                            palabrasActuales = palabras.map { it.palabra }

                            if (palabras.isNotEmpty()) {
                                val palabraAleatoria = palabras.random()
                                if (palabraAleatoria.examples.isNotEmpty()) {
                                    ejemploAleatorio = palabraAleatoria.examples.random()
                                }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error al cargar palabras", Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false
                        }
                    }
                }
                .addOnFailureListener {
                    isLoading = false
                    Toast.makeText(context, "Error al cargar datos", Toast.LENGTH_SHORT).show()
                }
        } else {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        HearderInicio(
            title = if (isLoading) "Cargando..." else "¡Hola, $userName! 👋"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Oraciones",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Color.LightGray,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    Text(
                        text = "Cargando...",
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray
                    )
                } else if (palabrasActuales.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "aún no tienes palabras nuevas buscadas",
                            textAlign = TextAlign.Center,
                            color = Color.DarkGray,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )

                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (ejemploAleatorio.isNotEmpty()) {
                            Text(
                                text = "\"$ejemploAleatorio\"",
                                fontSize = 14.sp,
                                color = Color.DarkGray,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        } else {
                            Text(
                                text = "Cargando ejemplo...",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Text(
                text = "Nueva palabra",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
                modifier = Modifier.padding(top = 16.dp)
            )

            val puedeAgregarPalabra = maxPalabras?.let { palabrasActuales.size < it } ?: true

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clickable(enabled = puedeAgregarPalabra) {
                        if (puedeAgregarPalabra) {
                            onNavigateToNewWord()
                        } else {
                            Toast.makeText(context, "Has alcanzado tu límite de $maxPalabras palabras", Toast.LENGTH_SHORT).show()
                        }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (puedeAgregarPalabra)
                        Color(0xFFEBDABF)
                    else
                        Color(0xFFE0E0E0)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (puedeAgregarPalabra) {
                        AddDocumentIcon(
                            modifier = Modifier.size(64.dp),
                            color = Color(0xFF8B7355)
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🚫",
                                fontSize = 32.sp
                            )
                            Text(
                                text = "Límite alcanzado",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            if (!isLoading && maxPalabras != null) {
                val restantes = maxPalabras!! - palabrasActuales.size
                when {
                    restantes == 0 -> {
                        Text(
                            text = "Has alcanzado tu límite de $maxPalabras palabras. Ve al perfil para aumentarlo.",
                            fontSize = 12.sp,
                            color = Color(0xFFF44336),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    restantes <= 2 -> {
                        Text(
                            text = "Te quedan $restantes palabras por agregar",
                            fontSize = 12.sp,
                            color = Color(0xFFFF9800),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    name = "Inicio Screen Preview"
)
@Composable
fun InicioScreenPreview() {
    MaterialTheme {
        InicioScreen()
    }
}