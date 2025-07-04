package com.example.proyecto_movil_parcial.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 *  Representa los distintos "estados" en los que un usuario puede estar
 * cuando abre la app. Ideal para tomar decisiones de navegación o UI.
 */
sealed class UserStatus {
    object NotLoggedIn : UserStatus()   // El usuario no ha iniciado sesión
    object NeedsSetup : UserStatus()    // El usuario está logueado pero necesita configurar su perfil
    object LoggedIn : UserStatus()      // Usuario autenticado y con configuración válida
    object Error : UserStatus()         // Fallo de conexión o error inesperado
}

/**
 * ViewModel encargado de verificar el estado del usuario usando FirebaseAuth y Firestore.
 * Se comunica con la vista (Activity o Composable) mediante un StateFlow llamado `status`.
 */
class UserStatusViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),    // 🔐 Controla la sesión del usuario
    // Acceso a la base de datos Firestore
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    // MutableStateFlow interno que contiene el estado actual del usuario.
    private val _status = MutableStateFlow<UserStatus>(UserStatus.Error)

    // Estado expuesto como solo-lectura para las vistas. Es lo que observará la UI.
    val status: StateFlow<UserStatus> = _status

    /**
     * 🔍 Verifica el estado actual del usuario.
     * 1. Si no hay usuario → NotLoggedIn.
     * 2. Si el usuario está logueado:
     *    - Busca su documento en Firestore.
     *    - Si existe y tiene `maxPalabrasDia > 0` → LoggedIn.
     *    - Si no existe o el valor es inválido → NeedsSetup.
     * 3. Si ocurre un error de conexión → Error.
     */
    fun checkUserStatus() {
        val currentUser = auth.currentUser

        // Paso 1: Validar si el usuario está logueado
        if (currentUser == null) {
            _status.value = UserStatus.NotLoggedIn
            return
        }

        // Paso 2: Consultar Firestore para ver si el usuario ya tiene datos guardados
        firestore.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                // El documento existe
                if (document.exists()) {
                    val maxPalabras = document.getLong("maxPalabrasDia")

                    // Si tiene una configuración válida (> 0), está listo
                    if (maxPalabras != null && maxPalabras > 0) {
                        updateLastAccess(currentUser.uid) // 🕒 Actualiza el último acceso
                        _status.value = UserStatus.LoggedIn
                    } else {
                        // 🛠️ Usuario necesita completar su configuración
                        _status.value = UserStatus.NeedsSetup
                    }
                } else {
                    // 🛠️ Usuario no tiene datos en Firestore, necesita configurarse
                    _status.value = UserStatus.NeedsSetup
                }
            }
            .addOnFailureListener {
                // Error al conectar con Firestore
                _status.value = UserStatus.Error
            }
    }

    /**
     * Actualiza la fecha de último acceso en Firestore.
     * Esto se usa para llevar control de actividad del usuario.
     */
    private fun updateLastAccess(uid: String) {
        firestore.collection("users").document(uid)
            .update("ultimoAcceso", Timestamp.now())
    }
}
