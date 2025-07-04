package com.example.proyecto_movil_parcial

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.proyecto_movil_parcial.services.OpenAIServiceProvider
import com.example.proyecto_movil_parcial.screens.NuevPaScreen
//import io.mockk.coEvery
//import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas para el Composable NuevPaScreen.
 *
 * Estas pruebas utilizan Compose UI Test para verificar el comportamiento de la interfaz
 * de usuario y se apoyan en MockK para simular el servicio de generación de ejercicios.
 */
@RunWith(AndroidJUnit4::class)
class NuevPaScreenTest {

    // Regla de prueba para Compose que permite testear Composables.
    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Verifica que el campo de texto solo acepte letras, espacios y apóstrofes.
     */
    @Test
    fun textoConCaracteresInvalidosNoSeAcepta() {
        composeTestRule.setContent {
            NuevPaScreen()
        }

        val input = "p@l4br@!" // contiene caracteres inválidos

        // Intenta escribir el texto inválido
        composeTestRule
            .onNode(hasSetTextAction())
            .performTextInput(input)

        // Verifica que el campo esté vacío (no se asignó el valor)
        composeTestRule
            .onNode(hasSetTextAction())
            .assertTextEquals("") // porque se rechaza toda la cadena
    }

    // la entrada de texto es letra por letra
    @Test
    fun aceptaSoloCaracteresValidosCuandoSeEscribeLetraPorLetra() {
        composeTestRule.setContent {
            NuevPaScreen()
        }

        val input = "p@l4br@!"
        val expected = "plbr"

        // Simula entrada letra por letra
        input.forEach { char ->
            composeTestRule
                .onNode(hasSetTextAction())
                .performTextInput(char.toString())
        }

        composeTestRule
            .onNode(hasSetTextAction())
            .assertTextEquals(expected)
    }


    /**
     * Verifica que el botón 'Buscar' esté deshabilitado cuando el campo está vacío.
     */
    @Test
    fun botonBuscarDeshabilitadoConCampoVacio() {
        composeTestRule.setContent {
            NuevPaScreen()
        }

        // Busca el botón con el texto "Buscar" y verifica que está deshabilitado.
        composeTestRule
            .onNodeWithText("Buscar")
            .assertIsNotEnabled()
    }


}


