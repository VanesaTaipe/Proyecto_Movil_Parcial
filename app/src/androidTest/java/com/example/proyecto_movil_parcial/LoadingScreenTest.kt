package com.example.proyecto_movil_parcial

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.hasTestTag
//import androidx.compose.ui.test.onNode
import org.junit.Rule
import org.junit.Test

class LoadingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loadingScreenDisplaysElementsCorrectly() {
        composeTestRule.setContent {
            LoadingScreen()
        }

        // Verifica que el texto principal está presente
        composeTestRule.onNodeWithText("Configurando tu experiencia...").assertIsDisplayed()

        // Verifica que el indicador de carga está presente (opcionalmente agrégale testTag)
        composeTestRule.onNode(hasTestTag("cargando")).assertIsDisplayed()
    }
}
