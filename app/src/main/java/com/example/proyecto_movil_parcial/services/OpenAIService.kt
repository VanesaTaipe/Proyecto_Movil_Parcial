package com.example.proyecto_movil_parcial.services

import com.example.proyecto_movil_parcial.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

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

data class CompleteWordResponse(
    val meaningEnglish: String,
    val meaningSpanish: String,
    val howToUseEnglish: String,
    val howToUseSpanish: String,
    val examples: List<String>
)

data class SentenceEvaluationResponse(
    val revisionAI: String,
    val pequenosAjustes: String,
    val oracionAjustada: String
)

interface OpenAIApi {
    @POST("v1/chat/completions")
    suspend fun generateContent(
        @Header("Authorization") authorization: String,
        @Body request: OpenAIRequest
    ): OpenAIResponse
}

class OpenAIService {

    private val apiKey = BuildConfig.OPENAI_API_KEY

    private val api = Retrofit.Builder()
        .baseUrl("https://api.openai.com/")
        .client(OkHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(OpenAIApi::class.java)

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
            """.trimIndent()
            val request = OpenAIRequest(
                messages = listOf(
                    Message("system", "You are an English teacher. Always respond in English only. Create simple, clear exercises where the correct/incorrect usage is obvious to students."),
                    Message("user", prompt)
                ),
                max_tokens = 200
            )
            val response = api.generateContent("Bearer $apiKey", request)
            parseQuickExerciseResponse(response.choices.firstOrNull()?.message?.content)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun generateCompleteWordContent(palabra: String): CompleteWordResponse? {
        return try {
            val prompt = """
                Create comprehensive bilingual educational content for the English word "$palabra":
                [MEANING_EN] Write a detailed explanation in English (3-4 sentences) about what this word means. Be thorough but clear.
                [MEANING_ES] Write the same detailed explanation in Spanish (3-4 sentences).
                [HOW_TO_USE_EN] Write a comprehensive explanation in English (4-5 sentences) including: What part of speech it is, how native speakers typically use it, when NOT to use it or common mistakes, and proper grammar patterns.
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
                max_tokens = 600
            )
            val response = api.generateContent("Bearer $apiKey", request)
            parseCompleteContentResponse(response.choices.firstOrNull()?.message?.content)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun evaluateSentence(palabra: String, userSentence: String): SentenceEvaluationResponse? {
        return try {
            val prompt = """
                You are a friendly and encouraging English teacher reviewing a sentence written by a student.
                The student was asked to use the word "$palabra" in a sentence. The student's sentence is: "$userSentence"
                Your task is to provide feedback in SPANISH.
                Your response MUST follow this exact format, using these specific labels, and ensuring each section has content:
                [REVISION_AI] Start with positive reinforcement. If the word "$palabra" was used correctly, say "¡Buen trabajo al usar '$palabra'! 👍".
                [PEQUENOS_AJUSTES] Provide a more detailed explanation. Analyze the student's full sentence. Explain any grammatical errors or awkward phrasing. Explain what was good about the sentence. Suggest specific changes to make it sound more natural.
                [ORACION_AJUSTADA] Provide the student's complete, corrected sentence.
            """.trimIndent()
            val request = OpenAIRequest(
                messages = listOf(
                    Message("system", "You are an English teacher who provides feedback in Spanish. Follow the user's formatting instructions precisely."),
                    Message("user", prompt)
                ),
                max_tokens = 500,
                temperature = 0.5
            )
            val response = api.generateContent("Bearer $apiKey", request)
            parseSentenceEvaluationResponse(response.choices.firstOrNull()?.message?.content)
        } catch (e: Exception) {
            null
        }
    }

    private fun parseSentenceEvaluationResponse(content: String?): SentenceEvaluationResponse? {
        if (content == null) return null
        return try {
            val revisionAI = content.substringAfter("[REVISION_AI]", "").substringBefore("[").trim()
            val pequenosAjustes = content.substringAfter("[PEQUENOS_AJUSTES]", "").substringBefore("[").trim()
            val oracionAjustada = content.substringAfter("[ORACION_AJUSTADA]", "").trim()

            if (revisionAI.isNotBlank() && pequenosAjustes.isNotBlank() && oracionAjustada.isNotBlank()) {
                SentenceEvaluationResponse(revisionAI, pequenosAjustes, oracionAjustada)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun parseQuickExerciseResponse(content: String?): QuickExerciseResponse? {
        if (content == null) return null
        return try {
            val definition = content.substringAfter("[DEFINITION]", "").substringBefore("[").trim()
            val correctSentence = content.substringAfter("[CORRECT]", "").substringBefore("[").trim()
            val incorrectSentence = content.substringAfter("[INCORRECT]", "").trim()

            if (definition.isEmpty() || correctSentence.isEmpty() || incorrectSentence.isEmpty()) {
                return null
            }
            val options = listOf(
                ExerciseOption(correctSentence, true),
                ExerciseOption(incorrectSentence, false)
            ).shuffled()
            QuickExerciseResponse(definition, correctSentence, incorrectSentence, options)
        } catch (e: Exception) {
            null
        }
    }

    private fun parseCompleteContentResponse(content: String?): CompleteWordResponse? {
        if (content == null) return null
        return try {
            val meaningEnglish = content.substringAfter("[MEANING_EN]", "").substringBefore("[").trim()
            val meaningSpanish = content.substringAfter("[MEANING_ES]", "").substringBefore("[").trim()
            val howToUseEnglish = content.substringAfter("[HOW_TO_USE_EN]", "").substringBefore("[").trim()
            val howToUseSpanish = content.substringAfter("[HOW_TO_USE_ES]", "").substringBefore("[").trim()
            val examplesText = content.substringAfter("[EXAMPLES]", "").trim()
            val examples = examplesText.split("|").map { it.trim() }.filter { it.isNotEmpty() }

            if (meaningEnglish.isBlank() || meaningSpanish.isBlank() || howToUseEnglish.isBlank() || howToUseSpanish.isBlank() || examples.size < 5) {
                return null
            }
            CompleteWordResponse(meaningEnglish, meaningSpanish, howToUseEnglish, howToUseSpanish, examples)
        } catch (e: Exception) {
            null
        }
    }
}

object OpenAIServiceProvider {
    val service = OpenAIService()
}