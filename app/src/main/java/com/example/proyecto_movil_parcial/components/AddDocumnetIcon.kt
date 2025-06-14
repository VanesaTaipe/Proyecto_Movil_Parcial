package com.example.proyecto_movil_parcial.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun AddDocumentIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF8B7355)
) {
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Dibujar el documento (rectángulo con esquina doblada)
        val docWidth = canvasWidth * 0.6f
        val docHeight = canvasHeight * 0.7f
        val docLeft = (canvasWidth - docWidth) / 2
        val docTop = (canvasHeight - docHeight) / 2

        // Cuerpo principal del documento
        drawRect(
            color = color,
            topLeft = Offset(docLeft, docTop + docHeight * 0.15f),
            size = Size(docWidth * 0.85f, docHeight * 0.85f),
            style = Stroke(width = 3.dp.toPx())
        )

        // Esquina doblada
        val cornerSize = docWidth * 0.15f
        drawLine(
            color = color,
            start = Offset(docLeft + docWidth * 0.85f - cornerSize, docTop + docHeight * 0.15f),
            end = Offset(docLeft + docWidth * 0.85f, docTop + docHeight * 0.15f + cornerSize),
            strokeWidth = 3.dp.toPx()
        )
        drawLine(
            color = color,
            start = Offset(docLeft + docWidth * 0.85f - cornerSize, docTop + docHeight * 0.15f),
            end = Offset(docLeft + docWidth * 0.85f - cornerSize, docTop + docHeight * 0.15f + cornerSize),
            strokeWidth = 3.dp.toPx()
        )

        // Líneas del texto en el documento
        val lineStart = docLeft + docWidth * 0.15f
        val lineEnd = docLeft + docWidth * 0.7f
        val lineSpacing = docHeight * 0.12f
        val firstLineTop = docTop + docHeight * 0.35f

        for (i in 0..2) {
            drawLine(
                color = color,
                start = Offset(lineStart, firstLineTop + i * lineSpacing),
                end = Offset(lineEnd - (i * docWidth * 0.1f), firstLineTop + i * lineSpacing),
                strokeWidth = 2.dp.toPx()
            )
        }

        // Círculo para el símbolo +
        val circleRadius = canvasWidth * 0.15f
        val circleCenter = Offset(
            docLeft + docWidth * 0.85f,
            docTop + docHeight * 0.85f
        )

        // Fondo del círculo
        drawCircle(
            color = color,
            radius = circleRadius,
            center = circleCenter
        )

        // Símbolo + en blanco
        val plusSize = circleRadius * 0.6f
        // Línea horizontal del +
        drawLine(
            color = Color.White,
            start = Offset(circleCenter.x - plusSize, circleCenter.y),
            end = Offset(circleCenter.x + plusSize, circleCenter.y),
            strokeWidth = 3.dp.toPx()
        )
        // Línea vertical del +
        drawLine(
            color = Color.White,
            start = Offset(circleCenter.x, circleCenter.y - plusSize),
            end = Offset(circleCenter.x, circleCenter.y + plusSize),
            strokeWidth = 3.dp.toPx()
        )
    }
}

@Preview
@Composable
fun AddDocumentIconPreview() {
    AddDocumentIcon(
        modifier = Modifier.size(64.dp)
    )
}