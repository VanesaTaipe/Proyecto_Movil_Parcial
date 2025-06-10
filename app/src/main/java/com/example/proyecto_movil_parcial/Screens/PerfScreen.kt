package com.example.proyecto_movil_parcial.Screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfScreen(
    userName: String,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    var maxPalabrasDia by remember { mutableStateOf<Int?>(null) }
    var palabrasAprendidasHoy by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var showEditDialog by remember { mutableStateOf(false) }
    var isUpdating by remember { mutableStateOf(false) }

    // Cargar maxPalabrasDia desde Firestore
    LaunchedEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        maxPalabrasDia = document.getLong("maxPalabrasDia")?.toInt()

                        // Aquí se va agreagar la lógica para contar palabras aprendidas hoy
                        // Por ahora uso un valor simulado
                        palabrasAprendidasHoy = 3 // Este valor vendría ser lógica de conteo
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                    Toast.makeText(context, "Error al cargar configuración", Toast.LENGTH_SHORT).show()
                }
        } else {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Perfil",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = userName,
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Progreso del día
        if (!isLoading && maxPalabrasDia != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Progreso",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Palabras aprendidas:",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "$palabrasAprendidasHoy / ${maxPalabrasDia}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        // Indicador de progreso circular
                        val progreso = if (maxPalabrasDia!! > 0) (palabrasAprendidasHoy.toFloat() / maxPalabrasDia!!).coerceIn(0f, 1f) else 0f
                        val colorProgreso = when {
                            progreso >= 1f -> Color(0xFF4CAF50) // Verde
                            progreso >= 0.5f -> Color(0xFFFF9800) // Naranja
                            else -> Color(0xFFF44336) // Rojo
                        }

                        Box(
                            modifier = Modifier
                                .background(
                                    color = colorProgreso.copy(alpha = 0.2f),
                                    shape = androidx.compose.foundation.shape.CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${(progreso * 100).toInt()}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorProgreso
                            )
                        }
                    }
                }
            }
        }

        // Card para mostrar la configuración actual
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Meta",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Palabras:",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        if (isLoading) {
                            Text(
                                text = "Cargando...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text(
                                text = "${maxPalabrasDia ?: "No configurado"}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    IconButton(
                        onClick = { showEditDialog = true },
                        enabled = !isLoading && maxPalabrasDia != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar palabras diarias",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de logout
        Button(
            onClick = onSignOut,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Cerrar Sesión",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }

    // Dialog para editar maxPalabrasDia
    if (showEditDialog) {
        EditMaxPalabrasDialog(
            currentValue = maxPalabrasDia ?: 0,
            isUpdating = isUpdating,
            onDismiss = { showEditDialog = false },
            onConfirm = { newValue ->
                isUpdating = true
                val auth = FirebaseAuth.getInstance()
                val firestore = FirebaseFirestore.getInstance()
                val currentUser = auth.currentUser

                if (currentUser != null) {
                    firestore.collection("users")
                        .document(currentUser.uid)
                        .update(
                            mapOf(
                                "maxPalabrasDia" to newValue,
                                "ultimoAcceso" to com.google.firebase.Timestamp.now()
                            )
                        )
                        .addOnSuccessListener {
                            maxPalabrasDia = newValue
                            isUpdating = false
                            showEditDialog = false
                            Toast.makeText(context, "Configuración actualizada", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { exception ->
                            isUpdating = false
                            Toast.makeText(context, "Error al actualizar: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    isUpdating = false
                    Toast.makeText(context, "Error: Usuario no encontrado", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMaxPalabrasDialog(
    currentValue: Int,
    isUpdating: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var palabrasText by remember { mutableStateOf(currentValue.toString()) }

    Dialog(onDismissRequest = { if (!isUpdating) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Editar Palabras Diarias",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFFEBDABF),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = "¿Cuántas palabras te gustaría aprender por día?",
                        fontSize = 14.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = palabrasText,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || (newValue.all { it.isDigit() } && newValue.length <= 3)) {
                            palabrasText = newValue
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Número de palabras") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !isUpdating
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isUpdating
                    ) {
                        Text(
                            text = "Cancelar",
                            color = Color.Gray
                        )
                    }

                    Button(
                        onClick = {
                            val newValue = palabrasText.toIntOrNull()
                            if (newValue != null && newValue > 0) {
                                onConfirm(newValue)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFCEA3D9)
                        ),
                        enabled = !isUpdating && palabrasText.isNotEmpty() &&
                                palabrasText.toIntOrNull() != null &&
                                palabrasText.toInt() > 0
                    ) {
                        if (isUpdating) {
                            Text(
                                text = "...",
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "Guardar",
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    name = "Perfil Screen Preview"
)
@Composable
fun PerfScreenPreview() {
    MaterialTheme {
        PerfScreen(
            userName = "Hola, Usuario",
            onSignOut = {}
        )
    }
}

@Preview(
    showBackground = true,
    name = "Edit Dialog Preview"
)
@Composable
fun EditMaxPalabrasDialogPreview() {
    MaterialTheme {
        EditMaxPalabrasDialog(
            currentValue = 15,
            isUpdating = false,
            onDismiss = {},
            onConfirm = {}
        )
    }
}