package com.example.proyecto_movil_parcial.Screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ArrowForward
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
import androidx.compose.ui.window.Dialog
import com.example.proyecto_movil_parcial.components.HearderInicio
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun DicScreen() {
    var palabrasGuardadas by remember { mutableStateOf<List<PalabraModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedPalabra by remember { mutableStateOf<PalabraModel?>(null) }
    val context = LocalContext.current

    // Cargar palabras del usuario desde Firebase
    LaunchedEffect(Unit) {
        loadUserWords(
            onSuccess = { palabras ->
                palabrasGuardadas = palabras
                isLoading = false
            },
            onError = { error ->
                isLoading = false
                Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        HearderInicio(
            title = "Mi Diccionario"
        )

        // Contenido
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Cargando palabras...",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        } else if (palabrasGuardadas.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = "📚",
                        fontSize = 48.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = "No tienes palabras guardadas",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "¡Agrega tu primera palabra desde la pantalla de inicio!",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Encabezado con contador total
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
                            text = "Tu Progreso",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Text(
                            text = "${palabrasGuardadas.size}",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Text(
                            text = if (palabrasGuardadas.size == 1) "palabra aprendida" else "palabras aprendidas",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Lista simple de palabras
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(palabrasGuardadas) { palabra ->
                        PalabraItemSimple(
                            palabra = palabra,
                            onClick = { selectedPalabra = palabra }
                        )
                    }
                }
            }
        }
    }

    // Dialog para mostrar detalles de la palabra
    selectedPalabra?.let { palabra ->
        PalabraDetalleDialog(
            palabra = palabra,
            onDismiss = { selectedPalabra = null }
        )
    }
}

@Composable
fun PalabraItemSimple(
    palabra: PalabraModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = palabra.palabra.replaceFirstChar { it.uppercase() },
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.ArrowForward,
                contentDescription = "Ver detalles",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun PalabraDetalleDialog(
    palabra: PalabraModel,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Título
                Text(
                    text = palabra.palabra.replaceFirstChar { it.uppercase() },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Definición
                if (palabra.definicion.isNotEmpty()) {
                    Text(
                        text = "Definición:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = palabra.definicion,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Ejemplos
                if (palabra.ejemplos.isNotEmpty()) {
                    Text(
                        text = "Ejemplos:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    palabra.ejemplos.forEach { ejemplo ->
                        Text(
                            text = "• $ejemplo",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Botón cerrar
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}

@Composable
fun EstadisticaItem(
    numero: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = numero,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun PalabraItem(palabra: PalabraModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = palabra.palabra.replaceFirstChar { it.uppercase() },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Estado de la palabra
                val (statusText, statusColor) = when {
                    palabra.isAprendida -> "Aprendida" to Color(0xFF4CAF50)
                    palabra.intentosTotales > 0 -> "En progreso" to Color(0xFFFF9800)
                    else -> "Nueva" to Color.Gray
                }

                Box(
                    modifier = Modifier
                        .background(
                            color = statusColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        color = statusColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (palabra.definicion.isNotEmpty()) {
                Text(
                    text = palabra.definicion,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Mostrar progreso si tiene intentos
            if (palabra.intentosTotales > 0) {
                Text(
                    text = "Intentos: ${palabra.intentosCorrectos}/${palabra.intentosTotales} correctos",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Fecha de agregada
            Text(
                text = "Agregada: ${formatearFecha(palabra.fechaAgregada)}",
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// Modelo de datos para las palabras
data class PalabraModel(
    val id: String = "",
    val palabra: String = "",
    val definicion: String = "",
    val ejemplos: List<String> = emptyList(),
    val userId: String = "",
    val fechaAgregada: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now(),
    val intentosCorrectos: Int = 0,
    val intentosTotales: Int = 0,
    val isAprendida: Boolean = false,
    val status: String = "pendiente"
)

// Función para cargar palabras del usuario
private fun loadUserWords(
    onSuccess: (List<PalabraModel>) -> Unit,
    onError: (String) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    if (currentUser == null) {
        onError("Usuario no encontrado")
        return
    }

    firestore.collection("palabras")
        .whereEqualTo("userId", currentUser.uid)
        .orderBy("fechaAgregada", com.google.firebase.firestore.Query.Direction.DESCENDING)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                onError("Error al cargar palabras: ${error.message}")
                return@addSnapshotListener
            }

            val palabras = snapshot?.documents?.mapNotNull { document ->
                try {
                    PalabraModel(
                        id = document.id,
                        palabra = document.getString("palabra") ?: "",
                        definicion = document.getString("definicion") ?: "",
                        ejemplos = document.get("ejemplos") as? List<String> ?: emptyList(),
                        userId = document.getString("userId") ?: "",
                        fechaAgregada = document.getTimestamp("fechaAgregada") ?: com.google.firebase.Timestamp.now(),
                        intentosCorrectos = document.getLong("intentosCorrectos")?.toInt() ?: 0,
                        intentosTotales = document.getLong("intentosTotales")?.toInt() ?: 0,
                        isAprendida = document.getBoolean("isAprendida") ?: false,
                        status = document.getString("status") ?: "pendiente"
                    )
                } catch (e: Exception) {
                    null
                }
            } ?: emptyList()

            onSuccess(palabras)
        }
}

// Función helper para formatear fechas
private fun formatearFecha(timestamp: com.google.firebase.Timestamp): String {
    val date = timestamp.toDate()
    val format = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
    return format.format(date)
}

@Preview(
    showBackground = true,
    name = "Diccionario Screen Preview"
)
@Composable
fun DiccionarioSb() {
    MaterialTheme {
        DicScreen()
    }
}