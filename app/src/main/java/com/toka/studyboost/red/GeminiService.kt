package com.toka.studyboost.red

import android.util.Log
import com.google.gson.Gson
import com.toka.studyboost.BuildConfig
import com.toka.studyboost.datos.GeminiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

private const val TAG = "GeminiService"
/** Límite de caracteres enviados a Gemini. ~15k chars ≈ 3.750 tokens — seguro y rápido. */
private const val MAX_CHARS_PROMPT = 15_000

/**
 * Servicio de comunicación con la API REST de Gemini 2.0 Flash.
 *
 * Decisión de diseño: se usa [HttpURLConnection] directamente en lugar del SDK oficial
 * o Retrofit, para tener control total sobre timeouts y no añadir dependencias pesadas.
 *
 * La API key se lee desde [BuildConfig.GEMINI_API_KEY], que a su vez se inyecta
 * desde `local.properties` en tiempo de compilación. Nunca está hardcodeada en el código.
 */
class GeminiService {

    // API key leída de local.properties via BuildConfig — nunca en código fuente
    private val apiKey = BuildConfig.GEMINI_API_KEY
    private val endpoint =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey"
    private val gson = Gson()

    // —— Análisis de documento (resumen + preguntas) ————————————————————————————————

    /**
     * Analiza un texto extraído de un documento y genera un resumen con preguntas de test.
     *
     * @param texto Texto extraído del PDF/imagen. Si supera [MAX_CHARS_PROMPT], se trunca
     *              automáticamente para evitar errores 400 y timeouts.
     * @return [GeminiResponse] con resumen y lista de preguntas, o `null` si hay error.
     */
    suspend fun analizarDocumento(texto: String): GeminiResponse? = withContext(Dispatchers.IO) {
        val textoSeguro = texto.take(MAX_CHARS_PROMPT)
        if (textoSeguro.length < texto.length) {
            Log.w(TAG, "Texto truncado de ${texto.length} a ${textoSeguro.length} chars")
        }

        val prompt = """
            Eres un asistente de estudio experto. Analiza el siguiente texto extraído de un documento y genera:
            1. Un resumen conciso pero completo (mínimo 3 párrafos).
            2. 5 preguntas de opción múltiple (test) basadas en el contenido.
            
            Debes responder ÚNICAMENTE con un objeto JSON con el siguiente formato:
            {
              "summary": "texto del resumen",
              "questions": [
                {
                  "question": "enunciado de la pregunta",
                  "options": ["opción A", "opción B", "opción C"],
                  "correct": 0
                }
              ]
            }
            
            Nota: 'correct' es el índice de la respuesta correcta en la lista 'options' (empezando desde 0).
            
            Texto a analizar:
            $textoSeguro
        """.trimIndent()

        return@withContext llamarApi(prompt, GeminiResponse::class.java)
    }

    // —— Generación de flashcards —————————————————————————————————————————————————

    /**
     * Genera flashcards de estudio desde el mismo texto del documento.
     * Se llama después de [analizarDocumento] para enriquecer la sesión.
     *
     * @return Lista de pares pregunta/respuesta, o `null` si hay error.
     */
    suspend fun generarFlashcards(texto: String): FlashcardsResponse? = withContext(Dispatchers.IO) {
        val textoSeguro = texto.take(MAX_CHARS_PROMPT)

        val prompt = """
            Del siguiente texto académico, extrae entre 5 y 8 conceptos o hechos clave
            y crea flashcards de estudio claras y concisas.
            
            Responde ÚNICAMENTE con JSON en este formato exacto:
            {
              "flashcards": [
                {
                  "pregunta": "¿Qué es [concepto]?",
                  "respuesta": "Definición clara y concisa del concepto."
                }
              ]
            }
            
            Texto:
            $textoSeguro
        """.trimIndent()

        return@withContext llamarApi(prompt, FlashcardsResponse::class.java)
    }

    // —— Motor HTTP compartido ——————————————————————————————————————————————————————

    private fun <T> llamarApi(prompt: String, tipoRespuesta: Class<T>): T? {
        val requestBody = """
            {
              "contents": [{"parts": [{"text": ${gson.toJson(prompt)}}]}],
              "generationConfig": {"responseMimeType": "application/json"}
            }
        """.trimIndent()

        return try {
            Log.d(TAG, "Llamando a Gemini con prompt de ${prompt.length} chars")

            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                connectTimeout = 30_000
                readTimeout = 90_000 // 90s para documentos largos
            }

            OutputStreamWriter(connection.outputStream).use { it.write(requestBody) }

            val responseCode = connection.responseCode
            Log.d(TAG, "Código de respuesta: $responseCode")

            val responseText = if (responseCode == 200) {
                connection.inputStream.bufferedReader().readText()
            } else {
                val error = connection.errorStream?.bufferedReader()?.readText()
                Log.e(TAG, "Error HTTP $responseCode: $error")
                return null
            }

            // Extraer el texto generado del wrapper de Gemini
            val geminiResp = gson.fromJson(responseText, GeminiApiResponse::class.java)
            val jsonTexto = geminiResp.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: run {
                    Log.e(TAG, "Texto nulo en respuesta de Gemini")
                    return null
                }

            // Limpiar posibles code fences que Gemini añade ocasionalmente
            val jsonLimpio = jsonTexto
                .replace(Regex("^```json\\s*", RegexOption.MULTILINE), "")
                .replace(Regex("\\s*```$", RegexOption.MULTILINE), "")
                .trim()

            Log.d(TAG, "JSON respuesta (primeros 300 chars): ${jsonLimpio.take(300)}")
            gson.fromJson(jsonLimpio, tipoRespuesta)

        } catch (e: Exception) {
            Log.e(TAG, "Excepción en llamada a Gemini: ${e.message}", e)
            null
        }
    }
}

// —— Modelos de respuesta de la API de Gemini ——————————————————————————————————————

data class GeminiApiResponse(val candidates: List<Candidate>?)
data class Candidate(val content: Content?)
data class Content(val parts: List<Part>?)
data class Part(val text: String?)

/** Respuesta del endpoint de generación de flashcards. */
data class FlashcardsResponse(val flashcards: List<FlashcardRaw>)
data class FlashcardRaw(val pregunta: String, val respuesta: String)
