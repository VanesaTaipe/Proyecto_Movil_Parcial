package com.example.proyecto_movil_parcial.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.proyecto_movil_parcial.components.HearderInicio
import com.example.proyecto_movil_parcial.services.OracionDesResult
import com.google.gson.Gson
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun DesResultadosScreen(
    resultJson: String?,
    onFinish: () -> Unit
) {
    val decodedJson = remember(resultJson) {
        resultJson?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }
    }
    val result = remember(decodedJson) {
        decodedJson?.let { Gson().fromJson(it, OracionDesResult::class.java) }
    }

    Column(Modifier.fillMaxSize()) {
        HearderInicio(title = "Crea Oraciones")

        if (result != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = result.palabra.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Your Sentence:", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = result.oracionUsuario,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                ResultSection(title = "Revisión AI", content = result.revisionAI)
                ResultSection(title = "Pequeños Ajustes", content = result.pequenosAjustes)
                ResultSection(title = "Tu Oración con un Pequeño Ajuste", content = result.oracionAjustada)

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onFinish,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Finalizar")
                }
            }
        }
    }
}

@Composable
private fun ResultSection(title: String, content: String) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(text = content, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview
@Composable
private fun DesResultadosScreenPreview() {
    MaterialTheme {
        DesResultadosScreen(
            resultJson = null,
            onFinish = {}
        )
    }
}