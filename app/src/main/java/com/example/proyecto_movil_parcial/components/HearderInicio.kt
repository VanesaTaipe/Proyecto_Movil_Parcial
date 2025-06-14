package com.example.proyecto_movil_parcial.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HearderInicio(
    title: String,
    backgroundColor: Color = Color(0xFFD3BCA0),
    textColor: Color = Color.Black,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 25.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HeaderPreview() {
    MaterialTheme {
        HearderInicio(title = "¡Hola, Vanesa Nelsy Morales Taipe! 👋")
    }
}

@Preview(showBackground = true)
@Composable
fun HeaderShortPreview() {
    MaterialTheme {
        HearderInicio(title = "¡Hola, Usuario! 👋")
    }
}