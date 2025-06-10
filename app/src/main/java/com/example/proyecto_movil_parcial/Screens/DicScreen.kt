package com.example.proyecto_movil_parcial.Screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun DicScreen(){
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Mi Diccionario",
            style = MaterialTheme.typography.headlineLarge
        )
    }
}

@Preview(
    showBackground = true,
    name = "Diccionario Screen Preview"
)
@Composable
fun DiccionarioScreen() {
    MaterialTheme {
        DesScreen()
    }
}