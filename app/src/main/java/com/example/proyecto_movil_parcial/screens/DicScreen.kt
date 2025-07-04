package com.example.proyecto_movil_parcial.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
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
    // Estado que contiene la lista de palabras almacenadas en Firestore por el usuario.
    var palabrasGuardadas by remember { mutableStateOf<List<PalabraAgregada>>(emptyList()) }

    // Estado que representa si los datos aún se están cargando.
    var isLoading by remember { mutableStateOf(true) }

    // Estado que almacena la palabra seleccionada para mostrar detalles. Si no hay selección, es null.
    var selectedPalabra by remember { mutableStateOf<PalabraAgregada?>(null) }

    // Contexto del Composable, útil para mostrar toasts o acceder a recursos.
    val context = LocalContext.current

    // Scope para lanzar corrutinas ligado al ciclo de vida del Composable (se cancela si desaparece de pantalla).
    val scope = rememberCoroutineScope()

    // Efecto secundario que se lanza una única vez al entrar en composición (similar a onCreate en una Activity).
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                // Llama al servicio remoto para obtener las palabras del usuario desde Firebase Firestore.
                val palabras = FirebaseWordServiceProvider.service.getPalabrasAgregadas()
                palabrasGuardadas = palabras // Actualiza el estado con las palabras cargadas.
            } catch (e: Exception) {
                // Notifica un error si la carga falla.
                Toast.makeText(context, "Error al cargar palabras", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false // Finaliza la animación de carga.
            }
        }
    }

    // Si el usuario selecciona una palabra, se muestra una pantalla de detalle.
    selectedPalabra?.let { palabra ->
        PalabraDetalleScreen(
            palabra = palabra,
            onBackClick = { selectedPalabra = null } // Al presionar atrás, se cierra el detalle y vuelve a la lista.
        )
        return // Interrumpe la composición de la pantalla principal.
    }

    // Estructura principal de la pantalla.
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Encabezado de la pantalla.
        HearderInicio(title = "Mi Diccionario")

        // Muestra un indicador de carga mientras se recuperan los datos.
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "Cargando palabras...",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }

            // Muestra un mensaje informativo si no hay palabras guardadas.
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

            // Si hay palabras guardadas, se muestran en una lista scrollable.
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Lista de palabras usando LazyColumn para rendimiento optimizado (solo renderiza lo visible).
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Itera sobre las palabras cargadas.
                    items(palabrasGuardadas, key = { it.id }) { palabra ->

                        // Componente que muestra cada palabra con opciones de selección o eliminación.
                        PalabraItemSimple(
                            palabra = palabra,
                            onClick = {
                                selectedPalabra = palabra // Activa pantalla de detalle al hacer clic.
                            },
                            onDeleteClick = {
                                scope.launch {
                                    try {
                                        // Elimina palabra desde Firebase.
                                        val result = FirebaseWordServiceProvider.service.deletePalabraAgregada(palabra.id)
                                        if (result.isSuccess) {
                                            // Actualiza la lista sin la palabra eliminada.
                                            palabrasGuardadas = palabrasGuardadas.filter { it.id != palabra.id }

                                            // Feedback al usuario sobre eliminación exitosa.
                                            Toast.makeText(
                                                context,
                                                "'${palabra.palabra}' eliminada",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            throw result.exceptionOrNull() ?: Exception("Error desconocido")
                                        }
                                    } catch (e: Exception) {
                                        // Manejo de error en la eliminación.
                                        Toast.makeText(
                                            context,
                                            "Error al eliminar: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
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
    palabra: PalabraAgregada,           // Datos de la palabra mostrada (texto y fecha).
    onClick: () -> Unit = {},           // Callback al tocar la tarjeta (e.g., ver detalle).
    onDeleteClick: () -> Unit = {}      // Callback al presionar el ícono de eliminar.
) {
    // Componente visual de tarjeta con efecto de elevación y fondo según el tema.
    Card(
        modifier = Modifier
            .fillMaxWidth()            // Ocupa todo el ancho disponible.
            .clickable { onClick() },  // Permite responder a clics sobre la tarjeta completa.
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), // Sombra sutil.
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // Color de fondo según tema Material 3.
        )
    ) {
        // Distribuye contenido en fila: palabra + acciones.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp), // Espaciado interno.
            horizontalArrangement = Arrangement.SpaceBetween, // Separa texto y botones.
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Columna con información textual: palabra + fecha.
            Column(
                modifier = Modifier.weight(1f) // Ocupa el máximo espacio posible (deja espacio para íconos).
            ) {
                Text(
                    text = palabra.palabra.replaceFirstChar { it.uppercase() }, // Capitaliza primera letra.
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface // Color según tema claro/oscuro.
                )

                Text(
                    // Muestra fecha formateada.
                    text = "Agregada: ${formatearFecha(palabra.fechaAgregada)}",
                    fontSize = 12.sp,
                    color = Color.Gray // Color sutil para contenido secundario.
                )
            }

            // Contenedor de íconos de acción.
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón de ícono para eliminar la palabra.
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar palabra", // Descripción accesible.
                        tint = Color.Gray
                    )
                }

                // Ícono decorativo (3 puntos verticales), sugiere opciones adicionales o navegación.
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Ver detalles", // Aunque no tiene funcionalidad directa aquí.
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Función helper/auxiliar que convierte un Timestamp de Firebase a una cadena con formato de fecha legible (dd/MM/yyyy).
private fun formatearFecha(timestamp: com.google.firebase.Timestamp): String {
    // Convierte el Timestamp a un objeto java.util.Date estándar.
    val date = timestamp.toDate()

    // Define el formato de salida de fecha como "día/mes/año" usando la configuración regional del sistema.
    val format = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())

    // Retorna la fecha formateada como string.
    return format.format(date)
}


@Preview(
    showBackground = true,        // Renderiza un fondo blanco en la vista previa.
    name = "Diccionario Screen Preview" // Nombre visible en el panel de vista previa en Android studio
)
@Composable
fun DiccionarioPreview() {
    MaterialTheme {
        // Composable principal del diccionario, envuelto en tema Material3 para visualización consistente.
        DicScreen()
    }
}
