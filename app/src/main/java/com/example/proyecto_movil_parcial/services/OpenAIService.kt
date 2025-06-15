package com.example.proyecto_movil_parcial.services

import com.example.proyecto_movil_parcial.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import okhttp3.OkHttpClient

// Data classes para la API
data class OpenAIRequest(
    val model: String = "gpt-4o-mini",
    val messages: List<Message>,
    val max_tokens: Int = 400,
    val temperature: Double = 0.7
)

data class Message(
    val role: String,
    val content: String
)

data class OpenAIResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)

// Respuesta para ejercicio rápido
data class QuickExerciseResponse(
    val definition: String,
    val correctSentence: String,
    val incorrectSentence: String,
    val options: List<ExerciseOption>
)

data class ExerciseOption(
    val text: String,
    val isCorrect: Boolean
)

// Respuesta para contenido completo BILINGÜE
data class CompleteWordResponse(
    val meaningEnglish: String,
    val meaningSpanish: String,
    val howToUseEnglish: String,
    val howToUseSpanish: String,
    val examples: List<String> // Solo en inglés
)

// Interface de la API
interface OpenAIApi {
    @POST("v1/chat/completions")
    suspend fun generateContent(
        @Header("Authorization") authorization: String,
        @Body request: OpenAIRequest
    ): OpenAIResponse
}

// Servicio principal
class OpenAIService {

    private val apiKey = BuildConfig.OPENAI_API_KEY

    private val api = Retrofit.Builder()
        .baseUrl("https://api.openai.com/")
        .client(OkHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(OpenAIApi::class.java)

    // Generar ejercicio rápido - MEJORADO
    suspend fun generateQuickExercise(palabra: String): QuickExerciseResponse? {
        return try {
            val prompt = """
                Create a vocabulary exercise for the English word "$palabra":

                Requirements:
                1. Write EVERYTHING in English only
                2. Give a clear, simple definition (1-2 sentences)
                3. Create exactly 2 sentences:
                   - One CORRECT sentence using the word properly
                   - One INCORRECT sentence with obvious grammar/usage error
                4. Make the sentences simple and intuitive so students can easily guess which is right/wrong

                Format EXACTLY like this:
                [DEFINITION] Your clear definition here
                [CORRECT] Simple correct sentence here
                [INCORRECT] Simple incorrect sentence here

                Example:
                [DEFINITION] Beautiful means very attractive or pleasing to look at
                [CORRECT] The sunset was beautiful tonight
                [INCORRECT] She is very beautiful person 
            """.trimIndent()

            val request = OpenAIRequest(
                messages = listOf(
                    Message("system", "You are an English teacher. Always respond in English only. Create simple, clear exercises where the correct/incorrect usage is obvious to students."),
                    Message("user", prompt)
                ),
                max_tokens = 200
            )

            val response = api.generateContent(
                authorization = "Bearer $apiKey",
                request = request
            )

            val content = response.choices.firstOrNull()?.message?.content
            parseQuickExerciseResponse(content, palabra)

        } catch (e: Exception) {
            generateFallbackExercise(palabra)
        }
    }

    // Generar contenido completo BILINGÜE - MEJORADO
    suspend fun generateCompleteWordContent(palabra: String): CompleteWordResponse? {
        return try {
            val prompt = """
                Create comprehensive bilingual educational content for the English word "$palabra":

                [MEANING_EN] Write a detailed explanation in English (3-4 sentences) about what this word means. Be thorough but clear.

                [MEANING_ES] Write the same detailed explanation in Spanish (3-4 sentences).

                [HOW_TO_USE_EN] Write a comprehensive explanation in English (4-5 sentences) including:
                - What part of speech it is (noun, verb, adjective, etc.)
                - How native speakers typically use it
                - When NOT to use it or common mistakes
                - Proper grammar patterns

                [HOW_TO_USE_ES] Write the same comprehensive usage explanation in Spanish (4-5 sentences).

                [EXAMPLES] Write exactly 5 simple English sentences showing different contexts. Only in English.

                Format EXACTLY like this:
                [MEANING_EN] Your detailed English explanation here
                [MEANING_ES] Tu explicación detallada en español aquí
                [HOW_TO_USE_EN] Your comprehensive English usage guide here
                [HOW_TO_USE_ES] Tu guía completa de uso en español aquí
                [EXAMPLES] Sentence1|Sentence2|Sentence3|Sentence4|Sentence5
            """.trimIndent()

            val request = OpenAIRequest(
                messages = listOf(
                    Message("system", "You are a bilingual English-Spanish teacher. Provide comprehensive explanations in both languages. Be detailed."),
                    Message("user", prompt)
                ),
                max_tokens = 600,
                temperature = 0.7
            )

            val response = api.generateContent(
                authorization = "Bearer $apiKey",
                request = request
            )

            val content = response.choices.firstOrNull()?.message?.content
            parseCompleteContentResponse(content, palabra)

        } catch (e: Exception) {
            generateFallbackCompleteContent(palabra)
        }
    }

    // Parser para ejercicio rápido
    private fun parseQuickExerciseResponse(content: String?, palabra: String): QuickExerciseResponse? {
        return try {
            if (content == null) return generateFallbackExercise(palabra)

            val lines = content.lines()
            var definition = ""
            var correctSentence = ""
            var incorrectSentence = ""

            for (line in lines) {
                when {
                    line.startsWith("[DEFINITION]") -> {
                        definition = line.removePrefix("[DEFINITION]").trim()
                    }
                    line.startsWith("[CORRECT]") -> {
                        correctSentence = line.removePrefix("[CORRECT]").trim()
                    }
                    line.startsWith("[INCORRECT]") -> {
                        incorrectSentence = line.removePrefix("[INCORRECT]").trim()
                    }
                }
            }

            if (definition.isEmpty() || correctSentence.isEmpty() || incorrectSentence.isEmpty()) {
                return generateFallbackExercise(palabra)
            }

            val options = listOf(
                ExerciseOption(correctSentence, true),
                ExerciseOption(incorrectSentence, false)
            ).shuffled()

            QuickExerciseResponse(
                definition = definition,
                correctSentence = correctSentence,
                incorrectSentence = incorrectSentence,
                options = options
            )

        } catch (e: Exception) {
            generateFallbackExercise(palabra)
        }
    }

    // Parser para contenido completo
    private fun parseCompleteContentResponse(content: String?, palabra: String): CompleteWordResponse? {
        return try {
            if (content == null) return generateFallbackCompleteContent(palabra)

            val lines = content.lines()
            var meaningEnglish = ""
            var meaningSpanish = ""
            var howToUseEnglish = ""
            var howToUseSpanish = ""
            var examples = listOf<String>()

            for (line in lines) {
                when {
                    line.startsWith("[MEANING_EN]") -> {
                        meaningEnglish = line.removePrefix("[MEANING_EN]").trim()
                    }
                    line.startsWith("[MEANING_ES]") -> {
                        meaningSpanish = line.removePrefix("[MEANING_ES]").trim()
                    }
                    line.startsWith("[HOW_TO_USE_EN]") -> {
                        howToUseEnglish = line.removePrefix("[HOW_TO_USE_EN]").trim()
                    }
                    line.startsWith("[HOW_TO_USE_ES]") -> {
                        howToUseSpanish = line.removePrefix("[HOW_TO_USE_ES]").trim()
                    }
                    line.startsWith("[EXAMPLES]") -> {
                        val ejemplosText = line.removePrefix("[EXAMPLES]").trim()
                        examples = ejemplosText.split("|").map { it.trim() }.filter { it.isNotEmpty() }
                    }
                }
            }

            if (meaningEnglish.isEmpty() || meaningSpanish.isEmpty() ||
                howToUseEnglish.isEmpty() || howToUseSpanish.isEmpty() ||
                examples.size < 5) {
                return generateFallbackCompleteContent(palabra)
            }

            CompleteWordResponse(
                meaningEnglish = meaningEnglish,
                meaningSpanish = meaningSpanish,
                howToUseEnglish = howToUseEnglish,
                howToUseSpanish = howToUseSpanish,
                examples = examples
            )

        } catch (e: Exception) {
            generateFallbackCompleteContent(palabra)
        }
    }

    // Fallback para ejercicio rápido
    private fun generateFallbackExercise(palabra: String): QuickExerciseResponse {
        val options = when (palabra.lowercase()) {
            "beautiful" -> listOf(
                ExerciseOption("The sunset was beautiful tonight.", true),
                ExerciseOption("She is very beautiful person.", false)
            )
            "ephemeral" -> listOf(
                ExerciseOption("The ephemeral beauty of cherry blossoms attracts many visitors.", true),
                ExerciseOption("The ephemeral building was designed to last for centuries.", false)
            )
            else -> listOf(
                ExerciseOption("The meaning of $palabra is important to understand.", true),
                ExerciseOption("She decided to $palabra the difficult situation.", false)
            )
        }.shuffled()

        return QuickExerciseResponse(
            definition = "A word that needs to be learned and understood properly.",
            correctSentence = options.find { it.isCorrect }?.text ?: "",
            incorrectSentence = options.find { !it.isCorrect }?.text ?: "",
            options = options
        )
    }

    // Fallback para contenido completo BILINGÜE
    private fun generateFallbackCompleteContent(palabra: String): CompleteWordResponse {
        return when (palabra.lowercase()) {
            "beautiful" -> CompleteWordResponse(
                meaningEnglish = "Beautiful is an adjective that describes something very attractive, pleasing, or appealing to the senses or mind. It refers to something that gives pleasure when you look at it, hear it, or think about it. The word can describe physical appearance, art, music, ideas, or experiences that create a sense of aesthetic pleasure.",
                meaningSpanish = "Beautiful es un adjetivo que describe algo muy atractivo, agradable o que apela a los sentidos o la mente. Se refiere a algo que da placer cuando lo miras, lo escuchas o piensas en ello. La palabra puede describir apariencia física, arte, música, ideas o experiencias que crean una sensación de placer estético.",
                howToUseEnglish = "Beautiful is an adjective used before nouns or after linking verbs like 'is', 'looks', 'seems'. Native speakers use it to describe people, places, objects, weather, or abstract concepts. Don't confuse it with 'beautifully' (adverb). Common patterns: 'a beautiful day', 'looks beautiful', 'beautiful to see'. Avoid overusing it; sometimes 'pretty', 'lovely', or 'gorgeous' work better.",
                howToUseSpanish = "Beautiful es un adjetivo que se usa antes de sustantivos o después de verbos como 'is', 'looks', 'seems'. Los hablantes nativos lo usan para describir personas, lugares, objetos, clima o conceptos abstractos. No lo confundas con 'beautifully' (adverbio). Patrones comunes: 'a beautiful day', 'looks beautiful', 'beautiful to see'. Evita usarlo en exceso; a veces 'pretty', 'lovely' o 'gorgeous' funcionan mejor.",
                examples = listOf(
                    "She has beautiful eyes that sparkle in the sunlight.",
                    "The beautiful sunset painted the sky in orange and pink.",
                    "They live in a beautiful house by the lake.",
                    "Her beautiful voice filled the entire concert hall.",
                    "It was beautiful to see how happy the children were."
                )
            )
            else -> CompleteWordResponse(
                meaningEnglish = "This word has a specific meaning that students should learn and understand properly. It represents an important concept in English vocabulary that requires practice and context to master fully.",
                meaningSpanish = "Esta palabra tiene un significado específico que los estudiantes deben aprender y entender adecuadamente. Representa un concepto importante en el vocabulario inglés que requiere práctica y contexto para dominarlo completamente.",
                howToUseEnglish = "This word can be used in various contexts depending on the situation and what you want to express. Native speakers use it naturally in conversation and writing when they need to convey specific ideas or emotions.",
                howToUseSpanish = "Esta palabra se puede usar en varios contextos dependiendo de la situación y lo que quieras expresar. Los hablantes nativos la usan naturalmente en conversación y escritura cuando necesitan transmitir ideas o emociones específicas.",
                examples = listOf(
                    "The word $palabra is important to learn.",
                    "Understanding $palabra helps improve vocabulary.",
                    "Practice using $palabra in sentences.",
                    "The meaning of $palabra is clear in context.",
                    "Students should study $palabra carefully."
                )
            )
        }
    }
}

// Singleton
object OpenAIServiceProvider {
    val service = OpenAIService()
}