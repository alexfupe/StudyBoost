package com.toka.studyboost.utils

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private const val TAG = "EscaneadorOCR"

/**
 * Extrae texto de imágenes (fotos de apuntes físicos) usando ML Kit Text Recognition.
 *
 * Ventajas sobre alternativas:
 * - On-device: funciona sin internet.
 * - Gratuito: sin cuota de API.
 * - Integración nativa con Jetpack Compose mediante launchers de cámara.
 *
 * Uso:
 * ```kotlin
 * val texto = EscaneadorOCR.extraerTexto(bitmap)
 * val textoLimpio = EscaneadorOCR.limpiarTexto(texto)
 * ```
 */
object EscaneadorOCR {

    /**
     * Instancia reutilizable del reconocedor de texto.
     * TextRecognizerOptions.DEFAULT_OPTIONS soporta latín (español incluido).
     */
    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    /**
     * Extrae texto de un [Bitmap] de forma asíncrona.
     * Envuelve la API de callbacks de ML Kit en una coroutine suspend.
     *
     * @throws Exception si ML Kit falla al procesar la imagen.
     */
    suspend fun extraerTexto(bitmap: Bitmap): String = suspendCoroutine { continuation ->
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { resultado ->
                val texto = resultado.text
                Log.d(TAG, "OCR extraído: ${texto.length} caracteres")
                continuation.resume(texto)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error de OCR: ${e.message}", e)
                continuation.resumeWithException(e)
            }
    }

    /**
     * Post-procesa el texto extraído por OCR para mejorar calidad:
     * - Une palabras cortadas con guión al final de línea ("apren-\nder" → "aprender").
     * - Colapsa múltiples líneas vacías consecutivas.
     * - Elimina espacios al inicio/fin.
     */
    fun limpiarTexto(texto: String): String {
        return texto
            .replace(Regex("-\\n"), "")          // Palabras cortadas con guión
            .replace(Regex("\\n{3,}"), "\n\n")   // Máximo 2 saltos seguidos
            .trim()
    }
}
