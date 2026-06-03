package com.toka.studyboost.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.BufferedReader
import java.io.InputStreamReader

private const val TAG = "LectorDocumentos"

/**
 * Utilidad para extraer texto de documentos locales.
 *
 * Formatos soportados:
 * - PDF: extracción mediante PDFBox-Android (solo PDFs con texto seleccionable;
 *   para PDFs escaneados como imagen, usar [EscaneadorOCR]).
 * - Texto plano (TXT, MD, etc.): lectura directa del stream.
 *
 * IMPORTANTE: [PDFBoxResourceLoader] ya se inicializa en [MainApplication.onCreate()].
 * No llamar a `PDFBoxResourceLoader.init()` aquí — causaría overhead en cada extracción.
 */
object LectorDocumentos {

    /**
     * Punto de entrada principal. Detecta el tipo MIME del archivo y delega
     * a la función de extracción apropiada.
     *
     * @param contexto Context para acceder al ContentResolver.
     * @param uri      URI del archivo seleccionado (proviene del system file picker).
     * @return El texto extraído, o String vacío si falla.
     */
    fun extraerTexto(contexto: Context, uri: Uri): String {
        val mimeType = contexto.contentResolver.getType(uri) ?: ""
        Log.d(TAG, "Extrayendo texto de URI con MIME: $mimeType")
        return when {
            mimeType.contains("pdf", ignoreCase = true) -> extraerTextoPdf(contexto, uri)
            else -> extraerTextoPlano(contexto, uri)
        }
    }

    private fun extraerTextoPdf(contexto: Context, uri: Uri): String {
        return try {
            // PDFBoxResourceLoader ya fue inicializado en MainApplication — no inicializar aquí
            val texto = contexto.contentResolver.openInputStream(uri)?.use { inputStream ->
                PDDocument.load(inputStream).use { document ->
                    Log.d(TAG, "PDF cargado. Número de páginas: ${document.numberOfPages}")
                    PDFTextStripper().getText(document)
                }
            } ?: ""

            Log.d(TAG, "Texto extraído del PDF: ${texto.length} caracteres")
            Log.d(TAG, "Preview (primeros 200 chars): ${texto.take(200)}")

            if (texto.isBlank()) "" else texto

        } catch (e: Exception) {
            Log.e(TAG, "Error extrayendo texto del PDF: ${e.message}", e)
            ""
        }
    }

    private fun extraerTextoPlano(contexto: Context, uri: Uri): String {
        return try {
            contexto.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).readText()
            } ?: ""
        } catch (e: Exception) {
            Log.e(TAG, "Error leyendo archivo de texto plano: ${e.message}", e)
            ""
        }
    }
}