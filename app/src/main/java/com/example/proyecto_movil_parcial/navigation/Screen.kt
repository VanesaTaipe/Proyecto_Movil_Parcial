package com.example.proyecto_movil_parcial.navigation

sealed class Screen(val rout: String) {
    object Inico: Screen("inicio")
    object Diccionario: Screen("diccionario")
    object Desafios: Screen("desafios")
    object Perfil: Screen("perfil")
    object NuevaPalabra: Screen("nueva_palabra")
    object Intentar: Screen("intenta_adivinar")
    object Resultado: Screen("resultado_screen/{palabra}/{esCorrecta}")
    object CrearOracion: Screen("crear_oracion")
    object ResultadoOracion: Screen("resultado_oracion")
}