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
fun DesScreen(){
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Desafio",
            style = MaterialTheme.typography.headlineLarge
        )
    }
}

 @Preview
@Composable
fun DiccionarioScreenPreview() {
    MaterialTheme {
        DesScreen()
    }
}