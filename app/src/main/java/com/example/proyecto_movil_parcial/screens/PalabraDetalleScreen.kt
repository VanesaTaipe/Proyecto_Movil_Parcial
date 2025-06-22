package com.example.proyecto_movil_parcial.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyecto_movil_parcial.components.HearderInicio
import com.example.proyecto_movil_parcial.services.PalabraAgregada

@Composable
fun PalabraDetalleScreen(
    palabra: PalabraAgregada,
    onBackClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Significado", "Cómo usar?", "Ejemplos")

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        HearderInicio(
            title = palabra.palabra.replaceFirstChar { it.uppercase() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { index, tab ->
                    Button(
                        onClick = { selectedTab = index },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == index)
                                Color(0xFF8DB4A6)
                            else
                                Color.LightGray
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = tab,
                            fontSize = 14.sp,
                            color = if (selectedTab == index) Color.White else Color.Gray,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Contenido según tab seleccionado
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (selectedTab) {
                        0 -> {
                            MeaningSection(palabra = palabra)
                        }
                        1 -> {
                            HowToSection(palabra = palabra)
                        }
                        2 -> {
                            ExamplesSection(palabra = palabra)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón para volver
            TextButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "← Volver al diccionario",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun MeaningSection(palabra: PalabraAgregada) {
    var showSpanish by remember { mutableStateOf(false) }

    Column {
        Text(
            text = "What Does \"${palabra.palabra.replaceFirstChar { it.uppercase() }}\" Really Mean?",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = if (showSpanish) palabra.meaningSpanish else palabra.meaningEnglish,
            fontSize = 16.sp,
            color = Color.Black,
            lineHeight = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Botón para cambiar idioma
        TextButton(
            onClick = { showSpanish = !showSpanish }
        ) {
            Text(
                text = if (showSpanish) "Show in English" else "Mostrar en español",
                fontSize = 14.sp,
                color = Color(0xFF4A90E2)
            )
        }
    }
}

@Composable
fun HowToSection(palabra: PalabraAgregada) {
    var showSpanish by remember { mutableStateOf(false) }

    Column {
        Text(
            text = "How to Use \"${palabra.palabra.replaceFirstChar { it.uppercase() }}\"",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = if (showSpanish) palabra.howToUseSpanish else palabra.howToUseEnglish,
            fontSize = 16.sp,
            color = Color.Black,
            lineHeight = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Botón para cambiar idioma
        TextButton(
            onClick = { showSpanish = !showSpanish }
        ) {
            Text(
                text = if (showSpanish) "Show in English" else "Mostrar en español",
                fontSize = 14.sp,
                color = Color(0xFF4A90E2)
            )
        }
    }
}

@Composable
fun ExamplesSection(palabra: PalabraAgregada) {
    Column {
        Text(
            text = "Examples of \"${palabra.palabra.replaceFirstChar { it.uppercase() }}\"",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        palabra.examples.forEachIndexed { index, ejemplo ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF8F9FA)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFF8DB4A6),
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                            .size(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = ejemplo,
                        fontSize = 15.sp,
                        color = Color.Black,
                        lineHeight = 22.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// Preview para testing
@Preview(showBackground = true)
@Composable
fun PalabraDetalleScreenPreview() {
    val samplePalabra = PalabraAgregada(
        palabra = "beautiful",
        meaningEnglish = "Beautiful is an adjective that describes something very attractive, pleasing, or appealing to the senses or mind. It refers to something that gives pleasure when you look at it, hear it, or think about it. The word can describe physical appearance, art, music, ideas, or experiences that create a sense of aesthetic pleasure.",
        meaningSpanish = "Beautiful es un adjetivo que describe algo muy atractivo, agradable o que apela a los sentidos o la mente. Se refiere a algo que da placer cuando lo miras, lo escuchas o piensas en ello. La palabra puede describir apariencia física, arte, música, ideas o experiencias que crean una sensación de placer estético.",
        howToUseEnglish = "Beautiful is an adjective used before nouns or after linking verbs like 'is', 'looks', 'seems'. Native speakers use it to describe people, places, objects, weather, or abstract concepts. Don't confuse it with 'beautifully' (adverb). Common patterns: 'a beautiful day', 'looks beautiful', 'beautiful to see'.",
        howToUseSpanish = "Beautiful es un adjetivo que se usa antes de sustantivos o después de verbos como 'is', 'looks', 'seems'. Los hablantes nativos lo usan para describir personas, lugares, objetos, clima o conceptos abstractos. No lo confundas con 'beautifully' (adverbio).",
        examples = listOf(
            "She has beautiful eyes that sparkle in the sunlight.",
            "The beautiful sunset painted the sky in orange and pink.",
            "They live in a beautiful house by the lake.",
            "Her beautiful voice filled the entire concert hall.",
            "It was beautiful to see how happy the children were."
        )
    )

    MaterialTheme {
        PalabraDetalleScreen(palabra = samplePalabra)
    }
}