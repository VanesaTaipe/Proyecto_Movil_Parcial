package com.example.proyecto_movil_parcial.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkAccent,           // Naranja vibrante
    secondary = DarkSecondary,      // Gris medio
    tertiary = DarkPrimary,         // Gris oscuro
    background = DarkBackground,    // Negro suave
    surface = DarkSecondary,        // Gris medio para superficies
    onPrimary = Color.White,        // Texto blanco en naranja
    onSecondary = Color.White,      // Texto blanco en gris medio
    onTertiary = Color.White,       // Texto blanco en gris oscuro
    onBackground = Color.White,     // Texto blanco en fondo
    onSurface = Color.White         // Texto blanco en superficies
)

private val LightColorScheme = lightColorScheme(
    primary = LightSecondary,       // Naranja vibrante
    secondary = LightAccent,        // Naranja oscuro
    tertiary = LightPrimary,        // Crema suave
    background = LightBackground,   // Crema muy claro
    surface = LightPrimary,         // Crema suave para superficies
    onPrimary = Color.White,        // Texto blanco en naranja
    onSecondary = Color.White,      // Texto blanco en naranja oscuro
    onTertiary = Color.Black,       // Texto negro en crema
    onBackground = Color.Black,     // Texto negro en fondo
    onSurface = Color.Black         // Texto negro en superficies
)

@Composable
fun Proyecto_Movil_parcialTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}