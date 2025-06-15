package com.example.proyecto_movil_parcial.Screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
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
import com.example.proyecto_movil_parcial.services.FirebaseWordServiceProvider
import com.example.proyecto_movil_parcial.services.PalabraAgregada
import kotlinx.coroutines.launch

@Composable
fun DicScreen() {
    var palabrasGuardadas by remember { mutableStateOf<List<PalabraAgregada>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedPalabra by remember { mutableStateOf<PalabraAgregada?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Cargar palabras del usuario desde Firebase
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val palabras = FirebaseWordServiceProvider.service.getPalabrasAgregadas()
                palabrasGuardadas = palabras
            } catch (e: Exception) {
                Toast.makeText(context, "Error al cargar palabras", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    // Si hay una palabra seleccionada, mostrar pantalla de detalles
    selectedPalabra?.let { palabra ->
        PalabraDetalleScreen(
            palabra = palabra,
            onBackClick = { selectedPalabra = null }
        )
        return
    }

    // Pantalla principal del diccionario
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        HearderInicio(
            title = "Mi Diccionario"
        )

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

                // Lista de palabras
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(palabrasGuardadas, key = { it.id }) { palabra ->
                        PalabraItemSimple(
                            palabra = palabra,
                            onClick = { selectedPalabra = palabra },
                            onDeleteClick = {
                                scope.launch {
                                    try {
                                        val result = FirebaseWordServiceProvider.service.deletePalabraAgregada(palabra.id)
                                        if (result.isSuccess) {
                                            palabrasGuardadas = palabrasGuardadas.filter { it.id != palabra.id }
                                            Toast.makeText(context, "'${palabra.palabra}' eliminada", Toast.LENGTH_SHORT).show()
                                        } else {
                                            throw result.exceptionOrNull() ?: Exception("Error desconocido")
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PalabraItemSimple(
    palabra: PalabraAgregada,
    onClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Columna con el nombre y la fecha
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = palabra.palabra.replaceFirstChar { it.uppercase() },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Agregada: ${formatearFecha(palabra.fechaAgregada)}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ícono para eliminar
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar palabra",
                        tint = Color.Gray
                    )
                }

                // Ícono para ver detalles
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Ver detalles",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Función helper
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
fun DiccionarioPreview() {
    MaterialTheme {
        DicScreen()
    }
}