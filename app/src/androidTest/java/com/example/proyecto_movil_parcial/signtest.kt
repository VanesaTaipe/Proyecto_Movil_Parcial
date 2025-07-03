package com.example.proyecto_movil_parcial

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas de UI para la pantalla de Login (SignInActivity)
 * Estas pruebas verifican que la interfaz de login funcione correctamente
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class Signtest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<SignInActivity>()

    /**
     * PRUEBA 1: Verificar que todos los elementos de UI estén presentes
     * Comprueba que se muestren correctamente el logo, título, descripción y botón
     */
    @Test
    fun loginScreen_displaysAllUIElements() {
        // Verificar que se muestra el título de la app
        composeTestRule
            .onNodeWithText("AiWordFlow")
            .assertIsDisplayed()

        // Verificar que se muestra la descripción
        composeTestRule
            .onNodeWithText("Crea tu diccionario personal,\naprende con IA y mejora tu\nvocabulario creando oraciones")
            .assertIsDisplayed()

        // Verificar que se muestra el botón de Google Sign-In
        composeTestRule
            .onNodeWithText("Iniciar sesión con Google")
            .assertIsDisplayed()

        // Verificar que se muestra el logo de Google en el botón
        composeTestRule
            .onNodeWithContentDescription("Google Logo")
            .assertIsDisplayed()

        // Verificar que se muestra la imagen de login
        composeTestRule
            .onNodeWithContentDescription("Login illustration")
            .assertIsDisplayed()
    }

    /**
     * PRUEBA 2: Verificar que el botón de Google Sign-In esté habilitado y sea clickeable
     * Comprueba que el botón responda a las interacciones del usuario
     */
    @Test
    fun loginScreen_googleSignInButton_isClickable() {
        // Verificar que el botón esté habilitado
        composeTestRule
            .onNodeWithText("Iniciar sesión con Google")
            .assertIsEnabled()

        // Simular click en el botón (sin verificar resultado por ser Google Auth)
        composeTestRule
            .onNodeWithText("Iniciar sesión con Google")
            .performClick()

        // Verificar que el botón sigue estando presente después del click
        composeTestRule
            .onNodeWithText("Iniciar sesión con Google")
            .assertIsDisplayed()
    }

    /**
     * PRUEBA 3: Verificar el layout y estructura de la pantalla
     * Comprueba que los elementos estén organizados correctamente
     */
    @Test
    fun loginScreen_hasCorrectLayout() {
        // Verificar que existe la imagen en la parte superior
        composeTestRule
            .onNodeWithContentDescription("Login illustration")
            .assertIsDisplayed()

        // Verificar que el título está presente y visible
        composeTestRule
            .onNodeWithText("AiWordFlow")
            .assertIsDisplayed()

        // Verificar que la descripción está presente
        composeTestRule
            .onNodeWithText("Crea tu diccionario personal,\naprende con IA y mejora tu\nvocabulario creando oraciones")
            .assertIsDisplayed()

        // Verificar que el botón de Google tiene el estilo correcto (con ícono y texto)
        composeTestRule
            .onNodeWithContentDescription("Google Logo")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Iniciar sesión con Google")
            .assertIsDisplayed()

        // Verificar que todos los elementos están visibles simultáneamente
        composeTestRule
            .onNodeWithText("AiWordFlow")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Iniciar sesión con Google")
            .assertIsDisplayed()
    }
}