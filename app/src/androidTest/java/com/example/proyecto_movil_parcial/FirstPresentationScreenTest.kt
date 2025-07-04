package com.example.proyecto_movil_parcial

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class PresentationScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun buttonDisabled_whenInputEmpty() {
        composeTestRule.setContent {
            PresentationScreen(onFinish = {})
        }
        composeTestRule.onNodeWithTag("input_npalabras").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_siguiente").assertIsNotEnabled()
    }

    @Test
    fun buttonEnabled_whenValidInput() {
        composeTestRule.setContent {
            PresentationScreen(onFinish = {})
        }
        composeTestRule.onNodeWithTag("input_npalabras").performTextInput("10")
        composeTestRule.onNodeWithTag("btn_siguiente").assertIsEnabled()
    }

    @Test
    fun buttonDisabled_whenInputTooHigh() {
        composeTestRule.setContent {
            PresentationScreen(onFinish = {})
        }
        composeTestRule.onNodeWithTag("input_npalabras").performTextInput("51")
        composeTestRule.onNodeWithTag("btn_siguiente").assertIsNotEnabled()
    }

    @Test
    fun buttonDisabled_whenInputIsZero() {
        composeTestRule.setContent {
            PresentationScreen(onFinish = {})
        }
        composeTestRule.onNodeWithTag("input_npalabras").performTextInput("0")
        composeTestRule.onNodeWithTag("btn_siguiente").assertIsNotEnabled()
    }

    @Test
    fun onFinishCalled_whenValidInputAndClick() {
        var callbackInvokedWith: Int? = null
        composeTestRule.setContent {
            PresentationScreen(onFinish = { callbackInvokedWith = it })
        }
        composeTestRule.onNodeWithTag("input_npalabras").performTextInput("20")
        composeTestRule.onNodeWithTag("btn_siguiente").performClick()

        assert(callbackInvokedWith == 20)
    }
}
