package com.example.proyecto_movil_parcial.services

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

data class OracionDesResult(
    @get:Exclude @set:Exclude var id: String = "",
    val userId: String = "",
    val palabra: String = "",
    val oracionUsuario: String = "",
    val revisionAI: String = "",
    val pequenosAjustes: String = "",
    val oracionAjustada: String = "",
    val fecha: Timestamp = Timestamp.now()
)