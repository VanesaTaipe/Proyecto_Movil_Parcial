package com.example.proyecto_movil_parcial

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.proyecto_movil_parcial.screens.DicScreen

import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class DicScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testListaVacia_muestraMensaje() {
        composeTestRule.setContent {
            DicScreen()
        }

        // Esperar a que isLoading pase a false (simulación)
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Cargando palabras...").fetchSemanticsNodes().isEmpty()
        }

        // Verifica que se muestre el mensaje de lista vacía
        composeTestRule.onNodeWithText("No tienes palabras guardadas")
            .assertIsDisplayed()
    }
}