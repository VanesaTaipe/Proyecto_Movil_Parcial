package com.example.proyecto_movil_parcial.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class PalabraAgregada(
    @get:com.google.firebase.firestore.Exclude @set:com.google.firebase.firestore.Exclude var id: String = "",
    val palabra: String = "",
    val userId: String = "",
    val fechaAgregada: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now(),

    // Contenido en inglés
    val meaningEnglish: String = "",
    val howToUseEnglish: String = "",

    // Contenido en español
    val meaningSpanish: String = "",
    val howToUseSpanish: String = "",

    // Ejemplos solo en inglés
    val examples: List<String> = emptyList()
)

// Servicio para Firebase
class FirebaseWordService {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun savePalabraAgregada(
        palabra: String,
        completeContent: CompleteWordResponse
    ): Result<String> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("Usuario no encontrado"))

            val palabraData = PalabraAgregada(
                palabra = palabra.lowercase().trim(),
                userId = currentUser.uid,
                fechaAgregada = com.google.firebase.Timestamp.now(),

                // Contenido en inglés
                meaningEnglish = completeContent.meaningEnglish,
                howToUseEnglish = completeContent.howToUseEnglish,

                // Contenido en español
                meaningSpanish = completeContent.meaningSpanish,
                howToUseSpanish = completeContent.howToUseSpanish,

                // Ejemplos solo en inglés
                examples = completeContent.examples
            )

            val documentRef = firestore.collection("palabras_agregadas")
                .add(palabraData)
                .await()

            Result.success(documentRef.id)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener todas las palabras del usuario
    suspend fun getPalabrasAgregadas(): List<PalabraAgregada> {
        return try {
            val currentUser = auth.currentUser ?: return emptyList()

            val query = firestore.collection("palabras_agregadas")
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .await()

            val palabras = query.documents.mapNotNull { document ->
                try {
                    // Convierte el documento y le asigna su ID
                    val palabra = document.toObject(PalabraAgregada::class.java)
                    palabra?.id = document.id
                    palabra
                } catch (e: Exception) {
                    null
                }
            }

            // Ordenar por fecha (más reciente primero)
            palabras.sortedByDescending { it.fechaAgregada.seconds }

        } catch (e: Exception) {
            emptyList()
        }
    }

    // Función para eliminar una palabra por su ID
    suspend fun deletePalabraAgregada(palabraId: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("Usuario no autenticado"))

            firestore.collection("palabras_agregadas")
                .document(palabraId)
                .delete()
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // Verificar si palabra ya existe
    suspend fun palabraYaExiste(palabra: String): Boolean {
        return try {
            val currentUser = auth.currentUser ?: return false

            val query = firestore.collection("palabras_agregadas")
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("palabra", palabra.lowercase().trim())
                .get()
                .await()

            !query.isEmpty

        } catch (e: Exception) {
            false
        }
    }
}

// Singleton
object FirebaseWordServiceProvider {
    val service = FirebaseWordService()
}