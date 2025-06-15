package com.example.proyecto_movil_parcial.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.proyecto_movil_parcial.navigation.Screen

@Composable
fun DesScreen(navController: NavController){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Pon a prueba tu entendimiento",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { navController.navigate(Screen.CrearOracion.rout) }) {
            Text("Crear Oraciones")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DesScreenPreview() {
    MaterialTheme {
        DesScreen(navController = rememberNavController())
    }
}