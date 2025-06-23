# Explicacion de los test aplicados

## Especificaciones 
los detalles del emulador usado es `Android 16 , API 36`
[Properties](documentation/emulator_properties.txt)

## SignInActivity
Para esta actividad no se le ha hecho pruebas unitarias por la complejidad de la logica.
Lo que pasa cuando intento hacer una prueba el emulador no logra conectarse correctamente con el servicio de google

Cabe resaltar que probando con el APK si se puede ejecutar lo cual es interesante

> Lineas futuras:
> Si hay disponibilidad y compatibilidad se hara test con appium

## Presentacion1Activity & PresentationScreenTest

En esta pantalla se define el numero de palabras que el usuario quiere aprender

Para esta actividad se ejecutan  pruebas que consiste en analizar el rendimientos de los valores de entrada

1. Botón deshabilitado si el campo está vacío.
2. Botón habilitado con entrada válida (por ejemplo, 10).
3. Botón deshabilitado si se ingresa un número mayor a 50.
4. Botón deshabilitado con 0.
5. Se llama a onFinish correctamente cuando la entrada es válida y se presiona el botón.

### Requisitos para que las pruebas funcionen
Agregar los identificadores al composable

```kotlin
import androidx.compose.ui.platform.testTag
// codigo programacion
OutlinedTextField(
    modifier = Modifier
        .testTag("input_npalabras")
)

Button(
    modifier = Modifier
        .width(120.dp)
        .testTag("btn_siguiente")
)

```

`testTag` es un identificador para que los tests puedan localizar elementos
específicos de forma confiable, incluso si el texto o contenido visual cambia.

Algunas modificaciones en el codigo fuente es el limite maximo de palabras al dia.
Si bien no debe ser tan limitado, pero la idea es no salir de la version free de firebase con las escrituras y lecturas
y tamebien para el APIKEY de openai

Razon por la que en `Presentation1Activity` modifico esta seccion de code:

```kotlin
// de esto 
enabled = !isLoading &&
        palabrasText.isNotEmpty() &&
        palabrasText.toIntOrNull() != null &&
        palabrasText.toInt() > 0

// a 
enabled = !isLoading &&
        palabrasText.isNotEmpty() &&
        palabrasText.toIntOrNull() != null &&
        // maximo de palabras 50
        palabrasText.toInt() in 1..50 

```
### Explicacion:
1. `@get:Rule val composeTestRule = createComposeRule()`
   Esta es la regla principal de testing para Jetpack Compose.
   Permite renderizar Composables en un entorno controlado de prueba y hacer assertions sobre su UI
2. `onNodeWithTag("input_npalabras")` y `onNodeWithTag("btn_siguiente")`
   Estas funciones buscan nodos (componentes de UI) usando su testTag
3. `composeTestRule.setContent { ... }`
   Aquí es donde se configura qué pantalla se va a probar.
4. `performTextInput("10")`
   Simula al usuario escribiendo texto dentro del OutlinedTextField.
   Es exactamente como si alguien escribiera 10 manualmente en el campo
5. `assertIsEnabled() `/ `assertIsNotEnabled()`
   Estas funciones validan si un componente está habilitado (clickeable) o no.
   Se usan para verificar que la lógica de validación del campo de entrada esté funcionando correctamente, según el número ingresado
6. `assertIsDisplayed()`
   Asegura que un componente existe y está visible en pantalla. Ideal para validar que el campo esté correctamente renderizado.
7. `performClick()`
   Simula un clic de usuario en un botón.
8. `var callbackInvokedWith: Int? = null` y lambda `onFinish = { callbackInvokedWith = it }
   Aquí no se usa un mock, sino una variable real que captura el valor enviado al callback `onFinish`.
   Esto reemplaza el uso de MockK y permite validar si el flujo del programa llegó correctamente a esa función:

    ```kotlin
   // la entrada fue 20
    assert(callbackInvokedWith == 20)
    
    ```

   Verifica que el botón realmente ejecutó el callback con el número correcto

### Output
![PresentationScreenTest.png](PresentationScreenTest.png)